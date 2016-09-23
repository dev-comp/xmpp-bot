package com.bftcom.devcomp.bots;

import com.bftcom.devcomp.api.IBotConst;
import com.bftcom.devcomp.api.IBotManager;
import com.bftcom.devcomp.bots.queues.AdapterQueueConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * @author ikka
 * @date: 16.09.2016.
 */
public class BotManager implements IBotManager {
  @SuppressWarnings("PackageAccessibility")
  private static final Logger logger = LoggerFactory.getLogger(BotManager.class);

  /**
   * Имя прослушиваемой очереди для сообщений от экземпляров ботов
   */
  private static final String XMPP_ADAPTER = "xmpp-adapter";

  private static final ConcurrentHashMap<String, Thread> botSessions = new ConcurrentHashMap<>();

  private ConnectionFactory factory;
  private Connection connection;
  private Channel channel;


  public BotManager() {
    logger.info("creating a new bot manager adapter");
    factory = new ConnectionFactory();
    try {
      connection = factory.newConnection();
      channel = connection.createChannel();
      AdapterQueueConsumer adapterQueueConsumer = new AdapterQueueConsumer(channel, this, IBotConst.QUEUE_TO_ADAPTER_PREFIX + XMPP_ADAPTER);
    } catch (ConnectException e ) {
      logger.error("Не удалось подключиться к системе обмена сообщений. Убедитесь, что сервер системы (RabbitMQ) запущен.", e);
    }catch (IOException | TimeoutException e) {
      logger.error("", e);
    } 
  }

  @Override
  public boolean startBotSession(String id, Map<String, String> userProps, Map<String, String> serviceProps) {
//    JabberBot b = new JabberBot();
//    Thread thread1 = new Thread(b);
//    thread1.start();
    
    logger.info("startBotSession id=" + id + ";userProperties=" + userProps.toString());
    //prevent starting bot sessions with the same id
    if (id != null) {
      synchronized (botSessions) {
        if (botSessions.get(id) != null) {
          logger.warn("prevented starting a duplicate bot session with the same id " + id);
          return true;
        }
      }
    }

    Bot bot = new Bot(serviceProps.get(IBotConst.PROP_BOT_NAME), userProps);
    try {

      String outQueueName = IBotConst.QUEUE_TO_BOT_PREFIX + bot.getName();
      String inQueueName = IBotConst.QUEUE_FROM_BOT_PREFIX + bot.getName();

      logger.debug("creating queues");
      bot.setInQueueName(inQueueName);
      bot.setOutQueueName(outQueueName);
      bot.setInChannel(createChannel(inQueueName));
      bot.setOutChannel(createChannel(outQueueName));
    } catch (IOException e) {
      return false;
    }

    try {
      logger.info("registering bot " + id + " " + userProps.toString());
      Thread thread = new Thread(bot);
      synchronized (botSessions) {
        botSessions.put(id, thread);
      }
      thread.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return true;
  }

  @Override
  public boolean stopBotSession(String id) {
    synchronized (botSessions) {
      if (id != null) {
        Thread botSession = botSessions.remove(id);
        if (botSession != null && botSession.isAlive()) {
          logger.info("bot session for bot " + id + " is closed.");
          botSession.interrupt();
        }
      }
    }
    return true;
  }

  @Override
  public void stopAllBotSessions() {
    logger.debug("stopping all bot sessions");
    synchronized (botSessions) {
      for (String botSessionKey : botSessions.keySet()) {
        Thread botSession = botSessions.remove(botSessionKey);
        if (botSession != null && botSession.isAlive()) {
          botSession.interrupt();
        }
      }
    }
  }

  @Override
  protected void finalize() throws Throwable {
    if (connection != null) {
      connection.close();
    }
    super.finalize();
  }

  private Channel createChannel(String queueName) throws IOException {
    Channel channel = null;
    try {
      channel = connection.createChannel();
      AMQP.Queue.DeclareOk declareOk = channel.queueDeclare(queueName, false, false, false, null);
      channel.addShutdownListener(cause -> logger.info("shutting down channel " + declareOk.getQueue()));
    } catch (IOException e) {
      logger.error("", e);
      throw e;
    }
    return channel;
  }

  public static void main(String[] args) {
    BotManager botManager = new BotManager();
  }
}

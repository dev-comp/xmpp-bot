package com.bftcom.devcomp.api;

import com.bftcom.devcomp.queues.AdapterQueueConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * @author ikka
 * @date: 16.09.2016.
 */
public abstract class AbstractMessengerAdapter<E> implements IMessengerAdapter {
  @SuppressWarnings("PackageAccessibility")
  private static final Logger logger = LoggerFactory.getLogger(AbstractMessengerAdapter.class);


  protected final ConcurrentHashMap<String, E> botSessions = new ConcurrentHashMap<>();

  private ConnectionFactory factory;
  private Connection connection;
  private Channel channel;


  public AbstractMessengerAdapter() {
    System.out.println("AbstractMessengerAdapter");
    logger.info("creating a new bot manager adapter");
    factory = new ConnectionFactory();
    try {
      connection = factory.newConnection();
      channel = connection.createChannel();
      new AdapterQueueConsumer(channel, this);
    } catch (IOException | TimeoutException e) {
      logger.error("", e);
    }
  }

  @Override
  public boolean startBotSession(String id, Map<String, String> userProps, Map<String, String> serviceProps) {
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

    IBot bot = createNewBot(serviceProps, userProps);
    String outQueueName = IBotConst.QUEUE_TO_BOT_PREFIX + bot.getName();
    String inQueueName = IBotConst.QUEUE_FROM_BOT_PREFIX + bot.getName();
    bot.setInQueueName(inQueueName);
    bot.setOutQueueName(outQueueName);

    try {
      logger.debug("creating queues");
      bot.setInChannel(createChannel(inQueueName));
      bot.setOutChannel(createChannel(outQueueName));
    } catch (IOException e) {
      return false;
    }

    return startBotSession(id, bot);
  }

  protected abstract boolean startBotSession(String id, IBot bot);

  protected abstract IBot createNewBot(Map<String, String> serviceProps, Map<String, String> userProps);

  abstract public boolean stopBotSession(E instance);

  @Override
  public boolean stopBotSession(String id) {
    synchronized (botSessions) {
      E botSession = botSessions.remove(id);
      if (botSession != null) {
        stopBotSession(botSession);
        logger.info("bot session for bot " + id + " is closed.");
      }
    }
    return true;
  }

  @Override
  public void stopAllBotSessions() {
    logger.debug("stopping all bot sessions");
    synchronized (botSessions) {
      for (String botSessionKey : botSessions.keySet()) {
        stopBotSession(botSessionKey);
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
}

package com.bftcom.devcomp.bots;

import com.bftcom.devcomp.api.BotCommand;
import com.bftcom.devcomp.api.Configuration;
import com.bftcom.devcomp.api.IBotConst;
import com.bftcom.devcomp.bots.queues.BotQueueConsumer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.proxy.ProxyInfo;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Бот для Telegram
 *
 * @author ikka
 * @date: 10.09.2016.
 */
@SuppressWarnings("PackageAccessibility")
public class Bot implements Runnable {
  @SuppressWarnings("PackageAccessibility")
  private static final Logger logger = LoggerFactory.getLogger(Bot.class);
  @SuppressWarnings("PackageAccessibility")
  private static final ObjectMapper mapper = new ObjectMapper();

  private String username;
  private String token;
  private Channel inChannel;
  private Channel outChannel;//
  private String outQueueName;
  private String inQueueName;

  private String name;
  private XMPPTCPConnection connection;
  private Map<String, String> config;


  public Bot(String name, Map<String, String> config) {
    this.config = config;
    this.name = name;
  }
  
  public void run(){
    try {
      XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
      String username = config.get(Configuration.USERNAME);
      String password = config.get(Configuration.PASSWORD);

      configBuilder.setUsernameAndPassword(username, password);
      configBuilder.setResource(config.get(Configuration.DOMAIN));
      configBuilder.setServiceName(config.get(Configuration.SERVER));
      configBuilder.setHost(config.get(Configuration.SERVER));
      configBuilder.setPort(Integer.parseInt(config.get(Configuration.PORT)));
      configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
      configBuilder.setConnectTimeout(60000);
      ProxyInfo.ProxyType proxyType = getProxyType(config.get(Configuration.PROXY_TYPE));
      if (proxyType != ProxyInfo.ProxyType.NONE)
        configBuilder.setProxyInfo(new ProxyInfo(proxyType, config.get(Configuration.PROXY_HOST),
            Integer.parseInt(config.get(Configuration.PROXY_PORT)), config.get(Configuration.PROXY_USER), config.get(Configuration.PROXY_PASSWORD)));
      connection = new XMPPTCPConnection(configBuilder.build());
      int priority = 10;
      connection.connect();
      connection.login(username, password);
      Presence presence = new Presence(Presence.Type.available);
      presence.setStatus("online");
      connection.sendStanza(presence);
      presence.setPriority(priority);

      StanzaFilter filter = new AndFilter(new StanzaTypeFilter(Message.class));

      StanzaListener myListener = packet -> {
        if (packet instanceof Message) {
          Message message = (Message) packet;
          processMessage(message);
        }
      };

      connection.addAsyncStanzaListener(myListener, filter);

      Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);

      while (connection.isConnected()) {
        Thread.sleep(60000);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
      connection.disconnect();
    } catch (XMPPException | IOException | SmackException e) {
      e.printStackTrace();
    }
  }

  /**
   * Обработка входящего сообщения<hr>
   *
   * @param message входящее сообщение
   */
  private void processMessage(Message message) {
    logger.info("processing message " + message);
    logger.info("body: " + message.getBody());
    String messageBody = message.getBody();
    if (messageBody == null)
      return;

    com.bftcom.devcomp.api.Message msgToForward = new com.bftcom.devcomp.api.Message();
    msgToForward.setCommand(BotCommand.SERVICE_PROCESS_BOT_MESSAGE);
    Map<String, String> userProperties = msgToForward.getUserProperties();
    Map<String, String> serviceProperties = msgToForward.getServiceProperties();

    userProperties.put(IBotConst.PROP_BODY_TEXT, messageBody);
    serviceProperties.put(IBotConst.PROP_BOT_NAME, getName());
    serviceProperties.put(IBotConst.PROP_USER_NAME, message.getFrom());
    serviceProperties.put("chatId", String.valueOf(message.getFrom()));

    try {
      inChannel.basicPublish("", inQueueName, null, mapper.writeValueAsString(msgToForward).getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      logger.error("", e);
    }
    String JID = message.getFrom();

    // обрабатываем сообщение. можно писать что угодно :)
    // пока что пусть будет эхо-бот
    sendMessage(JID, makeAnswer(messageBody));
  }

  private String makeAnswer(String msg) {
    String answer = "Здравствуйте!";
    if (msg == null)
      return answer;
    if (msg.contains("?"))
      answer = "Ваш вопрос очень важен для нас. Пожалуйста, оставайтесь на линии.";
    else
      answer = msg;
    return answer;
  }

  private ProxyInfo.ProxyType getProxyType(String pType) {
    if (pType == null)
      pType = "NONE";
    switch (pType) {
      case "HTTP":
        return ProxyInfo.ProxyType.HTTP;
      case "SOCKS4":
        return ProxyInfo.ProxyType.SOCKS4;
      case "SOCKS5":
        return ProxyInfo.ProxyType.SOCKS5;
    }
    return ProxyInfo.ProxyType.NONE;
  }

  public void sendMessage(String to, String message) {
    if (!message.equals("")) {
      ChatManager chatmanager = ChatManager.getInstanceFor(connection);
      Chat newChat = chatmanager.createChat(to, null);
      try {
        newChat.sendMessage(message);
      } catch (SmackException.NotConnectedException e) {
        logger.error("", e);
      }
    }
  }

  public void setInChannel(Channel inChannel) {
    this.inChannel = inChannel;
  }

  public Channel getInChannel() {
    return inChannel;
  }

  public void setOutChannel(Channel outChannel) {
    this.outChannel = outChannel;
    try {
      new BotQueueConsumer(outChannel, this);//todo не здесь должно быть
    } catch (IOException e) {
      logger.error("", e);
    }
  }

  public Channel getOutChannel() {
    return outChannel;
  }


  public void setOutQueueName(String outQueueName) {
    this.outQueueName = outQueueName;
  }

  public String getOutQueueName() {
    return outQueueName;
  }

  public void setInQueueName(String inQueueName) {
    this.inQueueName = inQueueName;
  }

  public String getInQueueName() {
    return inQueueName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}

package com.bftcom.devcomp.bots;

import com.bftcom.devcomp.api.*;
import com.bftcom.devcomp.queues.BotQueueConsumer;
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
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.proxy.ProxyInfo;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * date: 24.09.2016
 *
 * @author p.shapoval
 */
@SuppressWarnings("PackageAccessibility")
public class XmppBot implements IBot, Runnable {
  private static final Logger logger = LoggerFactory.getLogger(XmppBot.class);
  private final Map<String, String> config;
  private final String username;
  private final String password;
  private XMPPTCPConnection connection;
  private Channel inChannel;
  private Channel outChannel;
  private String outQueueName;
  private String inQueueName;
  private String name;

  private Presence presence;

  private XMPPTCPConnectionConfiguration.Builder configBuilder;

  public XmppBot(String name, Map<String, String> config) throws IOException, XMPPException, SmackException {
    logger.debug("About to create a new xmpp bot");
    this.config = config;
    this.name = name;

    configBuilder = XMPPTCPConnectionConfiguration.builder();
    username = config.get(Configuration.USERNAME);
    password = config.get(Configuration.PASSWORD);
    String domain = config.get(Configuration.DOMAIN);
    String server = config.get(Configuration.SERVER);
    String host = config.get(Configuration.SERVER);
    int port = Integer.parseInt(config.get(Configuration.PORT));

    String pHost = config.get(Configuration.PROXY_HOST);
    String pUser = config.get(Configuration.PROXY_USER);
    String pPass = config.get(Configuration.PROXY_PASSWORD);
//      String pUser = null;
//      String pPass = null;
    int pPort = Integer.parseInt(config.get(Configuration.PROXY_PORT));

    configBuilder.setUsernameAndPassword(username, password);
    configBuilder.setResource(domain);
    configBuilder.setServiceName(server);
    configBuilder.setHost(server);
    configBuilder.setPort(port);

    configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
    configBuilder.setConnectTimeout(60000);
    ProxyInfo.ProxyType proxyType = getProxyType(config.get(Configuration.PROXY_TYPE));
    if (proxyType != ProxyInfo.ProxyType.NONE) {
      configBuilder.setProxyInfo(new ProxyInfo(proxyType, pHost, pPort, pUser, pPass));
    }

    connectAndLogin();

    logger.debug("bot's configuration has been completed");
  }

  private void connectAndLogin() throws IOException, XMPPException, SmackException {
    connection = new XMPPTCPConnection(configBuilder.build());
    int priority = 10;
    connection.connect();
    connection.login(username, password);
    presence = new Presence(Presence.Type.available);
    presence.setStatus("online");
    connection.sendStanza(presence);
    presence.setPriority(priority);
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

  @Override
  public void sendMessage(Object o) {
    if (o instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, String> map = (Map<String, String>) o;
      String to = map.get("to");
      String message = map.get("message");
      sendMessage(to, message);
    }
  }

  @Override
  public String getOutQueueName() {
    return outQueueName;
  }

  @Override
  public String getInQueueName() {
    return inQueueName;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setInQueueName(String inQueueName) {
    this.inQueueName = inQueueName;
  }

  @Override
  public void setOutQueueName(String outQueueName) {
    this.outQueueName = outQueueName;
  }

  @Override
  public void setInChannel(Channel in) {
    inChannel = in;
  }

  @Override
  public Channel getInChannel() {
    return inChannel;
  }

  @Override
  public void setOutChannel(Channel out) throws IOException {
      new BotQueueConsumer(out, this);
    outChannel = out;
  }

  @Override
  public Channel getOutChannel() {
    return outChannel;
  }

  @Override
  public void handleDelivery(Message message) {
    Map<String, String> map = new HashMap<>();
    map.put("to", message.getServiceProperties().get("chatId"));
    map.put("message", message.getUserProperties().get(IBotConst.PROP_BODY_TEXT));
    sendMessage((Object) map);
  }

  @Override
  public void run() {
    try {

      StanzaFilter filter = new AndFilter(new StanzaTypeFilter(org.jivesoftware.smack.packet.Message.class));

      StanzaListener myListener = packet -> {
        if (packet instanceof org.jivesoftware.smack.packet.Message) {

          org.jivesoftware.smack.packet.Message message = (org.jivesoftware.smack.packet.Message) packet;
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
          String from = message.getFrom();
          int endIndex = from.indexOf("/");
          if (endIndex >= 0) {
            serviceProperties.put(IBotConst.PROP_USER_NAME, from.subSequence(0, endIndex).toString());
          } else {
            serviceProperties.put(IBotConst.PROP_USER_NAME, from);
          }
          serviceProperties.put("chatId", String.valueOf(from));
          handleUserIncomingData(msgToForward);
        }
      };

      connection.addAsyncStanzaListener(myListener, filter);
      Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);

      while (true) {
        Thread.sleep(15000);

        try {
          connection.sendStanza(presence);
        }
        catch (SmackException.NotConnectedException e) {
          e.printStackTrace();
          connection.disconnect();
        }

        if (!connection.isConnected()) {
          logger.info("Разорвана связь сервером XMPP. Восстановление.");
          try {
            connectAndLogin();
          } catch (IOException | XMPPException | SmackException e) {
            logger.info("Восстановление связи с сервером xmpp: " + e.getMessage());
            e.printStackTrace();
          }
        }
      }

    } catch (InterruptedException e) {
      logger.error("Exit from jabber account with error!", e);
      connection.disconnect();
    }
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
}

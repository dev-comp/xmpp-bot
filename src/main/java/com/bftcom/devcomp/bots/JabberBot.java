package com.bftcom.devcomp.bots;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.filter.*;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.proxy.ProxyInfo;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JabberBot implements Runnable {
  //slf4j logger
  private static Logger logger = LoggerFactory.getLogger(JabberBot.class);

  private String nick = "BftDevCompEchoService";
  private String password = "dev-comp";
  private String domain = "jabber.ru";
  private String server = "jabber.ru";
  private int port = 5222;

  private String proxy_ip = "10.0.0.1";
  private int proxy_port = 8080;
  //  private String proxy_type = "HTTP";
  private String proxy_type = "NONE";
  private String proxy_user = "proxy_user";
  private String proxy_password = "proxy_pwd";

  private XMPPTCPConnection connection;

  @Override
  public void run() {
    try {
      XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
      configBuilder.setUsernameAndPassword(nick, password);
      configBuilder.setResource(domain);
      configBuilder.setServiceName(server);
      configBuilder.setHost(server);
      configBuilder.setPort(port);
      configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
      configBuilder.setConnectTimeout(60000);
      ProxyInfo.ProxyType proxyType = getProxyType(proxy_type);
      if (proxyType != ProxyInfo.ProxyType.NONE)
        configBuilder.setProxyInfo(new ProxyInfo(proxyType, proxy_ip, proxy_port, proxy_user, proxy_password));
      connection = new XMPPTCPConnection(configBuilder.build());
      int priority = 10;
      connection.connect();
      connection.login(nick, password);
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
    } catch (Exception e) {
      e.printStackTrace();
      connection.disconnect();
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

  private void sendMessage(String to, String message) {
    if (!message.equals("")) {
      ChatManager chatmanager = ChatManager.getInstanceFor(connection);
      Chat newChat = chatmanager.createChat(to, null);
      try {
        newChat.sendMessage(message);
      } catch (SmackException.NotConnectedException e) {
        System.out.println(e.getMessage());
      }

    }
  }

}

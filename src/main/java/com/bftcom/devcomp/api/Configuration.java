package com.bftcom.devcomp.api;

/**
 * @author p.shapoval
 */
public interface Configuration {
  //telegram consts

  String BOT_USERNAME = "botUsername";
  String BOT_TOKEN = "botToken";


  //XMPP consts
  String USERNAME = "username";
  String PASSWORD = "password";
  String DOMAIN = "domain";
  String SERVER = "server";
  String PORT = "port";
  

  //common
  String PROXY_HOST = "proxyHost";
  String PROXY_PORT = "proxyPort";
  String PROXY_TYPE = "proxyType";
  String PROXY_USER = "proxyUser";
  String PROXY_PASSWORD = "proxyPassword";

  //rabbitmq
  String RABBITMQ_HOST = "rabbitmq.host";
  String RABBITMQ_USERNAME = "rabbitmq.username";
  String RABBITMQ_PASSWORD = "rabbitmq.password";

  
}

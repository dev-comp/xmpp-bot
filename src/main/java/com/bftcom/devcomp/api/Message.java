package com.bftcom.devcomp.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс, представляющий собой сообщение для бомена между сервисом и адаптерами/экземплярами ботов
 * date: 18.09.2016
 *
 * @author p.shapoval
 */
public class Message {

  private BotCommand command;
  private Map<String, String> userProperties = new HashMap<>();
  private Map<String, String> serviceProperties = new HashMap<>();

  public BotCommand getCommand() {
    return command;
  }

  public void setCommand(BotCommand command) {
    this.command = command;
  }

  @SuppressWarnings("unused")
  public Map<String, String> getUserProperties() {
    return userProperties;
  }

  public void setUserProperties(Map<String, String> userProperties) {
    this.userProperties = userProperties;
  }

  @SuppressWarnings("unused")
  public Map<String, String> getServiceProperties() {
    return serviceProperties;
  }

  public void setServiceProperties(Map<String, String> serviceProperties) {
    this.serviceProperties = serviceProperties;
  }

  @Override
  public String toString() {
    return "Message{" +
        "command=" + command +
        ", userProperties=" + userProperties +
        ", serviceProperties=" + serviceProperties +
        '}';
  }
}

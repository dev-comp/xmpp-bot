package com.bftcom.devcomp.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author ikka
 * @date: 24.09.2016.
 */
public interface IBot extends IDeliveryHandler {
  ObjectMapper mapper = new ObjectMapper();
  Logger logger = LoggerFactory.getLogger(IBot.class);

  void sendMessage(Object o);

  /**
   * @return Queue name for outgoing messages (from service to users)
   */
  String getOutQueueName();

  /**
   * @return Queue name for incoming messages from users to services
   */
  String getInQueueName();

  String getName();

  void setInQueueName(String inQueueName);

  void setOutQueueName(String outQueueName);

  void setInChannel(Channel in);

  Channel getInChannel();

  void setOutChannel(Channel out);

  Channel getOutChannel();

  default void handleUserIncomingData(Message message) {
    try {
      getInChannel().basicPublish("", getInQueueName(), null, mapper.writeValueAsString(message).getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      logger.error("", e);
    }
  }
}

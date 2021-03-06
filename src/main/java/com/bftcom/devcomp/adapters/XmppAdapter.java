package com.bftcom.devcomp.adapters;

import com.bftcom.devcomp.api.AbstractMessengerAdapter;
import com.bftcom.devcomp.api.IBot;
import com.bftcom.devcomp.api.IBotConst;
import com.bftcom.devcomp.bots.XmppBot;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * todo@shapoval add class description
 * <p>
 * date: 24.09.2016
 *
 * @author p.shapoval
 */
@SuppressWarnings("PackageAccessibility")
public class XmppAdapter extends AbstractMessengerAdapter<Thread> {
  @SuppressWarnings("PackageAccessibility")
  private static final Logger logger = LoggerFactory.getLogger(XmppAdapter.class);

  /**
   * Имя прослушиваемой очереди для сообщений от экземпляров ботов
   */
  private static final String ADAPTER_NAME = "xmpp-adapter";


  @Override
  protected boolean startBotSession(String id, IBot bot) {
    logger.info("registering bot " + id);
    if (bot instanceof Runnable) {
      Thread botSession = new Thread((Runnable) bot);
      synchronized (botSessions) {
        botSessions.put(id, botSession);
        logger.debug("starting bot");
        botSession.start();
      }
    }
    return true;
  }

  @Override
  protected IBot createNewBot(Map<String, String> serviceProps, Map<String, String> userProps) {
    Map<String, String> config = new HashMap<>(serviceProps);
    config.putAll(userProps);
    try {
      return new XmppBot(serviceProps.get(IBotConst.PROP_BOT_NAME), config);
    } catch (IOException | XMPPException | SmackException e) {
      logger.error("", e);
      return null;
    }
  }


 
  @Override
  public boolean stopBotSession(Thread botSession) {
    synchronized (botSessions) {
      if (botSession != null) {
        botSession.interrupt();
      }
    }
    return true;
  }


  @Override
  public String getAdapterName() {
    return ADAPTER_NAME;
  }
    
  public static void main(String[] args) {
    Properties props = new Properties();
    try {
      props.load(new FileInputStream("project.properties"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    for (Object o : props.keySet()) {
      System.setProperty((String) o, props.getProperty(String.valueOf(o)));
    }

    /*XmppAdapter botManager =*/ new XmppAdapter();

    /*
    HashMap<String, String> serviceProps = new HashMap<>();
    serviceProps.put(IBotConst.PROP_BOT_NAME, "IkkaBot");
    HashMap<String, String> userProps = new HashMap<>();
    userProps.put(Configuration.PROXY_HOST, "localhost");
    userProps.put(Configuration.PROXY_PORT, "53128");
    userProps.put(Configuration.PROXY_TYPE, "HTTP");

    userProps.put(Configuration.SERVER, "jabber.ru");
    userProps.put(Configuration.DOMAIN, "jabber.ru");
    userProps.put(Configuration.SERVER, "jabber.ru");
    userProps.put("host", "jabber.ru");
    userProps.put("port", "5222");
    
    userProps.put(Configuration.USERNAME, "ikka");
    userProps.put(Configuration.PASSWORD, "1Jabber@");

    botManager.startBotSession("IkkaBot", userProps, serviceProps);
    */


    

  }
}

package com.bftcom.devcomp.bots;

public class App {
  public static void main(String[] args) {
    try {
      JabberBot bot = new JabberBot();
      Thread botThread = new Thread(bot);
      botThread.start();
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}

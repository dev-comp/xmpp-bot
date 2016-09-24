package com.bftcom.devcomp.api;

/**
 *  онстанты
 */
public interface IBotConst {
  // ѕрефиксы очередей
  String QUEUE_TO_SERVICE_PREFIX = "TO_SERVICE_QUEUE_";     // ќчереди, которые слушает сервис
  String QUEUE_TO_ADAPTER_PREFIX = "TO_ADAPTER_QUEUE_";     // ќчереди, которые слушают адаптеры
  String QUEUE_TO_BOT_PREFIX = "TO_BOT_QUEUE_";       // ќчереди, которые слушают боты
  String QUEUE_FROM_BOT_PREFIX = "FROM_BOT_QUEUE_";   // ќчереди, которые слушает сервис (дл€ каждого бота собственна€ очередь)

  // »мена системных пропертей, передаваемых в сообщении
  String PROP_ADAPTER_NAME = "ADAPTER_NAME";                // —войство, определ€ющие им€ адаптера
  String PROP_BOT_NAME = "BOT_NAME";                        // —войство, определ€ющие им€ экземпл€ра адаптера
  String PROP_USER_NAME = "USER_NAME";                      // —войство, определ€ющие им€ пользовател€

  // »мена пользователских пропертей, передаваемых в сообщении
  String PROP_BODY_TEXT = "BODY_TEXT";                     // —войство, определ€ющие текст, передаваемый от бота и обратно (текст сообщени€)

}

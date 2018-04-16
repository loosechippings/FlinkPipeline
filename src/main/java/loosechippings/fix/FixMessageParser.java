package loosechippings.fix;


import loosechippings.FixMessage;
import loosechippings.WrappedFixMessage;

import java.io.Serializable;
import java.util.Arrays;

public class FixMessageParser implements Serializable {

   private static final Character FIELD_DELIMETER = 0x01;
   private static final String KEY_VALUE_SEPARTOR = "=";
   public static final String MESSAGE_TYPE = "35";
   public static final String SENDER_COMP_ID = "49";
   public static final String ID = "571";

   public FixMessage parse(WrappedFixMessage message) {
      String[] fields = message.getMessageBody().split(FIELD_DELIMETER.toString());
      FixMessage.Builder fixMessageBuilder = FixMessage.newBuilder();
      Arrays.stream(fields)
            .map(f -> f.split(KEY_VALUE_SEPARTOR))
            .forEach(v -> {
               switch (v[0]) {
                  case ID: fixMessageBuilder.setId(v[1]);
                     break;
                  case MESSAGE_TYPE: fixMessageBuilder.setMessageType(v[1]);
                     break;
                  case SENDER_COMP_ID: fixMessageBuilder.setSenderCompId(v[1]);
                     break;
                  default:
               }
            });
      return fixMessageBuilder
            .setMessageBody(message.getMessageBody())
            .build();
   }
}

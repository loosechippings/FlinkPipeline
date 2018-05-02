package loosechippings.avro;

import loosechippings.FixMessage;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class KeyedAvroSerializerTest {
   @Test
   public void serializesKeyField() {
      Properties props = new Properties();
      KeyedAvroSerializer<FixMessage> serializer = new KeyedAvroSerializer<>("topic", "id", props);

      FixMessage testMessage = FixMessage.newBuilder()
            .setId("ID1")
            .setMessageType("A")
            .setSenderCompId("S")
            .setMessageBody("ABCDEF")
            .build();

      Assert.assertArrayEquals("ID1".getBytes(), serializer.serializeKey(testMessage));
   }
}

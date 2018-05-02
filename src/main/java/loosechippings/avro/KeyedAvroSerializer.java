package loosechippings.avro;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class KeyedAvroSerializer<T extends GenericRecord> implements KeyedSerializationSchema<T> {
   private static final long serialVersionUID = 1L;
   private static final int SCHEMA_CAPACITY = 1000;
   private final String topic;
   private final String keyField;
   private KafkaAvroSerializer avroSerializer;
   Map<String, Object> propsMap;

   public KeyedAvroSerializer(String topic, String keyField, Properties props) {
      this.topic = topic;
      this.keyField = keyField;
      propsMap = new HashMap<>();
      propsMap.put("schema.registry.url", props.getProperty("schema.registry.url"));
      propsMap.put("auto.register.schemas", props.getProperty("auto.register.schemas"));
   }

   @Override
   public byte[] serializeKey(T t) {
      return t.get(keyField).toString().getBytes();
   }

   @Override
   public byte[] serializeValue(T obj) {
      if (avroSerializer == null) {
         SchemaRegistryClient schemaRegistryClient = new CachedSchemaRegistryClient(
               propsMap.get("schema.registry.url").toString(),
               SCHEMA_CAPACITY
         );
         avroSerializer = new KafkaAvroSerializer(schemaRegistryClient, propsMap);
      }
      return avroSerializer.serialize(topic, obj);
   }

   @Override
   public String getTargetTopic(T t) {
      return null;
   }
}

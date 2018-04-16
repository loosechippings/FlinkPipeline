package loosechippings.avro;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.flink.api.common.serialization.SerializationSchema;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class AvroSerializer<T> implements SerializationSchema<T> {
   private static final long serialVersionUID = 1L;
   private static final int SCHEMA_CAPACITY = 1000;
   private final String topic;
   private KafkaAvroSerializer avroSerializer;
   Map<String, Object> propsMap;

   public AvroSerializer(String topic, Properties props) {
      this.topic =topic;
      propsMap = new HashMap<>();
      propsMap.put("schema.registry.url", props.getProperty("schema.registry.url"));
      propsMap.put("auto.register.schemas", props.getProperty("auto.register.schemas"));
   }

   @Override
   public byte[] serialize(T obj) {
      if (avroSerializer == null) {
         SchemaRegistryClient schemaRegistryClient = new CachedSchemaRegistryClient(
               propsMap.get("schema.registry.url").toString(),
               SCHEMA_CAPACITY
         );
         avroSerializer = new KafkaAvroSerializer(schemaRegistryClient, propsMap);
      }
      return avroSerializer.serialize(topic, obj);
   }
}

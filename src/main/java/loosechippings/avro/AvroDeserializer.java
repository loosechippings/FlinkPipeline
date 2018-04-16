package loosechippings.avro;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.specific.SpecificRecord;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class AvroDeserializer<T> implements DeserializationSchema<T> {
   private static final long serialVersionUID = 1L;
   private static final int SCHEMA_CAPACITY = 1000;
   private final String topic;
   private KafkaAvroDeserializer avroDeserializer;
   Map<String, Object> propsMap;
   Class<T> schema;

   public AvroDeserializer(String topic, Properties props, Class<T> schema) {
      this.topic = topic;
      this.schema = schema;
      propsMap = new HashMap<>();
      propsMap.put("schema.registry.url", props.getProperty("schema.registry.url"));
      propsMap.put("auto.register.schemas", props.getProperty("auto.register.schemas"));
      propsMap.put("specific.avro.reader", props.getProperty("specific.avro.reader"));
   }

   @Override
   public T deserialize(byte[] bytes) throws IOException {
      if (avroDeserializer == null) {
         SchemaRegistryClient schemaRegistryClient = new CachedSchemaRegistryClient(
               propsMap.get("schema.registry.url").toString(),
               SCHEMA_CAPACITY
         );
         avroDeserializer = new KafkaAvroDeserializer(schemaRegistryClient, propsMap);
      }
      return schema.cast(avroDeserializer.deserialize(topic, bytes));
   }

   @Override
   public boolean isEndOfStream(T t) {
      return false;
   }

   @Override
   public TypeInformation<T> getProducedType() {
      return TypeInformation.of(schema);
   }
}

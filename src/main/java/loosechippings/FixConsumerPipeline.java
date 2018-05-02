package loosechippings;

import loosechippings.avro.AvroDeserializer;
import loosechippings.fix.FixMessageParser;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;

import java.util.Properties;

public class FixConsumerPipeline {

   public static void main(String[] args) throws Exception {
      StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
      env.getConfig().setLatencyTrackingInterval(100L);

      ParameterTool paramTool = ParameterTool.fromPropertiesFile("/home/nick/repos/flinkpipeline/src/main/resources/fix-pipeline.properties");
      Properties props = paramTool.getProperties();

      FixMessageParser parser = new FixMessageParser();
      String topicName = paramTool.getRequired("fix.topic");

      FlinkKafkaConsumer011<WrappedFixMessage> fixMessageConsumer = new FlinkKafkaConsumer011<>(
            topicName,
            new AvroDeserializer<>(topicName, props, WrappedFixMessage.class),
            props
      );

      DataStream<WrappedFixMessage> fixMessageStream = env.addSource(fixMessageConsumer);
      fixMessageConsumer.setStartFromEarliest();
      fixMessageStream
            .map(new ParseMapFunction())
            .print();

      env.execute();
   }
}

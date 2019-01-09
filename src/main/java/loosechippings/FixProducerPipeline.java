package loosechippings;

import loosechippings.fix.FixMessageMaker;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class FixProducerPipeline {

   private static final int LIST_SIZE = 1000000;

   public static void main(String[] args) throws Exception {
      Instant startTime = Instant.parse("2018-05-01T08:00:00.000Z");
      StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

      ParameterTool paramTool = ParameterTool.fromPropertiesFile("/home/nick/repos/flinkpipeline/src/main/resources/fix-pipeline.properties");
      Properties props = paramTool.getProperties();

      FixMessageMaker fixMessageMaker = new FixMessageMaker();

      FlinkKafkaProducer011<String> fixMessageProducer = new FlinkKafkaProducer011<String>(
            paramTool.getRequired("fix.topic"),
            new SimpleStringSchema(),
            props
      );

      List<Integer> sourceList = new ArrayList<>(LIST_SIZE);
      for (Integer i=0; i<LIST_SIZE; i++) {
         sourceList.add(i);
      }
      DataStream<Integer> intStream = env.fromCollection(sourceList);
      intStream.map(i -> fixMessageMaker.make(startTime, i))
            .addSink(fixMessageProducer);

      env.execute();
   }
}

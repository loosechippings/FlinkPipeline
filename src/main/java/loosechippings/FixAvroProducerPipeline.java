package loosechippings;

import loosechippings.avro.AvroSerializer;
import loosechippings.avro.KeyedAvroSerializer;
import loosechippings.fix.FixMessageMaker;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class FixAvroProducerPipeline {

   private static final int LIST_SIZE = 1000000;

   public static void main(String[] args) throws Exception {
      StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

      ParameterTool paramTool = ParameterTool.fromPropertiesFile("/home/nick/repos/flinkpipeline/src/main/resources/fix-pipeline.properties");
      Properties props = paramTool.getProperties();

      FixMessageMaker fixMessageMaker = new FixMessageMaker();
      String topicName = paramTool.getRequired("fix.topic");

      FlinkKafkaProducer011<WrappedFixMessage> fixMessageProducer = new FlinkKafkaProducer011<>(
            topicName,
            new KeyedAvroSerializer<WrappedFixMessage>(topicName, "id", props),
            props,
            Optional.empty(),
            FlinkKafkaProducer011.Semantic.EXACTLY_ONCE,
            FlinkKafkaProducer011.DEFAULT_KAFKA_PRODUCERS_POOL_SIZE
      );

      List<Integer> sourceList = new ArrayList<>(LIST_SIZE);
      for (Integer i=0; i<LIST_SIZE; i++) {
         sourceList.add(i);
      }
      DataStream<Integer> intStream = env.fromCollection(sourceList);
      intStream
            .map(new MapFunction<Integer, Tuple2<Integer, String>>() {
               @Override
               public Tuple2<Integer, String> map(Integer integer) throws Exception {
                  return new Tuple2<Integer, String>(integer, fixMessageMaker.make(integer));
               }
            })
            .map(new MapFunction<Tuple2<Integer,String>, WrappedFixMessage>() {
               @Override
               public WrappedFixMessage map(Tuple2<Integer, String> integerStringTuple2) throws Exception {
                  return WrappedFixMessage.newBuilder()
                        .setId(integerStringTuple2.f0.toString())
                        .setMessageBody(integerStringTuple2.f1)
                        .build();
               }
            })
            .addSink(fixMessageProducer);

      env.execute();
   }
}

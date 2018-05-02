package loosechippings;

import loosechippings.fix.FixMessageParser;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;

public class ParseMapFunction extends RichMapFunction<WrappedFixMessage, FixMessage> {
   private FixMessageParser parser;

   @Override
   public void open(Configuration config) {
      parser = new FixMessageParser();
   }

   @Override
   public FixMessage map(WrappedFixMessage wrappedFixMessage) throws Exception {
      return parser.parse(wrappedFixMessage);
   }
}

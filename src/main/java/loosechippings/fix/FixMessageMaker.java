package loosechippings.fix;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class FixMessageMaker implements Serializable {

   private static final int NUMBER_OF_ORDERS = 10;
   private static final Character DELIM = 0x01;
   private static final String HEADER = "8=FIX.4.1" + DELIM +
         "9=112" + DELIM +
         "35=AE" + DELIM +
         "49=BRKR" + DELIM +
         "56=INVMGR" + DELIM +
         "52=%s" + DELIM +
         "34=235" + DELIM;
   private static final String MESSAGE = HEADER +
         "37=%d" + DELIM +
         "571=%d" + DELIM;

   public String make(Instant startTime, Integer i) {
      return String.format(MESSAGE, startTime.plus(1, ChronoUnit.MILLIS), i % NUMBER_OF_ORDERS, i);
   }
}

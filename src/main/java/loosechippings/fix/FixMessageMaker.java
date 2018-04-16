package loosechippings.fix;

import java.io.Serializable;

public class FixMessageMaker implements Serializable {

   private static final Character DELIM = 0x01;
   private static final String HEADER = "8=FIX.4.1" + DELIM +
         "9=112" + DELIM +
         "35=AE" + DELIM +
         "49=BRKR" + DELIM +
         "56=INVMGR" + DELIM +
         "34=235" + DELIM;
   private static final String MESSAGE = HEADER +
         "571=%d" + DELIM;

   public String make(Integer i) {
      return String.format(MESSAGE, i);
   }
}

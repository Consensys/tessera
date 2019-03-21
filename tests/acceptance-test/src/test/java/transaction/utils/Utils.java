package transaction.utils;

import java.util.Random;

public class Utils {


    public static byte[] generateTransactionData() {
        Random random = new Random();
        byte[] bytes = new byte[random.nextInt(500) + 1];
        random.nextBytes(bytes);
        return bytes;
    }    
   
}


package exec;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;



public class ExecUtils {
    public static void kill(String pid) {
        try{
            List<String> args = Arrays.asList("kill", pid);
            ProcessBuilder processBuilder = new ProcessBuilder(args);
            Process process = processBuilder.start();
            
            int exitCode = process.waitFor();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (InterruptedException ex) {
            
        }
    } 
}

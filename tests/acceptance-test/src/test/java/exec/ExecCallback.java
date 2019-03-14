
package exec;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


public interface ExecCallback<T> {
    
    T doExecute() throws IOException,InterruptedException,ExecutionException,TimeoutException;
    
    static <T> T doExecute(ExecCallback<T> callback) {
        try{
            return callback.doExecute();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch(TimeoutException | ExecutionException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
    
}

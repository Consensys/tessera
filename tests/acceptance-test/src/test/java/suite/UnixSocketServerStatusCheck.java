package suite;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UnixSocketServerStatusCheck implements ServerStatusCheck {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UnixSocketServerStatusCheck.class);
    
    private final UnixSocketAddress unixAddress;

    public UnixSocketServerStatusCheck(URI uri) {
        File file = Paths.get(uri).toFile();
        this.unixAddress = new UnixSocketAddress(file);
    }

    @Override
    public boolean checkStatus() {

        try{
            UnixSocketChannel channel = UnixSocketChannel.open(unixAddress);
            channel.configureBlocking(false);
            try {
           return channel.isConnected();
            } finally {
                channel.close();
            }
            
        } catch (IOException ex) {
           return false;
        }
    }

    @Override
    public String toString() {
        return "UnixSocketServerStatusCheck{" + "unixAddress=" + unixAddress + '}';
    }
    
}

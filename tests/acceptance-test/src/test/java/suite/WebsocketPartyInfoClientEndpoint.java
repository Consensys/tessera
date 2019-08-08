package suite;

import com.quorum.tessera.sync.SyncRequestMessageCodec;
import com.quorum.tessera.sync.SyncResponseMessage;
import com.quorum.tessera.sync.SyncResponseMessageCodec;
import java.util.concurrent.BlockingQueue;
import javax.websocket.ClientEndpoint;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClientEndpoint(
        decoders = {SyncResponseMessageCodec.class},encoders = {SyncRequestMessageCodec.class}
)
public class WebsocketPartyInfoClientEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebsocketPartyInfoClientEndpoint.class);

    private final BlockingQueue<SyncResponseMessage> queue;

    public WebsocketPartyInfoClientEndpoint(BlockingQueue<SyncResponseMessage> queue) {
        this.queue = queue;
    }


    @OnMessage
    public void onResponse(Session session,SyncResponseMessage response) {
        LOGGER.info("Recieved response {} from {}",response,session.getRequestURI());
        try {
            queue.put(response);
            LOGGER.info("Added : {} to Queue", response);
        } catch (InterruptedException ex) {
            LOGGER.error("", ex);
        }
    }

    @OnError
    public void handleError(Throwable ex) {
        LOGGER.error(null, ex);
    }

}

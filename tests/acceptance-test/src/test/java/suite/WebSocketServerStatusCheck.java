package suite;

import com.quorum.tessera.server.websockets.*;
import java.io.IOException;
import java.net.URI;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketServerStatusCheck implements ServerStatusCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketServerStatusCheck.class);

    private final URI uri;

    public WebSocketServerStatusCheck(URI uri) {
        this.uri = uri;
    }

    @Override
    public boolean checkStatus() {

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        StatusClientEndpoint endpoint = new StatusClientEndpoint();
        try (Session session = container.connectToServer(endpoint, uri)) {
            session.getBasicRemote().sendText("Hellow " + uri);
            return session.isOpen();
        } catch (DeploymentException | IOException ex) {
            LOGGER.debug("", ex);
            return false;
        }
    }

    @Override
    public String toString() {
        return "WebSocketServerStatusCheck{" + "uri=" + uri + '}';
    }
}

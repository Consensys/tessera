
package suite;

import java.net.URL;

public class WebSocketServerStatusCheck implements ServerStatusCheck {

    private final HttpServerStatusCheck httpServerStatusCheck;

    public WebSocketServerStatusCheck(URL url) {
         this.httpServerStatusCheck = new HttpServerStatusCheck(url);
    }

    @Override
    public boolean checkStatus() {
        return httpServerStatusCheck.checkStatus();
    }

    @Override
    public String toString() {
        return "WebSocketServerStatusCheck{" + "httpServerStatusCheck=" + httpServerStatusCheck + '}';
    }
    
}

package com.github.nexus.socket;

import com.github.nexus.configuration.Configuration;
import com.github.nexus.junixsocket.adapter.UnixSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URI;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Create a server listening on a Unix Domain Socket for http requests. We
 * create a connection to an HTTP server, and act as a proxy between the socket
 * and the HTTP server. TODO: should possibly support connections from multiple
 * clients
 * <p>
 * FIXME: This object has far too many internal dependencies, making it
 * resistant to effective testing.
 */
public class SocketServer implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketServer.class);

    private final UnixDomainServerSocket serverUds;

    private HttpProxyFactory httpProxyFactory;

    private HttpProxy httpProxy;

    private final URI serverUri;

    private final ScheduledExecutorService executor;

    /**
     * Create the unix domain socket and start the listener thread.
     */
    public SocketServer(Configuration config,
                        HttpProxyFactory httpProxyFactory,
                        URI serverUri,
                        ScheduledExecutorService executor,
                        UnixSocketFactory unixSocketFactory) {

        Objects.requireNonNull(config);
        Objects.requireNonNull(unixSocketFactory);

        this.executor = Objects.requireNonNull(executor, "Executor service is required");
        this.httpProxyFactory = Objects.requireNonNull(httpProxyFactory);
        this.serverUri = Objects.requireNonNull(serverUri);

        serverUds = new UnixDomainServerSocket(unixSocketFactory);
        serverUds.create(config.workdir(), config.socket());
    }

    @PostConstruct
    public void start() {
        executor.scheduleWithFixedDelay(this, 1, 1, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void stop() {
        executor.shutdown();
    }

    /**
     * Wait for a client connection, then: - create a connection to the HTTP
     * server - read the client HTTP request and forward it to the HTTP server -
     * read the HTTP response and return it to the client Note that Quorum opens
     * a new client connection for each request.
     */
    @Override
    public void run() {
        LOGGER.info("Waiting for client connection on unix domain socket...");
        serverUds.connect();
        LOGGER.info("Client connection received");

        //Get a connection to the HTTP server.
        if (createHttpServerConnection()) {

            //Read the request from the socket and send it to the HTTP server
            byte[] message = serverUds.read();
            LOGGER.info("Received message on socket: {}", message);
            httpProxy.sendRequest(message);

            //Return the HTTP response to the socket
            byte[] response = httpProxy.getResponse();
            LOGGER.info("Received http response: {}", response);
            serverUds.write(response);

            httpProxy.disconnect();
        }
    }

    /**
     * Get a connection to the HTTP Server.
     */
    //FIXME: 
    private boolean createHttpServerConnection() {

        httpProxy = httpProxyFactory.create(serverUri);

        // TODO: add configurable number of attempts, instead of looping forever
        boolean connected = false;
        while (!connected) {
            LOGGER.info("Attempting connection to HTTP server...");
            connected = httpProxy.connect();
        }
        LOGGER.info("Connected to HTTP server");

        return true;
    }


}

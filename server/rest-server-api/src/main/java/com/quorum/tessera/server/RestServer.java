package com.quorum.tessera.server;

/**
 * An HTTP server that is fully configured
 */
public interface RestServer {

    /**
     * Starts the HTTP server listening on the configured port
     * @throws Exception any exception that occurs when starting the server, intended to fail fast
     */
    void start() throws Exception;

    /**
     * STops the HTTP server listening on the configured port
     * @throws Exception any exception that occurs when stopping the server, intended to fail fast
     */
    void stop() throws Exception;

}

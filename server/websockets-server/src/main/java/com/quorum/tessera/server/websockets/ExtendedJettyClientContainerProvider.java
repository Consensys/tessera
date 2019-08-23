package com.quorum.tessera.server.websockets;

import java.lang.reflect.Method;
import javax.websocket.WebSocketContainer;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.thread.ShutdownThread;
import org.eclipse.jetty.websocket.jsr356.ClientContainer;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import javax.net.ssl.SSLContext;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.common.scopes.SimpleContainerScope;
import org.slf4j.*;

public class ExtendedJettyClientContainerProvider extends javax.websocket.ContainerProvider {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedJettyClientContainerProvider.class);
    
    private static boolean useSingleton = false;
    
    private static boolean useServerContainer = false;
    
    private static WebSocketContainer instance;
    
    private static SSLContext sslContext;
    
    private static Object lock = new Object();

    /**
     * Change calls to {@link ContainerProvider#getWebSocketContainer()} to
     * always return a singleton instance of the same {@link WebSocketContainer}
     *
     * @param flag true to use a singleton instance of
     * {@link WebSocketContainer} for all calls to
     * {@link ContainerProvider#getWebSocketContainer()}
     */
    @SuppressWarnings("unused")
    public static void useSingleton(boolean flag) {
        useSingleton = flag;
    }
    
    protected static void setSslContext(SSLContext context) {
        sslContext = context;
    }

    /**
     * Test if {@link ContainerProvider#getWebSocketContainer()} will always
     * return a singleton instance of the same {@link WebSocketContainer}
     *
     * @return true if using a singleton instance of {@link WebSocketContainer}
     * for all calls to {@link ContainerProvider#getWebSocketContainer()}
     */
    @SuppressWarnings("unused")
    public static boolean willUseSingleton() {
        return useSingleton;
    }

    /**
     * Add ability of calls to {@link ContainerProvider#getWebSocketContainer()}
     * to find and return the {@code javax.websocket.server.ServerContainer}
     * from the active {@code javax.servlet.ServletContext}.
     * <p>
     * This will only work if the call to
     * {@link ContainerProvider#getWebSocketContainer()} occurs within a thread
     * being processed by the Servlet container.
     * </p>
     *
     * @param flag true to to use return the
     * {@code javax.websocket.server.ServerContainer} from the active
     * {@code javax.servlet.ServletContext} for all calls to
     * {@link ContainerProvider#getWebSocketContainer()} from within a Servlet
     * thread.
     */
    @SuppressWarnings("unused")
    public static void useServerContainer(boolean flag) {
        useServerContainer = flag;
    }

    /**
     * Test if {@link ContainerProvider#getWebSocketContainer()} has the ability
     * to find and return the {@code javax.websocket.server.ServerContainer}
     * from the active {@code javax.servlet.ServletContext}, before creating a
     * new client based {@link WebSocketContainer}.
     *
     * @return true if {@link WebSocketContainer} returned from calls to
     * {@link ContainerProvider#getWebSocketContainer()} could be the
     * {@code javax.websocket.server.ServerContainer} from the active
     * {@code javax.servlet.ServletContext}
     */
    @SuppressWarnings("unused")
    public static boolean willUseServerContainer() {
        return useServerContainer;
    }
    
    public Object getContextHandler() {
        try {
            // Equiv of: ContextHandler.Context context = ContextHandler.getCurrentContext()
            Class<?> clazzContextHandler = Class.forName("org.eclipse.jetty.server.handler.ContextHandler");
            Method methodGetContext = clazzContextHandler.getMethod("getCurrentContext");
            Object objContext = methodGetContext.invoke(null);
            if (objContext == null) {
                return null;
            }

            // Equiv of: ContextHandler handler = ContextHandler.getContextHandler(context);
            Class<?> clazzServletContext = Class.forName("javax.servlet.ServletContext");
            Method methodGetContextHandler = clazzContextHandler.getMethod("getContextHandler", clazzServletContext);
            return methodGetContextHandler.invoke(null, objContext);
        } catch (Throwable ignore) {
            // LOG.ignore(ignore);
            return null;
        }
    }

    /**
     * Used by {@link ContainerProvider#getWebSocketContainer()} to get a new
     * instance of the Client {@link WebSocketContainer}.
     */
    @Override
    protected WebSocketContainer getContainer() {
        synchronized (lock) {
            WebSocketContainer webSocketContainer = null;
            Object contextHandler = getContextHandler();
            
            if (useServerContainer && contextHandler != null) {
                try {
                    // Attempt to use the ServerContainer attribute.
                    Method methodGetServletContext = contextHandler.getClass().getMethod("getServletContext");
                    Object objServletContext = methodGetServletContext.invoke(contextHandler);
                    if (objServletContext != null) {
                        Method methodGetAttribute = objServletContext.getClass().getMethod("getAttribute", String.class);
                        Object objServerContainer = methodGetAttribute.invoke(objServletContext, "javax.websocket.server.ServerContainer");
                        if (objServerContainer != null && objServerContainer instanceof WebSocketContainer) {
                            webSocketContainer = (WebSocketContainer) objServerContainer;
                        }
                    }
                    
                } catch (Throwable ignore) {
                    //LOG.ignore(ignore);
                    // continue, without server container
                }
            }
            
            if (useSingleton && instance != null) {
                return instance;
            }

            // Still no instance?
            if (webSocketContainer == null) {

                // SimpleContainerScope containerScope = new SimpleContainerScope(WebSocketPolicy.newClientPolicy());
                final SslContextFactory ssl = new SslContextFactory.Client();
                ssl.setSslContext(sslContext);

                SimpleContainerScope containerScope = new SimpleContainerScope(WebSocketPolicy.newClientPolicy());
                containerScope.setSslContextFactory(ssl);

                ClientContainer clientContainer = new ClientContainer(containerScope);
                
                if (contextHandler != null && contextHandler instanceof ContainerLifeCycle) {
                    // Add as bean to contextHandler
                    // Allow startup to follow Jetty lifecycle
                    ((ContainerLifeCycle) contextHandler).addManaged(clientContainer);
                } else {
                    // Static Initialization
                    // register JVM wide shutdown thread
                    ShutdownThread.register(clientContainer);
                }
                
                if (!clientContainer.isStarted()) {
                    try {
                        clientContainer.start();
                        LOGGER.info("clientContainer : {}", clientContainer.dump());
                    } catch (Exception e) {
                        throw new RuntimeException("Unable to start Client Container", e);
                    }
                }
                
                webSocketContainer = clientContainer;
            }
            
            if (useSingleton) {
                instance = webSocketContainer;
            }
            
            return webSocketContainer;
        }
    }
}

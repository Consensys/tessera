package com.quorum.tessera.server;

import com.quorum.tessera.config.Config;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerDomainSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.channel.unix.ServerDomainSocketChannel;
import org.glassfish.jersey.netty.httpserver.NettyHttpContainerHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.ws.rs.core.Application;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class UnixSocketServer implements TesseraServer  {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnixSocketServer.class);

    private final DomainSocketAddress unixSocketAddress;

    private final ResourceConfig resourceConfig;

    private URI uri;

    private Channel unixSocketServer;

    public UnixSocketServer(final Config config, final Application application) {
        this.unixSocketAddress = new DomainSocketAddress(config.getUnixSocketFile().toFile());

        try {
            this.uri = new URI("unixsocket");
        } catch (final URISyntaxException ex) {
            LOGGER.error("Unable to ensure that the server URI ends with /");
            throw new RuntimeException(ex);
        }

        /////
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        //https://jersey.github.io/documentation/latest/appendix-properties.html
        final Map<String, Object> initParams = new HashMap<>();
        initParams.put("jersey.config.server.application.name", application.getClass().getSimpleName());
        initParams.put("jersey.config.server.tracing.type", "ON_DEMAND");
        initParams.put("jersey.config.server.tracing.threshold", "SUMMARY");
        initParams.put("jersey.config.logging.verbosity", "PAYLOAD_ANY");
        initParams.put("jersey.config.beanValidation.enableOutputValidationErrorEntity.server", "true");
        initParams.put("jersey.config.server.monitoring.statistics.enabled", "true");
        initParams.put("jersey.config.server.monitoring.enabled", "true");
        initParams.put("jersey.config.server.monitoring.statistics.mbeans.enabled", "true");
        this.resourceConfig = ResourceConfig.forApplication(application);
        this.resourceConfig.addProperties(initParams);
    }

    @Override
    public void start() throws InterruptedException {

        if (null != this.unixSocketAddress) {

            if(Epoll.isAvailable()) {
                this.unixSocketServer = this.createChannel(
                    EpollServerDomainSocketChannel.class, new EpollEventLoopGroup(), new EpollEventLoopGroup()
                );
            } else if (KQueue.isAvailable()) {
                this.unixSocketServer = this.createChannel(
                    KQueueServerDomainSocketChannel.class, new KQueueEventLoopGroup(), new KQueueEventLoopGroup()
                );
            }

        }

    }

    @Override
    public void stop() throws InterruptedException {
        this.unixSocketServer.close().sync();
    }

    private Channel createChannel(final Class<? extends ServerDomainSocketChannel> serverDomainClass,
                                  final EventLoopGroup bossGroup,
                                  final EventLoopGroup workerGroup) throws InterruptedException {

        final NettyHttpContainerHolder holder = new NettyHttpContainerHolder(uri, resourceConfig);

        final Channel ch = new ServerBootstrap()
            .option(ChannelOption.SO_BACKLOG, 4096)
            .group(bossGroup, workerGroup)
            .channel(serverDomainClass)
            .childHandler(holder.getChannelInitializer())
            .bind(unixSocketAddress)
            .sync()
            .channel();

        ch.closeFuture().addListener(holder.getContainerShutdownFuture());
        ch.closeFuture().addListener(future -> {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        });

        return ch;
    }

}

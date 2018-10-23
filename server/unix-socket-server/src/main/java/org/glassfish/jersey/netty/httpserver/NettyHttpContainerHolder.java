package org.glassfish.jersey.netty.httpserver;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.unix.DomainSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * A helper class that allows access to {@link NettyHttpContainer} and
 * {@link JerseyServerHandler}
 *
 * Creates the socket channel init and shutdown sequences that require the
 * package private classes.
 */
public class NettyHttpContainerHolder {

    private static final URI URI = UriBuilder.fromPath("unixsocket").build();

    private final NettyHttpContainer container;

    public NettyHttpContainerHolder(final ResourceConfig configuration) {

        this.container = new NettyHttpContainer(configuration);
    }

    public ChannelInitializer<DomainSocketChannel> getChannelInitializer() {
        return new ChannelInitializer<DomainSocketChannel>() {
            @Override
            protected void initChannel(final DomainSocketChannel ch) {
                ch.pipeline().addLast(new HttpServerCodec());
                ch.pipeline().addLast(new ChunkedWriteHandler());
                ch.pipeline().addLast(new JerseyServerHandler(URI, container));
            }
        };
    }

    public GenericFutureListener<? extends Future<? super Void>> getContainerShutdownFuture() {
         return future -> container.getApplicationHandler().onShutdown(container);
    }

}

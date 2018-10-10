package org.glassfish.jersey.netty.httpserver;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.unix.DomainSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class NettyHttpContainerHolder {

    private final NettyHttpContainer container;

    private final GenericFutureListener<? extends Future<? super Void>> containerShutdownFuture;

    private final ChannelInitializer<DomainSocketChannel> initializer;

    public NettyHttpContainerHolder(final URI baseUri, final ResourceConfig configuration) {

        this.container = new NettyHttpContainer(configuration);

        this.initializer = new ChannelInitializer<DomainSocketChannel>() {
            @Override
            protected void initChannel(final DomainSocketChannel ch) {
                ch.pipeline().addLast(new HttpServerCodec());
                ch.pipeline().addLast(new ChunkedWriteHandler());
                ch.pipeline().addLast(new JerseyServerHandler(baseUri, container));
            }
        };

        this.containerShutdownFuture = future -> container.getApplicationHandler().onShutdown(container);

    }

    public ChannelInitializer<DomainSocketChannel> getChannelInitializer() {
        return this.initializer;
    }

    public GenericFutureListener<? extends Future<? super Void>> getContainerShutdownFuture() {
         return this.containerShutdownFuture;
    }

}

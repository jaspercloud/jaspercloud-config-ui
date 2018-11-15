package org.springframework.config.rds.server.component;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.DatagramDnsQueryDecoder;
import io.netty.handler.codec.dns.DatagramDnsResponseEncoder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.config.rds.server.support.dns.DnsChannelInboundHandlerAdapter;
import org.springframework.stereotype.Component;

@Component
public class DnsServer implements InitializingBean, DisposableBean {

    @Autowired
    private DnsChannelInboundHandlerAdapter handlerAdapter;

    private Channel channel;

    @Override
    public void afterPropertiesSet() throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel nioDatagramChannel) throws Exception {
                            nioDatagramChannel.pipeline().addLast(new DatagramDnsQueryDecoder());
                            nioDatagramChannel.pipeline().addLast(new DatagramDnsResponseEncoder());
                            nioDatagramChannel.pipeline().addLast(handlerAdapter);
                        }
                    })
                    .option(ChannelOption.SO_BROADCAST, true);
            ChannelFuture future = bootstrap.bind(53).sync();
            channel = future.channel();
            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    @Override
    public void destroy() throws Exception {
        if (null != channel) {
            channel.close();
        }
    }
}

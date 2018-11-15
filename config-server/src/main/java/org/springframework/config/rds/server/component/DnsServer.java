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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.config.rds.server.support.dns.DnsChannelInboundHandlerAdapter;
import org.springframework.stereotype.Component;

@Component
public class DnsServer implements InitializingBean, DisposableBean, Runnable {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DnsChannelInboundHandlerAdapter handlerAdapter;

    private Channel channel;
    private Thread thread;

    @Override
    public void afterPropertiesSet() throws Exception {
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void destroy() throws Exception {
        if (null != channel) {
            channel.close();
        }
        if (null != thread) {
            thread.interrupt();
        }
    }

    @Override
    public void run() {
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
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            group.shutdownGracefully();
        }
    }
}

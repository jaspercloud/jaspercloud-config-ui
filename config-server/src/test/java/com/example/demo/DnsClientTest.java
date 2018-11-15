package com.example.demo;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.*;
import io.netty.resolver.ResolvedAddressTypes;
import io.netty.resolver.dns.DnsNameResolver;
import io.netty.resolver.dns.DnsNameResolverBuilder;
import io.netty.resolver.dns.NoopDnsCache;
import io.netty.resolver.dns.SequentialDnsServerAddressStreamProvider;
import io.netty.util.internal.SocketUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

public class DnsClientTest {

    public static void main(String[] args) throws Exception {
        DnsChannelInboundHandlerAdapter handlerAdapter = new DnsChannelInboundHandlerAdapter();
        handlerAdapter.init();
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
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    @ChannelHandler.Sharable
    private static class DnsChannelInboundHandlerAdapter extends ChannelInboundHandlerAdapter {

        private DnsNameResolver nameResolver;

        public void init() {
            InetSocketAddress inetSocketAddress = new InetSocketAddress("8.8.8.8", 53);
            NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
            nameResolver = new DnsNameResolverBuilder(eventExecutors.next())
                    .channelType(NioDatagramChannel.class)
                    .resolvedAddressTypes(ResolvedAddressTypes.IPV4_ONLY)
                    .resolveCache(NoopDnsCache.INSTANCE)
                    .nameServerProvider(new SequentialDnsServerAddressStreamProvider(inetSocketAddress))
                    .queryTimeoutMillis(5 * 1000)
                    .build();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            DatagramDnsQuery query = (DatagramDnsQuery) msg;
            DatagramDnsResponse response = new DatagramDnsResponse(query.recipient(), query.sender(), query.id());
            DefaultDnsQuestion dnsQuestion = query.recordAt(DnsSection.QUESTION);
            response.addRecord(DnsSection.QUESTION, dnsQuestion);
            DnsRecordType dnsRecordType = dnsQuestion.type();
            if (DnsRecordType.A.equals(dnsRecordType)) {
                String domain = dnsQuestion.name();
                List<InetAddress> inetAddressList = nameResolver.resolveAll(domain).get();
                for (InetAddress inetAddress : inetAddressList) {
                    byte[] address = SocketUtils.addressByName(inetAddress.getHostAddress()).getAddress();
                    DefaultDnsRawRecord queryAnswer = new DefaultDnsRawRecord(dnsQuestion.name(),
                            DnsRecordType.A, 5, Unpooled.wrappedBuffer(address));
                    response.addRecord(DnsSection.ANSWER, queryAnswer);
                }
            }
            ctx.writeAndFlush(response);
        }
    }
}

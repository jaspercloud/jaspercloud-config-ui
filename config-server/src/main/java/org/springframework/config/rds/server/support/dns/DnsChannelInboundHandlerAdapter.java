package org.springframework.config.rds.server.support.dns;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.*;
import io.netty.resolver.ResolvedAddressTypes;
import io.netty.resolver.dns.DefaultDnsCache;
import io.netty.resolver.dns.DnsNameResolver;
import io.netty.resolver.dns.DnsNameResolverBuilder;
import io.netty.resolver.dns.SequentialDnsServerAddressStreamProvider;
import io.netty.util.internal.SocketUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@ChannelHandler.Sharable
public class DnsChannelInboundHandlerAdapter extends ChannelInboundHandlerAdapter implements InitializingBean {

    @Value("${dns.servers}")
    private String[] dnsServers;

    @Value("${dns.query.timeout.millis}")
    private long queryTimeout;

    @Value("${dns.cache.timeout.seconds}")
    private int cacheSeconds;

    @Value("${dns.ttl.timeout.seconds}")
    private long ttlTimeout;

    @Value("${dns.domains}")
    private String[] dnsDomains;

    private DnsNameResolver nameResolver;

    private Map<String, String> domainMap = new HashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        for (String config : dnsDomains) {
            String[] splits = config.split("=");
            domainMap.put(splits[0], splits[1]);
        }
        List<InetSocketAddress> addressList = Arrays.stream(dnsServers).map(new Function<String, InetSocketAddress>() {
            @Override
            public InetSocketAddress apply(String dns) {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(dns, 53);
                return inetSocketAddress;
            }
        }).collect(Collectors.toList());
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        nameResolver = new DnsNameResolverBuilder(eventExecutors.next())
                .channelType(NioDatagramChannel.class)
                .resolvedAddressTypes(ResolvedAddressTypes.IPV4_ONLY)
                .resolveCache(new DefaultDnsCache(0, cacheSeconds, 0))
                .nameServerProvider(new SequentialDnsServerAddressStreamProvider(addressList.toArray(new InetSocketAddress[0])))
                .queryTimeoutMillis(queryTimeout)
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
            String domain = getDomain(dnsQuestion.name());
            String ip = domainMap.get(domain);
            List<InetAddress> inetAddressList = new ArrayList<>();
            if (null == ip) {
                List<InetAddress> list = nameResolver.resolveAll(domain).get();
                inetAddressList.addAll(list);
            } else {
                InetAddress inetAddress = InetAddress.getByName(ip);
                inetAddressList.add(inetAddress);
            }
            for (InetAddress inetAddress : inetAddressList) {
                byte[] address = SocketUtils.addressByName(inetAddress.getHostAddress()).getAddress();
                DefaultDnsRawRecord queryAnswer = new DefaultDnsRawRecord(dnsQuestion.name(),
                        DnsRecordType.A, ttlTimeout, Unpooled.wrappedBuffer(address));
                response.addRecord(DnsSection.ANSWER, queryAnswer);
            }
        }
        ctx.writeAndFlush(response);
    }

    private String getDomain(String value) {
        if (value.endsWith(".")) {
            String substring = value.substring(0, value.length() - 1);
            return substring;
        }
        return value;
    }
}

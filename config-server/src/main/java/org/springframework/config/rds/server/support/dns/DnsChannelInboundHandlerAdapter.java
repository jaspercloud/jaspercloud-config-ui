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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.config.rds.server.entity.DomainConfig;
import org.springframework.config.rds.server.service.DomainConfigService;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@ChannelHandler.Sharable
public class DnsChannelInboundHandlerAdapter extends ChannelInboundHandlerAdapter implements InitializingBean {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DomainConfigService domainConfigService;

    @Value("${dns.servers}")
    private String[] dnsServers;

    @Value("${dns.query.timeout.millis}")
    private long queryTimeout;

    @Value("${dns.cache.timeout.seconds}")
    private int cacheSeconds;

    private DnsNameResolver nameResolver;

    @Override
    public void afterPropertiesSet() throws Exception {
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
            DomainConfig config = domainConfigService.getDomainConfigCache(domain);
            if (null == config) {
                List<DnsRecord> recordList = nameResolver.resolveAll(dnsQuestion).get();
                for (DnsRecord record : recordList) {
                    response.addRecord(DnsSection.ANSWER, record);
                }
            } else {
                List<String> ips = config.getIps();
                if (null == ips) {
                    ips = new ArrayList<>();
                }
                for (String ip : ips) {
                    InetAddress inetAddress = InetAddress.getByName(ip);
                    byte[] bytes = inetAddress.getAddress();
                    DefaultDnsRawRecord queryAnswer = new DefaultDnsRawRecord(dnsQuestion.name(),
                            DnsRecordType.A, config.getTtl(), Unpooled.wrappedBuffer(bytes));
                    response.addRecord(DnsSection.ANSWER, queryAnswer);
                }
            }
        } else if (DnsRecordType.PTR.equals(dnsRecordType)) {
            logger.debug(dnsRecordType.toString());
        } else {
            logger.error(dnsRecordType.toString());
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

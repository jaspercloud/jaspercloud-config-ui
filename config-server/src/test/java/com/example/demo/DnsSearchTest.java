package com.example.demo;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

public class DnsSearchTest {

    public static void main(String[] args) throws Exception {
        System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
        System.setProperty("sun.net.spi.nameservice.nameservers", "127.0.0.1");
        {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("redmine.timogroup.com", 33000));
            System.out.println();
            socket.close();
        }
        {
            InetAddress[] all = InetAddress.getAllByName("www.baidu.com");
            System.out.println(Arrays.asList(all).toString());
        }
        {
            InetAddress[] all = InetAddress.getAllByName("postgres.vai.com");
            System.out.println(Arrays.asList(all).toString());
        }
    }
}

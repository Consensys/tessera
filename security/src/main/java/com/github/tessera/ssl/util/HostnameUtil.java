package com.github.tessera.ssl.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public interface HostnameUtil {

    default String getHostName() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }

    static HostnameUtil create(){
        return new HostnameUtil() {};
    }
}

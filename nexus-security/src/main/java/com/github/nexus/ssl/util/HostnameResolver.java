package com.github.nexus.ssl.util;

import java.net.InetAddress;

public interface HostnameResolver {

    default String getHostName(){
        try{
            return InetAddress.getLocalHost().getHostName();
        }
        catch (Exception ex){
            return "UNKNOWN";
        }
    }

    static HostnameResolver create(){
        return new HostnameResolver() {};
    }
}

package com.tale.kits;

import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName WebKit
 * @Desc TODO
 * @Author zhaoteng
 * @Date 2020/4/12 7:51
 * @Version 1.0
 **/
public class WebKit {

    public static final String UNKNOWN_MAGIC = "unknown";

    /**
     * Get the client IP address by request
     *
     * @param request Request instance
     * @return return ip address
     */
    public static String ipAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("x-forwarded-for");
        if (StringKit.isBlank(ipAddress) || UNKNOWN_MAGIC.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (StringKit.isBlank(ipAddress) || UNKNOWN_MAGIC.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringKit.isBlank(ipAddress) || UNKNOWN_MAGIC.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (StringKit.isBlank(ipAddress) || UNKNOWN_MAGIC.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringKit.isBlank(ipAddress) || UNKNOWN_MAGIC.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        return ipAddress;
    }

}


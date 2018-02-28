package com.airline.tools;

import java.security.MessageDigest;

public class MD5Util {

    public static String getMD5(String s){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(s.getBytes("utf-8"));
            return toHex(bytes);
        } catch (Exception e) {
            return null;
        }
    }

    private static String toHex(byte[] bytes){
        final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();
        StringBuffer ret = new StringBuffer(bytes.length*2);
        for(int i=0; i<bytes.length; i++){
            ret.append(HEX_DIGITS[(bytes[i] >> 4) & 0x0f]);
            ret.append(HEX_DIGITS[(bytes[i] & 0x0f)]);
        }
        return ret.toString();
    }
}

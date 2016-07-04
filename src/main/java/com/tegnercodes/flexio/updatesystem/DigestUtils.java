package com.tegnercodes.flexio.updatesystem;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class DigestUtils {

    public static String getSHA1(String text) {
        try {
            byte[] bytes = text.getBytes("iso-8859-1");
            return getSHA1(bytes);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getSHA1(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(bytes, 0, bytes.length);
            byte[] digest = md.digest();
            return toHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            if ((bytes[i] & 0xff) < 0x10) {
                sb.append('0');
            }
            sb.append(Long.toString(bytes[i] & 0xff, 16));
        }

        return sb.toString();
    }

}

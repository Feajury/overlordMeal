package com.test.zzc;

import java.awt.*;
import java.io.IOException;

/**
 * java调用浏览器打开url
 */
public class test01 {
    public static void main(String[] args) throws IOException {
        String url = "https://account.dianping.com/account/getqrcodeimg";
        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler "+url);
    }
}

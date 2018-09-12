package com.ntnikka.modules.pay.aliPay.utils;

/**
 * Created by liuq on 2018/9/11.
 */
public class SignUtil {
    /**
     * 校验MD5签名
     * @param sign
     * @param ParamStr
     * @return
     */
    public static Boolean checkSign(String sign , String ParamStr){

        String checkSign = MD5Utils.encode(ParamStr).toUpperCase();
        if (checkSign == sign || checkSign.equals(sign))
            return true;
        return false;
    }
}

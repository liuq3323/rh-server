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

    public static String doubleTrans(double d){
        if(Math.round(d)-d==0){
            return String.valueOf((long)d);
        }
        return String.valueOf(d);
    }

    public static void main(String[] args) {
        String sign = "12e6a9e761fcdddb8fbd4401ec44b572";
        String checkSign = "12e6a9e761fcdddb8fbd4401ec44b572";
    }
}

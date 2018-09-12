package com.ntnikka.modules.pay.aliPay.config;

/* *
 *类名：AlipayConfig
 *功能：基础配置类
 *详细：设置帐户有关信息及返回路径
 *版本：3.4
 *修改日期：2016-03-08
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
 */

public class AlipayConfig {
	
//↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

	// 合作身份者ID，签约账号，以2088开头由16位纯数字组成的字符串，查看地址：https://b.alipay.com/order/pidAndKey.htm
	public static String partner = "2088021273428911";
	
	// 收款支付宝账号，以2088开头由16位纯数字组成的字符串，一般情况下收款账号就是签约账号
	public static String seller_id = partner;

	public static String account_name = "";

	public static String account = "";

	//商户的私钥,需要PKCS8格式，RSA公私钥生成：https://doc.open.alipay.com/doc2/detail.htm?spm=a219a.7629140.0.0.nBDxfy&treeId=58&articleId=103242&docType=1
	public static String private_key = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBALDv+rzBHZ3Fg67D" +
			"RPbY5EBA7oMjCEnGpJMJkuaA4VKMSt88Rx4scP+7ORFLIkX5E6a0O9A0HIotpcyX" +
			"9jAmu2zx5CHPb6Wjsqge6yGqFXij8onRw4LMYdLLQtFACfGXZZGdfvcdH0WbLOTw" +
			"Bcmeu4L7+jT4Afdilq3ZG2aLXPrVAgMBAAECgYBlNS/YLiJxr5wOKBXXmOKeukVz" +
			"72L7SEu064ICpW+9VxAwtvr7EHKmZQJUmQSGv910up2ID3bPtyKib73bdxw3VnRU" +
			"NWJ6MbHcpHbRNriuwTOPCxMS9AbBjKyPkzKXVE31bBXpdVx+n7dCVDcLrnSfabQC" +
			"IMxOmw3pj77PPdEolQJBANjVXiA1zVz+OkhgoIgOIy8315E+piu2zfZqWR9SBuTN" +
			"+Z2KpTjaUTrxCp5QGJsT7ijDurJtAonXRL43ijEYl+sCQQDQ5cWh+RMGczWgRH7Q" +
			"b6FhyIz9Dd2fvATskaMFylhBrA/uVwskGcST/Co487FmbXzW2YydrGXS4MXkufp2" +
			"4Mg/AkAuEK5Ng4Cch/oT1EtmfDJnXqKyXa/py41YE2HZsJB8XXHxUTomqOLm9bx+" +
			"w59mmsZW7LYmH9iRNiWJj70RDxt/AkAw+o66CXJCguTB7Q1mxaWrDaCw/H1IJIdr" +
			"CEKW6viCfVtG4LXGAxyqLeegbtLbVzR8E4n6th8xsG310P0+vjl1AkAocKX3zqfl" +
			"KP8OJtHIc/2+Y//KRhxukaKPgC2UCQbqefxuzNUQ0O08UEqUSf5/6aDjTOODJ3Ow" +
			"91+rrG4dtrl/";
	
	// 支付宝的公钥,查看地址：https://b.alipay.com/order/pidAndKey.htm
	public static String alipay_public_key  = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";
	
	public static String zebra_public_key_md5 = "e8s54m06awo6f0kisv75v02df1t6s5yj";
	// 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
	public static String notify_url = "http://106.75.132.41:89/notify/alipay";

	public static String notify_url4enterprise = "http://106.75.130.135:9001/notify/aliTransactionNofity";

	//批量转账异步通知
	public static String transfer_notify_url = "http://api2.bmkp.cn/notify/alipayBatchTrans";

	// 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
	public static String return_url = "http://106.75.130.135/return.html";

	// 签名方式
	public static String sign_type = "RSA";
	public static String sign_type_md5 = "MD5";
	
	// 调试用，创建TXT日志文件夹路径，见AlipayCore.java类中的logResult(String sWord)打印方法。
	public static String log_path = "/Users/pandeng/Documents/";
		
	// 字符编码格式 目前支持utf-8
	public static String input_charset = "utf-8";
	public static String input_charset_utf8 = "utf-8";

	public static String output_charset_gbk = "GBK";
		
	// 支付类型 ，无需修改
	public static String payment_type = "1";
		
	// 调用的接口名，无需修改
	public static String service = "alipay.wap.create.direct.pay.by.user";

	public static String single_trade_query ="single_trade_query";

	public static String create_direct_play ="create_direct_pay_by_user";

	public static String batch_trans ="batch_trans_notify";

	public static String batch_trans_check ="btn_status_query";

	public static String alipay_service_gateway = "https://mapi.alipay.com/gateway.do";

//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

}


package net.rawburn.cxf.jaxws;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.junit.Test;

public class WebServiceTest {

    @Test
    public void testSend1(){
        // System.setProperty(StaxUtils.ALLOW_INSECURE_PARSER,"true");
        JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
        Client client = dcf.createClient("http://localhost:8080/services/TestService?wsdl");

        HTTPConduit conduit = (HTTPConduit) client.getConduit();
        HTTPClientPolicy policy = new HTTPClientPolicy();
        long timeout = 60*1000;//60秒
        policy.setConnectionTimeout(timeout);
        policy.setReceiveTimeout(timeout);
        conduit.setClient(policy);

        // 需要密码的情况需要加上用户名和密码
        // client.getOutInterceptors().add(new ClientLoginInterceptor(USER_NAME,PASS_WORD));
        try {

            Object[] objects = client.invoke("sendMessage", "wyj");
            System.out.println("返回数据:" + objects[0]);
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 代理类工厂的方式,需要拿到对方的接口
     */
    public static void cl1() {
        try {
            // 接口地址
            String address = "http://localhost:8080/services/CommonService?wsdl";
            // 代理工厂
            JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean();
            // 设置代理地址
            jaxWsProxyFactoryBean.setAddress(address);
            // 设置接口类型
            jaxWsProxyFactoryBean.setServiceClass(TestService.class);
            // 创建一个代理接口实现
            TestService cs = (TestService) jaxWsProxyFactoryBean.create();
            // 数据准备
            String userName = "Leftso";
            // 调用代理接口的方法调用并返回结果
            String result = cs.sendMessage(userName);
            System.out.println("返回结果:" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 动态调用方式
     */
    public static void cl2() {
        // 创建动态客户端
        JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
        Client client = dcf.createClient("http://localhost:8080/services/CommonService?wsdl");
        // 需要密码的情况需要加上用户名和密码
        // client.getOutInterceptors().add(new ClientLoginInterceptor(USER_NAME,
        // PASS_WORD));
        Object[] objects = new Object[0];
        try {
            // invoke("方法名",参数1,参数2,参数3....);
            objects = client.invoke("sayHello", "Leftso");
            System.out.println("返回数据:" + objects[0]);
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }
}
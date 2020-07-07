package net.rawburn.cxf.jaxws;

import org.springframework.stereotype.Component;

import javax.jws.WebService;

@WebService(serviceName = "TestService", // 与接口中指定的name一致
		targetNamespace = "http://service.wyj.com", // 与接口中的命名空间一致,一般是接口的包名倒
		endpointInterface = "net.rawburn.cxf.jaxws.TestService"// 接口地址
)
@Component
public class TestServiceImpl implements TestService {

	@Override
	public String sendMessage(String username) {

		return "hello " + username;
	}

}
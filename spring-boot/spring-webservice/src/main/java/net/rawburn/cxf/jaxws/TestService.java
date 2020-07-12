package net.rawburn.cxf.jaxws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

@WebService(name = "TestService", // 暴露服务名称
		targetNamespace = "http://service.wyj.com"// 命名空间,一般是接口的包名倒序
)
public interface TestService {

	@WebMethod
	@WebResult(name = "String", targetNamespace = "")
	String sendMessage(@WebParam(name = "username") String username);
}
package net.rawburn.cxf.jaxws.support.xml;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * @author renchao
 * @since v1.0
 */
public class RestTemplateWSTest {

	@Autowired
	private RestTemplate restTemplate;

	@Test
	public void test(String[] args) throws Exception {
		//构造webservice请求参数
		StringBuffer soapRequestData = new StringBuffer("");
		soapRequestData.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:park=\"http://park.webservice.ehl.com\">");
		soapRequestData.append("<soapenv:Header/>");
		soapRequestData.append("<soapenv:Body>");
		soapRequestData.append("<park:Parkinginfo>");
		soapRequestData.append("<park:xtlb>?</park:xtlb>");// 固定值参数
		soapRequestData.append("<park:jkxlh>?</park:jkxlh>");// 固定参数
		soapRequestData.append("<park:XmlString>");
		soapRequestData.append(
				"<![CDATA[<?xml version='1.0' encoding='UTF-8'?><xml><head><bh>?</bh><mc>?</mc><qqsj>?</qqsj><version>1.0</version></head></xml>]]>");// 固定参数
		soapRequestData.append("</park:XmlString>");
		soapRequestData.append("</park:Parkinginfo>");
		soapRequestData.append("</soapenv:Body>");
		soapRequestData.append("</soapenv:Envelope>");

		// 构造http请求头
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("text/xml;charset=UTF-8");
		headers.setContentType(type);
		HttpEntity<String> formEntity = new HttpEntity<String>(soapRequestData.toString(), headers);

		URI url = new URI("");

		// 返回结果
		String resultStr = restTemplate.postForObject(url, formEntity, String.class);
		// 转换返回结果中的特殊字符，返回的结果中会将 xml 转义
		String tmpStr = StringEscapeUtils.unescapeXml(resultStr);
		// 获取真正的结果
		String resultXML = StringUtils.substringBetween(tmpStr, "<ns1:out>", "</ns1:out>");
	}
}

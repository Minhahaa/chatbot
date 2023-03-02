package com.gp.chatbot.controller.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.gp.chatbot.model.repositories.goods.besti.ModenStatRepository;
import com.gp.chatbot.model.repositories.goods.erp.OrderDetailRepository;
import com.gp.chatbot.model.repositories.goods.erp.OrderMasterRepository;
import com.gp.chatbot.model.vo.order.besti.ModenStatEntity;
import com.gp.chatbot.model.vo.order.erp.OrderMasterEntity;

@RestController
@RequestMapping("/api")
public class KakaoChatBotContorller {

	@Autowired
	private ModenStatRepository modenStat;
	
	@Autowired
	private OrderMasterRepository orderMaster;
	
	@Autowired
	private OrderDetailRepository orderDetail;
	
	@RequestMapping(value="/sayHello", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String generateReport( HttpServletRequest request , HttpServletResponse response ) throws Exception {
		
		System.out.println("요청옴");
		
		return makingJson("Hello","","");
	
	}
	
	@RequestMapping(value="/orderCheck", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String orderCheck( HttpServletRequest request , HttpServletResponse response ) throws Exception {
		
		System.out.println("order check 요청옴");

		String readbody = readBody(request);
		System.out.println(readbody);
		
		JSONObject jsonObject = new JSONObject(readbody);
		String ordNo = jsonObject.get("utterance").toString();
		
		
		IntStream stream = ((CharSequence) ordNo).chars();
		String intStr = stream.filter((ch)-> (48 <= ch && ch <= 57))
		        .mapToObj(ch -> (char)ch)
		        .map(Object::toString)
		        .collect(Collectors.joining());
		
		System.out.println("숫자만 했을때 " + intStr);
		
		if(intStr.length() == 14) {
			return makingCheck(intStr, "SUCCESS");
		}else {
			return makingCheck(intStr, "FAIL");
		}

	}
	
	@RequestMapping(value="/orderSearch", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String orderSearch( HttpServletRequest request , HttpServletResponse response ) throws Exception {
		
		System.out.println("order search 요청옴");

		String readbody = readBody(request);
		System.out.println(readbody);
		
		/*넘어온 파라미터 확인*/
		JSONObject jsonObject = new JSONObject(readbody);
		JSONObject action = jsonObject.getJSONObject("action");
		JSONObject params = action.getJSONObject("params");
		
		String ordNo = params.get("sys_order_no").toString();
		String ordService = params.get("sys_order_service").toString();
		
		IntStream stream = ((CharSequence) ordNo).chars();
		String intStrOrdNo = stream.filter((ch)-> (48 <= ch && ch <= 57))
		        .mapToObj(ch -> (char)ch)
		        .map(Object::toString)
		        .collect(Collectors.joining());
		
		System.out.println("숫자만 했을때 " + intStrOrdNo);
		/*넘어온 파라미터 확인 끝*/
		if(intStrOrdNo.length() == 14) {
			
			List<OrderMasterEntity> orderCheck = orderMaster.findByOrdNo(ordNo);
			if(orderCheck != null && !orderCheck.isEmpty()) {
				System.out.println(orderCheck);
				List<ModenStatEntity> modenList = modenStat.findByOrdNo(ordNo);
				if(modenList != null && !modenList.isEmpty()) {
					System.out.println(modenStat);
				}else {
					return makingJson(ordNo , ordService , "FAIL");
				}
			}else {
				return makingJson(ordNo , ordService , "FAIL");
			}
			return makingJson(ordNo , ordService , "SUCCESS");
		}else {
			return makingJson(ordNo , ordService , "FAIL");
		}
		
	}
	
	public String makingCheck(String ord_no, String status) {
		
		JSONObject jo = new JSONObject();
		JSONObject retValue = new JSONObject();
		retValue.put("sys_order_no", ord_no);
		
		jo.put("status", status);
		jo.put("value", ord_no);
		jo.put("data", retValue);
		
		jo.put("version", "2.0");
		
		return jo.toString();
	}
	
	public String makingJson(String ord_no, String ord_service, String msg) {
		
		JSONObject retValue = new JSONObject();
		retValue.put("msg", "배송중");
		retValue.put("place","경기도");
		
		JSONObject jo = new JSONObject();
		jo.put("data", retValue);
		jo.put("version", "2.0");
		
		return jo.toString();
	}
	
	/*
	 * HttpServletRequest로 받은 jsonString을 변환가능한 String으로 변환하여 return 
	 * */
	public String readBody(HttpServletRequest request) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(request.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String buffer;
        
        //json string
        while ((buffer = input.readLine()) != null) {
            if (builder.length() > 0) {
                builder.append("\n");
            }
            if(buffer.substring(0,1).equals("\"") || buffer.substring(0,1).equals("'")) {
            	 buffer = buffer.substring(1,buffer.length());
            }
            
            if(buffer.substring(buffer.length()-1, buffer.length()).equals("\"") || buffer.substring(buffer.length()-1, buffer.length()).equals("'")) {
            	buffer = buffer.substring(0,buffer.length()-1);
            }
            
            builder.append(buffer);
        }
        return builder.toString();
	}
}

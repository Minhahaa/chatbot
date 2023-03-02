package com.gp.chatbot.controller.api;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.gp.chatbot.model.repositories.besti.order.ModenStatRepository;
import com.gp.chatbot.model.repositories.erp.order.OrderDetailRepository;
import com.gp.chatbot.model.repositories.erp.order.OrderMasterRepository;
import com.gp.chatbot.model.repositories.sm.common.TcodeDtlRepository;
import com.gp.chatbot.model.repositories.wis.order.TwDirectDeliRepository;
import com.gp.chatbot.model.vo.besti.order.ModenStatEntity;
import com.gp.chatbot.model.vo.erp.order.OrderDetailEntity;
import com.gp.chatbot.model.vo.erp.order.OrderMasterEntity;
import com.gp.chatbot.model.vo.sm.common.TcodeDtlEntity;
import com.gp.chatbot.model.vo.wis.order.TwDirectDeliEntity;

@RestController
@RequestMapping("/api")
public class KakaoChatBotContorller {

	@Autowired
	private ModenStatRepository modenStat; //BEST-I Moden office order status table
	
	@Autowired
	private OrderMasterRepository orderMaster; //ERP SAM0210 ORDER MASTER
	
	@Autowired
	private OrderDetailRepository orderDetail; //ERP SAM0211 ORDER DETAIL
	
	@Autowired
	private TwDirectDeliRepository directDeli; //WIS TW_DIRECT_DELI ORDER STATUS
	
	@Autowired
	private TcodeDtlRepository tcodeDetail; //SM TCODEDTL ALL CODE TABLE
	
	static final String MODEN_CUST = "94226"; 
	
	@RequestMapping(value="/sayHello", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String generateReport( HttpServletRequest request , HttpServletResponse response ) throws Exception {
		
		System.out.println("요청옴");
		
		return makingJson("Hello","","");
	
	}
	
	/*
	 * 카카오 챗봇 주문번호 검증 2023.02.28 황민하 대리
	 * ==========================================
	 * */
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
		
		//주문번호가 14자리 이면서 주문 마스터 테이블에 존재하는지 확인
		if(intStr.length() == 14) {
			List<OrderMasterEntity> orderCheck = orderMaster.findByOrdNo(ordNo);
			//주문정보가 없는지 확인
			if(orderCheck != null && !orderCheck.isEmpty()) {
				return makingCheck(intStr, "SUCCESS");
			}else {
				return makingCheck(intStr, "FAIL");
			}
		}else {
			return makingCheck(intStr, "FAIL");
		}

	}
	
	
	/* 2023.03.02 시스템에서 사용하는 택배사 코드 / 황민하 대리
	 * ==========================================
	 * CD_DTL_NO CD_DTL_NM
			000	 직배송
			001	 건영택배
			002	 경동택배
			003	 네덱스
			004	 대신택배
			005	 대한택배
			006	 우체국등기
			007	 로젠택배
			008	 삼성HTH
			009	 아주택배
			010	 옐로우캡
			011	 우체국택배
			012	 천일택배
			013	 퀵배송
			014	 트라넷택배
			015	 한서택배
			016	 한진택배
			017	 롯데택배
			018	 호남택배
			019	 동부익스프레스
			020	 CJ대한통운
			021	 케이지비택배
			022	 이젠택배
			023	 케이티로지스
			024	 기타
			025	 고려택배
			026	 직접배송
			027	 수령확인
			028	 신세계드림익스프레스
			029	 하나로택배
			030	 성화기업택배
			031	 KG로지스
			032	 합동택배
			033	 CSVnet 편의점택배
			034	 CU 편의점택배
			035	 일양로지스
			036	 GS PostBox택배
			037	 모든오피스직배
	 * */
	/*
	 * 2023.03.02 황민하 대리
	 * ==========================================
	 * 주문 확인에 대한 로직 ModenOffice cust : 94226 / 변수명 MODEN_CUST
	 * 1. ERP SAM0210에 존재하는 경우 PCH_TYPE을 확인 > ( 100 : IFFICE 재고 / 200 : 협력사 별도배송 / 400 : BEST-I TR 창원 or 평택 물류센터 배송 )
	 * 2. PCH_TYPE이 200이면서 CUST가 94226인 경우 Moden office 배송
	 * 2-1. Moden office 배송건은 Best-i - Moden Stat을 통해서 확인 ( STATUS 별로 상태값이 다르므로 참고 - webapp > files > 08-24 주문진행상태값_전송.xlsx 확인 )
	 * 2-2. status 19는 배송 출발 ( 송장번호가 입력되는 단계임 / Moden office 직접 배송은 별도 송장번호 없음 )
	 * 3. 그 외의 경우 WIS - TW_DIRECT_DELI를 통해서 택배사 / 송장번호 확인
	 * 4. 3번에서 확인된 택배사 코드를 사용해서 SM - TCODEDTL table에서 택배사 API를 통해 정보를 가져올 수 있는지 확인
	 * 5. 상태값 확인 후 return
	 * */
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
		
		//String은 제거하고 int만 남김
		IntStream stream = ((CharSequence) ordNo).chars();
		String intStrOrdNo = stream.filter((ch)-> (48 <= ch && ch <= 57))
		        .mapToObj(ch -> (char)ch)
		        .map(Object::toString)
		        .collect(Collectors.joining());
		
		System.out.println("숫자만 했을때 " + intStrOrdNo);
		/*넘어온 파라미터 확인 끝*/
		
		//주문번호 길이 확인
		if(intStrOrdNo.length() == 14) {
			//erp sam0210 table에 주문정보가 있는지 확인
			List<OrderDetailEntity> orderCheck = orderDetail.findByOrdNo(ordNo);
			//주문정보가 없는지 확인
			if(orderCheck != null && !orderCheck.isEmpty()) {
				System.out.println(orderCheck);
				HashMap<String, String> isss = pchTypeCheck(orderCheck,ordNo);
				//test용 wis 확인
				
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
	
	public HashMap<String,String> pchTypeCheck(List<OrderDetailEntity> orderCheck, String ordNo) {
		
		HashMap<String,String> nowStat = new HashMap<>();
		String pchType = orderCheck.get(0).getPchType();
		
		if(pchType.equals("100")) {
			
		}else if(pchType.equals("200")) {
			//best-i의 ModenStat Table에 모든 주문건인지 확인 ( 확인 로직 추가 )
			List<ModenStatEntity> modenList = modenStat.findByUnqNoLikeOrderByStatusDesc(ordNo+"%");
			if(modenList != null && !modenList.isEmpty() ) {
				//모든 주문건인 경우
				System.out.println("moden 입니다");
				/*
				 *  01	메모전달 MSG_MANAGER:내부메모, MSG_CUSTOMER: 고객에게 전달할 메모
					11	신규 주문확인(수주생성)
					12	검수지시서 출력( 배차 지시 -> 클레임 가능 ) > *사용
					13	검수시작( 이값이 존재하면 취소가 안됀다 )
					14	검수종료( 배차확정 ) > *사용
					15	검수보류
					19	배송출발 택배 (운송장 번호 어려개 생성 Repeater) > *사용
					19	배송출발 직배(정보 1개 생성 배송담당 이름과 전화번호) > *사용
					21	반품주문서 생성( 보류 ) > *사용
					22	반품입고 완료( 보류 ) > *사용
					31	교환주문접수
					32	교환주문신청건 반품입고 완료
					33	교환주문신청건 출고완료 > *사용
					91	주문취소 접수 > *사용
					92	주문취소 완료 > *사용
				 * */
				System.out.println(modenList);
				for(int i = 0 ; i < modenList.size() ; i++) {
					
					ModenStatEntity modens = modenList.get(i);
					
					String status = modens.getStatus();
					if(status != null && !status.isEmpty() && !status.isBlank()) {
						String isBackStatus = "";
						int stat = Integer.parseInt(status);
						//System.out.println("stat = " + stat);
						
						switch(stat) {
							case 1  : isBackStatus = modens.getMsg_manager() == null ? "담당자 확인중" : modens.getMsg_manager(); break;
							case 11 : isBackStatus = "주문 확인중"; break;
							case 12 : isBackStatus = "검수중"; break;
							case 13 : isBackStatus = "검수시작"; break;
							case 14 : isBackStatus = "배차확정"; break;
							case 15 : isBackStatus = "검수중"; break;
							case 19 : isBackStatus = "배송출발"; break;
							case 21 : isBackStatus = "반품접수"; break;
							case 22 : isBackStatus = "반품 입고완료"; break;
							case 31 : isBackStatus = "교환주문 접수"; break;
							case 32 : isBackStatus = "교환 입고완료"; break;
							case 33 : isBackStatus = "교환상품 출고완료"; break;
							case 91 : isBackStatus = "주문취소 접수"; break;
							case 92 : isBackStatus = "주문취소 완료"; break;
						}
						nowStat.put(modens.getGoodsCd(), isBackStatus);
						
					}else {
						continue;
					}
				}
				System.out.println(nowStat);
			}else {
				//모든 주문건이 아닌 경우 WIS - TW_DIRECTDELI 에서 조회함
				System.out.println("moden 아닙니다~");
				String soDt = ordNo.substring(0, 8);
				String soSr = ordNo.substring(8, 14);
				List<TwDirectDeliEntity> tddList = directDeli.findBySoDateAndSoSer(soDt, soSr);
				List<TcodeDtlEntity> tcode = tcodeDetail.findByCdNo("S45");
				System.out.println(tcode);
				
				System.out.println(soDt + ", " + soSr + " , " + tddList);
				//Deli House가 존재하면서 DeliNo를 확인
				for(int i = 0 ; i < tddList.size() ; i++) {
					TwDirectDeliEntity tdd = tddList.get(i);
					if(tdd.getDeliHouse() != null && !tdd.getDeliHouse().isEmpty()) {
						
						String deliNo = tdd.getDeliNo() == null ? "" : tdd.getDeliNo();
						if(!deliNo.equals("")) {
							IntStream stream = ((CharSequence) deliNo).chars();
							String intStrDeliNo = stream.filter((ch)-> (48 <= ch && ch <= 57))
							        .mapToObj(ch -> (char)ch)
							        .map(Object::toString)
							        .collect(Collectors.joining());
							//송장 번호가 아닌 다른 사유가 있는 경우 패스함
							if(intStrDeliNo.equals("")) {
								continue;
							}else {
								for(int j = 0 ; j < tcode.size() ; j++) {
									TcodeDtlEntity code = tcode.get(j);
									if(tdd.getDeliHouse().equals(code.getCdDtlNo())) {
										String codeUrl = code.getCodeUrl() == null ? "" : code.getCodeUrl();
										if(!codeUrl.equals("")) {
											boolean isUrl = false;
											isUrl = isUrl(codeUrl);
											// url인거 확인
											if(isUrl) {
												String callAPI = callLogAPI(codeUrl, deliNo);
												System.out.println(callAPI);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}else if(pchType.equals("400")) {
			
		}
		
		return nowStat;
	}
	
	/*
	 * 2023.02.28 황민하 대리
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
	
	/*
	 * URL 여부 확인
	 * */
	public static boolean isUrl(String text) {
	    Pattern p = Pattern.compile("^(?:http(s)?:\\/\\/)?(?:www\\.)?[a-zA-Z0-9./]+$");
	    Matcher m = p.matcher(text);
	    if  (m.matches()) return true;
	    URL u = null;
	    try {
	        u = new URL(text);
	    } catch (MalformedURLException e) {
	        return false;
	    }
	    try {
	        u.toURI();
	    } catch (URISyntaxException e) {
	        return false;
	    }
	    return true;
	}
	
	/*
	 * 택배사 API 조회
	 * url / 송장번호
	 * */
	public String callLogAPI(String url , String deliCode) {
	    
		String query;
		String responseBody = "";
	    try {
	        query = URLEncoder.encode(deliCode, "UTF-8");
	    } catch (UnsupportedEncodingException e) {
	        throw new RuntimeException("인코딩 실패", e);
	    }
	    String apiURL = url;
	    responseBody = GETMethod(apiURL, query);
	    System.out.println(responseBody);
	    JSONObject statusJson = new JSONObject(responseBody);
	    System.out.println(statusJson.toString());
	    
	    statusJson = new JSONObject(statusJson.get("message").toString());
	    statusJson = new JSONObject(statusJson.get("result").toString());
	    String translatedText = (String)statusJson.get("translatedText");
	    
	    return translatedText;
	}
	
	private static String GETMethod(String apiUrl, String text){
		
        HttpURLConnection con = connect(apiUrl+text);
        String postParams = "";
        
        try {
            con.setRequestMethod("GET");

            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.write(postParams.getBytes());
                wr.flush();
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 응답
                return readBody(con.getInputStream());
            } else {  // 에러 응답
                return readBody(con.getErrorStream());
            }
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
    }
	
	private static HttpURLConnection connect(String apiUrl){
        try {
            URL url = new URL(apiUrl);
            return (HttpURLConnection)url.openConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }

    private static String readBody(InputStream body){
        InputStreamReader streamReader = new InputStreamReader(body);

        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();

            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }

            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
        }
    }
}

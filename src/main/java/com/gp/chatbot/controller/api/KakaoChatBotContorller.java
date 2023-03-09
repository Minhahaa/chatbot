package com.gp.chatbot.controller.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gp.chatbot.model.bo.project.TrackerDeliBO;
import com.gp.chatbot.model.repositories.besti.order.ModenStatRepository;
import com.gp.chatbot.model.repositories.besti.order.ModenStatRepositorySupport;
import com.gp.chatbot.model.repositories.erp.order.OrderDetailRepository;
import com.gp.chatbot.model.repositories.erp.order.OrderMasterRepository;
import com.gp.chatbot.model.repositories.sm.common.TcodeDtlRepository;
import com.gp.chatbot.model.repositories.sm.order.TdeliStatRepository;
import com.gp.chatbot.model.repositories.wis.member.WisDriverDeliveryRepository;
import com.gp.chatbot.model.repositories.wis.member.WisDriverRepository;
import com.gp.chatbot.model.repositories.wis.order.TwDirectDeliRepository;
import com.gp.chatbot.model.vo.besti.order.ModenStatEntity;
import com.gp.chatbot.model.vo.erp.order.OrderDetailEntity;
import com.gp.chatbot.model.vo.erp.order.OrderMasterEntity;
import com.gp.chatbot.model.vo.sm.common.TcodeDtlEntity;
import com.gp.chatbot.model.vo.sm.order.TdeliStatEntity;
import com.gp.chatbot.model.vo.wis.member.WisDriverDeliveryEntity;
import com.gp.chatbot.model.vo.wis.member.WisDriverEntity;
import com.gp.chatbot.model.vo.wis.order.TwDirectDeliEntity;

@RestController
@RequestMapping("/api")
public class KakaoChatBotContorller {

	/*
	 * KakaoChatBot & OrderSearching API 2023.02.28 / 황민하 대리
	 * 연결 DB : SM / ERP / WIS / BEST-I
	 * --------------------------------------------------------------
	 * 주문번호 기반 조회
	 * --------------------------------------------------------------
	 * 1. 챗봇을 기반으로 정보를 조회하며 API의 역할도 수행
	 * 2. PCH_TYPE 100의 상품은 물류센터에서 배송을 출발하기 때문에 현재 진행상태만 전달
	 * 3. PCH_TYPE 200의 상품은 협력사 배송이므로 송장번호에 맞게 정보를 추적해서 전달
	 * 4. PCH_TYPE 200이면서 Moden Office 배송건은 별도의 로직을 구성
	 * ===============================================================
	 * 	 - Moden Office에서 전달하는 API 결과는 상품 <-> 송장번호의 일치가 불가능함 *Moden Office에서 불가판정 
	 * 		> 상품을 반드시 한 상자에 한번에 담는게 아니기 때문이라고 함
	 * 	 - Moden Office에서 별도의 송장번호가 입력되는 경우는 송장번호 조회만 가능함
	 *   - Moden Office에서 직접 배송을 하는 경우의 상태값은 WIS(TW_DIRECTDELI) or BEST-I(MODEN_STAT)를 통해서 조회
	 *   - 위의 경우는 Batch를 통해서 update되므로 반영이 늦음 
	 *   	> SM(TDELISTAT)을 활용하면 Moden Office 주문/배송을 처리하는 Batch와 연동되어 있으므로 해당 정보가 가장 정확
	 *   - 따라서 WIS와 BEST-I를 통해서 1차적으로 조회하지만 필요에 따라서 해당 로직을 걷어내고 SM으로만 수정이 필요할 수 있음 ( 현재는 모든 정보를 조회해서 비교함 )
	 *   - SM(TDELISTAT)과 ERP(SAM0210)로 진행하는 경우에는 Status Code를 활용하여 SM(TCODEDTL) Table의 상태 코드와 매칭하여 제공하면 됨
	 *   - 이때 하나라도 배송이 완료되지 않은 경우에는 전체 정보처리가 멈추므로 Case By Case로 지속 모니터링 필요
	 *   - 최종적으로 사용자에게 보여지는 값은
	 *   	> 모든 상품 배송완료 : 배송완료
	 *   	> 하나라도 배송중 : 배송중
	 *   	> 하나라도 준비중 : 준비중
	 *   - API 사용자에게 보여지는 값은
	 *   	> 모든 상품 / 송장에 대한 현재 상태를 전체 다 return 함
	 * ===============================================================
	 * ---------------------------------------------------------------
	 * 송장번호 기반 조회
	 * --------------------------------------------------------------
	 * 1. WIS(TW_DIRECTDELI)에서 1차적으로 송장번호 확인
	 * 2. 1번이 없는 경우에는 BEST-I(MODEN_STAT)에서 Moden Office 송장번호 조회
	 * 3. 기타 송장번호는 조회가 불가함
	 * 조회 가능 택배사
	 * ===============================================================
	 *  DHL
		Sagawa
		KuronekoYamato
		JapanPost
		천일택배
		CJ대한통운
		CU편의점
		GS POSTBOX 택배
		CWAY (woori express)
		대신택배
		우체국택배
		한의사랑 택배
		한진택배
		합동택배
		홈픽
		한서호남택배
		일양로지스
		경동택배
		건영택배
		로젠택배
		롯데택배
		SLX
		성원글로벌카고
		TNT
		EMS
		Fedex
		UPS
		USPS
		===============================================================
	 * */
	
	@Autowired
	private ModenStatRepository modenStat; //BEST-I Moden office order status table
	
	@Autowired
	private ModenStatRepositorySupport modenSupport;
	
	@Autowired
	private OrderMasterRepository orderMaster; //ERP SAM0210 ORDER MASTER
	
	@Autowired
	private OrderDetailRepository orderDetail; //ERP SAM0211 ORDER DETAIL
	
	@Autowired
	private TwDirectDeliRepository directDeli; //WIS TW_DIRECT_DELI ORDER STATUS
	
	@Autowired
	private TcodeDtlRepository tcodeDetail; //SM TCODEDTL ALL CODE TABLE
	
	@Autowired
	private TdeliStatRepository tdeliStat; //SM TDELISTAT
	
	@Autowired
	private WisDriverRepository wisDriver; //SM TCODEDTL ALL CODE TABLE
	
	@Autowired
	private WisDriverDeliveryRepository wisDelivery; //SM TCODEDTL ALL CODE TABLE
	
	@PersistenceContext
    private EntityManager em;
	
	private TrackerDeliBO tracker = new TrackerDeliBO();
	
	
	static final String MODEN_CUST = "94226"; 
	static final String MODEN_CODE = "037";
	
	static final String TRACKER_URL = "https://apis.tracker.delivery/carriers/";
	private static Pattern PATTERN_BRACKET = Pattern.compile("\\([^\\(\\)]+\\)");
	
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
	
	/*
	 * 카카오 챗봇 송장번호 검증 2023.03.07 황민하 대리
	 * ==========================================
	 * */
	@RequestMapping(value="/deliNoCheck", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String deliNoCheck( HttpServletRequest request , HttpServletResponse response ) throws Exception {
		
		System.out.println("deliNoCheck 요청옴");

		String readbody = readBody(request);
		System.out.println(readbody);
		
		JSONObject jsonObject = new JSONObject(readbody);
		
		JSONObject action = jsonObject.getJSONObject("action");
		JSONObject params = action.getJSONObject("params");
		
		String deliNo = params.get("sys_deli_no").toString();
		
		IntStream stream = ((CharSequence) deliNo).chars();
		String intStr = stream.filter((ch)-> (48 <= ch && ch <= 57))
		        .mapToObj(ch -> (char)ch)
		        .map(Object::toString)
		        .collect(Collectors.joining());
		
		//주문번호가 10 ~ 13자리 이면서 주문 마스터 테이블에 존재하는지 확인
		if(intStr.length() > 9 && intStr.length() < 14) {
			List<TwDirectDeliEntity> deliCheck = directDeli.findByDeliNo(deliNo);
			//주문정보가 없는지 확인
			if(deliCheck != null && !deliCheck.isEmpty()) {
				return makingCheck(intStr, "SUCCESS");
			}else {
				List<ModenStatEntity> moden = modenSupport.getDeliNo(deliNo);
				
				if(moden != null && !moden.isEmpty()) {
					return makingCheck(intStr, "SUCCESS");
				}else{
					return makingCheck(intStr, "FAIL");
				}
			}
		}else {
			return makingCheck(intStr, "FAIL");
		}
	}
	
	@RequestMapping(value="/deliNoSearch", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String deliNoSearch( HttpServletRequest request , HttpServletResponse response ) throws Exception {
		
		System.out.println("deliNoSearch 요청옴");

		String readbody = readBody(request);
		System.out.println(readbody);
		
		JSONObject jsonObject = new JSONObject(readbody);
		
		JSONObject action = jsonObject.getJSONObject("action");
		JSONObject params = action.getJSONObject("params");
		
		String deliNo = params.get("sys_deli_no").toString();
		
		IntStream stream = ((CharSequence) deliNo).chars();
		String intStr = stream.filter((ch)-> (48 <= ch && ch <= 57))
		        .mapToObj(ch -> (char)ch)
		        .map(Object::toString)
		        .collect(Collectors.joining());
		
		//주문번호가 10 ~ 13자리 이면서 주문 마스터 테이블에 존재하는지 확인
		if(intStr.length() > 9 && intStr.length() < 14) {
			
			TrackerDeliBO tracker = new TrackerDeliBO();
			HashMap<String,String> trackerCodes = tracker.getTrackerCode();
			
			List<TwDirectDeliEntity> deliCheck = directDeli.findByDeliNo(deliNo);
			//주문정보가 없는지 확인
			if(deliCheck != null && !deliCheck.isEmpty()) {
				
				//System.out.println("is deli = " + deliCheck);
				String crrCode = trackerCodes.get(deliCheck.get(0).getDeliHouse());
				JSONObject apiValue = callLogAPI(crrCode,deliNo);
				System.out.println("is deli = " + apiValue);
				
				return makingOrderStatJson(apiValue, "SUCCESS");
				
			}else {
				List<ModenStatEntity> modens = modenSupport.getDeliNo(deliNo);
				HashMap<String ,String> modenMap = new HashMap<>();
				HashMap<String ,String> modenDeliveryMan = new HashMap<>();
				HashMap<String, String> modenDeliveryMap = new HashMap<>();
				
				String trackCode = "";
				
				if(modens != null && !modens.isEmpty()) {
					
					for(int i = 0 ; i < modens.size() ; i++) {
						JSONArray parcelJson = new JSONArray();
						ModenStatEntity moden = modens.get(i);
						
						parcelJson.put(moden.getParcelData());
						//System.out.println(parcelJson);
						if(parcelJson.isEmpty() || parcelJson.isNull(0)) {
							//otherDelivery.add(modenList.get(i).getGoodsCd());
						}else {
							modenMap = parseListMap(parcelJson);
							modenDeliveryMan.put("deliveryWorker", modenMap.get("deliveryWorker"));
							for(String key : modenMap.keySet()) {
								modenDeliveryMap.put(key, modenMap.get(key));
							}
							System.out.println("***********" + modenMap);
						}
					}
					System.out.println("모맵 = " + modenMap);
					
					for(String key : modenDeliveryMap.keySet()) {
						if(deliNo.equals(key)) {
							trackCode = modenDeliveryMap.get(key);
							trackCode = trackerCodes.get(trackCode);
						}
					}
					JSONObject apiValue = callLogAPI(trackCode,deliNo);
					
					return makingOrderStatJson(apiValue, "SUCCESS");
				}else{
					return makingCheck(intStr, "FAIL");
				}
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
				
				JSONObject nowStatus = pchTypeCheck(orderCheck,ordNo);
				return makingOrderStatJson(nowStatus , "SUCCESS");
			}else {
				return makingJson(ordNo , ordService , "FAIL");
			}
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
	
	public String makingOrderStatJson(JSONObject status, String msg) {
		
		JSONObject jo = new JSONObject();
		jo.put("data", status);
		jo.put("version", "2.0");
		
		return jo.toString();
	}
	
	//PCH TYPE별 조회
	public JSONObject pchTypeCheck(List<OrderDetailEntity> orderCheck, String ordNo) throws ParseException {
		
		JSONObject retValue = new JSONObject();
		
		HashMap<String,String> nowStat = new HashMap<>();
		String pchType = orderCheck.get(0).getPchType();
		
		HashMap<String,String> deliInfo = new HashMap<>(); //전체 송장내용 저장 Map
		String allStatus = "";
		
		if(pchType.equals("100") || pchType.equals("400")) {
			//PCH TYPE이 100인 물류센터에서 바로 나가는 주문건
			List<OrderMasterEntity> master = orderMaster.findByOrdNo(ordNo);
			List<TcodeDtlEntity> tcode = tcodeDetail.findByCdNo("W04"); //물류 상태 코드
			HashMap<String,String> statusCodes = new HashMap<>();
			
			//배차 상태 코드를 Map에 담고 직접 접근
			for(TcodeDtlEntity code : tcode) {
				statusCodes.put(code.getCdDtlNo(), code.getCdDtlNm());
			}
			
			//status code를 확인해서 상태값을 넣어줌
			String status = statusCodes.get(master.get(0).getStatusCode());
			for(int i = 0 ; i < orderCheck.size() ; i++) {
				nowStat.put(orderCheck.get(i).getGoodsCd(), status);
				deliInfo.put(orderCheck.get(i).getGoodsCd(),status);
			}
			JSONArray apiValue = searchDeliStatus(deliInfo , ordNo);
			System.out.println(apiValue);
			System.out.println(nowStat);
			
			//송장에 대해서 전체 상태값을 확인
			String now = "";
			String before = "";
			
			HashMap<String, String> endDeliInfo = new HashMap<>();
			
			int index = 0;
			for (String key : deliInfo.keySet()) {
				System.out.println("2. key: " + key + " value: " + deliInfo.get(key));
				
				now = deliInfo.get(key);
				//moden office 주문에 대한 key는 패스
				if(key.equals(MODEN_CODE)) {
					for(String s : nowStat.keySet()) {
		    			endDeliInfo.put(s, now);
		    		}
					allStatus = now;
					continue;
				}else if(key.equals("deliveryWorker")) {
					continue;
				}
				
			    if(index == 0) {
			    	allStatus = now;
			    }else {
			    	if(!now.equals(before)) {
			    		List<TwDirectDeliEntity> tdd = directDeli.findByDeliNo(key);
			    		
			    		if(tdd != null && !tdd.isEmpty()) {
			    			for(TwDirectDeliEntity td : tdd) {
				    			endDeliInfo.put(td.getGoodsCd(), now);
				    		}
			    		}else {
			    			allStatus = now;
			    			break;
			    		}
			    	}else {
			    		allStatus = now;
			    	}
			    }
			    before = now;
			    index ++;
			}
			
			for(String key : nowStat.keySet()) {
				
				nowStat.put(key, allStatus);
				String goodsCdStatus = endDeliInfo.get(key) == null ? "" : endDeliInfo.get(key);
				if(goodsCdStatus.equals("")) {
					
				}else {
					nowStat.put(key, goodsCdStatus);
				}
			}
			
			
			retValue.put("deliveryStatus", deliInfo);
			retValue.put("apiValues", apiValue);
			
		}else if(pchType.equals("200")) {
			//best-i의 ModenStat Table에 모든 주문건인지 확인 ( 확인 로직 추가 )
			List<ModenStatEntity> modenList = modenStat.findByUnqNoLikeOrderByStatusDesc(ordNo+"%");
			HashMap<String, String> modenDeliveryMan = new HashMap<>();
			HashMap<String, String> modenDeliveryMap = new HashMap<>();
			
			ArrayList<String> otherDelivery = new ArrayList<>();
			
			if(modenList != null && !modenList.isEmpty() ) {
				//모든 주문건인 경우
				System.out.println("moden 입니다");
				/*
				 *  01	메모전달 MSG_MANAGER:내부메모, MSG_CUSTOMER: 고객에게 전달할 메모
					11	신규 주문확인(수주생성)
					12	검수지시서 출력( 배차 지시 -> 클레임 가능 ) > *기존 시스템 사용
					13	검수시작( 이 값이 존재하면 취소 불가 )
					14	검수종료( 배차확정 ) > *기존 시스템 사용
					15	검수보류
					19	배송출발 택배 (운송장 번호 여러개 생성 Repeater) > *기존 시스템 사용
					19	배송출발 직배(정보 1개 생성 배송담당 이름과 전화번호) > *기존 시스템 사용
					21	반품주문서 생성( 보류 ) > *기존 시스템 사용
					22	반품입고 완료( 보류 ) > *기존 시스템 사용
					31	교환주문접수
					32	교환주문신청건 반품입고 완료
					33	교환주문신청건 출고완료 > *기존 시스템 사용
					91	주문취소 접수 > *기존 시스템 사용
					92	주문취소 완료 > *기존 시스템 사용
				 * */
				System.out.println(modenList);
				//moden map에는 { 송장번호 : 택배사코드 } 로 되어있음
				HashMap<String, String> modenMap = new HashMap<String, String>();
				
				for(int i = 0 ; i < modenList.size() ; i++) {
					
					JSONArray parcelJson = new JSONArray();
					ModenStatEntity modens = modenList.get(i);
					
					parcelJson.put(modens.getParcelData());
					//System.out.println(parcelJson);
					if(parcelJson.isEmpty() || parcelJson.isNull(0)) {
						//otherDelivery.add(modenList.get(i).getGoodsCd());
					}else {
						modenMap = parseListMap(parcelJson);
						modenDeliveryMan.put("deliveryWorker", modenMap.get("deliveryWorker"));
						for(String key : modenMap.keySet()) {
							modenDeliveryMap.put(key, modenMap.get(key));
						}
						System.out.println("***********" + modenMap);
					}
					
					//Moden Office Code 확인
					String status = modens.getStatus() == null ? "1" : modens.getStatus();
					if(status != null && !status.isEmpty() && !status.isBlank()) {
						
							String isBackStatus = "";
							int stat = Integer.parseInt(status);
							System.out.println("stat = " + stat);
							
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
				System.out.println("1차 now stat " + nowStat);
				for(int i = 0 ; i < orderCheck.size() ; i++) {
					String check = nowStat.get(orderCheck.get(i).getGoodsCd()) == null ? "" : nowStat.get(orderCheck.get(i).getGoodsCd());
					if(check.equals("")) {
						otherDelivery.add(orderCheck.get(i).getGoodsCd());
					}
				}

				//Moden 주문의 경우 해당 배송정보를 확인 ( 모든 직배송인지, 택배 배송인지 )
				for (String key : modenDeliveryMap.keySet()) {
				    System.out.println("1. key: " + key + " value: " + modenDeliveryMap.get(key));
				    deliInfo.put(key, modenDeliveryMap.get(key));
				}
			}

			if((modenList == null || modenList.isEmpty()) || (otherDelivery != null && !otherDelivery.isEmpty())) {
				//모든 주문건이 아닌 경우 WIS - TW_DIRECTDELI 에서 조회함
				System.out.println("moden 아닙니다~");
				System.out.println(otherDelivery);
				String soDt = ordNo.substring(0, 8);
				String soSr = ordNo.substring(8, 14);
				List<TwDirectDeliEntity> tddList = new ArrayList<>();
				if(otherDelivery != null && !otherDelivery.isEmpty()) {
					for(int i = 0 ; i < otherDelivery.size() ; i++) {
						System.out.println(otherDelivery.get(i));
						TwDirectDeliEntity tdd = directDeli.findBySoDateAndSoSerAndGoodsCd(soDt, soSr,otherDelivery.get(i));
						tddList.add(tdd);
						nowStat.put(tdd.getGoodsCd(), tdd.getDeliNo());
					}
				}else {
					tddList = directDeli.findBySoDateAndSoSer(soDt, soSr);
				}
				List<TcodeDtlEntity> tcode = tcodeDetail.findByCdNo("S45");
				System.out.println(tcode);
				
				System.out.println(soDt + ", " + soSr + " , " + tddList);
				//Deli House가 존재하면서 DeliNo를 확인
				for(int i = 0 ; i < tddList.size() ; i++) {
					TwDirectDeliEntity tdd = tddList.get(i);
					if(tdd.getDeliHouse() != null && !tdd.getDeliHouse().isEmpty()) {
						
						String deliNo = tdd.getDeliNo() == null ? "" : tdd.getDeliNo();
						if(!deliNo.equals("")) {
							nowStat.put(tdd.getGoodsCd(), tdd.getDeliNo());
							deliInfo.put(deliNo, tdd.getDeliHouse());
						}else {
							nowStat.put(tdd.getGoodsCd(), "");
						}
					}
				}
			}
			
			JSONArray apiValue = searchDeliStatus(deliInfo , ordNo);
			
			
			System.out.println(apiValue);
			
			for(int i = 0 ; i < apiValue.length() ; i++) {
				JSONObject json = apiValue.getJSONObject(i);
				String deliNo = json.getString("carrierId");
				String deliWorker = json.getString("deliveryWorker");
				
				if(deliInfo.get(deliNo) != null) {
					deliInfo.put(deliNo,json.getString("status"));
				}
				if(deliWorker.equals("조회불가") && deliNo.equals(MODEN_CODE)) {
					deliWorker = modenDeliveryMan.get("deliveryWorker");
					json.remove("deliveryWorker");
					json.put("deliveryWorker", deliWorker);
					
					apiValue.remove(i);
					apiValue.put(json);
					
					allStatus = json.getString("status");
				}
			}
			
			//송장에 대해서 전체 상태값을 확인
			String now = "";
			String before = "";
			
			HashMap<String, String> endDeliInfo = new HashMap<>();
			JSONObject ModenDeliveryWorkers = new JSONObject();
			
			int index = 0;
			for (String key : deliInfo.keySet()) {
				System.out.println("2. key: " + key + " value: " + deliInfo.get(key));
				
				now = deliInfo.get(key);
				//moden office 주문에 대한 key는 패스
				if(key.equals(MODEN_CODE)) {
					for(String s : nowStat.keySet()) {
		    			endDeliInfo.put(s, now);
		    		}
					allStatus = now;
					continue;
				}else if(key.equals("deliveryWorker")) {
					ModenDeliveryWorkers.put("modenDelivery", deliInfo.get(key));
					deliInfo.remove(key);
					retValue.put("modenDelivery", ModenDeliveryWorkers);
					
					continue;
				}
				
			    if(index == 0) {
			    	allStatus = now;
			    }else {
			    	if(!now.equals(before)) {
			    		List<TwDirectDeliEntity> tdd = directDeli.findByDeliNo(key);
			    		
			    		if(tdd != null && !tdd.isEmpty()) {
			    			for(TwDirectDeliEntity td : tdd) {
				    			endDeliInfo.put(td.getGoodsCd(), now);
				    		}
			    		}else {
			    			allStatus = now;
			    			break;
			    		}
			    	}else {
			    		allStatus = now;
			    	}
			    }
			    before = now;
			    index ++;
			}
			
			for(String key : nowStat.keySet()) {
				
				String goodsCdStatus = endDeliInfo.get(key) == null ? "" : endDeliInfo.get(key);
				if(goodsCdStatus.equals("")) {
					nowStat.put(key, allStatus);
				}else {
					nowStat.put(key, goodsCdStatus);
				}
			}
			
			System.out.println("-----------" + nowStat);
			System.out.println(allStatus);
			System.out.println(deliInfo);
			retValue.put("deliveryStatus", deliInfo);
			retValue.put("apiValues", apiValue);
			retValue.put("allStatus", allStatus);
			
		}
		retValue.put("goodsStatus", nowStat);
		
		System.out.println("last retValue == " + retValue);
		return retValue;
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
	 * 택배사 API 조회
	 * url : TRACKER_URL / carrier_id : tracker code / track_id : 송장번호
	 * 
	 * https://apis.tracker.delivery/carriers/:carrier_id/tracks/:track_id
	 * 
	 * */
	public JSONObject callLogAPI(String carrierId , String trackId) {
	    
		
		HashMap<String,String> trackers = tracker.getTrackerName();
		
		String deliComName = trackers.get(carrierId);
		
		System.out.println(carrierId);
		
		String responseBody = "";
		JSONObject retVal = new JSONObject();
		
	    String apiURL = TRACKER_URL+carrierId+"/tracks/"+trackId;
	    
	    responseBody = GETMethod(apiURL);
	    System.out.println(responseBody);
	    
	    JSONObject statusJson = new JSONObject(responseBody);
	    JSONObject carrierJson = new JSONObject();
	    
	    JSONArray progressJson = new JSONArray();
	    
	    System.out.println(statusJson.toString());
	    
	    String mess = statusJson.has("message") ? statusJson.get("message").toString() : "";
	    
	    if(!mess.equals("")) {
	    	
	    	retVal.put("description", "보내시는 고객님께서 상품 발송 준비중입니다.");
			retVal.put("deliveryWorker", "미정");
    		retVal.put("time", "미정");
    		retVal.put("location", "미정");
    		retVal.put("status", "발송준비");
    	    retVal.put("carrierId", trackId);
    	    retVal.put("company", deliComName);
    	    
    	    System.out.println("발송준비 com = " + deliComName);
    	    
    	    JSONObject tempJson = new JSONObject();
    	    JSONObject tempJson2 = new JSONObject();
    	    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    		String datestr = format.format(Calendar.getInstance().getTime());
    		
    	    tempJson.put("description", "");
    	    
    	    tempJson2.put("name", "발송준비");
    	    tempJson.put("location", tempJson2);
    	    tempJson.put("time", datestr);
    	    
    	    tempJson2 = new JSONObject();
    	    tempJson2.put("id", "at_packing");
    	    tempJson2.put("text", "발송준비");
    	    
    	    tempJson.put("status", tempJson2);
    	    progressJson.put(tempJson);
    	    
    	    retVal.put("allProgress", progressJson);
    	    
	    	return retVal;
	    	
	    }
	    
	    carrierJson = new JSONObject(statusJson.get("carrier").toString());
	    progressJson = new JSONArray(statusJson.get("progresses").toString());
	    statusJson = new JSONObject(statusJson.get("state").toString());
	    
	    for(int i = 0 ; i < progressJson.length() ; i++ ) {
	    	JSONObject initJson = progressJson.getJSONObject(i);
	    	JSONObject stat = initJson.getJSONObject("status");
	    	
	    	if(statusJson.get("id").equals(stat.get("id"))) {
	    		
	    		String[] delBracketText = new String[2];
				try {
					delBracketText = deleteBracket(initJson.getString("description"));
					stat = initJson.getJSONObject("location");
					retVal.put("description", delBracketText[0] == null ? "" : delBracketText[0]);
					retVal.put("deliveryWorker", delBracketText[1] == null ? "" : delBracketText[1]);
		    		retVal.put("time", initJson.getString("time"));
		    		retVal.put("location", stat.getString("name"));
		    		retVal.put("company", deliComName);
		    		retVal.put("allProgress", progressJson);
		    		
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	    }
	    
	    retVal.put("status", statusJson.getString("text"));
	    retVal.put("carrierId", carrierJson.getString("id"));
	    System.out.println("retval is = " + retVal);
	    return retVal;
	}
	
	private static String GETMethod(String apiUrl){
		
        System.out.println(apiUrl);
        
        try {
            
        	URL url = new URL(apiUrl);
            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            
            int code = connection.getResponseCode();
            if(code == 200) {
            	String api = readBodyAPI(connection.getInputStream());
                return api;
            }else {
            	return "{\"message\":\"보내시는 고객님께서 상품 발송 준비중입니다.\"}";
            }
            
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } 
    }

    private static String readBodyAPI(InputStream body){
    	
    	System.out.println("1");
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
    
    //괄호내 정보 제거 ( 배송기사 이름 및 전화번호 제거 )
    static String[] deleteBracket(String text) throws Exception {
		
		Matcher matcher = PATTERN_BRACKET.matcher(text);
		
		String removeText = text;
		String pureText = text;
		String VOID = "";
		
		PATTERN_BRACKET = Pattern.compile("\\([^\\(\\)]+\\)");
		matcher = PATTERN_BRACKET.matcher(pureText);
		while(matcher.find()) {
			int startIndex = matcher.start();
			int endIndex = matcher.end();
			
			removeText = pureText.substring(startIndex, endIndex);
			pureText = pureText.replace(removeText, VOID);
			matcher = PATTERN_BRACKET.matcher(pureText);
		}
		
		String[] values = new String[2];
		values[0] = pureText;
		values[1] = removeText;
		
		return values;
    }
    
    public HashMap<String,String> parseListMap(JSONArray jarr){
    	
    	HashMap<String,String> parcelMap = new HashMap<String,String>();
    	
    	for(int j = 0; j < jarr.length() ; j++) {
			try {
				
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
				
				List<Map<String, Object>> paramMap = 
						mapper.readValue(jarr.get(j).toString(), new TypeReference<List<Map<String, Object>>>(){});
				
				System.out.println(paramMap);
				for(Map<String,Object> paramMaps : paramMap) {
					if(paramMaps.get("PARCELSERVICE").equals(MODEN_CODE)) {
						String modenHP = paramMaps.get("MODENHP") == null ? "" : " "+paramMaps.get("MODENHP").toString();
						parcelMap.put(MODEN_CODE,"Moden Office 직배송");
						parcelMap.put("deliveryWorker",paramMaps.get("MODENNAME") == null ? "정보없음" : 
							paramMaps.get("MODENNAME").toString() + modenHP);
					}else {
						String deliCode = paramMaps.get("PARCELNUMBER") == null ? "" : paramMaps.get("PARCELNUMBER").toString().replaceAll("-", "");
						parcelMap.put(deliCode, paramMaps.get("PARCELSERVICE") == null ? "" : paramMaps.get("PARCELSERVICE").toString());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    	
    	return parcelMap;
    }
    
    public JSONArray searchDeliStatus(HashMap<String,String> deliInfo, String ordNo) throws ParseException {

    	JSONArray retValue = new JSONArray();
		HashMap<String,String> trackers = tracker.getTrackerCode();
		
    	for (String key : deliInfo.keySet()) {
    		
    		String deliCode = deliInfo.get(key);
    		String deliNo = key;
    		
    		if(deliCode.equals(MODEN_CODE)) {
    			JSONObject apiValue = getModenAndGP(ordNo, deliNo);
				retValue.put(apiValue);
    		}else if(!deliNo.equals("")) {
    			IntStream stream = ((CharSequence) deliNo).chars();
    			String intStrDeliNo = stream.filter((ch)-> (48 <= ch && ch <= 57))
    			        .mapToObj(ch -> (char)ch)
    			        .map(Object::toString)
    			        .collect(Collectors.joining());
    			//송장 번호가 아닌 다른 사유가 있는 경우 패스함
    			if(intStrDeliNo.equals("")) {
    				continue;
    			}else {
    				//택배사에 따른 API 조회 코드를 확인
    				if(deliCode.equals("")) {
    					//API 조회코드 없음
    				}else {
    					//API 조회코드 있음
    					deliCode = trackers.get(deliCode);
    					if(deliCode != null && !deliCode.isEmpty()) {
    						JSONObject apiValue = callLogAPI(deliCode,deliNo);
        					System.out.println(apiValue);
        					retValue.put(apiValue);
    					}else {
    						JSONObject apiValue = getModenAndGP(ordNo, deliNo);
    						retValue.put(apiValue);
    					}
    				}
    			}
    		}
    	}
    	return retValue;
    }
    
    public JSONObject getModenAndGP(String ordNo, String deliNo) throws ParseException {
    	
    	JSONObject apiValue = new JSONObject();
    	
    	//물류에서 체크하는 상태값을 확인
		List<OrderDetailEntity> orderCheck = orderDetail.findByOrdNo(ordNo);
		List<TcodeDtlEntity> tcode = tcodeDetail.findByCdNo("W04"); //물류 상태 코드
		HashMap<String,String> statusCodes = new HashMap<>();
		
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		String datestr = format.format(Calendar.getInstance().getTime());
		
		//배차 상태 코드를 Map에 담고 직접 접근
		for(TcodeDtlEntity code : tcode) {
			statusCodes.put(code.getCdDtlNo(), code.getCdDtlNm());
		}
		
		//status code(직배송의 배송상태)를 확인해서 상태값을 넣어줌
		String status = "";
		
		String DriverName = "조회불가";
		List<WisDriverDeliveryEntity> driver = wisDelivery.findByOrdNo(ordNo);
		if(driver != null && !driver.isEmpty()) {
			String driverNo = driver.get(0) == null ? "" : driver.get(0).getTransSabon();
			if(driverNo.equals("")) {
				
			}else {
				List<WisDriverEntity> driverInfo = wisDriver.findByDriverNo(driverNo);
				if(driverInfo != null && !driverInfo.isEmpty()) {
					String name = driverInfo.get(0) == null ? "" : driverInfo.get(0).getDriverName();
					String ph = driverInfo.get(0) == null ? "" : driverInfo.get(0).getTelNo();
					if(name.equals("") || ph.equals("")) {
						
					}else {
						DriverName = name + " " + ph;
					}
				}
			}
		}
		
		//status도 확인되지 않는 주문건의 경우는 확인 불가
		if(status.equals("")) {
			apiValue.put("description", "정보가 확인되지 않습니다");
			apiValue.put("deliveryWorker", DriverName);
			apiValue.put("time", datestr);
			apiValue.put("location", "조회불가");
			//apiValue.put("status", "");
			apiValue.put("carrierId", deliNo);
			apiValue.put("company", "조회불가");
			
		}else {
			//pch type이 100이 아니지만 물류 상태코드를 통해 조회가 되는 경우
			for(int i = 0 ; i < orderCheck.size() ; i++) {
				apiValue.put("description", "고객님의 주문은 현재 " + status + " 상태 입니다");
				apiValue.put("deliveryWorker", DriverName);
				apiValue.put("time", datestr);
				apiValue.put("location", "조회불가");
				//apiValue.put("status", status);
				apiValue.put("carrierId", deliNo);
				apiValue.put("company", "직접배송");
				
			}
		}
		
		List<TdeliStatEntity> tdeli = tdeliStat.findByOrdNo(ordNo);
		if(tdeli != null && !tdeli.isEmpty()) {
			JSONArray allProgress = new JSONArray();
			SimpleDateFormat dbFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			
			String beforeCheck = "";
			String nowCheck ="";
			String nowStatus = "";
			for(int i = 0 ; i < tdeli.size() ; i++) {
				
				nowCheck = tdeli.get(i).getDelvItmCd();
				nowStatus = statusCodes.get(tdeli.get(i).getDelvItmCd());
				
				if(!nowCheck.equals(beforeCheck)) {

					JSONObject progress = new JSONObject();
					JSONObject tempJson = new JSONObject();
					tempJson.put("name", "확인불가");
					
					progress.put("description", tempJson);
					progress.put("location", tempJson);
					
					tempJson = new JSONObject();
					tempJson.put("id", "do_not_checking");
					
					String now = statusCodes.get(tdeli.get(i).getDelvItmCd());
					
					
					tempJson.put("text", now);
					progress.put("status", tempJson);
					
					
					Date date = dbFormat.parse(tdeli.get(i).getRegDm());
					datestr = format.format(date);
					
					progress.put("time", datestr);
					allProgress.put(progress);
					
				}
				
				beforeCheck = tdeli.get(i).getDelvItmCd();
				if(i == tdeli.size()-1) {
					System.out.println(nowStatus);
					apiValue.put("status", nowStatus);
				}
				System.out.println(nowCheck + " , " + beforeCheck);
			}
			apiValue.put("allProgress", allProgress);
		}else {
			apiValue.put("status", "확인불가");
		}
		System.out.println("moden status = " + status);
		
		return apiValue;
    }
}

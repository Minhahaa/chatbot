package com.gp.chatbot.model.bo.project;

import java.util.HashMap;

import lombok.Data;

public @Data class TrackerDeliBO {
		
	private String name;
	private String code;
	
	public TrackerDeliBO() {}
	
	public TrackerDeliBO(String name) {
		HashMap<String, String> codes = getTrackerName();
		this.name = name;
		this.code = codes.get(name);
	}

	public HashMap<String, String> getTrackerName() {
		
		HashMap<String, String> deliCodes = new HashMap<>();
		deliCodes.put("DHL", "de.dhl");
		deliCodes.put("Sagawa", "jp.sagawa");
		deliCodes.put("KuronekoYamato", "jp.yamato");
		deliCodes.put("JapanPost", "jp.yuubin"); 
		deliCodes.put("012", "kr.chunilps"); //천일택배
		deliCodes.put("020", "kr.cjlogistics"); //CJ대한통운
		deliCodes.put("034", "kr.cupost"); //CU편의점
		deliCodes.put("036", "kr.cvsnet"); //GS POSTBOX 택배
		deliCodes.put("CWAY", "kr.cway"); //CWAY (woori express)
		deliCodes.put("004", "kr.daesin"); //대신택배
		deliCodes.put("011", "kr.epost"); //우체국택배
		deliCodes.put("한의사랑택배", "kr.hanips"); //한의사랑 택배
		deliCodes.put("016", "kr.hanjin"); //한진택배
		deliCodes.put("032", "kr.hdexp"); //합동택배
		deliCodes.put("홈픽", "kr.homepick");
		deliCodes.put("018", "kr.honamlogis"); //호남택배 -- 한서호남택배 하나임
		deliCodes.put("015", "kr.honamlogis"); //한서택배
		deliCodes.put("035", "kr.ilyanglogis"); //일양로지스
		deliCodes.put("002", "kr.kdexp"); //경동택배
		deliCodes.put("001", "kr.kunyoung"); //건영택배
		deliCodes.put("007", "kr.logen"); //로젠택배
		deliCodes.put("017", "kr.lotte"); //롯데택배
		deliCodes.put("SLX", "kr.slx"); //SLX
		deliCodes.put("성원글로벌카고", "kr.swgexp"); //성원글로벌카고
		deliCodes.put("TNT", "nl.tnt"); //TNT
		deliCodes.put("EMS", "un.upu.ems"); //EMS
		deliCodes.put("Fedex", "us.fedex"); //Fedex
		deliCodes.put("UPS", "us.ups"); //UPS
		deliCodes.put("USPS", "us.usps"); //USPS
		
		return deliCodes;
	}
	
}

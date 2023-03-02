package com.gp.chatbot.model.api;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.google.gson.JsonObject;

public class PapagoNaver {
	
	private final String langClientID = "C8r5qAfsTNukRLAfbtGk";
	private final String langClientSecret = "eUthABN8Bm";
	
	private final String clientID = "fhzywnbgbu";
	private final String clientSecret = "X8qZtXKuNcea9EkIqIh2cws6On1sv87BOd1zR8jz";
	
	/* lang code */
	public String callPapago(String keyword) {
		
		String query;
	    try {
	        query = URLEncoder.encode(keyword, "UTF-8");
	    } catch (UnsupportedEncodingException e) {
	        throw new RuntimeException("인코딩 실패", e);
	    }
	    String apiURL = "https://openapi.naver.com/v1/papago/detectLangs";
        Map<String, String> requestHeaders = new HashMap<>();
	    requestHeaders.put("X-Naver-Client-Id", langClientID);
	    requestHeaders.put("X-Naver-Client-Secret", langClientSecret);
	    
	    String responseBody = post(apiURL, requestHeaders, query , "");
	    System.out.println(responseBody);

	    JSONObject jsonObject = new JSONObject(responseBody);
	    String langCode = (String)jsonObject.get("langCode");
		
		return langCode;
		
	}
	
	/* translation */
	public String callPapagoTranslate(String keyword , String langType) {
	    
		String query;
		String responseBody = "";
	    try {
	        query = URLEncoder.encode(keyword, "UTF-8");
	    } catch (UnsupportedEncodingException e) {
	        throw new RuntimeException("인코딩 실패", e);
	    }
	    String apiURL = "https://naveropenapi.apigw.ntruss.com/nmt/v1/translation";
        Map<String, String> requestHeaders = new HashMap<>();
	    requestHeaders.put("X-NCP-APIGW-API-KEY-ID", clientID);
	    requestHeaders.put("X-NCP-APIGW-API-KEY", clientSecret);
	    
	    if(langType.equals("ko")) {
	    	responseBody = post(apiURL, requestHeaders, query , "ko");
	    }else if(langType.equals("en")) {
	    	responseBody = post(apiURL, requestHeaders, query , "en");
	    }else {
	    	responseBody = post(apiURL, requestHeaders, query , "en");
	    }
	    JSONObject statusJson = new JSONObject(responseBody);
	    System.out.println(statusJson.toString());
	    
	    statusJson = new JSONObject(statusJson.get("message").toString());
	    statusJson = new JSONObject(statusJson.get("result").toString());
	    String translatedText = (String)statusJson.get("translatedText");
	    
	    return translatedText;
	}
	
	private static String post(String apiUrl, Map<String, String> requestHeaders, String text, String langType){
        HttpURLConnection con = connect(apiUrl);
        String postParams = "";
        if(langType.equals("ko")) {
        	 postParams = "source=ko&target=en&text=" + text;
        }else if(langType.equals("en")) {
        	 postParams = "source=en&target=ko&text=" + text;
        }else if(!langType.equals("")){
        	postParams = "source="+langType+"&target=ko&text=" + text;
        }else {
        	 postParams =  "query="  + text;
        }
        
        try {
            con.setRequestMethod("POST");
            for(Map.Entry<String, String> header :requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }

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

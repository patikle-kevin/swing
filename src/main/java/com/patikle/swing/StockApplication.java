package com.patikle.swing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import org.apache.ibatis.session.SqlSession;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.jayway.jsonpath.internal.Path;
import com.patikle.swing.contents.bars.Bars;
import com.patikle.swing.contents.buy.Buy;
import com.patikle.swing.contents.buy.BuyMultiple;
import com.patikle.swing.contents.buy.TradeRunnable;
import com.patikle.swing.contents.greenbars.GreenBars;
import com.patikle.swing.contents.split.SplitService;
import com.patikle.swing.contents.trade.KIS;
import com.patikle.swing.contents.util.APIRequestService;


@EnableScheduling 
@SpringBootApplication
public class StockApplication {

	@Autowired
	Bars bars;

    @Autowired
    SplitService splitService;

	@Autowired
	GreenBars greenBars;

	// @Autowired
	// Buy buy;

	@Autowired
	BuyMultiple buyMultiple;


	@Autowired
	KIS kis;

	@Autowired
    SqlSession sqlSession;


    private final String APP_KEY = "PS7AJ2nLKuZNOODHnrQqwa8RPDr4zOsb1xYo";
    private final String APP_SECRET = "vPHy9ZczPGKSDJbqT0nkcbNbu3D75Yl1lw5hK8ak8aySJc6a13qd7IYJhs3JuaRhi3HL12+QMqcmy73GJTNq2NB0SRidqoA6hnST0c4TZalPg1tnIRTwLfBFSLH1a1um0MNvXKTgSk4f8TXx6QcBmvDLMdMk7npv/UHybdg+YPsXFdsRcrM=";
    private final String URL_BASE = "https://openapivts.koreainvestment.com:29443";
    private final String ACCESS_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0b2tlbiIsImF1ZCI6IjQzNmM1NzllLTNhZTMtNDFkZS04MzNiLWY0NWE2MTc1YjhjNSIsImlzcyI6InVub2d3IiwiZXhwIjoxNjY3OTg0NTI1LCJpYXQiOjE2Njc4OTgxMjUsImp0aSI6IlBTN0FKMm5MS3VaTk9PREhuclFxd2E4UlBEcjR6T3NiMXhZbyJ9.mJ0OK1FmdlygHWKTdGSaEYNoPA0y_yx0VKvVjUFXnn2z94vvbFQrl-TX-nQVw7oO32uJplHGK5E2RzKFnbtSrg";
    
    @Autowired
    APIRequestService apiRequestService;

	public static void main(String[] args)throws IOException{
		SpringApplication application = new SpringApplication(StockApplication.class);
		application.setWebApplicationType(WebApplicationType.NONE);
		application.run(args);
	}
	
	@PostConstruct
	void started() throws IOException {
		// TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
		LocalDateTime localDateTime = LocalDateTime.now();
		String end = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "T00:00:00Z";;
		localDateTime = localDateTime.minusDays(1);
		String start = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "T00:00:00Z";;

		String timeframe = "1Day";
		String adjustment = "split";
		String limit = "10000";
		//bars.run("UXIN", timeframe, adjustment, "2015-10-25T04:00:00Z", end, limit);
		//greenBars.run("UXIN");
		
		splitService.updateStockSplit(start, end);
        System.out.println("updateStockSplit END");

        bars.run(timeframe, adjustment, start, end, limit);
        System.out.println("bars.run END");
        greenBars.run();
        System.out.println("greenBars END");

        // buyMultiple.run();



		// kis.run();

		// 국내 주식 시세 조회
        // get();
		// httpPostBodyConnection();
        // post();
        
    }
    
    private void get(){
		String url = URL_BASE + "/uapi/overseas-stock/v1/trading/inquire-balance";
		String tr_id = "VTTT3012R";
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("authorization", "Bearer " + ACCESS_TOKEN);
        headersMap.put("appKey", APP_KEY);
        headersMap.put("appSecret", APP_SECRET);
        headersMap.put("tr_id", tr_id);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        
        params.add("CANO", "50074422");
        params.add("ACNT_PRDT_CD", "01");
        params.add("OVRS_EXCG_CD", "NASD");

        params.add("TR_CRCY_CD", "USD");
        // params.add("SORT_SQN", "DS");
        params.add("CTX_AREA_FK200", "");
        params.add("CTX_AREA_NK200", "");

        Map<String, Object> map = apiRequestService.get(url, headersMap, params, Map.class);
        Iterator<String> iterator = map.keySet().iterator();
        while(iterator.hasNext()){
            String key  = iterator.next();
            System.out.println(key + ":" + map.get(key));
        }
    }

    private void post(){ 
        try{
            URL url = new URL(URL_BASE + "/uapi/overseas-stock/v1/trading/order");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestProperty("content-type", "application/json; charset=utf-8");
            conn.setRequestProperty("authorization", "Bearer " + ACCESS_TOKEN);
            conn.setRequestProperty("appkey", APP_KEY);
            conn.setRequestProperty("appsecret", APP_SECRET);
            conn.setRequestProperty("tr_id", "VTTT1002U");

            conn.setRequestMethod("POST"); // PUT is another valid option
            conn.setDoOutput(true);


            JSONObject jsonObject = new JSONObject();
            jsonObject.put("CANO", "50074422");
            jsonObject.put("ACNT_PRDT_CD", "01");
            jsonObject.put("OVRS_EXCG_CD", "NASD");
            
            jsonObject.put("PDNO", "MSFT");
            jsonObject.put("ORD_QTY", "100");
            jsonObject.put("OVRS_ORD_UNPR", "229.60");
    
            jsonObject.put("CTAC_TLNO", "");
            jsonObject.put("MGCO_APTM_ODNO", "");
            jsonObject.put("ORD_SVR_DVSN_CD", "0");
            jsonObject.put("ORD_DVSN", "00");

            // System.out.println(jsonObject.toString());

            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonObject.toString().getBytes("utf-8");
                os.write(input, 0, input.length);			
            }catch(Exception e){
                System.out.println(e.getMessage());

            }

            try(BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println(response.toString());
            }

            /* 
            byte[] out = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;

            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.connect();
            try(OutputStream os = http.getOutputStream()) {
                os.write(out);
            } */
            // Do something with http.getInputStrea


            // http.setRequestMethod("POST"); // PUT is another valid option
            // http.setDoOutput(true);
        }catch(Exception e){
            
        }

    }

	private void httpPostBodyConnection()throws IOException {
		String url = URL_BASE + "/uapi/overseas-stock/v1/trading/order";
		String tr_id = "VTTT1002U";

        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("content-type", "application/json; charset=utf-8");
        headersMap.put("authorization", "Bearer " + ACCESS_TOKEN);
        headersMap.put("appkey", APP_KEY);
        headersMap.put("appsecret", APP_SECRET);
        headersMap.put("tr_id", tr_id);


        // MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        // params.add("CTX_AREA_FK200", "");
        // params.add("CTX_AREA_NK200", "");
        // JSONObject jsonObject = new JSONObject();

        // {
        // "CANO":"50074422",
        // "ACNT_PRDT_CD":"01",
        // "OVRS_EXCG_CD": "NASD",
        // "PDNO": "AAPL",
        // "ORD_QTY": "100",
        // "OVRS_ORD_UNPR": "0",
        // "CTAC_TLNO": "",
        // "MGCO_APTM_ODNO": "",
        // "ORD_SVR_DVSN_CD": "0",
        // "ORD_DVSN": "00"
        // }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("CANO", "50074422");
        jsonObject.put("ACNT_PRDT_CD", "01");
        jsonObject.put("OVRS_EXCG_CD", "NASD");
        
        jsonObject.put("PDNO", "MSFT");
        jsonObject.put("ORD_QTY", "100");
        jsonObject.put("OVRS_ORD_UNPR", "229.20");

        jsonObject.put("CTAC_TLNO", "");
        jsonObject.put("MGCO_APTM_ODNO", "");
        jsonObject.put("ORD_SVR_DVSN_CD", "0");
        jsonObject.put("ORD_DVSN", "00");
        
        Map<String, Object> map = apiRequestService.post(url, headersMap, jsonObject, Map.class);

        Iterator<String> iterator = map.keySet().iterator();
        while(iterator.hasNext()){
            String key  = iterator.next();
            System.out.println(key + ":" + map.get(key));
        }
	}

	@Bean
	public WebMvcConfigurer corsConfigurer(){
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry corsRegistry){
				corsRegistry.addMapping("/**").allowedOrigins("*");
			}
		};
	}
}

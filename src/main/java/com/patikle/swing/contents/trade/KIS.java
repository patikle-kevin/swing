package com.patikle.swing.contents.trade;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.patikle.swing.contents.util.APIRequestService;

@Component
public class KIS {
    Logger log = LoggerFactory.getLogger(KIS.class);

    @Autowired
    APIRequestService apiRequestService;

    private final String APP_KEY = "PS7AJ2nLKuZNOODHnrQqwa8RPDr4zOsb1xYo";
    private final String APP_SECRET = "vPHy9ZczPGKSDJbqT0nkcbNbu3D75Yl1lw5hK8ak8aySJc6a13qd7IYJhs3JuaRhi3HL12+QMqcmy73GJTNq2NB0SRidqoA6hnST0c4TZalPg1tnIRTwLfBFSLH1a1um0MNvXKTgSk4f8TXx6QcBmvDLMdMk7npv/UHybdg+YPsXFdsRcrM";
    private final String URL_BASE = "https://openapivts.koreainvestment.com:29443";
    private final String ACCESS_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0b2tlbiIsImF1ZCI6IjNlZWE0NDhiLWI3Y2QtNDhlOS1hMWFhLWM4MDUzZTVlNDBiNyIsImlzcyI6InVub2d3IiwiZXhwIjoxNjY3MjgwMDIyLCJpYXQiOjE2NjcxOTM2MjIsImp0aSI6IlBTN0FKMm5MS3VaTk9PREhuclFxd2E4UlBEcjR6T3NiMXhZbyJ9.cszyp2JkMxgbqIh1ybZ-FOs1ezj7pEPCMJpLwtZJnBOtyH0l5UUtjYSypc5kjDdgAdMA2S0cBwvBOEQ16EL8Ag";

    public void run(){
        String uri = URL_BASE + "/uapi/overseas-stock/v1/trading/inquire-balance";
        // String tr_id = "VTTT3001R";
        // Map<String, String> map = new HashMap<>();
        // map.put("authorization","Bearer " + ACCESS_TOKEN);
        // map.put("appkey", APP_KEY);
        // map.put("appsecret", APP_SECRET);
        // map.put("tr_id", "VTTT3001R");
        // map.put("content-type", "application/json; charset=utf-8");


        try {
			URL url = new URL(uri);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setConnectTimeout(5000); //서버에 연결되는 Timeout 시간 설정
			con.setReadTimeout(5000); // InputStream 읽어 오는 Timeout 시간 설정
            con.setRequestProperty("authorization", "Bearer " + ACCESS_TOKEN);
            con.setRequestProperty("appkey", APP_KEY);
            con.setRequestProperty("appsecret", APP_SECRET);
            con.setRequestProperty("tr_id", "VTTT3001R");
            con.setDoOutput(true);
            //json으로 message를 전달하고자 할 때 
			con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
			con.setDoInput(true);  // InputStream으로 응답 헤더와 메시지를 읽어들이겠다는 옵션
			con.setDoOutput(true); //POST 데이터를 OutputStream으로 넘겨 주겠다는 설정 
			con.setUseCaches(false);
			con.setDefaultUseCaches(false);

            // params.add("CANO", "50074422");
            // params.add("ACNT_PRDT_CD", "01");
            // params.add("OVRS_EXCG_CD", "NASD");
            // params.add("TR_CRCY_CD", "USD");
            // params.add("CTX_AREA_FK200", "");
            // params.add("CTX_AREA_NK200", "");
            String jsonMessage = "{\n" +
            "    \"CANO\": \"50074422\",\n" +
            "    \"ACNT_PRDT_CD\": \"01\",\n" +
            "    \"OVRS_EXCG_CD\": \"NASD\",\n" +
            "    \"TR_CRCY_CD\": \"USD\",\n" +
            "    \"CTX_AREA_FK200\": \"\",\n" +
            "    \"CTX_AREA_NK200\": \"\"\n" +
            "}";


			OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
			wr.write(jsonMessage); //json 형식의 message 전달 
			wr.flush();
            con.connect();
			StringBuilder sb = new StringBuilder();
            System.out.println(con.getResponseCode());
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				//Stream을 처리해줘야 하는 귀찮음이 있음.
				BufferedReader br = new BufferedReader(
						new InputStreamReader(con.getInputStream(), "utf-8"));
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line).append("\n");
				}
				br.close();
                System.out.println("=================");
				System.out.println("" + sb.toString());
                System.out.println("=================");
			} else {
                System.out.println("----------------");
				System.out.println(con.getResponseMessage());
                System.out.println("----------------");
			}
		} catch (Exception e){
			e.printStackTrace();
		}
    }
    
}

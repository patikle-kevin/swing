package com.patikle.swing.contents.bars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.patikle.swing.contents.code.CodeDao;
import com.patikle.swing.contents.code.CodeVo;
import com.patikle.swing.contents.util.APIRequestService;

@Component
public class Bars {
    Logger logger = LoggerFactory.getLogger(Bars.class); 

    @Autowired
    APIRequestService apiRequestService;

    @Autowired
    SqlSession sqlSession;

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getAvaiableTraded(){
        CodeVo vo = new CodeVo("financialmodelingprep.apikey");
        vo.setKeyName("financialmodelingprep.apikey");
        vo = sqlSession.getMapper(CodeDao.class).selectCode(vo);
        String url = "https://financialmodelingprep.com/api/v3/available-traded/list?apikey=" + vo.getKeyValue();
        List<Map<String, Object>> result = null;
        result = apiRequestService.get(url, List.class);
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String ,Object> getBarData(Map<String, Object> map){
        StringBuilder sb = new StringBuilder("https://data.alpaca.markets/v2/stocks/");
        
        sb.append(map.get("symbol").toString().toUpperCase());
        sb.append("/bars?adjustment="+map.get("adjustment"));
        sb.append("&timeframe="+ map.get("timeframe"));
        sb.append("&limit="+ map.get("limit"));
        sb.append("&start=" + map.get("start"));
        sb.append("&end=" + map.get("end"));
        if(map.containsKey("next_page_token") && map.get("next_page_token") != null){
            sb.append("&page_token=" + map.get("next_page_token"));
        }
        CodeVo vo = new CodeVo("alpaca.market.apikey");
        vo = sqlSession.getMapper(CodeDao.class).selectCode(vo);
        String apiKey =  vo.getKeyValue();
        vo.setKeyName("alpaca.market.secretkey");
        vo = sqlSession.getMapper(CodeDao.class).selectCode(vo);
        String secretkey = vo.getKeyValue();
        Map<String, Object> result = apiRequestService.get(sb.toString(), Map.class, apiKey, secretkey);
        return result;
    }

    @SuppressWarnings("unchecked")
    private void insertBarsDataBulk(Map<String, Object> map){
        if(map.containsKey("bars") && map.get("bars") != null){
            List<Map<String, Object>> list = (List<Map<String, Object>>)map.get("bars");
            BarsBulkVo vo = new BarsBulkVo();
            vo.setSymbol(map.get("symbol").toString());
            vo.setList(list);
            sqlSession.getMapper(BarsDao.class).insertBarsDataBulk(vo);
            while(true){
                if(map.containsKey("next_page_token") && map.get("next_page_token") != null){
                    Map<String, Object> result = getBarData(map);
                    list = (List<Map<String, Object>>)result.get("bars");
                    vo.setList(list);
                    vo.setSymbol(map.get("symbol").toString());
                    sqlSession.getMapper(BarsDao.class).insertBarsDataBulk(vo);
                    
                }else{
                    break;
                }
            }
        }
    }

    public void run(String symbol, String timeframe, String adjustment, String start, String end, String limit){
        Map<String, Object> map = new HashMap<>();
        map.put("symbol", symbol); 
        map.put("timeframe", timeframe); 
        map.put("adjustment", adjustment); 
        map.put("start", start); 
        map.put("end", end); 
        map.put("limit", limit); 
        Map<String, Object> result = getBarData(map);
        insertBarsDataBulk(result);
    }

    public void run(String timeframe, String adjustment, String start, String end, String limit){
        List<Map<String, Object>> list = getAvaiableTraded();
        List<Map<String, Object>> insertList = new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++){
            Map<String, Object> map = list.get(i);
            if(map.get("exchangeShortName").toString().equals("NASDAQ") 
            || map.get("exchangeShortName").toString().equals("NYSE") 
            || map.get("exchangeShortName").toString().equals("AMEX")
            ){
                map.put("timeframe", timeframe); 
                map.put("adjustment", adjustment); 
                map.put("start", start); 
                map.put("end", end); 
                map.put("limit", limit); 
                insertList.add(map);
            }
        }

        for(int i = 0 ; i < insertList.size() ; i++){
            Map<String, Object> map = insertList.get(i);
            Map<String, Object> result = getBarData(map);
            insertBarsDataBulk(result);
        }
    }

    public int deleteBarsData(String symbol){
        sqlSession.getMapper(BarsDao.class).deleteBarsData(symbol);
        return 1;
    }
}


package com.patikle.swing.contents.split;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.patikle.swing.contents.bars.Bars;
import com.patikle.swing.contents.buy.BuyService;
import com.patikle.swing.contents.code.CodeDao;
import com.patikle.swing.contents.code.CodeVo;
import com.patikle.swing.contents.greenbars.GreenBars;
import com.patikle.swing.contents.util.APIRequestService;

@Service
public class SplitServiceImpl implements SplitService{
    
    @Autowired
    APIRequestService apiRequestService;

    @Autowired
    SqlSession sqlSession;

    @Autowired
	Bars bars;
    
    @Autowired
	GreenBars greenBars;

    @Autowired
    BuyService buyService;


    
    public void updateStockSplit(String from, String to){

        CodeVo vo = new CodeVo("financialmodelingprep.apikey");
        vo.setKeyName("financialmodelingprep.apikey");
        vo = sqlSession.getMapper(CodeDao.class).selectCode(vo);
        String url = "https://financialmodelingprep.com/api/v3/stock_split_calendar?apikey=" + vo.getKeyValue() + "&from=" + from + "&to="+ to;
        List<Map<String, Object>> result = null;
        result = apiRequestService.get(url, List.class);

		LocalDateTime localDateTime = LocalDateTime.now();
		String end = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "T00:00:00Z";;
		localDateTime = localDateTime.minusDays(1);

		String timeframe = "1Day";
		String adjustment = "split";
		String limit = "10000";


        for(int i = 0 ; i < result.size() ; i++){
            Map<String, Object> item = result.get(i);
            String symbol = item.get("symbol").toString();

            int count = buyService.selectTbTickerCountBySymbol(symbol); 
            
            if(count > 0) {
                bars.deleteBarsData(symbol);
                greenBars.deleteGreenBars(symbol);
                bars.run(symbol, timeframe, adjustment, "2015-12-01T04:00:00Z", end, limit);
                greenBars.run(symbol);
            }
        }
    }
}

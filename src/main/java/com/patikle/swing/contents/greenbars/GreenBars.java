package com.patikle.swing.contents.greenbars;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.patikle.swing.contents.bars.BarsVo;
import com.patikle.swing.contents.bars.TickerVo;

@Component
public class GreenBars {
    Logger logger = LoggerFactory.getLogger(GreenBars.class); 
    @Autowired
    private SqlSession sqlSession;

    public void run(){
        System.out.println(sqlSession);
		TickerVo tickerVo = new TickerVo();
		tickerVo.setFloatShares(50000000);
        List<TickerVo> list = sqlSession.getMapper(GreenBarsDao.class).selectTickerListByFloatShares(tickerVo);
		for(int i = 0 ; i < list.size() ; i++){
			collectGreenBars(list.get(i).getSymbol());
            System.out.println(list.get(i).getSymbol() + " collectGreenBars END");
		}
    }
    public void run(String symbol){
        collectGreenBars(symbol);
    }
    private void collectGreenBars(String symbol){
        BarsVo vo = new BarsVo();
        vo.setSymbol(symbol);
        List<BarsVo> list = sqlSession.getMapper(GreenBarsDao.class).selectBars1DayList(vo);
        if(list.size() > 0){
            BarsVo barsVo = list.get(0);

            BigDecimal c_ha =  barsVo.getO().add(barsVo.getC()).add(barsVo.getH()).add(barsVo.getL()).divide(BigDecimal.valueOf(4), 4, RoundingMode.HALF_UP);
            // double c_ha = (double)(barsVo.getO()+barsVo.getC()+barsVo.getH()+barsVo.getL()) / 4;

            BigDecimal o_ha = barsVo.getO().add(barsVo.getC()).divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
            // double o_ha = (double)(barsVo.getO() + barsVo.getC()) / 2;
            
            BigDecimal h_ha = barsVo.getH().max(c_ha.max(o_ha));
            // double h_ha = Math.max(barsVo.getH(), c_ha.max(o_ha));// Math.max(c_ha, o_ha));
            
            BigDecimal l_ha = barsVo.getL().min(c_ha.min(o_ha));
            // double l_ha = Math.min(barsVo.getL(), Math.min(c_ha, o_ha));

            String t = barsVo.getT();
            t = t.replace("T", " ");
            t = t.substring(0, 16);

            barsVo.setC_ha(c_ha);
            barsVo.setO_ha(o_ha);
            barsVo.setH_ha(h_ha);
            barsVo.setL_ha(l_ha);
            barsVo.setT(t);
            list.set(0, barsVo);

            List<GreenBarsVo> greenBarsVos = new ArrayList<>();
            for(int i = 1 ; i < list.size() ; i++){
                barsVo = list.get(i);
                BarsVo prevBarsVo = list.get(i-1);
                
                c_ha =  barsVo.getO().add(barsVo.getC()).add(barsVo.getH()).add(barsVo.getL()).divide(BigDecimal.valueOf(4), 4, RoundingMode.HALF_UP);
                // c_ha = (double)(barsVo.getO()+barsVo.getC() + barsVo.getH()+barsVo.getL()) / 4;
                
                o_ha = prevBarsVo.getO_ha().add(prevBarsVo.getC_ha()).divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
                // o_ha = (double)(prevBarsVo.getO_ha() + prevBarsVo.getC_ha()) / 2;
                
                h_ha = barsVo.getH().max(c_ha.max(o_ha));
                // h_ha = Math.max(barsVo.getH(), Math.max(c_ha, o_ha));
                
                l_ha = barsVo.getL().min(c_ha.min(o_ha));
                // l_ha = Math.min(barsVo.getL(), Math.min(c_ha, o_ha));
                
                t = barsVo.getT();
                t = t.replace("T", " ");
                t = t.substring(0, 16);
    
                barsVo.setC_ha(c_ha);
                barsVo.setO_ha(o_ha);
                barsVo.setH_ha(h_ha);
                barsVo.setL_ha(l_ha);
                barsVo.setT(t);
                list.set(i, barsVo);

                int compare1 = prevBarsVo.getC_ha().compareTo(barsVo.getC_ha());
                int compare2 = barsVo.getH_ha().compareTo(prevBarsVo.getH_ha());
                // compare < 0 == prevBarsVo.getC_ha 가 barsVo.getC_ha 보다 작다
                
                if(barsVo.getV() < prevBarsVo.getV() &&
                    compare1 < 0 &&
                    compare2 >= 0
                ){
                // if(
                //     barsVo.getV() < prevBarsVo.getV() &&
                //     prevBarsVo.getC_ha() < barsVo.getC_ha() &&
                //     barsVo.getH_ha() >= prevBarsVo.getH_ha()
                // ){
                    GreenBarsVo greenBarsVo = new GreenBarsVo(barsVo, prevBarsVo);
                    greenBarsVos.add(greenBarsVo);
                }
            }
            if(greenBarsVos.size() > 0){
                sqlSession.getMapper(GreenBarsDao.class).insertGreenBars(greenBarsVos);
            }
        }
    }

    public int deleteGreenBars(String symbol){
        sqlSession.getMapper(GreenBarsDao.class).deleteGreenBars(symbol);
        return 1;
    }

}

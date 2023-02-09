package com.patikle.swing.contents.buy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.patikle.swing.contents.bars.BarsVo;

@Service
public class BuyServiceImpl implements BuyService{

	@Autowired
	SqlSession sqlSession;

    public List<BarsVo> selectTbBarsOneDayListBySymbol(String symbol){
        List<BarsVo> list = sqlSession.getMapper(BuyDao.class).selectTbBarsOneDayListBySymbol(symbol);
        this.setHeikinAshi(list);
        return list;
    }
    public List<BuyVo> selectTbBarsOneDayGreenList(){
        List<BuyVo> list = sqlSession.getMapper(BuyDao.class).selectTbBarsOneDayGreenList();
        return list;
    }

	public List<BuyVo> selectTbBarsOneDayGreenListWithoutVolumeSize(){
		List<BuyVo> list = sqlSession.getMapper(BuyDao.class).selectTbBarsOneDayGreenListWithoutVolumeSize();
        return list;
	}

	public int selectTbTickerCountBySymbol(String symbol){
		int count = sqlSession.getMapper(BuyDao.class).selectTbTickerCountBySymbol(symbol);
        return count;
	}

    private void setHeikinAshi(List<BarsVo> list){
		if(list.size() > 0){
			BarsVo barsVo = list.get(0);
			BigDecimal c_ha = barsVo.getO().add(barsVo.getC()).add(barsVo.getH()).add(barsVo.getL()).divide(BigDecimal.valueOf(4), 4, RoundingMode.HALF_UP);
			BigDecimal o_ha = barsVo.getO().add(barsVo.getC()).divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
			BigDecimal h_ha = barsVo.getH().max(c_ha.max(o_ha));
			BigDecimal l_ha = barsVo.getL().min(c_ha.min(o_ha));

			String t = barsVo.getT();
			t = t.replace("T", " ");
			t = t.substring(0, 16);

			barsVo.setC_ha(c_ha);
			barsVo.setO_ha(o_ha);
			barsVo.setH_ha(h_ha);
			barsVo.setL_ha(l_ha);
			barsVo.setT(t);
			list.set(0, barsVo);

			for(int i = 1 ; i < list.size() ; i++){
				barsVo = list.get(i);
				BarsVo prevBarsVo = list.get(i-1);
				c_ha =  barsVo.getO().add(barsVo.getC()).add(barsVo.getH()).add(barsVo.getL()).divide(BigDecimal.valueOf(4), 4, RoundingMode.HALF_UP);
				o_ha = prevBarsVo.getO_ha().add(prevBarsVo.getC_ha()).divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
				h_ha = barsVo.getH().max(c_ha.max(o_ha));
				l_ha = barsVo.getL().min(c_ha.min(o_ha));

				t = barsVo.getT();
				t = t.replace("T", " ");
				t = t.substring(0, 16);
	
				barsVo.setC_ha(c_ha);
				barsVo.setO_ha(o_ha);
				barsVo.setH_ha(h_ha);
				barsVo.setL_ha(l_ha);
				barsVo.setT(t);
				list.set(i, barsVo);
			}
		}
	}
    
}

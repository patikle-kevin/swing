package com.patikle.swing.contents.greenbars;

import java.util.List;

import com.patikle.swing.contents.bars.BarsVo;
import com.patikle.swing.contents.bars.TickerVo;


public interface GreenBarsDao {
    public List<BarsVo> selectBars1DayList(BarsVo vo);
    public List<TickerVo> selectTickerListByFloatShares(TickerVo vo);
    public int insertGreenBars(List<GreenBarsVo> list);
    public int deleteGreenBars(String symbol);
}

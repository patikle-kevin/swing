package com.patikle.swing.contents.buy;

import java.util.List;

import com.patikle.swing.contents.bars.BarsVo;

public interface BuyDao {
    List<BarsVo> selectTbBarsOneDayListBySymbol(String symbol);
    List<BuyVo> selectTbBarsOneDayGreenListWithoutVolumeSize();
    List<BuyVo> selectTbBarsOneDayGreenList();
    int selectTbTickerCountBySymbol(String symbol);
}

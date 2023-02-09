package com.patikle.swing.contents.bars;
import java.util.Map;
public interface BarsDao {
    public int insertBarsData(Map<String, Object> map);
    public int insertBarsDataBulk(BarsBulkVo vo);

    public int deleteBarsData(String symbol);
}

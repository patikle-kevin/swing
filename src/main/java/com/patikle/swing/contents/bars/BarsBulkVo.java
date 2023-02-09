package com.patikle.swing.contents.bars;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BarsBulkVo {
    String symbol;
    List<Map<String, Object>> list;
}

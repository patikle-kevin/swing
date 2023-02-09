package com.patikle.swing.contents.bars;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BarsVo {

    public BarsVo(){

    }
    public BarsVo(Map<String, Object> map){
        this.symbol = map.get("symbol").toString();
        this.t = map.get("t").toString();
        this.o = BigDecimal.valueOf(Double.parseDouble(map.get("o").toString()));
        this.h = BigDecimal.valueOf(Double.parseDouble(map.get("h").toString()));
        this.l = BigDecimal.valueOf(Double.parseDouble(map.get("l").toString()));
        this.c = BigDecimal.valueOf(Double.parseDouble(map.get("c").toString()));
        this.v = Long.parseLong(map.get("v").toString());
        this.n = Long.parseLong(map.get("n").toString());
        this.vw = BigDecimal.valueOf(Double.parseDouble(map.get("vw").toString()));
    }
    public String getTFormattedString(){
        this.t = this.t.replaceAll("T", " ").replaceAll("Z", "");
        LocalDateTime dt = LocalDateTime.parse(this.t, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        dt = dt.minusHours(4);
        return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    String symbol;
    String t;
    BigDecimal o;
    BigDecimal h;
    BigDecimal l;
    BigDecimal c;
    long v;
    long n;
    BigDecimal vw;

    BigDecimal o_ha;
    BigDecimal h_ha;
    BigDecimal l_ha;
    BigDecimal c_ha;

    String start;
    String end;
}

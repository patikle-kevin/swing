package com.patikle.swing.contents.greenbars;


import java.math.BigDecimal;

import com.patikle.swing.contents.bars.BarsVo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GreenBarsVo {
    public GreenBarsVo(){

    }
    public GreenBarsVo(BarsVo cur, BarsVo prev){
        this.symbol = cur.getSymbol();
        this.t = cur.getT();
        this.o = cur.getO();
        this.h = cur.getH();
        this.l = cur.getL();
        this.c = cur.getC();
        this.v = cur.getV();
        this.n = cur.getN();
        this.vw =cur.getVw();

        this.o_ha = cur.getO_ha();
        this.h_ha = cur.getH_ha();
        this.l_ha = cur.getL_ha();
        this.c_ha = cur.getC_ha();

        this.prev_t = prev.getT();
        this.prev_o = prev.getO();
        this.prev_h = prev.getH();
        this.prev_l = prev.getL();
        this.prev_c = prev.getC();
        this.prev_v = prev.getV();
        this.prev_n = prev.getN();
        this.prev_vw =prev.getVw();

        this.prev_o_ha = prev.getO_ha();
        this.prev_h_ha = prev.getH_ha();
        this.prev_l_ha = prev.getL_ha();
        this.prev_c_ha = prev.getC_ha();

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
    String prev_t;
    BigDecimal prev_o;
    BigDecimal prev_h;
    BigDecimal prev_l;
    BigDecimal prev_c;
    long prev_v;
    long prev_n;
    BigDecimal prev_vw;
    BigDecimal prev_o_ha;
    BigDecimal prev_h_ha;
    BigDecimal prev_l_ha;
    BigDecimal prev_c_ha;
}

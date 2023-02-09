package com.patikle.swing.contents.buy;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BuyVo {
    
    boolean used = false;

    String symbol;
    String t;
    long floatShares;
    BigDecimal marketCap;
    
    BigDecimal o;
    BigDecimal c;
    BigDecimal h;
    BigDecimal l;
    
    BigDecimal haO;
    BigDecimal haC;
    BigDecimal haH;
    BigDecimal haL;

    long volune;

    BigDecimal buy1st;
    BigDecimal sell1st;
    boolean isBought1st = false;
    int countBought1st = 0;


    BigDecimal buy2nd;
    BigDecimal sell2nd;
    boolean isBought2nd = false;
    int countBought2nd = 0;

    BigDecimal buy3rd;
    BigDecimal sell3rd;
    boolean isBought3rd = false;
    int countBought3rd = 0;

    float percent;

    int purchase = 0;

    boolean complete = false;
}

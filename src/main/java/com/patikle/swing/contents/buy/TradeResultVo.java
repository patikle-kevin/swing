package com.patikle.swing.contents.buy;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TradeResultVo {
    public TradeResultVo(){
    }
    public TradeResultVo(String symbol){
        this.symbol = symbol;
    }

    String symbol;
    boolean success;
    boolean stoploss;
    String firstBuyDate;
    String finalSellDate;

    double proceeds;

    double buyAndSellPeriod;

    int round;

    String buy1stDate;
    String buy2ndDate;
    String buy3rdDate;

    BigDecimal buy1st;
    BigDecimal buy2nd;
    BigDecimal buy3rd;

    int buy1stCount;
    int buy2ndCount;
    int buy3rdCount;

    String sell1stDate;
    String sell2ndDate;
    String sell3rdDate;

    BigDecimal sell1st;
    BigDecimal sell2nd;
    BigDecimal sell3rd;

    int sell1stCount;
    int sell2ndCount;
    int sell3rdCount;

}

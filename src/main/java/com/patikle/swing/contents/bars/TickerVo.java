package com.patikle.swing.contents.bars;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TickerVo {
    
    private String symbol ;
    private String name ;
    private double price ;
    private double changesPercentage ;
    private double change;

    private double dayLow;
    private double dayHigh;
    private double yearHigh;
    private double yearLow;
    private long volume;
    private long avgVolume;
    private String exchange ;
    private double open;
    private double previousClose;

    private double floatShares;
    private String latestTrade;
}

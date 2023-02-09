package com.patikle.swing.contents.buy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.patikle.swing.contents.bars.BarsVo;
import com.patikle.swing.contents.code.CodeDao;
import com.patikle.swing.contents.code.CodeVo;
import com.patikle.swing.contents.util.APIRequestService;

public class TradeRunnable implements Runnable{

	private final int BEFORE_PURCHASE = 0; 
	private final int FIRST_PURCHASE = 1;
	private final int SECOND_PURCHASE = 2;
	private final int THIRD_PURCHASE = 3;
	private final int RATIO = 3;

	SqlSession sqlSession;

	APIRequestService apiRequestService;

	BuyService buyService;

	String threadName;

	List<BuyVo> list = null;
	
	public TradeRunnable(SqlSession sqlSession, APIRequestService apiRequestService, BuyService buyService, List<BuyVo> list, String threadName){
		this.sqlSession = sqlSession;
		this.apiRequestService = apiRequestService;
		this.buyService = buyService;
		this.list = list;
		this.threadName = threadName;
	}

	@Override
	public void run(){
		LocalDateTime finalSellDate = LocalDateTime.now();
		List<TradeResultVo> tradeResultVos = new ArrayList<>();
		double cumulativeProceeds = 0;

		double money = 20000;
		for(int i = 0 ;; i++){
			BuyVo item = null;
			synchronized(this){
				if(i > 0){
					int index = getNextTradeItemIndex(list, finalSellDate);
					if(index == -1){
						break;
					}
					item = list.get(index);
					TradeResultVo vo = tradeResultVos.get(tradeResultVos.size()-1);
					cumulativeProceeds = vo.getProceeds();
				}else{
					item = list.get(0);
					if(item.isUsed()){
						LOOP:
						for(; i < list.size() ; i++){
							item = list.get(i);
							if(!item.isUsed()){
								break LOOP;
							}
						}
					}
				}
				if(item.isUsed()){
					continue;
				}
				item.setUsed(true);
			}

			List<BarsVo> bars = buyService.selectTbBarsOneDayListBySymbol(item.getSymbol());
			int baseIndex = 0;
			BarsVo tMinusOneBar = new BarsVo();
			BarsVo tMinusZeroBar = new BarsVo();
			
			for(int k = 0 ; k < bars.size() ; k++){
				if(bars.get(k).getT().equals(item.getT())){
					baseIndex = k+1;
					tMinusOneBar = bars.get(k-1); 
					tMinusZeroBar = bars.get(k);
					break;
				}
			}
			if(i > 0){
				BarsVo bar = bars.get(baseIndex);
				LocalDateTime datetime = LocalDateTime.parse(bar.getT(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
				if(datetime.isBefore(finalSellDate)){
					LOOP:
					for(int k = 0 ; k < bars.size() ; k++){
						bar = bars.get(k);
						datetime = LocalDateTime.parse(bar.getT(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
						if(datetime.isAfter(finalSellDate)){
							baseIndex = k;
							break LOOP;
						}
					}
				}
			}

			// t-1이 양봉일 경우만실행한다
			if(tMinusOneBar.getO_ha().compareTo(tMinusOneBar.getC_ha()) <= 0){
				// 전략선 설정
				item.setSell2nd(item.getBuy1st());
				item.setBuy2nd(tMinusZeroBar.getO_ha());
				item.setSell3rd(tMinusZeroBar.getO_ha());
				item.setBuy3rd(tMinusOneBar.getO_ha());
				TradeResultVo tradeResultVo = getTradeResult(bars, baseIndex, item, money, cumulativeProceeds);
				String str = tradeResultVo.getSymbol() + ", " + tradeResultVo.isSuccess() + ", 거래일 " + tradeResultVo.getFirstBuyDate() + ", " + tradeResultVo.getFinalSellDate();
				if(tradeResultVo.isSuccess()){
					if(tradeResultVo.isStoploss()){
						money = money - Math.abs(tradeResultVo.getProceeds());
						str += ", 손절 ," + tradeResultVo.getRound() + "차  ,";
						tradeResultVo.setProceeds(0);
						if(tradeResultVos.size() > 0){
							TradeResultVo prev = tradeResultVos.get(tradeResultVos.size()-1);
							prev.setProceeds(0);
						}
					}else{
						if(tradeResultVo.getBuy3rdCount() > 0){
							// 3차 까지 왔음
							// 2차 매수 가격으로 전체 매도
							double sell = tradeResultVo.getSell3rd().doubleValue() * (tradeResultVo.getBuy1stCount() + tradeResultVo.getBuy2ndCount() + tradeResultVo.getBuy3rdCount());
							double buy1st = tradeResultVo.getBuy1stCount() * tradeResultVo.getBuy1st().doubleValue();
							double buy2nd = tradeResultVo.getBuy2ndCount() * tradeResultVo.getBuy2nd().doubleValue();
							double buy3rd = tradeResultVo.getBuy3rdCount() * tradeResultVo.getBuy3rd().doubleValue();
							double buy = buy1st + buy2nd + buy3rd;
							str += ", 성공 , 3차 " + buy + " 에 구매, " + sell +" 에 판매";
							tradeResultVo.setProceeds(sell - buy);
						}else if(tradeResultVo.getBuy2ndCount() > 0){
							// 2차 까지 왔음
							// 1차 매수 가격으로 전체 매도
							double sell = tradeResultVo.getSell2nd().doubleValue() * (tradeResultVo.getBuy1stCount() + tradeResultVo.getBuy2ndCount());
							double buy1st = tradeResultVo.getBuy1stCount() * tradeResultVo.getBuy1st().doubleValue();
							double buy2nd = tradeResultVo.getBuy2ndCount() * tradeResultVo.getBuy2nd().doubleValue();
							double buy = buy1st + buy2nd;
							str += ", 성공 , 2차 " + buy + " 에 구매, " + sell +" 에 판매";
							tradeResultVo.setProceeds(sell - buy);
						}else if(tradeResultVo.getBuy1stCount() > 0){
							// 1차 까지 왔음
							// 1차 매도 가격으로 전체 매도
							double sell = tradeResultVo.getSell1st().doubleValue() * tradeResultVo.getBuy1stCount();
							double buy1st = tradeResultVo.getBuy1stCount() * tradeResultVo.getBuy1st().doubleValue();
							double buy = buy1st;
							str += ", 성공 , 1차 " + buy + " 에 구매, " + sell +" 에 판매";
							tradeResultVo.setProceeds(sell - buy);
						}
					}
					finalSellDate = LocalDateTime.parse(tradeResultVo.getFinalSellDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
				}
				tradeResultVos.add(tradeResultVo);
				double proceedsSum = 0;
				for(int j = 0 ; j < tradeResultVos.size() ; j++){
					proceedsSum += tradeResultVos.get(j).getProceeds();
				}
				System.out.println(threadName +" >> " + str + ", 현재 원금은: " + money + ", 그리고 수익은: " + tradeResultVo.getProceeds() + ", 마지막 누적 수익은: " + proceedsSum);
			}
			item.setComplete(true);
		}
	}

	private double getRate(TradeResultVo tradeResultVo){
		if(tradeResultVo.getRound() == 3){
			BigDecimal avg = tradeResultVo.getBuy1st().multiply(BigDecimal.valueOf(0.25))
			.add(tradeResultVo.getBuy2nd().multiply(BigDecimal.valueOf(0.25)))
			.add(tradeResultVo.getBuy3rd().multiply(BigDecimal.valueOf(0.5)));
			double a = tradeResultVo.getBuy2nd().subtract(avg).multiply(BigDecimal.valueOf(0.5)).doubleValue();
			double b = tradeResultVo.getBuy1st().subtract(avg).multiply(BigDecimal.valueOf(0.5)).doubleValue();
			return (a+b) / avg.doubleValue() * 100;

		}else{
			BigDecimal avg = tradeResultVo.getBuy1st().multiply(BigDecimal.valueOf(0.5))
			.add(tradeResultVo.getBuy2nd().multiply(BigDecimal.valueOf(0.5)));
			double price = tradeResultVo.getSell2nd().subtract(avg).doubleValue();
			return price / avg.doubleValue() * 100;
		}
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> getBarData(String symbol, String start, String end){
		StringBuilder sb = new StringBuilder("https://data.alpaca.markets/v2/stocks/");
		sb.append(symbol);
		sb.append("/bars?adjustment=split");
		// sb.append("/bars?");
		sb.append("&timeframe=1min");
		sb.append("&limit=10000");
		sb.append("&start=" + start);
		sb.append("&end=" + end);
		CodeVo vo = new CodeVo("alpaca.market.apikey");
		vo = sqlSession.getMapper(CodeDao.class).selectCode(vo);
		String apiKey =  vo.getKeyValue();
		vo.setKeyName("alpaca.market.secretkey");
		vo = sqlSession.getMapper(CodeDao.class).selectCode(vo);
		String secretkey = vo.getKeyValue();

		Map<String, Object> result = apiRequestService.get(sb.toString(), Map.class, apiKey, secretkey);
		if(result.get("bars") == null){
			return new ArrayList<Map<String, Object>>();
		}
		return (List<Map<String, Object>>)result.get("bars");
	}

	private boolean tryFirstBuy(TradeResultVo tradeResultVo, BuyVo base, BarsVo vo, double money){
		if(base.getCountBought1st() == 0){
			tradeResultVo.setBuy1st(base.getBuy1st());
			tradeResultVo.setBuy1stDate(vo.getTFormattedString());
			tradeResultVo.setFirstBuyDate(vo.getTFormattedString());
			tradeResultVo.setRound(1);
			base.setCountBought1st(base.getCountBought1st()+1);
			base.setPurchase(FIRST_PURCHASE);
			double count = money / tradeResultVo.getBuy1st().doubleValue();
			tradeResultVo.setBuy1stCount((int)count);
			return true;
		}
		return false;
	}
	
	private boolean trySecondBuy(TradeResultVo tradeResultVo, BuyVo base, BarsVo vo, double money){
		if(vo.getL().compareTo(base.getBuy2nd()) <= 0){
			if(base.getCountBought2nd() == 0){
				tradeResultVo.setBuy2nd(base.getBuy2nd());
				tradeResultVo.setBuy2ndDate(vo.getTFormattedString());
				tradeResultVo.setRound(2);
				base.setCountBought2nd(base.getCountBought2nd()+1);
				base.setPurchase(SECOND_PURCHASE);

				double count = money / tradeResultVo.getBuy2nd().doubleValue();
				tradeResultVo.setBuy2ndCount((int)count);
				return true;
			}
		}
		return false;
	}
	
	private boolean tryThirdBuy(TradeResultVo tradeResultVo, BuyVo base, BarsVo vo, double money){
		if(vo.getL().compareTo(base.getBuy3rd()) <= 0){
			if(base.getCountBought3rd() == 0){
				tradeResultVo.setBuy3rd(base.getBuy3rd());
				tradeResultVo.setBuy3rdDate(vo.getTFormattedString());
				tradeResultVo.setRound(3);
				base.setCountBought3rd(base.getCountBought3rd()+1);
				base.setPurchase(THIRD_PURCHASE);
				double count = money / tradeResultVo.getBuy3rd().doubleValue();
				tradeResultVo.setBuy3rdCount((int)count);
				return true;
			}
		}
		return false;
	}

	private boolean tryFirstSell(TradeResultVo tradeResultVo, BuyVo strategyVo, BarsVo bar){
		if(bar.getH().compareTo(strategyVo.getSell1st()) >= 0){
			tradeResultVo.setSell1st(strategyVo.getSell1st());
			tradeResultVo.setSell1stDate(bar.getTFormattedString());
			tradeResultVo.setFinalSellDate(bar.getTFormattedString());
			tradeResultVo.setBuyAndSellPeriod(strategyVo.getPercent());
			tradeResultVo.setSuccess(true);
			return true;
		}
		return false;
	}

	private boolean trySecondSell(TradeResultVo tradeResultVo, BuyVo strategyVo, BarsVo bar){
		if(bar.getH().compareTo(strategyVo.getSell2nd()) >= 0){
			tradeResultVo.setSell2nd(strategyVo.getSell2nd());
			tradeResultVo.setSell2ndDate(bar.getTFormattedString());
			tradeResultVo.setFinalSellDate(bar.getTFormattedString());
			tradeResultVo.setBuyAndSellPeriod(getRate(tradeResultVo));
			tradeResultVo.setSuccess(true);
			return true;
		}
		return false;
	}

	private boolean tryThirdSell(TradeResultVo tradeResultVo, BuyVo strategyVo, BarsVo bar){
		if(bar.getH().compareTo(strategyVo.getSell3rd()) >= 0){
			tradeResultVo.setSell3rd(strategyVo.getSell3rd());
			tradeResultVo.setSell3rdDate(bar.getTFormattedString());
			tradeResultVo.setFinalSellDate(bar.getTFormattedString());
			tradeResultVo.setBuyAndSellPeriod(getRate(tradeResultVo));
			tradeResultVo.setSuccess(true);
			return true;
		}
		return false;
	}

	/**
	 * 오픈 가격보다 손절가격이 작은 경우 무조건 손절한다
	 */
	private boolean tryStopLossForce(BuyVo strategyVo, TradeResultVo tradeResultVo, BarsVo bar, double previousProceeds, String finalSellDate, double principal){
		boolean b = false;
		double buyPrice = 0;
		// 1차매수
		if(strategyVo.getPurchase() == 1){
			buyPrice = tradeResultVo.getBuy1st().doubleValue();
		}else if(strategyVo.getPurchase() == 2){
			buyPrice = tradeResultVo.getBuy2nd().doubleValue();
		}else if(strategyVo.getPurchase() == 3){
			buyPrice = tradeResultVo.getBuy3rd().doubleValue();
		}
		// 1 차 조건 만족
		// 마지막 거래한 가격보다 분봉 오픈 가격 작은 경우 
		if(bar.getO().doubleValue() < buyPrice){
			double buyPriceSum = 0;
			int count = 0;
			if(tradeResultVo.getBuy1st() != null){
				buyPriceSum += tradeResultVo.getBuy1st().doubleValue() * tradeResultVo.getBuy1stCount();
				count += tradeResultVo.getBuy1stCount();
			}
			if(tradeResultVo.getBuy2nd() != null){
				buyPriceSum += tradeResultVo.getBuy2nd().doubleValue() * tradeResultVo.getBuy2ndCount();
				count += tradeResultVo.getBuy2ndCount();
			}
			if(tradeResultVo.getBuy3rd() != null){
				buyPriceSum += tradeResultVo.getBuy3rd().doubleValue() * tradeResultVo.getBuy3rdCount();
				count += tradeResultVo.getBuy3rdCount();
			}
			// 2차조건 
			// 분봉 오픈 가격보다 손절가격이 작은 경우
			// 이전 수익이 0인경우 원금의 3%를 손절가로 본다
			boolean bPrevNoneProceeds = false;
			if(previousProceeds == 0){
				previousProceeds = (double)principal * ((double)RATIO/100);
				bPrevNoneProceeds = true;
			}

			double price = (buyPriceSum - previousProceeds) / count;
			if(price > bar.getO().doubleValue()){
				tradeResultVo.setFinalSellDate(finalSellDate);
				tradeResultVo.setSuccess(true);
				tradeResultVo.setStoploss(true);
				if(bPrevNoneProceeds){
					tradeResultVo.setProceeds(-(double)principal * ((double)RATIO/100));
				}
				b = true;
			}
		}
		return b;
	}

	private boolean tryStopLoss(TradeResultVo tradeResultVo, double proceeds, double previousProceeds, String finalSellDate, double principal){
		if(previousProceeds == 0){
			double buyPrice = 0;
			if(tradeResultVo.getBuy1st() != null){
				buyPrice += tradeResultVo.getBuy1st().doubleValue() * tradeResultVo.getBuy1stCount();
			}
			if(tradeResultVo.getBuy2nd() != null){
				buyPrice += tradeResultVo.getBuy2nd().doubleValue() * tradeResultVo.getBuy2ndCount();
			}
			if(tradeResultVo.getBuy3rd() != null){
				buyPrice += tradeResultVo.getBuy3rd().doubleValue() * tradeResultVo.getBuy3rdCount();
			}
			double percent = (buyPrice - (buyPrice - Math.abs(proceeds))) / (buyPrice - Math.abs(proceeds));
			if(percent > RATIO){
				tradeResultVo.setFinalSellDate(finalSellDate);
				tradeResultVo.setSuccess(true);
				tradeResultVo.setStoploss(true);
				tradeResultVo.setProceeds(-(double)principal * ((double)RATIO/100));
				return true;
			}
		}else{
			if(proceeds < 0 && previousProceeds <= Math.abs(proceeds)){
				tradeResultVo.setFinalSellDate(finalSellDate);
				tradeResultVo.setSuccess(true);
				tradeResultVo.setStoploss(true);
				return true;
			}
		}
		return false;
	}
	
	private boolean tryFirstTradeJob(TradeResultVo tradeResultVo, BarsVo minBar, BuyVo strategyVo, double twentyFive, double fifty, double previousProceeds){
		boolean b = false;
		// int count = tradeResultVo.getBuy1stCount() + tradeResultVo.getBuy2ndCount() + tradeResultVo.getBuy3rdCount();

		// 분봉의 OPEN 가격이 매도가보다 높은경우 무조건 매도 됐을것
		if(minBar.getO().doubleValue() >= strategyVo.getSell1st().doubleValue()){
			if(tryFirstSell(tradeResultVo, strategyVo, minBar)){
				b = true;
			}
		}

		// 분봉의 OPEN 가격이 손절가 보다 낮으면 무조건 손절 했을것
		// if(!b){
		// 	if(tryStopLossForce(strategyVo, tradeResultVo, minBar, previousProceeds, minBar.getTFormattedString(), twentyFive * 4)){
		// 		b = true;
		// 	}
		// }
		
		// 매도시도 
		if(!b){
			if(tryFirstSell(tradeResultVo, strategyVo, minBar)){
				b = true;
			}
		}

		// 손절시도
		// if(!b){
		// 	if(tryStopLoss(tradeResultVo, (minBar.getL().doubleValue() * count) - twentyFive, previousProceeds, minBar.getTFormattedString(), twentyFive * 4)){
		// 		b = true;
		// 	}
		// }

		if(!b){
			trySecondBuy(tradeResultVo, strategyVo, minBar, twentyFive);
			tryThirdBuy(tradeResultVo, strategyVo, minBar, fifty);
		}
		return b;
	}

	private boolean trySecondTradeJob(TradeResultVo tradeResultVo, BarsVo minBar, BuyVo strategyVo, double money, double fifty, double previousProceeds){
		boolean b = false;
		// int count = tradeResultVo.getBuy1stCount() + tradeResultVo.getBuy2ndCount() + tradeResultVo.getBuy3rdCount();
		// 분봉의 OPEN 가격이 매도가보다 높은경우 무조건 판매가 됐을것
		if(minBar.getO().doubleValue() >= strategyVo.getSell2nd().doubleValue()){
			if(trySecondSell(tradeResultVo, strategyVo, minBar)){
				b = true;
			}
		}
		
		// 분봉의 OPEN 가격이 손절가 보다 낮으면 무조건 손절 했을것
		// if(!b){
		// 	if(tryStopLossForce(strategyVo, tradeResultVo, minBar, previousProceeds, minBar.getTFormattedString(),  fifty * 2)){
		// 		b = true;
		// 	}
		// }

		if(!b){
			if(trySecondSell(tradeResultVo, strategyVo, minBar)){
				b = true;
			}
		}
		// if(!b){
		// 	// Low 로 팔았을 경우 원금을 뺀 나머지 금액
		// 	// 3차 까지 구매 한 경우는 원금이 100% 들어갔다
		// 	if(tryStopLoss(tradeResultVo, (minBar.getL().doubleValue() * count) - fifty, previousProceeds, minBar.getTFormattedString(), fifty * 2)){
		// 		b = true;
		// 	}
		// }
		
		if(!b){
			tryThirdBuy(tradeResultVo, strategyVo, minBar, fifty);
		}
		return b;
	}

	private boolean tryThirdTradeJob(TradeResultVo tradeResultVo, BarsVo minBar, BuyVo strategyVo, double money, double fifty, double previousProceeds){
		boolean b = false;
		// 분봉의 OPEN 가격이 매도가보다 높은경우 무조건 판매가 됐을것
		if(minBar.getO().doubleValue() >= strategyVo.getSell3rd().doubleValue()){
			if(tryThirdSell(tradeResultVo, strategyVo, minBar)){
				b = true;
			}
		}

		// 분봉의 OPEN 가격이 손절가 보다 낮으면 무조건 손절 했을것
		if(!b){
			if(tryStopLossForce(strategyVo, tradeResultVo, minBar, previousProceeds, minBar.getTFormattedString(), money)){
				b = true;
			}
		}

		if(!b){
			tryThirdSell(tradeResultVo, strategyVo, minBar);
		}
		if(!b){
			int count = tradeResultVo.getBuy1stCount() + tradeResultVo.getBuy2ndCount() + tradeResultVo.getBuy3rdCount();
			// Low 로 팔았을 경우 원금을 뺀 나머지 금액
			// 누적 수익 보다 현재 손해(손해는 마이너스) 가 크면 문제임
			if(tryStopLoss(tradeResultVo, (minBar.getL().doubleValue() * count) - money, previousProceeds, minBar.getTFormattedString(), money)){
				b = true;
			}
		}
		return b;
	}

	public TradeResultVo getTradeResult(List<BarsVo> list, int baseIndex, BuyVo strategyVo, double money, double previousProceeds){
		double twentyFive = money / 4;
		double fifty = money / 2; 
		TradeResultVo tradeResultVo = new TradeResultVo(strategyVo.getSymbol());
		FOR_1:
		for(int i = baseIndex ; i < list.size() ; i++){
			BarsVo dayBar = list.get(i);
			String start = dayBar.getT().substring(0, 10) + "T04:00:00Z";
			String end = dayBar.getT().substring(0, 10) + "T23:59:00Z";
			
			if(strategyVo.getPurchase() == BEFORE_PURCHASE){
				if(dayBar.getL().compareTo(strategyVo.getBuy1st()) <= 0){
					List<Map<String, Object>> bars = getBarData(strategyVo.getSymbol(), start, end);
					for(int j = 0; j < bars.size() ; j++){
						bars.get(j).put("symbol", strategyVo.getSymbol());
						BarsVo minBar  = new BarsVo(bars.get(j));
						if(strategyVo.getPurchase() == BEFORE_PURCHASE){
							if(minBar.getL().compareTo(strategyVo.getBuy1st()) <= 0){
								tryFirstBuy(tradeResultVo, strategyVo, minBar, twentyFive);

								// int count = tradeResultVo.getBuy1stCount() + tradeResultVo.getBuy2ndCount() + tradeResultVo.getBuy3rdCount();
								// if(tryStopLoss(tradeResultVo, (minBar.getL().doubleValue() * count) - twentyFive, previousProceeds, minBar.getTFormattedString(), twentyFive * 4)){
								// 	break FOR_1;
								// }

								if(tryFirstSell(tradeResultVo, strategyVo, minBar)){
									break FOR_1;
								}
								trySecondBuy(tradeResultVo, strategyVo, minBar, twentyFive);
								tryThirdBuy(tradeResultVo, strategyVo, minBar, fifty);
							}
						}else{
							if(strategyVo.getPurchase() == FIRST_PURCHASE){
								if(tryFirstTradeJob(tradeResultVo, minBar, strategyVo, twentyFive, fifty, previousProceeds)){
									break FOR_1;
								}
							}else if(strategyVo.getPurchase() == SECOND_PURCHASE){
								if(trySecondTradeJob(tradeResultVo, minBar, strategyVo, money, fifty, previousProceeds)){
									break FOR_1;
								}
							}else if(strategyVo.getPurchase() == THIRD_PURCHASE){
								if(tryThirdTradeJob(tradeResultVo, minBar, strategyVo, money, fifty, previousProceeds)){
									break FOR_1;
								}
							}
						}
					}
				}
			}else if(strategyVo.getPurchase() == FIRST_PURCHASE){
				if(dayBar.getH().compareTo(strategyVo.getSell1st()) >= 0){
					List<Map<String, Object>> bars = getBarData(strategyVo.getSymbol(), start, end);
					for(int j = 0; j < bars.size() ; j++){
						bars.get(j).put("symbol", strategyVo.getSymbol());
						BarsVo minBar  = new BarsVo(bars.get(j));
						if(strategyVo.getPurchase() == FIRST_PURCHASE){
							if(tryFirstTradeJob(tradeResultVo, minBar, strategyVo, twentyFive, fifty, previousProceeds)){
								break FOR_1;
							}
						}else if(strategyVo.getPurchase() == SECOND_PURCHASE){
							if(trySecondTradeJob(tradeResultVo, minBar, strategyVo, money, fifty, previousProceeds)){
								break FOR_1;
							}
						}else if(strategyVo.getPurchase() == THIRD_PURCHASE){
							if(tryThirdTradeJob(tradeResultVo, minBar, strategyVo, money, fifty, previousProceeds)){
								break FOR_1;
							}
						}
					}
				}else if(dayBar.getL().compareTo(strategyVo.getBuy2nd()) <= 0 && strategyVo.getCountBought2nd() == 0){
					List<Map<String, Object>> bars = getBarData(strategyVo.getSymbol(), start, end);
					for(int j = 0; j < bars.size() ; j++){
						bars.get(j).put("symbol", strategyVo.getSymbol());
						BarsVo minBar  = new BarsVo(bars.get(j));
						if(strategyVo.getPurchase() == FIRST_PURCHASE){
							if(tryFirstTradeJob(tradeResultVo, minBar, strategyVo, twentyFive, fifty, previousProceeds)){
								break FOR_1;
							}
						}else if(strategyVo.getPurchase() == SECOND_PURCHASE){
							if(trySecondTradeJob(tradeResultVo, minBar, strategyVo, money, fifty, previousProceeds)){
								break FOR_1;
							}
						}else if(strategyVo.getPurchase() == THIRD_PURCHASE){
							if(tryThirdTradeJob(tradeResultVo, minBar, strategyVo, money, fifty, previousProceeds)){
								break FOR_1;
							}
						}
					}
				}else{
					/* int count = tradeResultVo.getBuy1stCount() + tradeResultVo.getBuy2ndCount() + tradeResultVo.getBuy3rdCount();
					if(tryStopLoss(tradeResultVo, (dayBar.getL().doubleValue() * count) - twentyFive, previousProceeds, dayBar.getT()+":00", twentyFive * 4)){
						List<Map<String, Object>> bars = getBarData(strategyVo.getSymbol(), start, end);
						for(int j = 0; j < bars.size() ; j++){
							bars.get(j).put("symbol", strategyVo.getSymbol());
							BarsVo minBar = new BarsVo(bars.get(j));
							count = tradeResultVo.getBuy1stCount() + tradeResultVo.getBuy2ndCount() + tradeResultVo.getBuy3rdCount();
							// Low 로 팔았을 경우 원금을 뺀 나머지 금액
							//  누적 수익 보다 현재 손해(손해는 마이너스) 가 크면 문제임
							if(tryStopLoss(tradeResultVo, (minBar.getL().doubleValue() * count) - twentyFive, previousProceeds, minBar.getTFormattedString(), twentyFive * 4)){
								break FOR_1;
							}
						}
					} */
				}
			}else if(strategyVo.getPurchase() == SECOND_PURCHASE){
				if(dayBar.getH().compareTo(strategyVo.getSell2nd()) >= 0){
					List<Map<String, Object>> bars = getBarData(strategyVo.getSymbol(), start, end);
					for(int j = 0; j < bars.size() ; j++){
						bars.get(j).put("symbol", strategyVo.getSymbol());
						BarsVo minBar = new BarsVo(bars.get(j));
						if(strategyVo.getPurchase() == SECOND_PURCHASE){
							if(trySecondTradeJob(tradeResultVo, minBar, strategyVo, money, fifty, previousProceeds)){
								break FOR_1;
							}
						}else if(strategyVo.getPurchase() == THIRD_PURCHASE){
							if(tryThirdTradeJob(tradeResultVo, minBar, strategyVo, money, fifty, previousProceeds)){
								break FOR_1;
							}
						}
					}
				}else if(dayBar.getL().compareTo(strategyVo.getBuy3rd()) <= 0 && strategyVo.getCountBought3rd() == 0){
					List<Map<String, Object>> bars = getBarData(strategyVo.getSymbol(), start, end);
					for(int j = 0; j < bars.size() ; j++){
						bars.get(j).put("symbol", strategyVo.getSymbol());
						BarsVo minBar = new BarsVo(bars.get(j));
						if(strategyVo.getPurchase() == SECOND_PURCHASE){
							if(trySecondTradeJob(tradeResultVo, minBar, strategyVo, money, fifty, previousProceeds)){
								break FOR_1;
							}
						}else if(strategyVo.getPurchase() == THIRD_PURCHASE){
							if(tryThirdTradeJob(tradeResultVo, minBar, strategyVo, money, fifty, previousProceeds)){
								break FOR_1;
							}
						}
					}
				}else{
					/* int count = tradeResultVo.getBuy1stCount() + tradeResultVo.getBuy2ndCount() + tradeResultVo.getBuy3rdCount();
					if(tryStopLoss(tradeResultVo,  (dayBar.getL().doubleValue() * count) - fifty, previousProceeds,dayBar.getT()+":00", fifty * 2)){
						List<Map<String, Object>> bars = getBarData(strategyVo.getSymbol(), start, end);
						count = tradeResultVo.getBuy1stCount() + tradeResultVo.getBuy2ndCount() + tradeResultVo.getBuy3rdCount();
						for(int j = 0; j < bars.size() ; j++){
							bars.get(j).put("symbol", strategyVo.getSymbol());
							BarsVo minBar = new BarsVo(bars.get(j));
							if(tryStopLoss(tradeResultVo , (minBar.getL().doubleValue() * count) - fifty, previousProceeds, minBar.getTFormattedString(), fifty * 2)){
								break FOR_1;
							}
						}
					} */
				}
			}else if(strategyVo.getPurchase() == THIRD_PURCHASE){
				if(dayBar.getH().compareTo(strategyVo.getSell3rd()) >= 0){
					List<Map<String, Object>> bars = getBarData(strategyVo.getSymbol(), start, end);
					for(int j = 0; j < bars.size() ; j++){
						bars.get(j).put("symbol", strategyVo.getSymbol());
						BarsVo minBar = new BarsVo(bars.get(j));
						if(strategyVo.getPurchase() == THIRD_PURCHASE){
							if(tryThirdTradeJob(tradeResultVo, minBar, strategyVo, money, fifty, previousProceeds)){
								break FOR_1;
							}
						}
					}
				}else{
					int count = tradeResultVo.getBuy1stCount() + tradeResultVo.getBuy2ndCount() + tradeResultVo.getBuy3rdCount();
					if(tryStopLoss(tradeResultVo, (dayBar.getL().doubleValue() * count) - money, previousProceeds,dayBar.getT()+":00", money)){
						List<Map<String, Object>> bars = getBarData(strategyVo.getSymbol(), start, end);
						count = tradeResultVo.getBuy1stCount() + tradeResultVo.getBuy2ndCount() + tradeResultVo.getBuy3rdCount();
						for(int j = 0; j < bars.size() ; j++){
							bars.get(j).put("symbol", strategyVo.getSymbol());
							BarsVo minBar = new BarsVo(bars.get(j));
							if(tryStopLoss(tradeResultVo , (minBar.getL().doubleValue() * count) - money, previousProceeds, minBar.getTFormattedString(), money)){
								break FOR_1;
							}
						}
					}
				}
			}
		}
		return tradeResultVo;
	}

	private int getNextTradeItemIndex(List<BuyVo> list, LocalDateTime finalSellDate){
		int index = -1;
		LocalDateTime proximateDateTime = LocalDateTime.now();
		for(int i = 0 ; i < list.size() ; i++){
			BuyVo item = list.get(i);
			if(!item.isUsed()){
				List<BarsVo> bars = buyService.selectTbBarsOneDayListBySymbol(item.getSymbol());
				LOOP:
				for(int j = 0 ; j < bars.size() ; j++){
					BarsVo bar = bars.get(j);
					LocalDateTime datetime = LocalDateTime.parse(bar.getT(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
					LocalDateTime strategyDatetime = LocalDateTime.parse(item.getT(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
					if(datetime.isAfter(strategyDatetime)){
						if(datetime.isAfter(finalSellDate)){
							if(bar.getL().compareTo(item.getBuy1st()) <= 0){
								if(datetime.isBefore(proximateDateTime)){
									index = i;
									proximateDateTime = LocalDateTime.parse(datetime.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
									break LOOP;
								}
							}
						}
					}
				}
			}
		}
		return index;
	}
}

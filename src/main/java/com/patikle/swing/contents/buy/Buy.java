package com.patikle.swing.contents.buy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.patikle.swing.contents.bars.Bars;
import com.patikle.swing.contents.bars.BarsVo;
import com.patikle.swing.contents.code.CodeDao;
import com.patikle.swing.contents.code.CodeVo;
import com.patikle.swing.contents.util.APIRequestService;

@Component
public class Buy {

	private final int BEFORE_PURCHASE = 0; 
	private final int FIRST_PURCHASE = 1;
	private final int SECOND_PURCHASE = 2;
	private final int THIRD_PURCHASE = 3;

	
	@Autowired
	SqlSession sqlSession;

	@Autowired
	APIRequestService apiRequestService;
	
	@Autowired
	Bars bars; 

	@Autowired
	BuyService buyService;
	
	@SuppressWarnings("unchecked")
	private Map<String ,Object> getBarData(String symbol, String start, String end){
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
		return result;
    }

	private void tryFirstBuy(TradeResultVo tradeResultVo, BuyVo base, BarsVo vo){
		if(base.getCountBought1st() == 0){
			// dur = 0;
			// log += vo.getTFormattedString() + ", 1차 매수: ," + base.getBuy1st();
			tradeResultVo.setBuy1st(base.getBuy1st());
			tradeResultVo.setBuy1stDate(vo.getTFormattedString());
			tradeResultVo.setFirstBuyDate(vo.getTFormattedString());
			tradeResultVo.setRound(1);
			base.setCountBought1st(base.getCountBought1st()+1);
			base.setPurchase(FIRST_PURCHASE);
		}
	}

	private void trySecondBuy(TradeResultVo tradeResultVo, BuyVo base, BarsVo vo){
		if(vo.getL().compareTo(base.getBuy2nd()) <= 0){
			if(base.getCountBought2nd() == 0){
				// log += ", " +  vo.getTFormattedString() + ", 2차 매수(동일 분봉): ," + base.getBuy2nd();
				tradeResultVo.setBuy2nd(base.getBuy2nd());
				tradeResultVo.setBuy2ndDate(vo.getTFormattedString());
				tradeResultVo.setRound(2);
				base.setCountBought2nd(base.getCountBought2nd()+1);
				base.setPurchase(SECOND_PURCHASE);
			}
		}
	}
	private void tryThirdBuy(TradeResultVo tradeResultVo, BuyVo base, BarsVo vo){
		if(vo.getL().compareTo(base.getBuy3rd()) <= 0){
			if(base.getCountBought3rd() == 0){
				// log += ", " +  vo.getTFormattedString() + ", 3차 매수(동일 분봉): ," + base.getBuy3rd();
				tradeResultVo.setBuy3rd(base.getBuy3rd());
				tradeResultVo.setBuy3rdDate(vo.getTFormattedString());
				tradeResultVo.setRound(3);
				base.setCountBought3rd(base.getCountBought3rd()+1);
				base.setPurchase(THIRD_PURCHASE);
			}
		}
	}

	private boolean tryFirstSell(TradeResultVo tradeResultVo, BuyVo base, BarsVo vo){
		if(vo.getH().compareTo(base.getSell1st()) >= 0){
			// log += ", " +  vo.getTFormattedString() + ", 1차 매도(동일 분봉): ," + base.getSell1st() + ", 예상수익율: "+ base.getPercent();
			tradeResultVo.setSell1st(base.getSell1st());
			tradeResultVo.setSell1stDate(vo.getTFormattedString());
			tradeResultVo.setFinalSellDate(vo.getTFormattedString());
			tradeResultVo.setBuyAndSellPeriod(base.getPercent());
			tradeResultVo.setSuccess(true);
			return true;
		}
		return false;
	}
	private boolean trySecondSell(TradeResultVo tradeResultVo, BuyVo base, BarsVo vo){
		if(vo.getH().compareTo(base.getSell2nd()) >= 0){
			// double rate = getRate(tradeResultVo);
			// log += ", " + vo.getTFormattedString() + ", 2차 매도(동일 일자): ," + base.getSell2nd() + ", 예상수익율: " + rate;
			tradeResultVo.setSell2nd(base.getSell2nd());
			tradeResultVo.setSell2ndDate(vo.getTFormattedString());
			tradeResultVo.setFinalSellDate(vo.getTFormattedString());
			tradeResultVo.setBuyAndSellPeriod(getRate(tradeResultVo));
			tradeResultVo.setSuccess(true);
			return true;
		}
		return false;

	}

	private void tryThirdSell(TradeResultVo tradeResultVo, BuyVo base, BarsVo vo){
		if(vo.getH().compareTo(base.getSell3rd()) >= 0){
			// double rate = get3rdRate(base);
			// log += ", " + vo.getTFormattedString() + ", 3차 매도(동일 일자): ," + base.getSell3rd();
			tradeResultVo.setSell3rd(base.getSell3rd());
			tradeResultVo.setSell3rdDate(vo.getTFormattedString());
			base.setPurchase(SECOND_PURCHASE); 
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
	public TradeResultVo run(List<BarsVo> list, int baseIndex, BuyVo base){
		TradeResultVo tradeResultVo = new TradeResultVo(base.getSymbol());
				
		String log = "[" + base.getSymbol() +"], ";
		int dur = 0;
		FOR_1:
		for(int i = baseIndex ; i < list.size() ; i++){
			BarsVo bar = list.get(i);
			String start = bar.getT().substring(0, 10) + "T04:00:00Z";
			String end = bar.getT().substring(0, 10) + "T23:59:00Z";

			if(base.getPurchase() == BEFORE_PURCHASE){
				if(bar.getL().compareTo(base.getBuy1st()) <= 0){
					Map<String, Object> map = getBarData(base.getSymbol(), start, end);
					List<Map<String, Object>> bars = (List<Map<String, Object>>)map.get("bars");
					for(int j = 0; j < bars.size() ; j++){
						bars.get(j).put("symbol", base.getSymbol());
						BarsVo vo  = new BarsVo(bars.get(j));
						// 매수 조건 만족
						if(base.getPurchase() == BEFORE_PURCHASE){
							if(vo.getL().compareTo(base.getBuy1st()) <= 0){
								// 매수
								tryFirstBuy(tradeResultVo, base, vo);
								// 1분봉 안에 매수 매도 구간이 겹쳐져 있다.
								if(tryFirstSell(tradeResultVo, base, vo)){
									break FOR_1;
								}
								trySecondBuy(tradeResultVo, base, vo);
								tryThirdBuy(tradeResultVo, base, vo);
							}
						}else{
							if(base.getPurchase() == FIRST_PURCHASE){
								if(tryFirstSell(tradeResultVo, base, vo)){
									break FOR_1;
								}
								trySecondBuy(tradeResultVo, base, vo);
								tryThirdBuy(tradeResultVo, base, vo);

							}else if(base.getPurchase() == SECOND_PURCHASE){
								if(trySecondSell(tradeResultVo, base, vo)){
									break FOR_1;
								}
								tryThirdBuy(tradeResultVo, base, vo);

							}else if(base.getPurchase() == THIRD_PURCHASE){
								tryThirdSell(tradeResultVo, base, vo);
							}
						}
					}
				}
			}else if(base.getPurchase() == FIRST_PURCHASE){
				if(bar.getH().compareTo(base.getSell1st()) >= 0){
					Map<String, Object> map = getBarData(base.getSymbol(), start, end);
					List<Map<String, Object>> bars = (List<Map<String, Object>>)map.get("bars");
					for(int j = 0; j < bars.size() ; j++){
						bars.get(j).put("symbol", base.getSymbol());
						BarsVo vo  = new BarsVo(bars.get(j));
						// 매도 조건 만족
						if(base.getPurchase() == FIRST_PURCHASE){
							if(tryFirstSell(tradeResultVo, base, vo)){
								break FOR_1;
							}
							trySecondBuy(tradeResultVo, base, vo);
							tryThirdBuy(tradeResultVo, base, vo);

						}else if(base.getPurchase() == SECOND_PURCHASE){
							if(trySecondSell(tradeResultVo, base, vo)){
								break FOR_1;
							}
							trySecondBuy(tradeResultVo, base, vo);
							tryThirdBuy(tradeResultVo, base, vo);
						}else if(base.getPurchase() == THIRD_PURCHASE){
							tryThirdSell(tradeResultVo, base, vo);
						}
					}
				}else if(bar.getL().compareTo(base.getBuy2nd()) <= 0 && base.getCountBought2nd() == 0){
					Map<String, Object> map = getBarData(base.getSymbol(), start, end);
					List<Map<String, Object>> bars = (List<Map<String, Object>>)map.get("bars");
					for(int j = 0; j < bars.size() ; j++){
						bars.get(j).put("symbol", base.getSymbol());
						BarsVo vo  = new BarsVo(bars.get(j));
						// 매수 조건 만족
						if(base.getPurchase() == FIRST_PURCHASE){
							trySecondBuy(tradeResultVo, base, vo);
							tryThirdBuy(tradeResultVo, base, vo);

						}else if(base.getPurchase() == SECOND_PURCHASE){
							if(trySecondSell(tradeResultVo, base, vo)){
								break FOR_1;
							}
							tryThirdBuy(tradeResultVo, base, vo);

						}else if(base.getPurchase() == THIRD_PURCHASE){
							tryThirdSell(tradeResultVo, base, vo);
						}
					}
				}
			}else if(base.getPurchase() == SECOND_PURCHASE){
				if(bar.getH().compareTo(base.getSell2nd()) >= 0){
					Map<String, Object> map = getBarData(base.getSymbol(), start, end);
					List<Map<String, Object>> bars = (List<Map<String, Object>>)map.get("bars");
					for(int j = 0; j < bars.size() ; j++){
						bars.get(j).put("symbol", base.getSymbol());
						BarsVo vo  = new BarsVo(bars.get(j));
						if(base.getPurchase() == SECOND_PURCHASE){
							if(trySecondSell(tradeResultVo, base, vo)){
								break FOR_1;
							}
							tryThirdBuy(tradeResultVo, base, vo);
						}else if(base.getPurchase() == THIRD_PURCHASE){
							tryThirdSell(tradeResultVo, base, vo);
						}
					}
				}else if(bar.getL().compareTo(base.getBuy3rd()) <= 0 && base.getCountBought3rd() == 0){
					Map<String, Object> map = getBarData(base.getSymbol(), start, end);
					List<Map<String, Object>> bars = (List<Map<String, Object>>)map.get("bars");
					for(int j = 0; j < bars.size() ; j++){
						bars.get(j).put("symbol", base.getSymbol());
						BarsVo vo  = new BarsVo(bars.get(j));
						// 매수 조건 만족
						if(base.getPurchase() == SECOND_PURCHASE){
							tryThirdBuy(tradeResultVo, base, vo);
						}else if(base.getPurchase() == THIRD_PURCHASE){
							tryThirdSell(tradeResultVo, base, vo);
						}
					}
				}
			}else if(base.getPurchase() == THIRD_PURCHASE){
				if(bar.getH().compareTo(base.getSell3rd()) >= 0){
					Map<String, Object> map = getBarData(base.getSymbol(), start, end);
					List<Map<String, Object>> bars = (List<Map<String, Object>>)map.get("bars");
					for(int j = 0; j < bars.size() ; j++){
						bars.get(j).put("symbol", base.getSymbol());
						BarsVo vo  = new BarsVo(bars.get(j));
						if(base.getPurchase() == SECOND_PURCHASE){
							if(trySecondSell(tradeResultVo, base, vo)){
								break FOR_1;
							}
						}else if(base.getPurchase() == THIRD_PURCHASE){
							tryThirdSell(tradeResultVo, base, vo);
						}
					}
				}
			}
			dur++;
		}
		if(log.indexOf("매도") != -1){
			System.out.println( "매수매도 기간 :" + dur + ","+ log);
		}
		
		return tradeResultVo;
	}

	public void run(){
		List<BuyVo> list = buyService.selectTbBarsOneDayGreenList();
		// System.out.println("전체 개수 : "+ list.size());
		// int count = 0;
		
		List<TradeResultVo> tradeResultVos = new ArrayList<>();

		for(int j = 0 ; j < list.size() ; j++){
			BuyVo item = list.get(j);
			// if(item.getSymbol().equals("AHPI")){
				List<BarsVo> bars = buyService.selectTbBarsOneDayListBySymbol(item.getSymbol());
				int baseIndex = 0;
				BarsVo tMinusOneBar = new BarsVo();
				BarsVo tMinusZeroBar = new BarsVo();
				
				for(int i = 0 ; i < bars.size() ; i++){
					if(bars.get(i).getT().equals(item.getT())){
						baseIndex = i+1;
						tMinusOneBar = bars.get(i-1);
						tMinusZeroBar = bars.get(i);
						break;
					}
				}
				// t-1이 양봉일 경우만실행한다
				if(tMinusOneBar.getO_ha().compareTo(tMinusOneBar.getC_ha()) <= 0){
					// 전략선 설정
					item.setSell2nd(item.getBuy1st());
					item.setBuy2nd(tMinusZeroBar.getO_ha());
					item.setSell3rd(tMinusZeroBar.getO_ha());
					item.setBuy3rd(tMinusOneBar.getO_ha());
					tradeResultVos.add(run(bars, baseIndex, item));
					// count++;
				}
			// }
		}

		System.out.println("종목,성공여부,추가매수횟수,예상수익율,매수일,매도일,1차 매수일,1차 매수금액,1차 매도일,1차 매도금액,2차 매수일,2차 매수금액,2차 매도일,2차 매도금액,3차 매수일,3차 매수금액,3차 매도일,3차 매도금액");
		for(int i = 0 ; i < tradeResultVos.size() ; i++){
			TradeResultVo item = tradeResultVos.get(i);
			StringBuilder sb = new StringBuilder();
			
			sb.append(item.getSymbol()).append(","); // 종목
			sb.append(item.isSuccess()).append(","); // 성공여부
			sb.append(item.getRound()).append(","); // 추가매수 횟수
			sb.append(item.getBuyAndSellPeriod()).append(","); //  예상수익율
			sb.append(item.getFirstBuyDate()).append(","); // 매수일
			sb.append(item.getFinalSellDate()).append(","); // 매도일

			sb.append(item.getBuy1stDate()).append(","); // 1 차매수일
			sb.append(item.getBuy1st()).append(","); // 1차 매수 금액
			sb.append(item.getSell1stDate()).append(","); // 1차 매도일
			sb.append(item.getSell1st()).append(","); // 1차 매도 금액

			sb.append(item.getBuy2ndDate()).append(","); // 2차 매수일
			sb.append(item.getBuy2nd()).append(","); // 2차 매수 금액
			sb.append(item.getSell2ndDate()).append(","); // 2차 매도일
			sb.append(item.getSell2nd()).append(","); // 2차 매도 금액

			sb.append(item.getBuy3rdDate()).append(","); // 3차 매수일
			sb.append(item.getBuy3rd()).append(","); // 3차 매수 금액
			sb.append(item.getSell3rdDate()).append(","); // 3차 매도일
			sb.append(item.getSell3rd()); // 3차 매도 금액

			System.out.println(sb.toString());
		}
		// System.out.println(">>>>>>> 실행 된 전략 수: " + count);
	}
}

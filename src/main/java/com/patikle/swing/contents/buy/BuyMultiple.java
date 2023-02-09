package com.patikle.swing.contents.buy;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.patikle.swing.contents.util.APIRequestService;

@Component
public class BuyMultiple {

	@Autowired
	SqlSession sqlSession;

	@Autowired
	APIRequestService apiRequestService;
	
	@Autowired
	BuyService buyService;

	public void run(){
		List<BuyVo> list = buyService.selectTbBarsOneDayGreenList();
		for(int i = 0 ; i < 5 ; i++){
			Runnable runnable = new TradeRunnable(sqlSession, apiRequestService, buyService, list, "0" + (i+1));
			Thread thread = new Thread(runnable);
			thread.start();
		}
	}
}

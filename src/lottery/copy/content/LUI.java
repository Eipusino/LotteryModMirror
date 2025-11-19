package lottery.copy.content;

import lottery.copy.ui.BeforeLottery;
import lottery.copy.ui.LotteryRes;
import lottery.copy.ui.MdfUI;

public class LUI {
	public static MdfUI mdfUI;
	public static BeforeLottery beforeLottery;
	public static LotteryRes lotteryRes;

	public static void init() {
		mdfUI = new MdfUI();
		beforeLottery = new BeforeLottery();
		lotteryRes = new LotteryRes();
	}
}

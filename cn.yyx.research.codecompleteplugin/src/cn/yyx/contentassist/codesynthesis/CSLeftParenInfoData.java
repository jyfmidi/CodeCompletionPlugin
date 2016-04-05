package cn.yyx.contentassist.codesynthesis;

import cn.yyx.contentassist.codepredict.Sentence;
import cn.yyx.contentassist.codesynthesis.flowline.CSFlowLineData;
import cn.yyx.contentassist.commonutils.SynthesisHandler;

public class CSLeftParenInfoData extends CSFlowLineData{
	
	private int times = -1;
	private int usedtimes = -1;
	
	public CSLeftParenInfoData(int times, Integer id, Sentence sete, String data, Integer structsignal, Class<?> dcls,
			boolean hashole, SynthesisHandler handler) {
		super(id, sete, data, structsignal, dcls, hashole, handler);
		this.setTimes(times);
	}

	public int getTimes() {
		return times;
	}

	public void setTimes(int times) {
		this.times = times;
	}

	public int getUsedtimes() {
		return usedtimes;
	}

	public void decreaseUsedtimes(int usedtimes) {
		this.usedtimes -= usedtimes;
	}
	
}
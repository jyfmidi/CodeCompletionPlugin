package cn.yyx.contentassist.codesynthesis.data;

import java.util.Stack;

import cn.yyx.contentassist.codepredict.CodeSynthesisException;
import cn.yyx.contentassist.codesynthesis.flowline.FlowLineNode;
import cn.yyx.contentassist.commonutils.ComplicatedSignal;

public class CSRightParenInfoData extends CSFlowLineData{
	
	private int times = 0;
	private FlowLineNode<CSFlowLineData> mostleft = null;
	private int mostleftremain = 0;
	
	public CSRightParenInfoData(int times, CSFlowLineData dt) {
		super(dt.getId(), dt.getSete(), dt.getData(), dt.getDcls(), dt.isHaspre(), dt.isHashole(), dt.getPretck(), dt.getPosttck(), dt.getHandler());
		this.setTimes(times);
	}

	public int getTimes() {
		return times;
	}

	public void setTimes(int times) {
		this.times = times;
	}

	public int getMostleftremain() {
		return mostleftremain;
	}

	public void setMostleftremain(int mostleftremain) {
		this.mostleftremain = mostleftremain;
	}

	public FlowLineNode<CSFlowLineData> getMostleft() {
		return mostleft;
	}

	public void setMostleft(FlowLineNode<CSFlowLineData> mostleft) {
		this.mostleft = mostleft;
	}
	
	@Override
	public void HandleStackSignal(Stack<Integer> signals) throws CodeSynthesisException{
		signals.push(ComplicatedSignal.GenerateComplicatedSignal(DataStructureSignalMetaInfo.ParentheseBlock, times));
	}
	
}
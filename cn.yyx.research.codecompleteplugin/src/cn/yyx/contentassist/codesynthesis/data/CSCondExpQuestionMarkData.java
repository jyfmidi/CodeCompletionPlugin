package cn.yyx.contentassist.codesynthesis.data;

import java.util.Stack;

import cn.yyx.contentassist.codepredict.CodeSynthesisException;
import cn.yyx.contentassist.codepredict.Sentence;
import cn.yyx.contentassist.codesynthesis.typeutil.CCType;
import cn.yyx.contentassist.codesynthesis.typeutil.TypeComputationKind;
import cn.yyx.contentassist.commonutils.SynthesisHandler;

public class CSCondExpQuestionMarkData extends CSFlowLineData{

	public CSCondExpQuestionMarkData(Integer id, Sentence sete, String data, CCType dcls, boolean haspre,
			boolean hashole, TypeComputationKind pretck, TypeComputationKind posttck, SynthesisHandler handler) {
		super(id, sete, data, dcls, haspre, hashole, pretck, posttck, handler);
	}
	
	@Override
	public void HandleStackSignal(Stack<Integer> signals) throws CodeSynthesisException {
		super.HandleStackSignal(signals);
		Integer sl = signals.peek();
		if (sl == null || sl != DataStructureSignalMetaInfo.ConditionExpressionColon)
		{
			throw new CodeSynthesisException("cond colon mark does not have common for prefixed.");
		}
		signals.pop();
		signals.push(DataStructureSignalMetaInfo.ConditionExpressionQuestion);
	}
	
}
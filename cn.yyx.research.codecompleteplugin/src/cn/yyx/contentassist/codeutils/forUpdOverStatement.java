package cn.yyx.contentassist.codeutils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import cn.yyx.contentassist.codepredict.CodeSynthesisException;
import cn.yyx.contentassist.codesynthesis.CSFlowLineBackTraceGenerationHelper;
import cn.yyx.contentassist.codesynthesis.CSFlowLineHelper;
import cn.yyx.contentassist.codesynthesis.CSFlowLineQueue;
import cn.yyx.contentassist.codesynthesis.data.CSFlowLineData;
import cn.yyx.contentassist.codesynthesis.data.CSForUpdOverProperty;
import cn.yyx.contentassist.codesynthesis.flowline.FlowLineNode;
import cn.yyx.contentassist.codesynthesis.statementhandler.CSStatementHandler;

public class forUpdOverStatement extends rawForUpdOverStatement {
	
	statement smt = null;
	
	public forUpdOverStatement(statement smt, String smtcode) {
		super(smtcode);
		this.smt = smt;
	}

	@Override
	public boolean CouldThoughtSame(OneCode t) {
		if (t instanceof forUpdOverStatement)
		{
			return smt.CouldThoughtSame(((forUpdOverStatement)t).smt);
		}
		if (t instanceof rawForUpdOverStatement)
		{
			return true;
		}
		if (t instanceof statement)
		{
			return smt.CouldThoughtSame(t);
		}
		return false;
	}

	@Override
	public double Similarity(OneCode t) {
		if (t instanceof forUpdOverStatement)
		{
			return smt.Similarity(((forUpdOverStatement)t).smt);
		}
		if (t instanceof rawForUpdOverStatement)
		{
			return 0.5;
		}
		if (t instanceof statement)
		{
			return smt.Similarity(t);
		}
		return 0;
	}

	/*
	 * @Override public boolean HandleOverSignal(Stack<Integer> cstack) { int
	 * signal = cstack.peek(); if (signal !=
	 * StructureSignalMetaInfo.CommonForExpWaitingOver) { return true; //
	 * System.err.println("What the fuck, pre is not for?"); // new
	 * Exception().printStackTrace(); // System.exit(1); } else { cstack.pop();
	 * } return false; }
	 * 
	 * @Override public boolean HandleCodeSynthesis(CodeSynthesisQueue squeue,
	 * Stack<TypeCheck> expected, SynthesisHandler handler, CSNode result,
	 * AdditionalInfo ai) { CSNode cs = new
	 * CSNode(CSNodeType.HalfFullExpression); cs.AddOneData("", null);
	 * squeue.add(cs); return false; }
	 */

	@Override
	public List<FlowLineNode<CSFlowLineData>> HandleCodeSynthesis(CSFlowLineQueue squeue, CSStatementHandler smthandler)
			throws CodeSynthesisException {
		List<FlowLineNode<CSFlowLineData>> result = new LinkedList<FlowLineNode<CSFlowLineData>>();
		List<FlowLineNode<CSFlowLineData>> smtres = new LinkedList<FlowLineNode<CSFlowLineData>>();
		List<FlowLineNode<CSFlowLineData>> smtls = smt.HandleCodeSynthesis(squeue, smthandler);
		smtls = CSFlowLineHelper.ConcateOneFlowLineList(null, smtls, ") {\n}");
		Iterator<FlowLineNode<CSFlowLineData>> smtitr = smtls.iterator();
		while (smtitr.hasNext())
		{
			FlowLineNode<CSFlowLineData> smtln = smtitr.next();
			CSFlowLineData smtdata = smtln.getData();
			smtdata.setCsep(CSForUpdOverProperty.GetInstance());
		}
		Iterator<FlowLineNode<CSFlowLineData>> ritr = smtres.iterator();
		while (ritr.hasNext())
		{
			FlowLineNode<CSFlowLineData> fln = ritr.next();
			result.addAll(CSFlowLineBackTraceGenerationHelper.GenerateNotYetAddedSynthesisCode(squeue, smthandler, fln, null));
		}
		return result;
	}
	
}
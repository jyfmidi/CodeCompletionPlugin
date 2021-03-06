package cn.yyx.contentassist.codesynthesis.typeutil.computations;

import cn.yyx.contentassist.codesynthesis.typeutil.CCType;
import cn.yyx.contentassist.codesynthesis.typeutil.InferredCCType;
import cn.yyx.contentassist.codesynthesis.typeutil.SameTypeConflictException;
import cn.yyx.contentassist.codesynthesis.typeutil.TypeComputer;
import cn.yyx.contentassist.codesynthesis.typeutil.TypeConflictException;

public class BooleanRTwoSideSame extends TypeComputationKind {
	
	@Override
	public void HandlePre(CCType pre) throws TypeConflictException {
		this.setPre(pre);
	}
	
	@Override
	public void HandlePost(CCType post) throws TypeConflictException {
		this.setPost(post);
	}
	
	@Override
	public boolean HandleOver() throws TypeConflictException {
		if (getPre() != null && getPost() != null)
		{
			return true;
		}
		return false;
	}
	
	@Override
	public CCType HandleResult() throws TypeConflictException {
		if (getPre() instanceof InferredCCType || getPost() instanceof InferredCCType)
		{
			return new InferredCCType();
		}
		if (TypeComputer.CCTypeSame(getPre(), getPost()))
		{
			return new CCType(boolean.class, "boolean");
		}
		if (TypeComputer.IsStrictNumberBit(getPre().getCls()) && TypeComputer.IsStrictNumberBit(getPost().getCls()))
		{
			return new CCType(boolean.class, "boolean");
		}
		throw new SameTypeConflictException("pre:" + pre + " and post:" + post + " are not same in BooleanRTwoSideSame.", post);
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		BooleanRTwoSideSame tcmp = new BooleanRTwoSideSame();
		CCType postc = post == null ? null : (CCType) post.clone();
		CCType prec = pre == null ? null : (CCType) pre.clone();
		tcmp.setPost(postc);
		tcmp.setPre(prec);
		return tcmp;
	}
	
}
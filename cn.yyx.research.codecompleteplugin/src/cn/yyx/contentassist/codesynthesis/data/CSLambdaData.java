package cn.yyx.contentassist.codesynthesis.data;

import java.util.List;

import cn.yyx.contentassist.codepredict.Sentence;
import cn.yyx.contentassist.codesynthesis.typeutil.CCType;
import cn.yyx.contentassist.codesynthesis.typeutil.computations.TypeComputationKind;
import cn.yyx.contentassist.commonutils.SynthesisHandler;

public class CSLambdaData extends CSFlowLineData {
	
	private List<String> declares = null;
	
	public CSLambdaData(Integer id, Sentence sete, String data, CCType dcls, TypeComputationKind tck, SynthesisHandler handler) {
		super(id, sete, data, dcls, tck, handler);
	}
	
	public CSLambdaData(List<String> declares, CSFlowLineData fld) {
		super(fld.getId(), fld.getSete(), fld.getData(), fld.getDcls(), fld.getTck(), fld.getHandler());
		if (fld.isHashole())
		{
			this.setHashole(true);
		}
		this.setDeclares(declares);
		this.setCsep(fld.getCsep());
		this.setCsep(new CSLambdaProperty(null));
		this.setScm(fld.getSynthesisCodeManager());
		this.setExtraData(fld.getExtraData());
	}

	public List<String> getDeclares() {
		return declares;
	}

	public void setDeclares(List<String> declares) {
		this.declares = declares;
	}
	
}
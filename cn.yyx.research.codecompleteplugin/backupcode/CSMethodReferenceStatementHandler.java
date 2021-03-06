package cn.yyx.contentassist.codesynthesis.statementhandler;

public class CSMethodReferenceStatementHandler extends CSStatementHandler{
	
	private String field = null;
	
	private boolean fieldused = false;
	
	public CSMethodReferenceStatementHandler(String field, CSStatementHandler csh) {
		super(csh.getSete(), csh.getProb(), csh.getAoi());
		this.setField(field);
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public boolean isFieldused() {
		return fieldused;
	}

	public void setFieldused(boolean fieldused) {
		this.fieldused = fieldused;
	}
	
}
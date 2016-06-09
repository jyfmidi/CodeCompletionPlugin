package cn.yyx.contentassist.codecompletion;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import cn.yyx.contentassist.codepredict.preference.PreferenceManager;

public class IpPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	public IpPreferencePage() {
		super(GRID);
		setPreferenceStore(PreferenceManager.GetPreference());
		setDescription("IpPreferencePage viable.");
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected void createFieldEditors() {
		StringFieldEditor sfe = new StringFieldEditor(PreferenceManager.IPPreference, "AeroSpike IP:", getFieldEditorParent());
		String sval = getPreferenceStore().getString(PreferenceManager.IPPreference);
		System.out.println("sval:" + sval);
		sfe.setStringValue(sval);
		addField(sfe);
	}
	
}
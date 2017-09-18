package info.iconmaster.tnclipse.preferences;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import info.iconmaster.tnclipse.TnClipse;

public class PreferencePageEditorColors extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public PreferencePageEditorColors() {
		super(GRID);
		setPreferenceStore(TnClipse.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		addField(new ColorFieldEditor("editor.color.default", "Normal text", getFieldEditorParent()));
		addField(new ColorFieldEditor("editor.color.number", "Numbers", getFieldEditorParent()));
		addField(new ColorFieldEditor("editor.color.annotation", "Annotations", getFieldEditorParent()));
		addField(new ColorFieldEditor("editor.color.comment", "Comments", getFieldEditorParent()));
		addField(new ColorFieldEditor("editor.color.doc_comment", "Doc comments", getFieldEditorParent()));
		addField(new ColorFieldEditor("editor.color.string", "Strings", getFieldEditorParent()));
		addField(new ColorFieldEditor("editor.color.char", "Chars", getFieldEditorParent()));
		addField(new ColorFieldEditor("editor.color.keyword", "Keywords", getFieldEditorParent()));
		
		initialize();
	}
	
	@Override
	public void init(IWorkbench workbench) {
		// we don't really need to do anything on init.
	}
}

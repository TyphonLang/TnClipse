package info.iconmaster.tnclipse.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;

import info.iconmaster.tnclipse.TyphonPlugin;

public class TyphonPreferences extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = TyphonPlugin.getDefault().getPreferenceStore();
		
		store.setDefault("editor.color.default", StringConverter.asString(new RGB(0,0,0)));
		store.setDefault("editor.color.number", StringConverter.asString(new RGB(128,128,0)));
		store.setDefault("editor.color.annotation", StringConverter.asString(new RGB(0,128,0)));
		store.setDefault("editor.color.comment", StringConverter.asString(new RGB(128,128,128)));
		store.setDefault("editor.color.doc_comment", StringConverter.asString(new RGB(128,128,128)));
		store.setDefault("editor.color.string", StringConverter.asString(new RGB(255,128,0)));
		store.setDefault("editor.color.char", StringConverter.asString(new RGB(255,128,0)));
		store.setDefault("editor.color.keyword", StringConverter.asString(new RGB(0,0,128)));
	}

}

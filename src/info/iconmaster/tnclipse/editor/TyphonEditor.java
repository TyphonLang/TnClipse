package info.iconmaster.tnclipse.editor;

import org.eclipse.ui.editors.text.TextEditor;

public class TyphonEditor extends TextEditor {
	public TyphonEditor() {
		super();
		setSourceViewerConfiguration(new TyphonConfiguration());
		setDocumentProvider(new TyphonDocumentProvider());
	}
}

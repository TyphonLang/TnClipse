package info.iconmaster.tnclipse.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class TyphonEditor extends TextEditor {
	private TyphonOutlinePage outline;
	
	public TyphonEditor() {
		super();
		setSourceViewerConfiguration(new TyphonConfiguration());
		setDocumentProvider(new TyphonDocumentProvider());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (IContentOutlinePage.class.equals(adapter)) {
			if (outline == null) {
				outline = new TyphonOutlinePage(this, getEditorInput());
			}
			return (T) outline;
		} else {
			return super.getAdapter(adapter);
		}
	}
	
	@Override
	public void dispose() {
		if (outline != null)
			outline.setInput(null);
		super.dispose();
	}
	
	@Override
	public void doRevertToSaved() {
		super.doRevertToSaved();
		if (outline != null)
			outline.update();
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		super.doSave(monitor);
		if (outline != null)
			outline.update();
	}
	
	@Override
	public void doSaveAs() {
		super.doSaveAs();
		if (outline != null)
			outline.update();
	}
	
	@Override
	public void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		if (outline != null)
			outline.setInput(input);
	}
}

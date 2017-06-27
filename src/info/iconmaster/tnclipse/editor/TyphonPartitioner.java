package info.iconmaster.tnclipse.editor;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TypedRegion;

public class TyphonPartitioner implements IDocumentPartitioner {
	IDocument doc;

	@Override
	public void connect(IDocument document) {
		doc = document;
	}

	@Override
	public void disconnect() {
		
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
		
	}

	@Override
	public boolean documentChanged(DocumentEvent event) {
		return false;
	}

	@Override
	public String[] getLegalContentTypes() {
		return new String[] {IDocument.DEFAULT_CONTENT_TYPE};
	}

	@Override
	public String getContentType(int offset) {
		return IDocument.DEFAULT_CONTENT_TYPE;
	}

	@Override
	public ITypedRegion[] computePartitioning(int offset, int length) {
		return new ITypedRegion[] {new TypedRegion(0, doc.getLength(), IDocument.DEFAULT_CONTENT_TYPE)};
	}

	@Override
	public ITypedRegion getPartition(int offset) {
		return new TypedRegion(0, doc.getLength(), IDocument.DEFAULT_CONTENT_TYPE);
	}
}
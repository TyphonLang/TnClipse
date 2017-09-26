package info.iconmaster.tnclipse.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import info.iconmaster.tnclipse.nature.TyphonBuilder;
import info.iconmaster.typhon.model.Field;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.Package;
import info.iconmaster.typhon.model.TyphonModelEntity;
import info.iconmaster.typhon.types.Type;

public class TyphonContentAssist implements IContentAssistProcessor {
	private Package getPackage(ITextViewer viewer) {
		Package p = null;
		
		if (viewer.getDocument() != null) {
			// get the version created from the Typhon builder, if possible
			ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
			ITextFileBuffer buffer = bufferManager.getTextFileBuffer(viewer.getDocument());
			if (buffer != null) {
				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(buffer.getLocation());
				try {
					p = (Package) file.getSessionProperty(TyphonBuilder.STORAGE_COMPILED_PACKAGE);
				} catch (CoreException e) {
					// ignore, fall back
				}
			}
		}
		
		return p == null ? null : p.getParent();
	}
	
	private List<String> getContentAssist(List<String> a, TyphonModelEntity e) {
		if (e instanceof Package) {
			if (((Package) e).getName() != null) {
				a.add(((Package) e).getName());
			}
			for (Package p : ((Package) e).getSubpackges()) {
				getContentAssist(a, p);
			}
			for (Type t : ((Package) e).getTypes()) {
				getContentAssist(a, t);
			}
			for (Function f : ((Package) e).getFunctions()) {
				getContentAssist(a, f);
			}
			for (Field f : ((Package) e).getFields()) {
				getContentAssist(a, f);
			}
		} else if (e instanceof Type) {
			if (((Type) e).getName() != null) {
				a.add(((Type) e).getName());
			}
			getContentAssist(a, ((Type) e).getTypePackage());
		} else if (e instanceof Function) {
			a.add(((Function) e).getName());
		}
		
		return a;
	}
	
	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		List<String> options = getContentAssist(new ArrayList<>(), getPackage(viewer));
		
		return options.stream().map((str)->new CompletionProposal(str, offset, 0, str.length())).toArray((l)->new ICompletionProposal[l]);
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

}

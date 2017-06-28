package info.iconmaster.tnclipse.nature;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.compiler.TyphonSourceReader;
import info.iconmaster.typhon.errors.TyphonError;

public class TyphonBuilder extends IncrementalProjectBuilder {

	/**
	 * ID of this builder.
	 */
	public static final String ID = "info.iconmaster.tnclipse.typhon";

	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	private void fullBuild(final IProgressMonitor monitor) throws CoreException {
		try {
			getProject().accept(new IResourceVisitor() {
				@Override
				public boolean visit(IResource resource) throws CoreException {
					if (resource instanceof IFile)
					buildResource((IFile)resource);
					return true;
				}
			});
		} catch (CoreException e) {
		}
	}

	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		delta.accept(new IResourceDeltaVisitor() {
			@Override
			public boolean visit(IResourceDelta delta) throws CoreException {
				IResource resource = delta.getResource();
				if (resource instanceof IFile && delta.getKind() != IResourceDelta.REMOVED) {
					buildResource((IFile)resource);
				}
				return true;
			}
		});
	}
	
	private void buildResource(IFile file) {
		if ("tn".equals(file.getFileExtension())) {
			try {
				file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
				
				TyphonInput tni = new TyphonInput();
				TyphonSourceReader.parseFile(tni, new File(file.getLocationURI()));
				
				for (TyphonError error : tni.errors) {
					IMarker marker = file.createMarker(IMarker.PROBLEM);
					marker.setAttribute(IMarker.MESSAGE, error.getMessage());
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
					marker.setAttribute(IMarker.CHAR_START, error.source.begin);
					marker.setAttribute(IMarker.CHAR_END, error.source.end+1);
					marker.setAttribute(IMarker.LOCATION, error.source.begin+"-"+error.source.end);
				}
			} catch (IOException e) {
				// ignore; if we can't read it, we can't parse it
			} catch (CoreException e) {
				// TODO
			}
		}
	}
}

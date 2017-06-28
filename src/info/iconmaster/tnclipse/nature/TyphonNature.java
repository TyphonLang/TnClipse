package info.iconmaster.tnclipse.nature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class TyphonNature implements IProjectNature {
	private IProject project;
	
	/**
	 * ID of this nature.
	 */
	public static final String ID = "info.iconmaster.tnclipse.typhon";

	@Override
	public void configure() throws CoreException {
		
	}

	@Override
	public void deconfigure() throws CoreException {
		
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}
}

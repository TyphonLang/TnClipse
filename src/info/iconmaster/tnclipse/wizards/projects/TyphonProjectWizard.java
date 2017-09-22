package info.iconmaster.tnclipse.wizards.projects;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import info.iconmaster.tnclipse.nature.TyphonBuilder;
import info.iconmaster.tnclipse.nature.TyphonNature;

public class TyphonProjectWizard extends Wizard implements INewWizard {
	public static final String ID = "info.iconmaster.tnclipse.project.typhon";
	
	private TyphonProjectWizardPage page;
	private ISelection selection;

	public TyphonProjectWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		page = new TyphonProjectWizardPage(selection);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		String name = new String(page.getName());
		String location = new String(page.getLocation());
		
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				ICoreRunnable myRunnable = new ICoreRunnable() {
					public void run(IProgressMonitor unused) throws CoreException {
						actuallyPerformFinish(name, location, monitor);
					}
				};
				
				try {
					ResourcesPlugin.getWorkspace().run(myRunnable, null);
				} catch (CoreException e) {
					showError(e);
					monitor.done();
				}
			}
		};
		
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			showError(e.getTargetException());
			return false;
		}
		
		return true;
	}

	private void actuallyPerformFinish(String name, String location, IProgressMonitor monitor) {
		monitor.beginTask("Creating project " + name, 2);
		
		try {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
			IProjectDescription desc = ResourcesPlugin.getWorkspace().newProjectDescription(name);
			
			desc.setLocationURI(Paths.get(location).toUri());
			desc.setNatureIds(new String[] {TyphonNature.ID});
			ICommand builder = desc.newCommand();
			builder.setBuilderName(TyphonBuilder.ID);
			desc.setBuildSpec(new ICommand[] {builder});
			
			project.create(desc, new NullProgressMonitor() {
				@Override
				public void done() {
					try {
						monitor.worked(1);

						// TODO: add stuff like folders, files, etc.

						project.open(new NullProgressMonitor() {
							@Override
							public void done() {
								monitor.done();
							}
						});
					} catch (CoreException e) {
						showError(e);
						monitor.done();
					}
				}
			});
		} catch (CoreException e) {
			showError(e);
			monitor.done();
		}
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	private void showError(Throwable e) {
		e.printStackTrace();
		// TODO: we need to call this on the GUI thread?
		// MessageDialog.openError(getShell(), "Error", e.getMessage());
	}
}

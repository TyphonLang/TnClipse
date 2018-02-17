package info.iconmaster.tnclipse.launch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import info.iconmaster.tnbox.model.TnBoxEnvironment;
import info.iconmaster.tnbox.model.TnBoxThread;
import info.iconmaster.tnclipse.TnClipse;
import info.iconmaster.tnclipse.TyphonIcons;
import info.iconmaster.tnclipse.launch.TnBoxProcessFactory.TnBoxProcess;
import info.iconmaster.tnclipse.nature.TyphonBuilder;
import info.iconmaster.tnclipse.nature.TyphonNature;
import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.Package;

public class TnBoxLauncher implements ILaunchConfigurationDelegate, ILaunchShortcut, ILaunchShortcut2 {
	public static final String ID = "info.iconmaster.tnclipse.tnbox";
	
	public static final String LAUNCH_CONFIG_PROJECT = "info.iconmaster.tnclipse.tnbox.project";
	public static final String LAUNCH_CONFIG_MAIN_FUNC = "info.iconmaster.tnclipse.tnbox.mainFunc";
	
	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) monitor = new NullProgressMonitor();
		
		// launch a new process
		TnBoxProcess process = DebugPlugin.newProcess(launch, null, configuration.getAttribute(LAUNCH_CONFIG_PROJECT, "")).getAdapter(TnBoxProcess.class);
		if (process == null) {
			// ERROR
			throw new CoreException(new Status(Status.ERROR, TnClipse.ID, "Process factory not configured correctly in the launch configuration"));
		}
		
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(configuration.getAttribute(TnBoxLauncher.LAUNCH_CONFIG_PROJECT, ""));
		
		TyphonInput tni = (TyphonInput) project.getSessionProperty(TyphonBuilder.STORAGE_TNI);
		if (tni == null) {
			project.build(IncrementalProjectBuilder.FULL_BUILD, null);
			tni = (TyphonInput) project.getSessionProperty(TyphonBuilder.STORAGE_TNI);
		}
		
		List<Function> fs = TyphonBuilder.fromIdentifierString(tni, configuration.getAttribute(TnBoxLauncher.LAUNCH_CONFIG_MAIN_FUNC, ""));
		Function f = fs.get(0);
		
		TnBoxThread thread = new TnBoxThread(new TnBoxEnvironment(tni), f, new HashMap<>());
		process.run(thread, true);
	}
	
	@Override
	public void launch(ISelection selection, String mode) {
		launch(getLaunchableResource(selection), getLaunchConfigurations(selection), mode);
	}
	
	@Override
	public void launch(IEditorPart editor, String mode) {
		launch(getLaunchableResource(editor), getLaunchConfigurations(editor), mode);
	}
	
	private void launch(IResource resource, ILaunchConfiguration[] configs, String mode) {
		if (configs == null) {
			// TODO: ERROR
			System.out.println("This isn't a Typhon project!");
			return;
		}
		
		if (configs.length == 0) {
			final ILaunchConfiguration[] selConfig = new ILaunchConfiguration[1];
			
			PlatformUI.getWorkbench().getDisplay().syncExec(()->{
				try {
					if (resource == null) {
						return;
					}
					
					IProject project = resource.getAdapter(IProject.class);
					if (project == null) {
						return;
					}
					
					ElementListSelectionDialog dialog = new ElementListSelectionDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), new LabelProvider() {
						@Override
						public String getText(Object element) {
							return TyphonBuilder.toIdentifierString((Function) element);
						}
						
						@Override
						public Image getImage(Object element) {
							return TyphonIcons.ICON_FUNC;
						}
					});
					
					dialog.setTitle("Select Main Function");
					dialog.setMessage("Select the main function to run via TnBox.");
					
					if (project.getSessionProperty(TyphonBuilder.STORAGE_TNI) == null) {
						project.build(IncrementalProjectBuilder.FULL_BUILD, null);
					}
					
					List<Package> ps = TyphonBuilder.getPackagesInProject(project);
					if (ps != null) {
						dialog.setElements(TyphonBuilder.getMainFunctions(ps).toArray());
					}
					
					if (dialog.open() == Window.OK) {
						String fid = TyphonBuilder.toIdentifierString((Function) dialog.getFirstResult());
						String configName = DebugPlugin.getDefault().getLaunchManager().generateLaunchConfigurationName(project.getName());
						
						ILaunchConfiguration config = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(ID).newInstance(null, configName);
						ILaunchConfigurationWorkingCopy workingCopy = config.getWorkingCopy();
						
						setupLaunchConfig(workingCopy);
						workingCopy.setAttribute(TnBoxLauncher.LAUNCH_CONFIG_PROJECT, project.getName());
						workingCopy.setAttribute(TnBoxLauncher.LAUNCH_CONFIG_MAIN_FUNC, fid);
						
						config = workingCopy.doSave();
						
						selConfig[0] = config;
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
			});
			
			if (selConfig[0] == null) return;
			configs = selConfig;
		}
		
		if (configs.length > 1) {
			final ILaunchConfiguration[] finalConfigs = configs;
			final ILaunchConfiguration[] selConfig = new ILaunchConfiguration[1];
			
			PlatformUI.getWorkbench().getDisplay().syncExec(()->{
				ElementListSelectionDialog dialog = new ElementListSelectionDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), new LabelProvider() {
					@Override
					public String getText(Object element) {
						return ((ILaunchConfiguration) element).getName();
					}
					
					@Override
					public Image getImage(Object element) {
						return TyphonIcons.ICON_LOGO;
					}
				});
				dialog.setTitle("Select Configuration"); 
				dialog.setMessage("Select a launch configuration to run."); 
				dialog.setElements(finalConfigs);
				
				if (dialog.open() == Window.OK) {
					selConfig[0] = (ILaunchConfiguration) dialog.getFirstResult();
				}
			});
			
			if (selConfig[0] == null) return;
			configs = selConfig;
		}
		
		try {
			DebugUITools.buildAndLaunch(configs[0], mode, new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object r = ((IStructuredSelection)selection).getFirstElement();
			if (r instanceof IResource) {
				return getLaunchConfigurations((IResource) r);
			}
		}
		
		return null;
	}
	
	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editorpart) {
		return getLaunchConfigurations((IResource) editorpart.getEditorInput().getAdapter(IResource.class));
	}
	
	@Override
	public IResource getLaunchableResource(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object r = ((IStructuredSelection)selection).getFirstElement();
			if (r instanceof IResource) {
				return (IResource) r;
			}
		}
		
		return null;
	}
	
	@Override
	public IResource getLaunchableResource(IEditorPart editorpart) {
		return (IResource) editorpart.getEditorInput().getAdapter(IResource.class);
	}
	
	private ILaunchConfiguration[] getLaunchConfigurations(IResource r) {
		try {
			if (r == null) {
				return null;
			}
			
			if (!r.getProject().hasNature(TyphonNature.ID)) {
				return null;
			}
			
			List<ILaunchConfiguration> applicable = new ArrayList<>();
			
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			for (ILaunchConfiguration config : manager.getLaunchConfigurations(manager.getLaunchConfigurationType(ID))) {
				if (config.getAttribute(LAUNCH_CONFIG_PROJECT, "").equals(r.getProject().getName())) {
					applicable.add(config);
				}
			}
			
			return applicable.toArray(new ILaunchConfiguration[0]);
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void setupLaunchConfig(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, TnBoxProcessFactory.ID);
		
		configuration.setAttribute(TnBoxLauncher.LAUNCH_CONFIG_PROJECT, "");
		configuration.setAttribute(TnBoxLauncher.LAUNCH_CONFIG_MAIN_FUNC, "");
	}
}

package info.iconmaster.tnclipse.launch;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.ide.IDE;

import info.iconmaster.tnclipse.TyphonIcons;
import info.iconmaster.tnclipse.nature.TyphonBuilder;
import info.iconmaster.tnclipse.nature.TyphonNature;
import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.Package;

public class TnBoxLauncherTabs extends AbstractLaunchConfigurationTabGroup {
	public static class MainTab extends AbstractLaunchConfigurationTab {
		private Text projectText;
		private Text mainFuncText;
		
		private Button mainFuncButton;
		
		@Override
		public void createControl(Composite parent) {
			Composite container = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			container.setLayout(layout);
			layout.numColumns = 3;
			layout.verticalSpacing = 9;
			
			Label projectLabel = new Label(container, SWT.NULL);
			projectLabel.setText("Project:");
			
			projectText = new Text(container, SWT.BORDER | SWT.SINGLE);
			GridData projectGD = new GridData(GridData.FILL_HORIZONTAL);
			projectText.setLayoutData(projectGD);
			projectText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					dialogChanged();
				}
			});
			
			Button projectButton = new Button(container, SWT.PUSH);
			projectButton.setText("Browse...");
			projectButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider() {
						@Override
						public String getText(Object element) {
							return ((IProject) element).getName();
						}
						
						@Override
						public Image getImage(Object element) {
							return PlatformUI.getWorkbench().getSharedImages().getImage(IDE.SharedImages.IMG_OBJ_PROJECT);
						}
					});
					dialog.setTitle("Select Project"); 
					dialog.setMessage("Select a project to run via TnBox."); 
					dialog.setElements(ResourcesPlugin.getWorkspace().getRoot().getProjects());
					
					if (dialog.open() == Window.OK) {
						IProject result = (IProject) dialog.getFirstResult();
						projectText.setText(result.getName());
						dialogChanged();
					}
				}
			});
			
			Label mainFuncLabel = new Label(container, SWT.NULL);
			mainFuncLabel.setText("Main function:");
			
			mainFuncText = new Text(container, SWT.BORDER | SWT.SINGLE);
			GridData mainFuncGD = new GridData(GridData.FILL_HORIZONTAL);
			mainFuncText.setLayoutData(mainFuncGD);
			mainFuncText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					dialogChanged();
				}
			});
			
			mainFuncButton = new Button(container, SWT.PUSH);
			mainFuncButton.setText("Browse...");
			mainFuncButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider() {
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
					
					if (new Path("").isValidSegment(projectText.getText())) {
						IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectText.getText());
						if (project.isAccessible()) {
							List<Package> ps = TyphonBuilder.getPackagesInProject(project);
							if (ps != null) {
								dialog.setElements(TyphonBuilder.getMainFunctions(ps).toArray());
							}
						}
					}
					
					if (dialog.open() == Window.OK) {
						mainFuncText.setText(TyphonBuilder.toIdentifierString((Function) dialog.getFirstResult()));
						dialogChanged();
					}
				}
			});
			
			dialogChanged();
			setControl(container);
		}
		
		@Override
		public boolean isValid(ILaunchConfiguration launchConfig) {
			try {
				// check if the project is valid
				if (!new Path("").isValidSegment(launchConfig.getAttribute(TnBoxLauncher.LAUNCH_CONFIG_PROJECT, ""))) {
					return false;
				}
				
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(launchConfig.getAttribute(TnBoxLauncher.LAUNCH_CONFIG_PROJECT, ""));
				
				if (!project.isAccessible()) {
					return false;
				}
				
				if (!project.hasNature(TyphonNature.ID)) {
					return false;
				}
				
				// check if the main function is valid
				TyphonInput tni = (TyphonInput) project.getSessionProperty(TyphonBuilder.STORAGE_TNI);
				if (tni == null) {
					project.build(IncrementalProjectBuilder.FULL_BUILD, null);
					tni = (TyphonInput) project.getSessionProperty(TyphonBuilder.STORAGE_TNI);
				}
				
				List<Function> fs = TyphonBuilder.fromIdentifierString(tni, launchConfig.getAttribute(TnBoxLauncher.LAUNCH_CONFIG_MAIN_FUNC, ""));
				
				if (fs.size() != 1) {
					return false;
				}
				
				Function f = fs.get(0);
				
				if (!f.hasAnnot(tni.corePackage.ANNOT_MAIN)) {
					return false;
				}
				
				// no errors
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		
		private void dialogChanged() {
			try {
				mainFuncButton.setEnabled(false);
				
				// check if the project is valid
				if (!new Path("").isValidSegment(projectText.getText())) {
					setErrorMessage("The project specified is not a valid project path.");
					return;
				}
				
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectText.getText());
				
				if (!project.isAccessible()) {
					setErrorMessage("The project specified does not exist.");
					return;
				}
				
				if (!project.hasNature(TyphonNature.ID)) {
					setErrorMessage("The project specified is not a Typhon project.");
					return;
				}
				
				// if the project is valid, we can search for a main function
				mainFuncButton.setEnabled(true);
				
				// check if the main function is valid
				TyphonInput tni = (TyphonInput) project.getSessionProperty(TyphonBuilder.STORAGE_TNI);
				if (tni == null) {
					project.build(IncrementalProjectBuilder.FULL_BUILD, null);
					tni = (TyphonInput) project.getSessionProperty(TyphonBuilder.STORAGE_TNI);
				}
				
				List<Function> fs = TyphonBuilder.fromIdentifierString(tni, mainFuncText.getText());
				
				if (fs.isEmpty()) {
					setErrorMessage("No function with that name exists in the project.");
					return;
				}
				
				if (fs.size() > 1) {
					setErrorMessage("There are multiple functions with that name.");
					return;
				}
				
				Function f = fs.get(0);
				
				if (!f.hasAnnot(tni.corePackage.ANNOT_MAIN)) {
					setErrorMessage("The specified function is not annotated with @main.");
					return;
				}
				
				// no errors
				setErrorMessage(null);
			} catch (Exception e) {
				setErrorMessage("An internal error occured: "+e.getMessage());
				e.printStackTrace();
			} finally {
				getLaunchConfigurationDialog().updateButtons();
			}
		}
		
		@Override
		public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
			TnBoxLauncher.setupLaunchConfig(configuration);
		}
		
		@Override
		public void initializeFrom(ILaunchConfiguration configuration) {
			try {
				projectText.setText(configuration.getAttribute(TnBoxLauncher.LAUNCH_CONFIG_PROJECT, ""));
				mainFuncText.setText(configuration.getAttribute(TnBoxLauncher.LAUNCH_CONFIG_MAIN_FUNC, ""));
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void performApply(ILaunchConfigurationWorkingCopy configuration) {
			configuration.setAttribute(TnBoxLauncher.LAUNCH_CONFIG_PROJECT, projectText.getText());
			configuration.setAttribute(TnBoxLauncher.LAUNCH_CONFIG_MAIN_FUNC, mainFuncText.getText());
			
			if (new Path("").isValidSegment(projectText.getText()))
				configuration.setMappedResources(new IResource[] {ResourcesPlugin.getWorkspace().getRoot().getProject(projectText.getText())});
		}
		
		@Override
		public String getName() {
			return "Main";
		}
		
		@Override
		public Image getImage() {
			try {
				return ImageDescriptor.createFromURL(new URL("platform:/plugin/org.eclipse.debug.ui/icons/full/etool16/run_exc.png")).createImage();
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		setTabs(new ILaunchConfigurationTab[] {new MainTab(), new CommonTab()});
	}
}

package info.iconmaster.tnclipse.wizards.projects;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class TyphonProjectWizardPage extends WizardPage {
	private Text nameBox;
	private Text locBox;
	private Button defaultLocButton;
	private Button browseButton;
	
	boolean updatingGui;
	
	public TyphonProjectWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle("Typhon Project");
		setDescription("Create a new Typhon project.");
	}

	@Override
	public void createControl(Composite parent) {
		// begin of GUI
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout containerLayout = new GridLayout(); container.setLayout(containerLayout);
		containerLayout.numColumns = 1;
		containerLayout.verticalSpacing = 9;
		
		// name field
		Composite nameContainer = new Composite(container, SWT.NONE);
		nameContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout nameContainerLayout = new GridLayout(); nameContainer.setLayout(nameContainerLayout);
		nameContainerLayout.numColumns = 2;
		
		new Label(nameContainer, SWT.NONE).setText("Project name:");
		
		nameBox = new Text(nameContainer, SWT.BORDER|SWT.SINGLE);
		nameBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		nameBox.addModifyListener((ev)->{
			if (updatingGui) return;
			updateGui();
		});
		
		// use default location field
		defaultLocButton = new Button(container, SWT.CHECK);
		defaultLocButton.setText("Use default location");
		defaultLocButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		defaultLocButton.setSelection(true);
		
		defaultLocButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (updatingGui) return;
				updateGui();
			}
		});
		
		// location field
		Composite locContainer = new Composite(container, SWT.NONE);
		locContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout locContainerLayout = new GridLayout(); locContainer.setLayout(locContainerLayout);
		locContainerLayout.numColumns = 3;
		
		new Label(locContainer, SWT.NONE).setText("Location:");
		
		locBox = new Text(locContainer, SWT.BORDER|SWT.SINGLE);
		locBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		browseButton = new Button(locContainer, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		
		locBox.addModifyListener((ev)->{
			if (updatingGui) return;
			updateGui();
		});
		
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				locBox.setText(dialog.open());
				updateGui();
			}
		});
		
		// end of GUI
		updateGui();
		setControl(container);
	}
	
	private void updateGui() {
		IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
		
		// update the location box if we're using the default path
		boolean useDefLoc = defaultLocButton.getSelection();
		
		locBox.setEnabled(!useDefLoc);
		browseButton.setEnabled(!useDefLoc);
		
		if (useDefLoc) {
			updatingGui = true;
			locBox.setText(workspace.getFullPath().append(nameBox.getText()).makeAbsolute().toOSString());
			updatingGui = false;
		}
		
		// check for errors in user entry
		String name = getName();
		String location = getLocation();
		File locationFile = new File(location);
		
		if (name.length() == 0) {
			updateStatus("Name must be specified");
			return;
		}
		if (location.length() == 0) {
			updateStatus("Location must be specified");
			return;
		}
		if (locationFile.exists() && !locationFile.isDirectory()) {
			updateStatus("Location exists and is not a directory");
			return;
		}
		if (name.contains("/") || name.contains("\\") || name.contains(":")) {
			updateStatus("Illegal project name");
			return;
		}
		if (workspace.getProject(name).exists()) {
			updateStatus("Project with specified name already exists");
			return;
		}
		
		for (IProject project : workspace.getProjects()) {
			try {
				if (new File(project.getFullPath().toOSString()).getCanonicalPath().equals(locationFile.getCanonicalPath())) {
					updateStatus("Project with specified path already exists");
					return;
				}
			} catch (IOException e) {
				// ignore; we tried our best
			}
		}
		
		updateStatus(null);
	}
	
	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}
	
	public String getName() {
		return nameBox.getText();
	}
	
	public String getLocation() {
		return locBox.getText();
	}
}
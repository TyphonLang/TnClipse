package info.iconmaster.tnclipse.editor;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import info.iconmaster.tnclipse.wizards.files.TyphonFileWizard;
import info.iconmaster.tnclipse.wizards.projects.TyphonProjectWizard;

public class TyphonPerspectiveFactory implements IPerspectiveFactory {
	@Override
	public void createInitialLayout(IPageLayout layout) {
        // Get the editor area
        String editorArea = layout.getEditorArea();
        
        // Top left: Project Explorer
        IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.3f, editorArea);
        topLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);
        topLeft.addPlaceholder("org.eclipse.ui.views.ResourceNavigator");
        
        // Bottom: Problems, Console, etc.
        IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.7f, editorArea);
        bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
        bottom.addPlaceholder(IPageLayout.ID_PROGRESS_VIEW);
        bottom.addPlaceholder("org.eclipse.ui.console.ConsoleView");
        
        // Top right: Outline, Types, etc.
        IFolderLayout topRight = layout.createFolder("topRight", IPageLayout.RIGHT, 0.7f, editorArea);
        topRight.addView(IPageLayout.ID_OUTLINE);
        
        // Add shortcuts
        layout.addNewWizardShortcut(TyphonFileWizard.ID);
        layout.addNewWizardShortcut(TyphonProjectWizard.ID);
        
        layout.addShowViewShortcut(IPageLayout.ID_PROJECT_EXPLORER);
        layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
        layout.addShowViewShortcut(IPageLayout.ID_PROGRESS_VIEW);
        layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
        layout.addShowViewShortcut("org.eclipse.ui.views.ResourceNavigator");
        layout.addShowViewShortcut("org.eclipse.ui.console.ConsoleView");
        
        layout.addActionSet("org.eclipse.debug.ui.launchActionSet");
        layout.addActionSet("org.eclipse.ui.edit.text.actionSet.presentation");
	}
}

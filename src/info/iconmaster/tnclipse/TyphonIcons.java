package info.iconmaster.tnclipse;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class TyphonIcons {
	private TyphonIcons() {}
	
	// do this to get a shared image:
	// return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
	
	public static Image ICON_PACKAGE = AbstractUIPlugin.imageDescriptorFromPlugin("info.iconmaster.tnclipse", "icons/typhon_package.gif").createImage();
	public static Image ICON_FUNC = AbstractUIPlugin.imageDescriptorFromPlugin("info.iconmaster.tnclipse", "icons/typhon_function.gif").createImage();
	public static Image ICON_FIELD = AbstractUIPlugin.imageDescriptorFromPlugin("info.iconmaster.tnclipse", "icons/typhon_field.gif").createImage();
	public static Image ICON_ANNOT = AbstractUIPlugin.imageDescriptorFromPlugin("info.iconmaster.tnclipse", "icons/typhon_annot.gif").createImage();
	public static Image ICON_CLASS = AbstractUIPlugin.imageDescriptorFromPlugin("info.iconmaster.tnclipse", "icons/typhon_class.gif").createImage();
	public static Image ICON_ENUM = AbstractUIPlugin.imageDescriptorFromPlugin("info.iconmaster.tnclipse", "icons/typhon_enum.gif").createImage();
	public static Image ICON_IMPORT = AbstractUIPlugin.imageDescriptorFromPlugin("info.iconmaster.tnclipse", "icons/typhon_import.gif").createImage();
}

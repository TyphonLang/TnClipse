package info.iconmaster.tnclipse;

import org.eclipse.ui.plugin.AbstractUIPlugin;

// DO NOT REMOVE THE IMPORT TO TNBOX.
import info.iconmaster.tnbox.TnBox;
import info.iconmaster.typhon.plugins.PluginLoader;
import info.iconmaster.typhon.plugins.TyphonPlugin;

public class TnClipse extends AbstractUIPlugin {
	public static final String ID = "info.iconmaster.tnclipse";
	
	private static TnClipse defaultPlugin;
	
	public static ColorManager colorManager = new ColorManager();
	
	public static TnClipse getDefault() {
		if (defaultPlugin == null) {
			defaultPlugin = new TnClipse();
		}
		return defaultPlugin;
	}
	
	static {
		// register Typhon plugins, usually the job of PluginLoader.loadPlugins().
		// we have to do this manually, because the classpath in Eclipse is all sorts of fucked up.
		
		PluginLoader.plugins.add(TnBox.class);
		PluginLoader.runHook(TyphonPlugin.OnLoad.class);
	}
}

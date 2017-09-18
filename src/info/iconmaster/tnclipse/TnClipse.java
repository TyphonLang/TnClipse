package info.iconmaster.tnclipse;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import info.iconmaster.typhon.plugins.PluginLoader;

public class TnClipse extends AbstractUIPlugin {
	private static TnClipse defaultPlugin;
	
	public static ColorManager colorManager = new ColorManager();
	
	public static TnClipse getDefault() {
		if (defaultPlugin == null) {
			defaultPlugin = new TnClipse();
		}
		return defaultPlugin;
	}
	
	static {
		// register Typhon plugins
		PluginLoader.loadPlugins();
	}
}

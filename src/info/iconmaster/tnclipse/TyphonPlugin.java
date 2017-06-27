package info.iconmaster.tnclipse;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class TyphonPlugin extends AbstractUIPlugin {
	private static TyphonPlugin defaultPlugin;
	
	public static ColorManager colorManager = new ColorManager();
	
	public static TyphonPlugin getDefault() {
		if (defaultPlugin == null) {
			defaultPlugin = new TyphonPlugin();
		}
		return defaultPlugin;
	}
}

package info.iconmaster.tnclipse;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorManager {

	protected Map<RGB, Color> colorMap = new HashMap<>();

	public void dispose() {
		for (Color c : colorMap.values()) {
			c.dispose();
		}
	}
	
	public Color getColor(RGB rgb) {
		Color color = colorMap.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			colorMap.put(rgb, color);
		}
		return color;
	}
	
	public Color getColorFromPreferences(String pref) {
		return getColor(StringConverter.asRGB(TyphonPlugin.getDefault().getPreferenceStore().getString(pref)));
	}
}

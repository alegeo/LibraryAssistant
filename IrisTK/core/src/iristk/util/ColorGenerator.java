package iristk.util;

import java.awt.Color;
import java.util.Random;

public class ColorGenerator {

	//private HashMap<String,Color> colors = new HashMap<String,Color>();
	
	static Random random = new Random();
	
	public static Color getColor(String key) {
		//if (colors.containsKey(key)) {
		//	return colors.get(key);
		//} else {
			random.setSeed(key.hashCode());
			float hue = random.nextFloat();
			float saturation = (random.nextInt(2000) + 1000) / 10000f;
			float luminance = 0.9f;
			Color color = Color.getHSBColor(hue, saturation, luminance);
		//	colors.put(key,color);
			return color;
		//}
	}
	
}

package iristk.util;

public class StringUtils {
	
	public static int countMatches(String string, char find) {
		int count = 0;
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) == find)
				count++;
		}
		return count;
	}
	
	public static String join(String[] parts, String glue) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			if (i > 0)
				result.append(glue);
			result.append(parts[i]);
		}
		return result.toString();
	}

}

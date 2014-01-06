package iristk.util;

public enum Language {

	SWEDISH("sv-SE"), ENGLISH_US("en-US"), ENGLISH_UK("en-GB");
	
	public final String code;

	Language(String code) {
		this.code = code;
	}

	public static Language fromCode(String string) {
		for (Language lang : values()) {
			if (lang.code.equals(string))
				return lang;
		}
		return null;
	}
	
}

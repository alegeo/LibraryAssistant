package iristk.speech;

import iristk.util.Language;

public class Voice {

	public enum Gender {
		MALE, FEMALE;

		public static Gender fromString(String string) {
			for (Gender gender : values()) {
				if (gender.name().equalsIgnoreCase(string))
					return gender;
			}
			return null;
		}
	}

	private final String name;
	private final Language language;
	private final Gender gender;

	public Voice(String name, Gender gender, Language language) {
		this.name = name;
		this.language = language;
		this.gender = gender;
	}

	public String getName() {
		return name;
	}

	public Language getLanguage() {
		return language;
	}

	public Gender getGender() {
		return gender;
	}

	@Override
	public String toString() {
		return name + "(" + language + ", " + gender.toString() + ")";
	}
}

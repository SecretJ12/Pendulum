package de.secretj12.JSON;

public class ParseException extends Exception{
	private static final long serialVersionUID = -3474964548804735750L;

	public ParseException(String message, int line, int character) {
		super(message + " - Line: " + line + ", Character: " + character);
	}
}

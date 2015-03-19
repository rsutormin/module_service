package us.kbase.moduleservice.gwt.client.parser;

import java.util.ArrayList;
import java.util.List;

public class CustomParseException extends ParseException {
	private final Token token;
	private List<String> altKeywords = new ArrayList<String>();
	private List<String> altModules = new ArrayList<String>();
	private List<String> altTypes = new ArrayList<String>();
	
	public CustomParseException(String message, Token t) {
		super(message);
		this.token = t;
	}
	
	public Token getToken() {
		return token;
	}
	
	public List<String> getAltKeywords() {
		return altKeywords;
	}
	
	public List<String> getAltModules() {
		return altModules;
	}
	
	public List<String> getAltTypes() {
		return altTypes;
	}
}

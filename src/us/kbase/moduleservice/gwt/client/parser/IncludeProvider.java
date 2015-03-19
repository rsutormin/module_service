package us.kbase.moduleservice.gwt.client.parser;

import java.util.Map;

/**
 * Class represents abstract provider that can find spec-documents referred to in includes.
 */
public interface IncludeProvider {
	public Map<String, KbModule> parseInclude(String includeLine) throws KidlParseException;
}

package us.kbase.moduleservice.gwt.client.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ModuleTokens {
	public Map<String, int[]> typedefToRowCol = new HashMap<String, int[]>();
	public Map<Integer, Map<Integer, String[]>> rowToColToModuleType = new TreeMap<Integer, Map<Integer, String[]>>();
}

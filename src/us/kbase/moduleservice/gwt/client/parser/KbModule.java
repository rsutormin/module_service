package us.kbase.moduleservice.gwt.client.parser;

import java.util.Map;
import java.util.TreeMap;

public class KbModule {
	private String moduleName;
	private String comment;
	private Map<String, String> typeToDoc = new TreeMap<String, String>();
	
	public KbModule(String name, String comment) {
		this.moduleName = name;
		this.comment = comment;
	}

	public String getModuleName() {
		return moduleName;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public Map<String, String> getTypeToDoc() {
		return typeToDoc;
	}
}

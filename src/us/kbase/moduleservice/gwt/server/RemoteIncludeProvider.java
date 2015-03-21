package us.kbase.moduleservice.gwt.server;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

import us.kbase.common.service.JsonClientException;
import us.kbase.jkidl.IncludeProvider;
import us.kbase.jkidl.ParseException;
import us.kbase.jkidl.SpecParser;
import us.kbase.kidl.KbModule;
import us.kbase.kidl.KidlParseException;
import us.kbase.workspace.GetModuleInfoParams;
import us.kbase.workspace.ModuleInfo;
import us.kbase.workspace.WorkspaceClient;

public class RemoteIncludeProvider implements IncludeProvider {
    private WorkspaceClient client = null;
	private Map<String, String> moduleNameToSpec = new LinkedHashMap<String, String>();
    private Map<String, Long> moduleNameToVer = new LinkedHashMap<String, Long>();

	public RemoteIncludeProvider(WorkspaceClient cl, ModuleInfo rootModuleInfo) {
	    this.client = cl;
	    registerMI(rootModuleInfo);
	}
	
	private void registerMI(ModuleInfo mi) {
	    for (Map.Entry<String, Long> entry : mi.getIncludedSpecVersion().entrySet()) {
	        Long oldVer = moduleNameToVer.get(entry.getKey());
	        if (oldVer != null && (long)oldVer != (long)entry.getValue())
	            throw new IllegalStateException("Diamond version discrepance for module " + 
	                    entry.getKey() + ": " + oldVer + " and " + entry.getValue());
	        moduleNameToVer.put(entry.getKey(), entry.getValue());
	    }
	}
	
	public void addSpecFile(String moduleName, String specDocument) {
		moduleNameToSpec.put(moduleName, specDocument);
	}
	
	@Override
	public Map<String, KbModule> parseInclude(String includeLine) throws KidlParseException {
		String moduleName = includeLine.trim();
		if (moduleName.startsWith("#include"))
			moduleName = moduleName.substring(8).trim();
		if (moduleName.startsWith("<"))
			moduleName = moduleName.substring(1).trim();
		if (moduleName.endsWith(">"))
			moduleName = moduleName.substring(0, moduleName.length() - 1).trim();
		if (moduleName.contains("/"))
			moduleName = moduleName.substring(moduleName.lastIndexOf('/') + 1).trim();
		if (moduleName.contains("\\"))
			moduleName = moduleName.substring(moduleName.lastIndexOf('\\') + 1).trim();
		if (moduleName.contains("."))
			moduleName = moduleName.substring(0, moduleName.indexOf('.')).trim();
		String ret = moduleNameToSpec.get(moduleName);
		if (ret == null) {
            GetModuleInfoParams gmiParams = new GetModuleInfoParams().withMod(moduleName);
		    Long ver = moduleNameToVer.get(moduleName);
            if (ver != null)
                gmiParams.setVer(ver);
            try {
                ModuleInfo mi = client.getModuleInfo(gmiParams);
                registerMI(mi);
                ret = mi.getSpec();
                moduleNameToSpec.put(moduleName, ret);
            } catch (IOException | JsonClientException e) {
                throw new KidlParseException("Error loading module info for [" + moduleName + "]: " + e.getMessage());
            }
		}
        SpecParser p = new SpecParser(new StringReader(ret));
		try {
			return p.SpecStatement(this);
		} catch (ParseException e) {
			throw new KidlParseException("Error parsing spec-document of module [" + moduleName + "]: " + e.getMessage());
		}
	}
}

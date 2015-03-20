package us.kbase.moduleservice.gwt.server;

import java.util.HashMap;
import java.util.Map;

public class DeployConfig {

    public static Map<String, String> getConfig() throws Exception {
        Map<String, String> ret = new HashMap<String, String>();
        ret.put("ws.url", "https://kbase.us/services/ws");
        return ret;
    }
}

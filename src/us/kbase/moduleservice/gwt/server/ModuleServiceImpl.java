package us.kbase.moduleservice.gwt.server;

import java.util.Map;

import us.kbase.auth.AuthService;
import us.kbase.moduleservice.gwt.client.ModuleService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class ModuleServiceImpl extends RemoteServiceServlet implements
        ModuleService {

    public String login(String user, String passwd) throws Exception {
        return AuthService.login(user, passwd).getTokenString();
    }
    
    public Map<String, String> getConfig() throws Exception {
        return DeployConfig.getConfig();
    }
    
    @Override
    public boolean validateAuthToken(String token) {
        try {
            return AuthService.validateToken(token);
        } catch (Exception ex) {
            return false;
        }
    }
}

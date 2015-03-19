package us.kbase.moduleservice.gwt.client;

import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("gwt")
public interface ModuleService extends RemoteService {
    String login(String user, String passwd) throws Exception;
    Map<String, String> getConfig() throws Exception;
}

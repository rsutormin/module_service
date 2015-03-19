package us.kbase.moduleservice.gwt.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("gwt")
public interface ModuleService extends RemoteService {
    String login(String user, String passwd) throws Exception;
    String greetServer(String name) throws IllegalArgumentException;
}

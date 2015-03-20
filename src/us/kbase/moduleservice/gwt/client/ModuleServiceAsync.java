package us.kbase.moduleservice.gwt.client;

import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>ModuleService</code>.
 */
public interface ModuleServiceAsync {
    void login(String user, String passwd, AsyncCallback<String> token);

    void getConfig(AsyncCallback<Map<String, String>> callback);
    
    void validateAuthToken(String token, AsyncCallback<Boolean> callback);
}

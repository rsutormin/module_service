package us.kbase.moduleservice.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>ModuleService</code>.
 */
public interface ModuleServiceAsync {
    void login(String user, String passwd, AsyncCallback<String> token);

    void greetServer(String input, AsyncCallback<String> callback)
            throws IllegalArgumentException;
}

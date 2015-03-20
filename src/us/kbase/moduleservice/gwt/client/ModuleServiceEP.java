package us.kbase.moduleservice.gwt.client;

import java.util.Map;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootLayoutPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ModuleServiceEP implements EntryPoint {

    /**
     * Create a remote service proxy to talk to the server-side Module service.
     */
    private final ModuleServiceAsync gwtService = GWT
            .create(ModuleService.class);

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        gwtService.getConfig(new AsyncCallback<Map<String,String>>() {
            @Override
            public void onSuccess(final Map<String, String> config) {
                String token = Cookies.getCookie(KidlWebEditorPanel.COOKIES_TOKEN_KEY);
                if (token == null) {
                    showMainPanel(config);
                } else {
                    gwtService.validateAuthToken(token, new AsyncCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            if (!result)
                                Cookies.removeCookie(KidlWebEditorPanel.COOKIES_TOKEN_KEY);
                            showMainPanel(config);
                        }
                        @Override
                        public void onFailure(Throwable caught) {
                            Cookies.removeCookie(KidlWebEditorPanel.COOKIES_TOKEN_KEY);
                            showMainPanel(config);
                        }
                    });
                }
            }
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }
        });
    }
    
    private static void showMainPanel(Map<String, String> config) {
        final String token = Cookies.getCookie(KidlWebEditorPanel.COOKIES_TOKEN_KEY);
        final String moduleName = Window.Location.getParameter("module");
        final String typeName = Window.Location.getParameter("type");
        String wsUrlInput = config.get("ws.url");
        final KidlWebEditorPanel mainPanel = new KidlWebEditorPanel(token, moduleName, typeName, wsUrlInput);
        RootLayoutPanel.get().add(mainPanel);
    }
}

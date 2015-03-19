package us.kbase.moduleservice.gwt.client;

import java.util.Map;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
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
        final String token = null;
        final String moduleName = Window.Location.getParameter("module");  //"KBaseGenomes";
        final String typeName = Window.Location.getParameter("type");      //"Genome";
        gwtService.getConfig(new AsyncCallback<Map<String,String>>() {
            @Override
            public void onSuccess(Map<String, String> result) {
                String wsUrlInput = result.get("ws.url");
                final KidlWebEditorPanel mainPanel = new KidlWebEditorPanel(token, moduleName, typeName, wsUrlInput);
                RootLayoutPanel.get().add(mainPanel);
            }
            @Override
            public void onFailure(Throwable caught) {
            }
        });
    }
}

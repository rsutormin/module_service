package us.kbase.moduleservice.gwt.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

public class LoginDialog extends DialogBox {

    public LoginDialog(final KidlWebEditorPanel parent, 
            final ModuleServiceAsync gwtService) {
        final DialogBox dialogBox = this;
        dialogBox.getElement().getStyle().setZIndex(10000);
        dialogBox.setText("Log In");
        dialogBox.setAnimationEnabled(true);
        FlowPanel main = new FlowPanel();
        dialogBox.setWidget(main);
        main.add(new HTML("User name: "));
        final TextBox tb1 = new TextBox();
        //tb1.setValue();
        tb1.getElement().getStyle().setDisplay(Display.BLOCK);
        tb1.getElement().getStyle().setWidth(300, Unit.PX);
        main.add(tb1);
        main.add(new HTML("<br>"));
        main.add(new HTML("Password: "));
        final TextBox tb2 = new PasswordTextBox();
        //tb2.setValue();
        tb2.getElement().getStyle().setDisplay(Display.BLOCK);
        tb2.getElement().getStyle().setWidth(300, Unit.PX);
        main.add(tb2);
        main.add(new HTML("<br>"));
        final Button logButton = new Button("Log In");
        logButton.getElement().setAttribute("style", "margin: 15px 6px 6px;");
        main.add(logButton);
        logButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                String loginName = tb1.getValue();
                if (loginName == null || loginName.trim().isEmpty()) {
                    Window.alert("Login can not be empty.");
                    return;
                }
                gwtService.login(tb1.getValue(), tb2.getValue(), new AsyncCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        parent.setToken(result);
                        Cookies.setCookie(KidlWebEditorPanel.COOKIES_TOKEN_KEY, result);
                        parent.getJsServices().removeAllKeys();
                        parent.listAllTypes(new Callback<Boolean, Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                dialogBox.hide();
                                parent.focus();
                            }
                            @Override
                            public void onFailure(Boolean reason) {
                                Window.alert("Login error");
                            }
                        });
                    }
                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("Login error: " + caught.getMessage());
                    }
                });
            }
        });
        final Button cancelButton = new Button("Cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
                parent.focus();
            }
        });
        main.add(cancelButton);
        dialogBox.center();
        dialogBox.show();
    }
}

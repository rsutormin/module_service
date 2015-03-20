package us.kbase.moduleservice.gwt.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;

public class GenerateDialog extends DialogBox {
    private int labelW = 150;
    private int paramW = 200;

    public GenerateDialog(final KidlWebEditorPanel parent, final String token,
            final String module, final ModuleServiceAsync gwtService) {
        final DialogBox dialogBox = this;
        dialogBox.getElement().getStyle().setZIndex(10000);
        dialogBox.setText("Log In");
        dialogBox.setAnimationEnabled(true);
        FlowPanel main = new FlowPanel();
        dialogBox.setWidget(main);
        final CheckBox cbJs = new CheckBox();
        main.add(kv("Add JS client", cbJs));
        final TextBox tbJsClName = new TextBox();
        main.add(kv("JS client name", tbJsClName));
        final CheckBox cbPerl = new CheckBox();
        main.add(kv("Add Perl client", cbPerl));
        final TextBox tbPerlClName = new TextBox();
        main.add(kv("Perl client name", tbPerlClName));
        final CheckBox cbPy = new CheckBox();
        main.add(kv("Add Python client", cbPy));
        final TextBox tbPyClName = new TextBox();
        main.add(kv("Python client name", tbPyClName));
        final CheckBox cbJava = new CheckBox();
        main.add(kv("Add Java client", cbJava));
        final TextBox tbJavaClName = new TextBox();
        main.add(kv("Java client name", tbJavaClName));
        //
        final Button logButton = new Button("Generate");
        logButton.getElement().setAttribute("style", "margin: 15px 6px 6px;");
        main.add(logButton);
        logButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                String url = add("generate?", "token", token);
                url = add(url, "module", module);
                url = add(url, "js", cbJs.getValue());
                url = add(url, "jsclname", tbJsClName.getValue());
                url = add(url, "perl", cbPerl.getValue());
                url = add(url, "perlclname", tbPerlClName.getValue());
                url = add(url, "py", cbPy.getValue());
                url = add(url, "pyclname", tbPyClName.getValue());
                url = add(url, "java", cbJava.getValue());
                url = add(url, "javaclname", tbJavaClName.getValue());
                parent.downloadFile(url);
                dialogBox.hide();
                parent.focus();
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

    private Widget kv(String label, Widget widget) {
        HorizontalPanel ret = new HorizontalPanel();
        ret.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        HTML html = new HTML(label + ":&nbsp;");
        html.setWidth(labelW + "px");
        ret.add(html);
        widget.setWidth(paramW + "px");
        widget.setHeight("14px");
        ret.add(widget);
        ret.setHeight("26px");
        return ret;
    }
    
    private String add(String url, String name, boolean value) {
        if (!value)
            return url;
        return add(url, name, "" + value);
    }
    
    private String add(String url, String name, String value) {
        if (value == null || value.isEmpty())
            return url;
        if (!url.endsWith("?"))
            url += "&";
        return url + name + "=" + URL.encodeQueryString(value);
    }
}

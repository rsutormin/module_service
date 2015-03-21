package us.kbase.moduleservice.gwt.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class GenerateDialog extends DialogBox {

    public GenerateDialog(final KidlWebEditorPanel parent, final String token,
            final String module, final ModuleServiceAsync gwtService) {
        final DialogBox dialogBox = this;
        dialogBox.getElement().getStyle().setZIndex(10000);
        dialogBox.setText("Log In");
        dialogBox.setAnimationEnabled(true);
        FormPanel main = new FormPanel(150, 200);
        dialogBox.setWidget(main);
        final TextBox tbModule = new TextBox();
        tbModule.setValue(module);
        main.add("Module name", tbModule);
        final TextBox tbUrl = new TextBox();
        main.add("Default URL", tbUrl);
        final CheckBox cbJsonSchema = new CheckBox();
        main.add("Add JSON schema", cbJsonSchema);
        //////////////// End of common /////////////////
        DisclosurePanel jsDc = new DisclosurePanel("JavaScript options");
        main.add(jsDc);
        jsDc.setAnimationEnabled(true);
        FormPanel jsForm = new FormPanel(133, 200);
        jsDc.setContent(jsForm);
        final CheckBox cbJs = new CheckBox();
        jsForm.add("Add JS client", cbJs);
        final TextBox tbJsClName = new TextBox();
        jsForm.add("JS client name", tbJsClName);
        //////////////// End of JS /////////////////
        DisclosurePanel perlDc = new DisclosurePanel("Perl options");
        main.add(perlDc);
        perlDc.setAnimationEnabled(true);
        FormPanel perlForm = new FormPanel(133, 200);
        perlDc.setContent(perlForm);
        final CheckBox cbPerl = new CheckBox();
        perlForm.add("Add Perl client", cbPerl);
        final TextBox tbPerlClName = new TextBox();
        perlForm.add("Perl client name", tbPerlClName);
        final CheckBox cbPerlEnableRetries = new CheckBox();
        perlForm.add("Enable retries", cbPerlEnableRetries);
        final CheckBox cbPerlSrv = new CheckBox();
        final TextBox tbPerlSrvName = new TextBox();
        final TextBox tbPerlImplName = new TextBox();
        final TextBox tbPerlPsgiName = new TextBox();
        {
            DisclosurePanel perlSrvDc = new DisclosurePanel("Perl server options");
            perlForm.add(perlSrvDc);
            perlSrvDc.setAnimationEnabled(true);
            FormPanel perlSrvForm = new FormPanel(116, 200);
            perlSrvDc.setContent(perlSrvForm);
            perlSrvForm.add("Add Perl server", cbPerlSrv);
            perlSrvForm.add("Perl server name", tbPerlSrvName);
            perlSrvForm.add("Perl impl name", tbPerlImplName);
            perlSrvForm.add("Perl psgi name", tbPerlPsgiName);
        }
        //////////////// End of Perl /////////////////
        DisclosurePanel pyDc = new DisclosurePanel("Python options");
        main.add(pyDc);
        pyDc.setAnimationEnabled(true);
        FormPanel pyForm = new FormPanel(133, 200);
        pyDc.setContent(pyForm);
        final CheckBox cbPy = new CheckBox();
        pyForm.add("Add Python client", cbPy);
        final TextBox tbPyClName = new TextBox();
        pyForm.add("Python client name", tbPyClName);
        final CheckBox cbPySrv = new CheckBox();
        final TextBox tbPySrvName = new TextBox();
        final TextBox tbPyImplName = new TextBox();
        {
            DisclosurePanel pySrvDc = new DisclosurePanel("Python server options");
            pyForm.add(pySrvDc);
            pySrvDc.setAnimationEnabled(true);
            FormPanel pySrvForm = new FormPanel(116, 200);
            pySrvDc.setContent(pySrvForm);
            pySrvForm.add("Add Python server", cbPySrv);
            pySrvForm.add("Python server name", tbPySrvName);
            pySrvForm.add("Python impl name", tbPyImplName);
        }
        //////////////// End of Python /////////////////
        DisclosurePanel javaDc = new DisclosurePanel("Java options");
        main.add(javaDc);
        javaDc.setAnimationEnabled(true);
        FormPanel javaForm = new FormPanel(133, 200);
        javaDc.setContent(javaForm);
        final CheckBox cbJava = new CheckBox();
        javaForm.add("Add Java client", cbJava);
        final TextBox tbJavaSrc = new TextBox();
        tbJavaSrc.setValue("src");
        javaForm.add("Source folder", tbJavaSrc);
        final TextBox tbJavaPackage = new TextBox();
        tbJavaPackage.setValue("us.kbase");
        javaForm.add("Package parent", tbJavaPackage);
        final TextBox tbJavaLib = new TextBox();
        javaForm.add("Lib folder", tbJavaLib);
        final TextBox tbJavaBuildXml = new TextBox();
        javaForm.add("Ant build.xml", tbJavaBuildXml);
        final TextBox tbJavaGwt = new TextBox();
        javaForm.add("GWT package", tbJavaGwt);
        final CheckBox cbJavaSrv = new CheckBox();
        javaForm.add("Add Java server", cbJavaSrv);
        //////////////// End of Java /////////////////
        final Button genButton = new Button("Generate");
        genButton.getElement().setAttribute("style", "margin: 15px 6px 6px;");
        main.add(genButton);
        genButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                StringBuilder url = new StringBuilder("generate?");
                add(url, "token", token);
                add(url, "module", tbModule.getValue());
                add(url, "url", tbUrl.getValue());
                add(url, "jsonschema", cbJsonSchema.getValue());
                add(url, "js", cbJs.getValue());
                add(url, "jsclname", tbJsClName.getValue());
                add(url, "perl", cbPerl.getValue());
                add(url, "perlclname", tbPerlClName.getValue());
                add(url, "perlenableretries", cbPerlEnableRetries.getValue());
                add(url, "perlsrv", cbPerlSrv.getValue());
                add(url, "perlsrvname", tbPerlSrvName.getValue());
                add(url, "perlimplname", tbPerlImplName.getValue());
                add(url, "perlpsginame", tbPerlPsgiName.getValue());
                add(url, "py", cbPy.getValue());
                add(url, "pyclname", tbPyClName.getValue());
                add(url, "pysrv", cbPySrv.getValue());
                add(url, "pysrvname", tbPySrvName.getValue());
                add(url, "pyimplname", tbPyImplName.getValue());
                add(url, "java", cbJava.getValue());
                add(url, "javasrc", tbJavaSrc.getValue());
                add(url, "javapackage", tbJavaPackage.getValue());
                add(url, "javalib", tbJavaLib.getValue());
                add(url, "javasrv", cbJavaSrv.getValue());
                add(url, "javabuildxml", tbJavaBuildXml.getValue());
                add(url, "javagwt", tbJavaGwt.getValue());
                parent.downloadFile(url.toString());
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

    private StringBuilder add(StringBuilder url, String name, boolean value) {
        if (!value)
            return url;
        return add(url, name, "" + value);
    }
    
    private StringBuilder add(StringBuilder url, String name, String value) {
        if (value == null || value.isEmpty())
            return url;
        if (url.charAt(url.length() - 1) != '?')
            url.append("&");
        url.append(name).append("=").append(URL.encodeQueryString(value));
        return url;
    }
    
    private static class FormPanel extends FlowPanel {
        private int labelW = 150;
        private int paramW = 200;

        public FormPanel(int labelW, int paramW) {
            this.labelW = labelW;
            this.paramW = paramW;
        }
        
        public void add(String label, Widget widget) {
            HorizontalPanel ret = new HorizontalPanel();
            ret.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
            HTML html = new HTML(label + ":&nbsp;");
            html.setWidth(labelW + "px");
            ret.add(html);
            widget.setWidth(paramW + "px");
            widget.setHeight("14px");
            ret.add(widget);
            ret.setHeight("26px");
            add(ret);
        }
    }
}

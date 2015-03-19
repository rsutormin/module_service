package us.kbase.moduleservice.gwt.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import us.kbase.moduleservice.gwt.client.parser.KbModule;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

import edu.ycp.cs.dh.acegwt.client.ace.AceCompletion;
import edu.ycp.cs.dh.acegwt.client.ace.AceCompletionCallback;
import edu.ycp.cs.dh.acegwt.client.ace.AceCompletionProvider;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditor;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorCursorPosition;

public class KidlWebEditorPanel extends DockLayoutPanel {

	private String token = null;
	private DblClckTree typeTree = null;
	private JSO jsServices = JSO.newObject();
	private Map<String, Map<String, String>> mod2type2ver = null;				// For all modules and types
	private Set<String> ownedModules = new HashSet<String>();                   // Is used if token != null
	private Map<String, KbModule> modules = new HashMap<String, KbModule>();	// For ones used in tabs and included only
	private ScrolledTabLayoutPanel tabPanel = null;
	private Map<String, KidlWebEditorTab> tabMap = new TreeMap<String, KidlWebEditorTab>();
	private Map<String, Integer> tabIndexMap = new TreeMap<String, Integer>();
	private String wsUrl = "https://kbase.us/services/ws";
    private final ModuleServiceAsync gwtService = GWT
            .create(ModuleService.class);

	public KidlWebEditorPanel(final String token, final String moduleName, 
			final String typeName, final String wsUrlInput) {
	    super(Unit.PX);
		this.token = token;
		if (wsUrlInput != null)
			this.wsUrl = wsUrlInput;

		FlowPanel upper = new FlowPanel();
		upper.getElement().getStyle().setProperty("border", "1px solid #ccc");

		Button create = new Button("C", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				openNewModuleDialog();
			}
		});
		create.getElement().getStyle().setHeight(30, Unit.PX);
		create.getElement().getStyle().setWidth(30, Unit.PX);
		upper.add(create);
		TooltipListener.addFor(create, "Create new module", 5000);
		Button reg = new Button("E", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				openRegisterModuleDialog();
			}
		});
		reg.getElement().getStyle().setHeight(30, Unit.PX);
		reg.getElement().getStyle().setWidth(30, Unit.PX);
		upper.add(reg);
		TooltipListener.addFor(reg, "Register current module", 5000);
		Button rel = new Button("L", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				openReleaseModuleDialog();
			}
		});
		rel.getElement().getStyle().setHeight(30, Unit.PX);
		rel.getElement().getStyle().setWidth(30, Unit.PX);
		upper.add(rel);
		TooltipListener.addFor(rel, "Release current module", 5000);
		Button undo = new Button("U", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				undo();
			}
		});
		undo.getElement().getStyle().setHeight(30, Unit.PX);
		undo.getElement().getStyle().setWidth(30, Unit.PX);
		upper.add(undo);
		TooltipListener.addFor(undo, "Undo (see keyboard shortcuts)", 5000);
		Button redo = new Button("R", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				redo();
			}
		});
		redo.getElement().getStyle().setHeight(30, Unit.PX);
		redo.getElement().getStyle().setWidth(30, Unit.PX);
		upper.add(redo);
		TooltipListener.addFor(redo, "Redo (see keyboard shortcuts)", 5000);
		Button auto = new Button("A", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				autocomplete();
			}
		});
		auto.getElement().getStyle().setHeight(30, Unit.PX);
		auto.getElement().getStyle().setWidth(30, Unit.PX);
		upper.add(auto);
		TooltipListener.addFor(auto, "Autocomplete (see keyboard shortcuts)", 5000);
		Button find = new Button("F", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				find();
			}
		});
		find.getElement().getStyle().setHeight(30, Unit.PX);
		find.getElement().getStyle().setWidth(30, Unit.PX);
		upper.add(find);
		TooltipListener.addFor(find, "Find (see keyboard shortcuts)", 5000);
		Button bind = new Button("K", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				keyboardShortcuts();
			}
		});
		bind.getElement().getStyle().setHeight(30, Unit.PX);
		bind.getElement().getStyle().setWidth(30, Unit.PX);
		upper.add(bind);
		TooltipListener.addFor(bind, "Keyboard shortcuts", 5000);

		DockLayoutPanel panel = this;  //new DockLayoutPanel(Unit.PX);
		//rootPanel.add(panel);
		//panel.setHeight(rootPanel.getElement().getClientHeight() + "px");
		panel.getElement().getStyle().setOverflow(Overflow.HIDDEN);
		panel.getElement().getStyle().setPosition(Position.ABSOLUTE);
		panel.getElement().getStyle().setLeft(0, Unit.PX);
		panel.getElement().getStyle().setTop(0, Unit.PX);
		panel.getElement().getStyle().setRight(0, Unit.PX);
		panel.getElement().getStyle().setBottom(0, Unit.PX);
		//position: absolute; overflow: hidden; left: 0px; top: 32px; right: 0px; bottom: 0px;
		panel.addNorth(createMenu(), 32);
		DockLayoutPanel centerPanel = new DockLayoutPanel(Unit.PX);
		panel.add(centerPanel);
		typeTree = new DblClckTree();
		ScrollPanel sp = new ScrollPanel(typeTree); 
		centerPanel.addEast(sp, 200);
		Style sps = sp.getElement().getStyle();
		sps.setProperty("border", "1px solid #ccc");
		tabPanel = new ScrolledTabLayoutPanel(25, Unit.PX);
		centerPanel.add(tabPanel);
		tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				int tabIndex = event.getSelectedItem();
				KidlWebEditorTab tab = getTabByIndex(tabIndex);
				if (tab != null) {
					tab.getAceEditor().resize();
					tab.getAceEditor().focus();
				}
			}
		});
		
		typeTree.setWidth("200px");
		TooltipListener.addFor(typeTree, "Double click on nodes to open content", 5000);

		listAllTypes(new Callback<Boolean, Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				if (moduleName != null) {
					Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
						@Override
						public void execute() {
							openTab(moduleName, typeName, false);
						}
					});
				}
			}
			@Override
			public void onFailure(Boolean reason) {
			}
		});
		AceEditor.removeAllExistingCompleters();
		AceEditor.addCompletionProvider(new AceCompletionProvider() {
			@Override		
			public void getProposals(AceEditor editor, AceEditorCursorPosition pos, String prefix, AceCompletionCallback callback) {
				Widget par = editor.getParent();
				if (par instanceof KidlWebEditorTab) {
					List<AceCompletion> cmps;
					try {
						cmps = ((KidlWebEditorTab)par).getAutocompletionProposals(pos, prefix);
					} catch (Throwable ex) {
						lg("Error: " + ex.getMessage());
						cmps = new ArrayList<AceCompletion>();
					}
					callback.invokeWithCompletions(cmps.toArray(new AceCompletion[cmps.size()]));
				} else {
					callback.invokeWithCompletions(new AceCompletion[0]);
				}
			}
		});
	}

	public void openTab(String moduleName, final String typeName, boolean isNew) {
		if (tabIndexMap.containsKey(moduleName)) {
			final KidlWebEditorTab tab = tabMap.get(moduleName);
			tabPanel.selectTab(tabIndexMap.get(moduleName));
			Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
				@Override
				public void execute() {
					tab.scrollToType(typeName);
				}
			});
		} else {
			int tabIndex = tabMap.size();
			boolean readOnly = !ownedModules.contains(moduleName);
			KidlWebEditorTab tab = new KidlWebEditorTab(this, moduleName, token, jsServices, 
			        typeName, isNew, readOnly);
			tabMap.put(moduleName, tab);
			tabIndexMap.put(moduleName, tabIndex);
			tabPanel.add(tab, moduleName);
			tabPanel.selectTab(tabIndex);
		}
	}
	
	public KidlWebEditorTab getSelectedTab() {
		int ind = tabPanel.getSelectedIndex();
		return getTabByIndex(ind);
	}
	
	public KidlWebEditorTab getTabByIndex(int ind) {
		for (String tabName : tabIndexMap.keySet()) {
			if (tabIndexMap.get(tabName) == ind)
				return tabMap.get(tabName);
		}
		return null;
	}

	public String getSelectedTabName() {
		int ind = tabPanel.getSelectedIndex();
		for (String tabName : tabIndexMap.keySet()) {
			if (tabIndexMap.get(tabName) == ind)
				return tabName;
		}
		return null;
	}

	public Map<String, KbModule> getLoadedModules() {
		return modules;
	}
	
	public void addModuleAndLoadDocs(final KbModule module) {
		modules.put(module.getModuleName(), module);
		wsCall(jsServices, token, "get_module_info", "{\"mod\": \"" + module.getModuleName() + "\"}", new Callback<JSO, String>() {
			@Override
			public void onSuccess(JSO result) {
				String moduleComment = result.getString("description");
				module.setComment(moduleComment);
				JSO types = result.get("types");
				JSO typeKeys = types.keys();
				for (int i = 0; i < typeKeys.length(); i++) {
					String typeKey = typeKeys.getString(i);
					JSO type = types.getFromJson(typeKey);
					String typeName = type.getString("id");
					String typeComment = type.getString("description");
					module.getTypeToDoc().put(typeName, typeComment);
				}
			}
			@Override
			public void onFailure(String reason) {
				lg(reason);
			}
		});
	}
	
	public String escapeHtml(String html) {
		if (html == null)
			return null;
		return new SafeHtmlBuilder().appendEscaped(html).toSafeHtml().asString();
	}
	
	private String getUser() {
        String user = null;
        if (token != null) {
            for (String part : token.split("\\|")) {
                String[] keyVal = part.split("\\=");
                if (keyVal == null || keyVal.length != 2)
                    continue;
                if (keyVal[0].equals("un"))
                    user = keyVal[1];
            }
        }
        return user;
	}
	
	private void listAllTypes(final Callback<Boolean, Boolean> callback) {
	    ownedModules = new HashSet<String>();
	    String user = getUser();
	    if (user != null) {
	        wsCall(jsServices, token, "list_modules", "{\"owner\": \"" + user + "\"}", new Callback<JSO, String>() {
	            @Override
	            public void onSuccess(JSO result) {
                    lg("In list_modules");
	                int size = result.length();
	                for (int i = 0; i < size; i++) {
	                    String module = result.getString(i);
	                    lg(module);
	                    ownedModules.add(module);
	                }
	                lg("Before listAllTypes");
	                listAllTypes(ownedModules, callback);
	            }
	            public void onFailure(String reason) {
	                lg(reason);
	                callback.onFailure(true);
	            }
	        });
	    } else {
	        listAllTypes(ownedModules, callback);
	    }
	}

	private void listAllTypes(Set<String> ownedModules, final Callback<Boolean, Boolean> callback) {
	    wsCall(jsServices, token, "list_all_types", "{\"with_empty_modules\": 0}", new Callback<JSO, String>() {
			@Override
			public void onSuccess(JSO result) {
				mod2type2ver = new TreeMap<String, Map<String, String>>();
				JSO moduleNames = result.keys();
				for (int i = 0; i < moduleNames.length(); i++) {
					String moduleName = moduleNames.getString(i);
					JSO typeMap = result.get(moduleName);
					JSO typeNames = typeMap.keys();
					Map<String, String> type2ver = new TreeMap<String, String>();
					for (int j = 0; j < typeNames.length(); j++) {
						String typeName = typeNames.getString(j);
						type2ver.put(typeName, typeMap.getString(typeName));
					}
					mod2type2ver.put(moduleName, type2ver);
				}
				buildTypeTreeWidget();
				callback.onSuccess(true);
			}
			public void onFailure(String reason) {
				lg(reason);
				callback.onFailure(true);
			}
		});
	}
	
	private void buildTypeTreeWidget() {
		if (mod2type2ver == null || typeTree == null)
			return;
		typeTree.removeItems();
		for (final String mod : mod2type2ver.keySet()) {
		    String modLabel = mod;
		    if (ownedModules.contains(mod))
		        modLabel += " (owned)";
			DblClckTreeItem modTi = new DblClckTreeItem(new SafeHtmlBuilder().appendEscaped(modLabel).toSafeHtml()) {
				@Override
				public void onDoubleClick() {
					openTab(mod, null, false);
				}
			};
			typeTree.addItem(modTi);
			for (final String type : mod2type2ver.get(mod).keySet()) {
				DblClckTreeItem typeTi = new DblClckTreeItem(new SafeHtmlBuilder().appendEscaped(type).toSafeHtml()) {
					@Override
					public void onDoubleClick() {
						openTab(mod, type, false);
					}
				};
				modTi.addItem(typeTi);
			}
		}
	}
	
	public Map<String, Map<String, String>> getMod2type2ver() {
		return mod2type2ver;
	}
	
	private void openNewModuleDialog() {
		final DialogBox dialogBox = new DialogBox();
		dialogBox.getElement().getStyle().setZIndex(10000);
		dialogBox.setText("New module creation");
		dialogBox.setAnimationEnabled(true);
		FlowPanel main = new FlowPanel();
		dialogBox.setWidget(main);
		main.add(new HTML("Module name: "));
		final TextBox tb = new TextBox();
		tb.getElement().getStyle().setDisplay(Display.BLOCK);
		main.add(tb);
		main.add(new HTML("<br>"));
		final CheckBox cb = new CheckBox("request ownership");
		cb.getElement().getStyle().setDisplay(Display.BLOCK);
		main.add(cb);
		main.add(new HTML("<br>"));
		final Button openButton = new Button("Open");
		openButton.getElement().setAttribute("style", "margin: 15px 6px 6px;");
		main.add(openButton);
		openButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				String moduleName = tb.getValue();
				if (cb.getValue()) {
					if (moduleName == null || moduleName.trim().isEmpty()) {
						Window.alert("Module can not be empty if request ownership checkbox is selected.");
						return;
					}
					final String mn = moduleName.trim();
					wsCall2(jsServices, wsUrl, token, "request_module_ownership", mn, new Callback<JSO, String>() {
						@Override
						public void onSuccess(JSO result) {
							openTab(mn, null, true);
							dialogBox.hide();
							focus();
						}
						@Override
						public void onFailure(String reason) {
							Window.alert("Error requesting ownership: " + reason);
						}
					});
				} else {
					if (moduleName == null || moduleName.trim().isEmpty()) {
						moduleName = "New" + System.currentTimeMillis();
					} else {
						moduleName = moduleName.trim();
					}
					openTab(moduleName, null, true);
					dialogBox.hide();
					focus();
				}
			}
		});
		final Button cancelButton = new Button("Cancel", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				dialogBox.hide();
				focus();
			}
		});
		main.add(cancelButton);
		dialogBox.center();
		dialogBox.show();
	}

	private void openReleaseModuleDialog() {
		final DialogBox dialogBox = new DialogBox();
		dialogBox.getElement().getStyle().setZIndex(10000);
		dialogBox.setText("Module release");
		dialogBox.setAnimationEnabled(true);
		FlowPanel main = new FlowPanel();
		dialogBox.setWidget(main);
		main.add(new HTML("Module name: "));
		final TextBox tb = new TextBox();
		tb.setValue(getModuleNameForSelectedTab());
		tb.getElement().getStyle().setDisplay(Display.BLOCK);
		main.add(tb);
		main.add(new HTML("<br>"));
		final Button relButton = new Button("Release");
		relButton.getElement().setAttribute("style", "margin: 15px 6px 6px;");
		main.add(relButton);
		relButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				String moduleName = tb.getValue();
				if (moduleName == null || moduleName.trim().isEmpty()) {
					Window.alert("Module can not be empty if request ownership checkbox is selected.");
					return;
				}
				wsCall2(jsServices, wsUrl, token, "release_module", moduleName.trim(), new Callback<JSO, String>() {
					@Override
					public void onSuccess(JSO result) {
						dialogBox.hide();
						focus();
					}
					@Override
					public void onFailure(String reason) {
						Window.alert("Error requesting ownership: " + reason);
					}
				});
			}
		});
		final Button cancelButton = new Button("Cancel", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				dialogBox.hide();
				focus();
			}
		});
		main.add(cancelButton);
		dialogBox.center();
		dialogBox.show();
	}

	private void openRegisterModuleDialog() {
		KidlWebEditorTab tab = getSelectedTab();
		if (tab == null) {
			Window.alert("No module is open");
			return;
		}
		final String spec = tab.getAceEditor().getText();
		final List<String> allTypes = tab.getAllTypeNames();
		Set<String> prevRegTypes = new TreeSet<String>();
		String mod = getModuleNameForSelectedTab();
		if (mod2type2ver.containsKey(mod))
			prevRegTypes.addAll(mod2type2ver.get(mod).keySet());
		/////////////////////////////////////////////////////////
		final DialogBox dialogBox = new DialogBox();
		dialogBox.getElement().getStyle().setZIndex(10000);
		dialogBox.setText("Module registration");
		dialogBox.setAnimationEnabled(true);
		FlowPanel main = new FlowPanel();
		dialogBox.setWidget(main);
		main.add(new HTML("Module name: "));
		final TextBox tb = new TextBox();
		tb.setValue(mod);
		tb.getElement().getStyle().setDisplay(Display.BLOCK);
		main.add(tb);
		main.add(new HTML("<br>"));
		final CheckBox cb = new CheckBox("dry mode");
		cb.getElement().getStyle().setDisplay(Display.BLOCK);
		main.add(cb);
		main.add(new HTML("<br>"));
		main.add(new HTML("Selected types: "));
		FlexTable ft = new FlexTable();
		final Map<String, CheckBox> typeToCheck = new HashMap<String, CheckBox>();
		for (int i = 0; i < allTypes.size(); i++) {
			String type = allTypes.get(i);
			CheckBox typeCh = new CheckBox(type);
			if (prevRegTypes.contains(type))
				typeCh.setValue(true);
			ft.setWidget(i, 0, typeCh);
			typeToCheck.put(type, typeCh);
		}
		ScrollPanel sp = new ScrollPanel(ft);
		sp.setWidth("300px");
		sp.setHeight("300px");
		main.add(sp);
		main.add(new HTML("<br>"));
		final Button regButton = new Button("Register");
		regButton.getElement().setAttribute("style", "margin: 15px 6px 6px;");
		main.add(regButton);
		regButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				String moduleName = tb.getValue();
				if (moduleName == null || moduleName.trim().isEmpty()) {
					Window.alert("Module can not be empty if request ownership checkbox is selected.");
					return;
				}
				String mod = moduleName.trim();
				Set<String> prevRegTypes = new TreeSet<String>();
				if (mod2type2ver.containsKey(mod))
					prevRegTypes.addAll(mod2type2ver.get(mod).keySet());
				JSO typesToAdd = JSO.newArray();
				JSO typesToDel = JSO.newArray();
				for (String type : prevRegTypes)
					if (typeToCheck.containsKey(type) && !typeToCheck.get(type).getValue())
						typesToDel.add(type);
				for (String type : typeToCheck.keySet())
					if (typeToCheck.get(type).getValue() && !prevRegTypes.contains(type))
						typesToAdd.add(type);
				JSO param = JSO.newObject();
				param.put("spec", spec);
				param.put("new_types", typesToAdd);
				param.put("remove_types", typesToDel);
				param.put("dryrun", cb.getValue() ? 1 : 0);
				wsCall2(jsServices, wsUrl, token, "register_typespec", param, new Callback<JSO, String>() {
					@Override
					public void onSuccess(JSO result) {
						dialogBox.hide();
						focus();
					}
					@Override
					public void onFailure(String reason) {
						Window.alert("Error requesting ownership: " + reason);
					}
				});
			}
		});
		final Button cancelButton = new Button("Cancel", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				dialogBox.hide();
				focus();
			}
		});
		main.add(cancelButton);
		dialogBox.center();
		dialogBox.show();
	}

	public String getModuleNameForSelectedTab() {
		String tabName = getSelectedTabName();
		if (tabName == null)
			return "";
		KidlWebEditorTab tab = tabMap.get(tabName);
		String ret = tab.guessModuleName();
		if (ret == null)
			ret = tabName;
		return ret;
	}
	
	public String getStackTrace(Throwable throwable) {
	    String ret="";
	    while (throwable!=null) {
	            if (throwable instanceof com.google.gwt.event.shared.UmbrellaException){
	                    for (Throwable thr2 :((com.google.gwt.event.shared.UmbrellaException)throwable).getCauses()){
	                            if (ret != "")
	                                    ret += "\nCaused by: ";
	                            ret += thr2.toString();
	                            ret += "\n  at "+getStackTrace(thr2);
	                    }
	            } else if (throwable instanceof com.google.web.bindery.event.shared.UmbrellaException){
	                    for (Throwable thr2 :((com.google.web.bindery.event.shared.UmbrellaException)throwable).getCauses()){
	                            if (ret != "")
	                                    ret += "\nCaused by: ";
	                            ret += thr2.toString();
	                            ret += "\n  at "+getStackTrace(thr2);
	                    }
	            } else {
	                    if (ret != "")
	                            ret += "\nCaused by: ";
	                    ret += throwable.toString();
	                    for (StackTraceElement sTE : throwable.getStackTrace())
	                            ret += "\n  at "+sTE;
	            }
	            throwable = throwable.getCause();
	    }

	    return ret;
	}
	
	public void resize() {
		KidlWebEditorTab tab = getSelectedTab();
		if (tab != null)
			tab.getAceEditor().resize();
		tabPanel.checkIfScrollButtonsNecessary();
	}
	
	public void focus() {
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				KidlWebEditorTab tab = getSelectedTab();
				if (tab != null)
					tab.getAceEditor().focus();
			}
		});
	}
	
	public native void lg(Object message) /*-{
    	console.log(message);
	}-*/;
	
	public void wsCall(JavaScriptObject jsServices, String authToken, String method, String jsonParam, Callback<JSO, String> callback) {
		wsCall2(jsServices, wsUrl, authToken, method, JSO.fromJson(jsonParam), callback);
	}

	public native void wsCall2(JavaScriptObject jsServices, String wsUrl, String authToken, String method, Object param, Callback<JSO, String> callback) /*-{
        var kbws = jsServices['kbws'];
        if (!kbws) {
            kbws = new $wnd.Workspace(wsUrl, {token: authToken});
            jsServices['kbws'] = kbws;
        }
		kbws[method](param, function(data) {
			if (typeof data === "boolean" || typeof data === "number" || typeof data === "string")
				data = {data: data};
			callback.@com.google.gwt.core.client.Callback::onSuccess(Ljava/lang/Object;) (data);
		}, function(data) {
			callback.@com.google.gwt.core.client.Callback::onFailure(Ljava/lang/Object;) (data.error.error);
		});
	}-*/;

    public void undo() {
        KidlWebEditorTab tab = getSelectedTab();
        if (tab != null)
            tab.getAceEditor().undo();
        focus();
    }

    public void redo() {
        KidlWebEditorTab tab = getSelectedTab();
        if (tab != null)
            tab.getAceEditor().redo();
        focus();
    }

    public void autocomplete() {
        KidlWebEditorTab tab = getSelectedTab();
        if (tab != null)
            tab.getAceEditor().autocomplete();
        focus();
    }

    public void find() {
        KidlWebEditorTab tab = getSelectedTab();
        if (tab != null)
            tab.getAceEditor().openFind();
        focus();
    }

    public void logIn() {
        final DialogBox dialogBox = new DialogBox();
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
                        KidlWebEditorPanel.this.token = result;
                        KidlWebEditorPanel.this.jsServices.removeAllKeys();
                        listAllTypes(new Callback<Boolean, Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                dialogBox.hide();
                                focus();
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
                focus();
            }
        });
        main.add(cancelButton);
        dialogBox.center();
        dialogBox.show();
    }
    
    public void keyboardShortcuts() {
        KidlWebEditorTab tab = getSelectedTab();
        if (tab != null)
            tab.getAceEditor().showKeyboardShortcuts();
        focus();
    }

    public MenuBar createMenu() {
        final KidlWebEditorPanel mainPanel = this;
        MenuBar menu = new MenuBar();
        MenuBar sysMenu = new MenuBar(true);
        menu.addItem("System", sysMenu);
        sysMenu.addItem("Log In", new Command() {
            @Override
            public void execute() {
                mainPanel.logIn();
            }
        });
        MenuBar regMenu = new MenuBar(true);
        menu.addItem("Registration", regMenu);
        MenuBar edtMenu = new MenuBar(true);
        menu.addItem("Edit", edtMenu);
        edtMenu.addItem("Undo", new Command() {
            @Override
            public void execute() {
                mainPanel.undo();
            }
        });
        edtMenu.addItem("Redo", new Command() {
            @Override
            public void execute() {
                mainPanel.redo();
            }
        });
        edtMenu.addItem("Autocomplete", new Command() {
            @Override
            public void execute() {
                mainPanel.autocomplete();
            }
        });
        edtMenu.addItem("Find", new Command() {
            @Override
            public void execute() {
                mainPanel.find();
            }
        });
        MenuBar hlpMenu = new MenuBar(true);
        menu.addItem("Help", hlpMenu);
        hlpMenu.addItem("Keyboard shortcuts", new Command() {
            @Override
            public void execute() {
                mainPanel.keyboardShortcuts();
            }
        });
        return menu;
    }
    
	public static class DblClckTree extends Tree {
		public DblClckTree() {
	        sinkEvents(Event.ONDBLCLICK);
	    }

	    public void onBrowserEvent(Event event)  {
	        super.onBrowserEvent(event);
	        if (DOM.eventGetType(event) == Event.ONDBLCLICK){
	            handleDoubleClick();
	        }
	    }
	    
	    public void handleDoubleClick() {
	    	TreeItem item = getSelectedItem();
	    	if (item instanceof DblClckTreeItem) {
	    		((DblClckTreeItem)item).onDoubleClick();
	    	}
	    }
	}
	
	public static class DblClckTreeItem extends TreeItem {
		public DblClckTreeItem(SafeHtml sh) {
			super(sh);
		}
		
		public void onDoubleClick() {
		}
	}
}

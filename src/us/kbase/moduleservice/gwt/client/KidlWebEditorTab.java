package us.kbase.moduleservice.gwt.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import us.kbase.moduleservice.gwt.client.parser.CustomParseException;
import us.kbase.moduleservice.gwt.client.parser.IncludeProvider;
import us.kbase.moduleservice.gwt.client.parser.KbModule;
import us.kbase.moduleservice.gwt.client.parser.KidlParseException;
import us.kbase.moduleservice.gwt.client.parser.ModuleTokens;
import us.kbase.moduleservice.gwt.client.parser.ParseException;
import us.kbase.moduleservice.gwt.client.parser.SpecParser;
import us.kbase.moduleservice.gwt.client.parser.Token;
import us.kbase.moduleservice.gwt.client.parser.TokenMgrException;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;

import edu.ycp.cs.dh.acegwt.client.ace.AceAnnotationType;
import edu.ycp.cs.dh.acegwt.client.ace.AceCompletion;
import edu.ycp.cs.dh.acegwt.client.ace.AceCompletionValue;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditor;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorCallback;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorCursorPosition;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorTheme;

public class KidlWebEditorTab extends DockLayoutPanel {
	private final KidlWebEditorPanel parent;
	private String moduleName = null;
	private final String token;
	private AceEditor ta = null;
	private final JavaScriptObject jsServices;
	private PopupPanel popup = new PopupPanel(true, false);
	private MenuBar popupMenu = new MenuBar(true);
	private ModuleTokens mt = null;
	private Map<String, KbModule> localModules = null;

	public KidlWebEditorTab(final KidlWebEditorPanel parent, final String moduleName, String token, 
			JavaScriptObject jsServices, final String typeToShow, boolean isNew, final boolean readOnly) {
		super(Unit.PX);
		this.parent = parent;
		this.moduleName = moduleName;
		this.token = token;
		this.jsServices = jsServices;
		ta = new AceEditor();
		this.add(ta);
		ta.startEditor(); // must be called before calling setTheme/setMode/etc.
		ta.setTheme(AceEditorTheme.ECLIPSE);
		ta.setModeByName("kidl");
		ta.setAutocompleteEnabled(true);
		
		popup.add(popupMenu);
		popup.getElement().getStyle().setProperty("padding", "0px");
		popup.getElement().getStyle().setProperty("border", "1px solid #979797");

		TokenTooltip.install(ta.getJSO(), new TokenTooltip.TokenTooltipListener() {
			@Override
			public String onTokenMouseMove(JSO token, int row) {
				try {
				String[] info = getTokenUsageInfo(token, row);
				if (info != null) {
					String value = "";
					String comment = null;
					if (info.length == 1) {
						value = "module " + info[0];
						if (localModules != null && localModules.containsKey(info[0]))
							comment = localModules.get(info[0]).getComment();
						if (comment == null && parent.getLoadedModules().containsKey(info[0])) 
							comment = parent.getLoadedModules().get(info[0]).getComment();
					} else if (info.length == 2) {
						if (info[0] == null) {
							parent.lg("Null in type usage info!!!");
							return null;
						}
						value = "type " + (info[0] == null ? info[1] : (info[0] + "." + info[1]));
						String modName = info[0];
						if (localModules != null && localModules.containsKey(modName))
							comment = localModules.get(modName).getTypeToDoc().get(info[1]);
						if (comment == null && parent.getLoadedModules().containsKey(modName)) {
							KbModule moduleInfo = parent.getLoadedModules().get(modName);
							comment = moduleInfo.getTypeToDoc().get(info[1]);
						}
					}
					value = "Double click to jump to " + value;
					if (comment != null) {
						comment = comment.trim();
						if (!comment.isEmpty()) {
							value += "\n\nDescription:\n" + comment;
						}
					}
					return value;
				}
				} catch (Throwable ex) {
					parent.lg(parent.getStackTrace(ex));
				}
				return null;
			}
		});

		ta.addOnTokenMouseDoubleClickListener(new AceEditor.TokenMouseDoubleClickListener() {
			@Override
			public boolean onMouseDoubleClick(JavaScriptObject token, int row, int btn) {
				String[] info = getTokenUsageInfo((JSO)token, row);
				final AceEditorCursorPosition pos = ta.getCursorPosition();
				if (info != null) {
					if (info.length == 1) {
						parent.openTab(info[0], null, false);
						new Timer() {
							public void run() {
								parent.lg("dblclck: row=" + pos.getRow() + ", col=" + pos.getColumn());
								ta.setCursor(pos.getRow(), pos.getColumn());
							}
						}.schedule(100);
					} else if (info.length == 2) {
						if (info[0] == null) {
							parent.lg("Null in type usage info!!!");
							return false;
						} else if (localModules != null && localModules.containsKey(info[0])) {
							scrollToType(info[1]);
						} else {
							parent.openTab(info[0], info[1], false);
							new Timer() {
								public void run() {
									parent.lg("dblclck: row=" + pos.getRow() + ", col=" + pos.getColumn());
									ta.setCursor(pos.getRow(), pos.getColumn());
								}
							}.schedule(100);
						}
					}
					return true;
				}
				return false;
			}
		});
		
		if (!isNew) {
			ta.setText("Please wait, loading document...");
			ta.setReadOnly(true);
			parent.wsCall(jsServices, token, "get_module_info", "{\"mod\": \"" + moduleName + "\"}", new Callback<JSO, String>() {
				@Override
				public void onSuccess(JSO result) {
					String spec = (String)result.getString("spec");
					ta.setText(spec);
					ta.setReadOnly(readOnly);
					parseSpec();
					ta.addOnChangeHandler(new AceEditorCallback() {
						@Override
						public void invokeAceCallback(JavaScriptObject obj) {
							try {
								parseSpec();
							} catch (Throwable e) {
								parent.lg(parent.getStackTrace(e));
							}
						}
					});
					scrollToType(typeToShow);
				}
				@Override
				public void onFailure(String reason) {
					ta.setText(reason);
					parent.lg(reason);
				}
			});
		} else {
			ta.addOnChangeHandler(new AceEditorCallback() {
				@Override
				public void invokeAceCallback(JavaScriptObject obj) {
					try {
						parseSpec();
					} catch (Throwable e) {
						parent.lg(parent.getStackTrace(e));
					}
				}
			});
		}
	}

	public void scrollToType(String type) {
		if (type != null) {
			final int[] rowCol = mt.typedefToRowCol.get(type);
			if (rowCol != null) {
				Timer timer = new Timer() {
					public void run() {
						ta.setCursor(rowCol[0], rowCol[1]);
						ta.scrollToLine(rowCol[0]);
					}
				};
				timer.schedule(100);
			}
		}
	}
	
	private String[] getTokenUsageInfo(JSO token, int row) {
		try {
		String type = token.getString("type");
		if (type == null || !type.equals("identifier"))
			return null;
		int col = token.getInt("start");
		String[] info = null;
		if (mt != null) {
			Map<Integer, String[]> colToInfo = mt.rowToColToModuleType.get(row);
			if (colToInfo != null) {
				info = colToInfo.get(col);
			}
		}
		return info;
		} catch (Throwable ex) {
			parent.lg(parent.getStackTrace(ex));
			return null;
		}
	}
	
	private void addMenuItem(String text) {
		MenuItem item = new MenuItem(new SafeHtmlBuilder().appendHtmlConstant("<font size='1'>")
				.appendEscaped(text).appendHtmlConstant("</font>").toSafeHtml(), new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				//lg("menu!");
			}
		});
		popupMenu.addItem(item);
		item.getElement().getStyle().setProperty("padding", "0px 5px");
		item.getElement().getStyle().setProperty("borderSpacing", "0px");
	}
	
	private void parseSpec() {
		String text = ta.getText();
		//int[] pos = getTextPos();
		//String posInfo = "Position: " + getTextPosLabel(pos);
		ta.clearAnnotations();
		try {
			mt = new ModuleTokens();
			Map<String, KbModule> local = new SpecParser(text).SpecStatement(new IncludeProvider() {
				@Override
				public Map<String, KbModule> parseInclude(String includeLine)
						throws KidlParseException {
					String moduleName = parseIncludeLine(includeLine);
					Map<String, KbModule> ret = new HashMap<String, KbModule>();
					KbModule module = parent.getLoadedModules().get(moduleName);
					if (module == null) {
						Map<String, String> type2ver = parent.getMod2type2ver().get(moduleName);
						if (type2ver == null)
							throw new IllegalStateException("Unknown module: " + moduleName);
						module = new KbModule(moduleName, "Documentation is loading...");
						for (String type : type2ver.keySet())
							module.getTypeToDoc().put(type, "Documentation is loading...");
						parent.addModuleAndLoadDocs(module);
					}
					ret.put(moduleName, module);
					return ret;
				}
			}, mt);
			localModules = local;
		} catch (ParseException ex) {
			Token tok = (ex instanceof CustomParseException) ? ((CustomParseException)ex).getToken() : ex.currentToken.next;
			String error = "" + ex.getMessage();
			ta.addAnnotation(tok.beginLine - 1, tok.beginColumn - 1, error, AceAnnotationType.ERROR);
			ta.setAnnotations();
		} catch (TokenMgrException e) {
			String error = e.getMessage();
			String prefix = "Lexical error at line ";
			boolean showed = false;
			if (error.contains(prefix)) {
				String rowText = error.substring(error.indexOf(prefix) + prefix.length());
				if (rowText.contains(",")) {
					rowText = rowText.substring(0, rowText.indexOf(',')).trim();
					try {
						int row = Integer.parseInt(rowText);
						ta.addAnnotation(row - 1, 0, error, AceAnnotationType.ERROR);
						ta.setAnnotations();
						showed = true;
					} catch (NumberFormatException ex) {}
				}
			}
			if (!showed)
				parent.lg(error);
		}
	}
	
	private String parseIncludeLine(String includeLine) {
		String moduleName = includeLine;
		if (moduleName.startsWith("#include"))
			moduleName = moduleName.substring(8).trim();
		if (moduleName.startsWith("<") && moduleName.endsWith(">"))
			moduleName = moduleName.substring(1, moduleName.length() - 1).trim();
		if (moduleName.contains("/"))
			moduleName = moduleName.substring(moduleName.lastIndexOf('/') + 1).trim();
		if (moduleName.contains("\\"))
			moduleName = moduleName.substring(moduleName.lastIndexOf('\\') + 1).trim();
		if (moduleName.contains("."))
			moduleName = moduleName.substring(0, moduleName.indexOf('.')).trim();
		return moduleName;
	}
	
	public List<AceCompletion> getAutocompletionProposals(AceEditorCursorPosition pos, String prefix) {
		List<AceCompletion> ret = new ArrayList<AceCompletion>();
		String text = ta.getText();
		int indPos = ta.getIndexFromPosition(ta.getCursorPosition());
		text = text.substring(0, indPos);
		//parent.lg(text);
		try {
			ModuleTokens mt = new ModuleTokens();
			new SpecParser(text).SpecStatement(new IncludeProvider() {
				@Override
				public Map<String, KbModule> parseInclude(String includeLine)
						throws KidlParseException {
					String moduleName = parseIncludeLine(includeLine);
					Map<String, KbModule> ret = new HashMap<String, KbModule>();
					KbModule module = parent.getLoadedModules().get(moduleName);
					if (module == null) 
						throw new IllegalStateException("Unknown module: " + moduleName);
					ret.put(moduleName, module);
					return ret;
				}
			}, mt);
		} catch (ParseException ex) {
			if (ex instanceof CustomParseException) {
				CustomParseException cpe = (CustomParseException)ex;
				Token t = cpe.getToken();
				int trow = t.beginLine - 1;
				int tcol = t.beginColumn - 1;
				int erow = pos.getRow();
				int ecol = pos.getColumn();
				if (trow == erow && tcol < ecol) {
					for (String kwd : cpe.getAltKeywords())
						ret.add(new AceCompletionValue(kwd, kwd, "language", 10));
					for (String mod : cpe.getAltModules())
						ret.add(new AceCompletionValue(mod, mod, "module", 10));
					for (String type : cpe.getAltTypes())
						ret.add(new AceCompletionValue(type, type, "type", 10));
				}
			} else {
				Set<String> items = new TreeSet<String>();
				for (int i = 0; i < ex.expectedTokenSequences.length; i++) 
					if (ex.expectedTokenSequences[i].length > 0)
						items.add(ex.tokenImage[ex.expectedTokenSequences[i][0]]);
				for (String item : items) {
					if (item.startsWith("\"") && item.endsWith("\""))
						item = item.substring(1, item.length() - 1).trim();
					if (!item.isEmpty())
						ret.add(new AceCompletionValue(item, item, "language", 10));
				}
			}
		} catch (TokenMgrException ignore) {
		}
		return ret;
	}
	
	public AceEditor getAceEditor() {
		return ta;
	}
	
	public String guessModuleName() {
		if (localModules != null && localModules.keySet().size() == 1)
			return localModules.keySet().iterator().next();
		return null;
	}
	
	public List<String> getAllTypeNames() {
		Set<String> ret = new TreeSet<String>();
		if (localModules != null) {
			for (String mod : localModules.keySet()) {
				for (String type : localModules.get(mod).getTypeToDoc().keySet())
					ret.add(type);
			}
		}
		return new ArrayList<String>(ret);
	}
}

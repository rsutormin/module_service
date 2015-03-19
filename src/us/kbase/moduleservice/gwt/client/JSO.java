package us.kbase.moduleservice.gwt.client;

import com.google.gwt.core.client.JavaScriptObject;

public class JSO extends JavaScriptObject {
    protected JSO() {}
    public final native boolean isArray() /*-{
    	return this instanceof Array;
    }-*/;
    public final native boolean isObject() /*-{
    	if (this instanceof Array)
    		return false;
    	if (typeof this === "boolean" || typeof this === "number" || typeof this === "string")
    		return false;
		return true;
	}-*/;
    public final native int length() /*-{
		return this.length;
	}-*/;
    public final native JSO keys() /*-{
		var ret = [];
		for (var key in this)
			ret.push(key);
		return ret;
	}-*/;
    public final native JSO get(int i) /*-{
      return this[i];
    }-*/;
    public final native JSO get(String key) /*-{
      return this[key];
    }-*/;
    public final native JSO getFromJson(int i) /*-{
      var json = this[i];
      return $wnd.$.parseJSON(json);
  	}-*/;
    public final native JSO getFromJson(String key) /*-{
      var json = this[key];
      return $wnd.$.parseJSON(json);
  	}-*/;
    public final native String getString(int i) /*-{
      return this[i];
    }-*/;
    public final native String getString(String key) /*-{
      return this[key];
    }-*/;
    public final native int getInt(int i) /*-{
      return this[i];
    }-*/;
    public final native int getInt(String key) /*-{
      return this[key];
    }-*/;
    public final native double getDouble(int i) /*-{
      return this[i];
    }-*/;
    public final native double getDouble(String key) /*-{
      return this[key];
    }-*/;
    public final native void add(Object item) /*-{
    	return this.push(item);
  	}-*/;
    public final native void put(String key, Object value) /*-{
		return this[key] = value;
	}-*/;
    public final native void add(int item) /*-{
		return this.push(item);
	}-*/;
    public final native void put(String key, int value) /*-{
		return this[key] = value;
	}-*/;
    public final native void add(double item) /*-{
		return this.push(item);
	}-*/;
    public final native void put(String key, double value) /*-{
		return this[key] = value;
	}-*/;

    public static final native JSO newArray() /*-{
    	return [];
  	}-*/;
    public static final native JSO newObject() /*-{
		return {};
	}-*/;
    public static final native JSO fromJson(String json) /*-{
    	return $wnd.$.parseJSON(json);
	}-*/;
}

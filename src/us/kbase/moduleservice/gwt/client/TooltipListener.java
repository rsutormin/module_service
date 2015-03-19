package us.kbase.moduleservice.gwt.client;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

class TooltipListener {
	private static final String DEFAULT_TOOLTIP_STYLE = "TooltipPopup";
	private static final int DEFAULT_OFFSET_X = 5;
	private static final int DEFAULT_OFFSET_Y = 15;

	public static void addFor(Widget w, String text, int delay) {
		TooltipListener tl = new TooltipListener(text, delay);
		w.addDomHandler(tl.getMouseOverHandler(w), MouseOverEvent.getType());
		w.addDomHandler(tl.getMouseOutHandler(), MouseOutEvent.getType());
	}
	
	private static class Tooltip extends PopupPanel {
		private int delay;

		public Tooltip(Widget sender, final String text, final int delay, final String styleName) {
			super(true);

			this.delay = delay;

			HTML contents = new HTML(text);
			add(contents);

			Style s = getElement().getStyle();
			s.setDisplay(Style.Display.INLINE);
			s.setMarginLeft(10, Unit.PX);
			s.setPadding(2, Unit.PX);
			s.setBackgroundColor("#ffe");
			s.setZIndex(10000);
			s.setBorderWidth(1, Unit.PX);
			s.setBorderColor("black");
			s.setBorderStyle(BorderStyle.SOLID);
		}

		public void show() {
			super.show();

			Timer t = new Timer() {

				public void run() {
					Tooltip.this.hide();
				}

			};
			t.schedule(delay);
		}
	}

	private Tooltip tooltip;
	private String text;
	private String styleName;
	private int delay;
	private int offsetX = DEFAULT_OFFSET_X;
	private int offsetY = DEFAULT_OFFSET_Y;

	public TooltipListener(String text, int delay) {
		this(text, delay, DEFAULT_TOOLTIP_STYLE);
	}

	public TooltipListener(String text, int delay, String styleName) {
		this.text = text;
		this.delay = delay;
		this.styleName = styleName;
	}

	public MouseOverHandler getMouseOverHandler(final Widget sender) {
		return new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				if (tooltip == null) {
					tooltip = new Tooltip(sender, text, delay, styleName);
				} else {
					tooltip.hide();
				}
				int left = event.getClientX();  // sender.getAbsoluteLeft();
				left += offsetX;
				int top = event.getClientY();  // sender.getAbsoluteTop();
				top += offsetY;
				tooltip.setPopupPosition(left, top);
				tooltip.show();
			}
		};
	}
	
	public MouseOutHandler getMouseOutHandler() {
		return new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				if (tooltip != null) {
					tooltip.hide();
				}
			}
		};
	}
	
	public String getStyleName() {
		return styleName;
	}

	public void setStyleName(String styleName) {
		this.styleName = styleName;
	}

	public int getOffsetX() {
		return offsetX;
	}

	public void setOffsetX(int offsetX) {
		this.offsetX = offsetX;
	}

	public int getOffsetY() {
		return offsetY;
	}

	public void setOffsetY(int offsetY) {
		this.offsetY = offsetY;
	}
} 
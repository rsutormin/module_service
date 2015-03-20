package us.kbase.moduleservice.gwt.client;

import com.google.gwt.dom.client.Style.Unit; 
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.ClickEvent; 
import com.google.gwt.event.dom.client.ClickHandler; 
import com.google.gwt.event.logical.shared.ResizeEvent; 
import com.google.gwt.event.logical.shared.ResizeHandler; 
import com.google.gwt.event.shared.HandlerRegistration; 
import com.google.gwt.user.client.Command; 
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand; 
import com.google.gwt.user.client.Window; 
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel; 
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel; 
import com.google.gwt.user.client.ui.TabLayoutPanel; 
import com.google.gwt.user.client.ui.Widget; 

/** 
 * A {@link TabLayoutPanel} that shows scroll buttons if necessary 
 */ 
public class ScrolledTabLayoutPanel extends TabLayoutPanel { 

        private static final int IMAGE_PADDING_PIXELS = 4; 

        private LayoutPanel panel; 
        private FlowPanel tabBar; 
        private Button scrollLeftButton; 
        private Button scrollRightButton; 
        private HandlerRegistration windowResizeHandler; 
        private int imageSize = 20;

        public ScrolledTabLayoutPanel(double barHeight, Unit barUnit) { 
                super(barHeight, barUnit); 

                // The main widget wrapped by this composite, which is a LayoutPanel with the tab bar & the tab content 
                panel = (LayoutPanel) getWidget(); 

                // Find the tab bar, which is the first flow panel in the LayoutPanel 
                for (int i = 0; i < panel.getWidgetCount(); ++i) { 
                        Widget widget = panel.getWidget(i); 
                        if (widget instanceof FlowPanel) { 
                                tabBar = (FlowPanel) widget; 
                                break; // tab bar found 
                        } 
                } 

                initScrollButtons(); 
        } 

        @Override 
        public void add(Widget child, String text) {
        	super.add(child, text);
        	checkIfScrollButtonsNecessary();
        }

        public void add(final Widget child, final String text, boolean closable) {
            if (closable) {
                HorizontalPanel hPanel = new HorizontalPanel();
                hPanel.getElement().getStyle().setProperty("marginBottom", "-2px");
                final Label label = new Label(text);
                DOM.setStyleAttribute(label.getElement(), "whiteSpace", "nowrap");
                hPanel.add(label);
                hPanel.add(new HTML("&nbsp;"));
                HTML closeBtn = new HTML("&#10006;");
                closeBtn.addClickHandler(new ClickHandler() { 
                    @Override 
                    public void onClick(ClickEvent event) {
                        int widgetIndex = getWidgetIndex(child);
                        if (widgetIndex == getSelectedIndex()) {
                            int selInd = widgetIndex + 1 == getWidgetCount() ? (widgetIndex - 1) : (widgetIndex + 1);
                            if (selInd >= 0)
                                selectTab(selInd);
                        }
                        remove(widgetIndex);
                        afterTabWasClosed(child, text);
                    }
                });
                hPanel.add(closeBtn);
                super.add(child, hPanel);
            } else {
                super.add(child, text);
            }
            checkIfScrollButtonsNecessary();
        }
        
        public void afterTabWasClosed(Widget w, String text) {
            // Method could be overwritten
        }

        @Override 
        public void add(Widget child, Widget tab) { 
                super.add(child, tab); 
                checkIfScrollButtonsNecessary(); 
        } 

        @Override 
        public boolean remove(Widget w) { 
                boolean b = super.remove(w); 
                checkIfScrollButtonsNecessary(); 
                return b; 
        } 

        @Override 
        protected void onLoad() { 
                super.onLoad(); 

                if (windowResizeHandler == null) { 
                        windowResizeHandler = Window.addResizeHandler(new ResizeHandler() { 
                                @Override 
                                public void onResize(ResizeEvent event) { 
                                        checkIfScrollButtonsNecessary(); 
                                } 
                        }); 
                } 
        } 

        @Override 
        protected void onUnload() { 
                super.onUnload(); 

                if (windowResizeHandler != null) { 
                        windowResizeHandler.removeHandler(); 
                        windowResizeHandler = null; 
                } 
        } 

        private ClickHandler createScrollClickHandler(final int diff) { 
                return new ClickHandler() { 
                        @Override 
                        public void onClick(ClickEvent event) { 
                                Widget lastTab = getLastTab(); 
                                if (lastTab == null) 
                                        return; 

                                int newLeft = parsePosition(tabBar.getElement().getStyle().getLeft()) + diff; 
                                int rightOfLastTab = getRightOfWidget(lastTab); 

                                // Prevent scrolling the last tab too far away form the right border, 
                                // or the first tab further than the left border position 
                                if (newLeft <= 0 && (getTabBarWidth() - newLeft < (rightOfLastTab + 20))) { 
                                        scrollTo(newLeft); 
                                } 
                        } 
                }; 
        } 

        /** Create and attach the scroll button images with a click handler */ 
        private void initScrollButtons() { 
                scrollLeftButton = new Button("&#65513;");
                scrollLeftButton.getElement().getStyle().setWidth(imageSize, Unit.PX);
                scrollLeftButton.getElement().getStyle().setHeight(imageSize, Unit.PX);
                scrollLeftButton.getElement().getStyle().setProperty("padding", "0px 0px");
                scrollLeftButton.getElement().getStyle().setPaddingBottom(5, Unit.PX);
                panel.insert(scrollLeftButton, 0); 
                panel.setWidgetLeftWidth(scrollLeftButton, 0, Unit.PX, imageSize, Unit.PX); 
                panel.setWidgetTopHeight(scrollLeftButton, 0, Unit.PX, imageSize, Unit.PX); 
                scrollLeftButton.addClickHandler(createScrollClickHandler(+20)); 
                scrollLeftButton.setVisible(false); 

                scrollRightButton = new Button("&#65515;"); 
                scrollRightButton.getElement().getStyle().setWidth(imageSize, Unit.PX);
                scrollRightButton.getElement().getStyle().setHeight(imageSize, Unit.PX);
                scrollRightButton.getElement().getStyle().setProperty("padding", "0px 0px");
                panel.insert(scrollRightButton, 0); 
                panel.setWidgetLeftWidth(scrollRightButton, imageSize + IMAGE_PADDING_PIXELS, Unit.PX, imageSize, Unit.PX); 
                panel.setWidgetTopHeight(scrollRightButton, 0, Unit.PX, imageSize, Unit.PX); 
                scrollRightButton.addClickHandler(createScrollClickHandler(-20)); 
                scrollRightButton.setVisible(false); 
        } 

        public void checkIfScrollButtonsNecessary() { 
                // Defer size calculations until sizes are available, when calculating immediately after 
                // add(), all size methods return zero 
                DeferredCommand.addCommand(new Command() { 
                        @Override 
                        public void execute() { 
                                boolean isScrolling = isScrollingNecessary(); 
                                // When the scroll buttons are being hidden, reset the scroll position to zero to 
                                // make sure no tabs are still out of sight 
                                if (scrollRightButton.isVisible() && !isScrolling) { 
                                        resetScrollPosition(); 
                                } 
                                scrollRightButton.setVisible(isScrolling); 
                                scrollLeftButton.setVisible(isScrolling); 
                        } 
                }); 
        } 

        private void resetScrollPosition() { 
                scrollTo(0); 
        } 

        private void scrollTo(int pos) { 
                tabBar.getElement().getStyle().setLeft(pos, Unit.PX); 
        } 

        private boolean isScrollingNecessary() { 
                Widget lastTab = getLastTab(); 
                if (lastTab == null) 
                        return false; 
                return getRightOfWidget(lastTab) > getTabBarWidth(); 
        } 

        private int getRightOfWidget(Widget widget) { 
                return widget.getElement().getOffsetLeft() + widget.getElement().getOffsetWidth(); 
        } 

        private int getTabBarWidth() { 
                return tabBar.getElement().getParentElement().getClientWidth(); 
        } 

        private Widget getLastTab() { 
                if (tabBar.getWidgetCount() == 0) 
                        return null; 

                return tabBar.getWidget(tabBar.getWidgetCount() - 1); 
        } 

        private static int parsePosition(String positionString) { 
                int position; 
                try { 
                        for (int i = 0; i < positionString.length(); i++) { 
                                char c = positionString.charAt(i); 
                                if (c != '-' && !(c >= '0' && c <= '9')) { 
                                        positionString = positionString.substring(0, i); 
                                } 
                        } 

                        position = Integer.parseInt(positionString); 
                } catch (NumberFormatException ex) { 
                        position = 0; 
                } 
                return position; 
        } 
} 
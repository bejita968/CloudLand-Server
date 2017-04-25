package org.dragonet.cloudland.server.gui;

/**
 * Created on 2017/3/2.
 */
public abstract class ViewerWindow extends BaseGUIWindow {

    private GUIWindow original;

    public ViewerWindow(GUIWindow original) {
        super(original.getTitle());
        this.original = original;
    }
}

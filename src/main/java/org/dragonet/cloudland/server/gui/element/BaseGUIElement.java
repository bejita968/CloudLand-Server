package org.dragonet.cloudland.server.gui.element;

import org.dragonet.cloudland.net.protocol.GUI;
import org.dragonet.cloudland.server.entity.PlayerEntity;
import org.dragonet.cloudland.server.gui.InternalGUIElement;

/**
 * Created on 2017/3/14.
 */
public abstract class BaseGUIElement implements InternalGUIElement {
    public int x;
    public int y;
    public int width;
    public int height;

    public BaseGUIElement withSize(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        return this;
    }

    protected GUI.GUIElement.Builder createBuilder(){
        return GUI.GUIElement.newBuilder()
                .setType(getType())
                .setX(x)
                .setY(y)
                .setWidth(width)
                .setHeight(height);
    }

    @Override
    public void onAction(PlayerEntity player, int elementId, GUI.ClientWindowInteractMessage.WindowAction action, int param1, int param2) {
    }

    @Override
    public BaseGUIElement setRawDimensions(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        return this;
    }
}

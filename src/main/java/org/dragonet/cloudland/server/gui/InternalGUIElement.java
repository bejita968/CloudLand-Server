package org.dragonet.cloudland.server.gui;

import org.dragonet.cloudland.net.protocol.GUI;
import org.dragonet.cloudland.server.entity.PlayerEntity;

/**
 * Created on 2017/3/14.
 */
public interface InternalGUIElement {

    void onAction(PlayerEntity player, int elementId, GUI.ClientWindowInteractMessage.WindowAction action, int param1, int param2);

    GUI.GUIElementType getType();

    GUI.GUIElement serialize();
}

package org.dragonet.cloudland.server.gui;

import org.dragonet.cloudland.net.protocol.GUI;
import org.dragonet.cloudland.server.entity.PlayerEntity;

/**
 * Created on 2017/3/2.
 */
public interface GUIWindow {

    /**
     * Window unique ID is globally unique
     * @return
     */
    long getUniqueId();

    String getTitle();

    int getWindowId();
    void setWindowId(int id);

    InternalGUIElement[] getElements();

    int getWidth();
    int getHeight();

    boolean isOpenedTo(PlayerEntity player);

    PlayerEntity getOwner();

    void openTo(PlayerEntity owner);

    /**
     * resend all elements to update
     */
    void refresh();

    void close(PlayerEntity owner);
    void onClose();

    void onAction(PlayerEntity player, int elementId, GUI.ClientWindowInteractMessage.WindowAction action, int param1, int param2);
}

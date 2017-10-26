package org.dragonet.cloudland.server.gui;

import org.dragonet.cloudland.net.protocol.GUI;
import org.dragonet.cloudland.server.entity.PlayerEntity;

import java.util.List;

/**
 * Created on 2017/3/2.
 */
public interface GUIWindow {

    /**
     * Window unique ID is globally unique
     */
    int getUniqueId();

    String getTitle();

    InternalGUIElement[] getElements();

    int getWidth();
    int getHeight();

    boolean isOpenedTo(PlayerEntity player);

    List<PlayerEntity> getViewers();

    void openTo(PlayerEntity owner);

    /**
     * resend all elements to update
     */
    void refresh();

    void close(PlayerEntity owner);
    void onClose();

    void onAction(PlayerEntity player, int elementId, GUI.ClientWindowInteractMessage.WindowAction action, int param1, int param2);
}

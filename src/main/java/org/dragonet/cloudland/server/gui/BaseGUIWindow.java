package org.dragonet.cloudland.server.gui;

import org.dragonet.cloudland.net.protocol.GUI;
import org.dragonet.cloudland.server.CloudLandServer;
import org.dragonet.cloudland.server.entity.PlayerEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 * Created on 2017/3/2.
 */
public abstract class BaseGUIWindow implements GUIWindow {

    @Getter
    protected final long uniqueId = CloudLandServer.getServer().getNextWindowUniqueId();

    @Getter
    protected int windowId = -1;

    @Getter
    @Setter
    protected int width;
    @Getter
    @Setter
    protected int height;

    @Getter
    @Setter
    protected String title;

    @Getter
    protected PlayerEntity owner;

    @Getter
    public InternalGUIElement[] elements;

    public BaseGUIWindow(String title){
        this.title = title;
    }

    @Override
    public boolean isOpenedTo(PlayerEntity player) {
        return player.isWindowOpened(this);
    }

    @Override
    public void openTo(PlayerEntity owner) {
        owner.openWindow(this);
    }

    @Override
    public void close(PlayerEntity owner) {
        if(!isOpenedTo(owner)){
            return;
        }
        onClose();
        owner.closeWindow(windowId, false);
    }

    @Override
    public void onClose() {
        windowId = -1;

        // TODO: Close all viewers' window as well
    }

    @Override
    public void refresh() {
        if(windowId == -1) return;
        ArrayList<GUI.GUIElement> elements = new ArrayList<>();
        for(InternalGUIElement element : getElements()) {
            elements.add(element.serialize());
        }
        owner.getSession().sendNetworkMessage((GUI.ServerWindowOpenMessage.newBuilder()
                .setWindowId(getWindowId())
                .addAllItems(elements)
                .setWidth(getWidth())
                .setHeight(getHeight())
                .setTitle(title)
                .build()));
    }

    public void setWindowId(int windowId) {
        if(this.windowId != -1) {
            throw new IllegalStateException("window already opened! ");
        }
        this.windowId = windowId;
    }
}

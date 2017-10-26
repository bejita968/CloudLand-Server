package org.dragonet.cloudland.server.gui;

import org.dragonet.cloudland.net.protocol.GUI;
import org.dragonet.cloudland.server.CloudLandServer;
import org.dragonet.cloudland.server.entity.PlayerEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created on 2017/3/2.
 */
public abstract class BaseGUIWindow implements GUIWindow {

    @Getter
    protected final int uniqueId = CloudLandServer.getServer().getNextWindowUniqueId();

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
    public InternalGUIElement[] elements;

    public List<PlayerEntity> viewers = new LinkedList<>();

    public BaseGUIWindow(String title){
        this.title = title;
    }

    @Override
    public boolean isOpenedTo(PlayerEntity player) {
        return player.isWindowOpened(this);
    }

    @Override
    public void openTo(PlayerEntity owner) {
        if(viewers.contains(owner)) return;
        viewers.add(owner);
        owner.openWindow(this);
    }

    @Override
    public void close(PlayerEntity owner) {
        if(!isOpenedTo(owner)){
            return;
        }
        owner.closeWindow(uniqueId, false);
        viewers.remove(owner);
        onClose();
    }

    @Override
    public void onClose() {
        if (viewers.size() <= 0) {
            // this window can be closed and de-referenced
            System.out.println("Window un-referenced! ");
            // TODO: save data? maybe?
        }
    }

    @Override
    public void refresh() {
        ArrayList<GUI.GUIElement> elements = new ArrayList<>();
        for(InternalGUIElement element : getElements()) {
            elements.add(element.serialize());
        }
        viewers.forEach(p ->p.getSession().sendNetworkMessage((GUI.ServerWindowOpenMessage.newBuilder()
                .setWindowId(uniqueId)
                .addAllItems(elements)
                .setWidth(getWidth())
                .setHeight(getHeight())
                .setTitle(title)
                .build())));
    }

    public void setRawSize(int w, int h) {
        width = w;
        height = h;
    }

    public List<PlayerEntity> getViewers() {
        return Collections.unmodifiableList(viewers);
    }
}

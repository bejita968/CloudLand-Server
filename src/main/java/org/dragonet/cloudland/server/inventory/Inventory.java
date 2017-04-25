package org.dragonet.cloudland.server.inventory;

import org.dragonet.cloudland.server.gui.GUIWindow;
import org.dragonet.cloudland.server.item.Item;

/**
 * Created on 2017/1/15.
 */
public interface Inventory extends GUIWindow {

    int getSize();

    Item[] getItems();

    void setItems(Item[] items);

    void setSlot(int slot, Item item);

    /**
     * @param item
     * @return how many items added to the inventory
     */
    int addItem(Item item);

    void sendContents();
}

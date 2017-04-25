package org.dragonet.cloudland.server.inventory;

import org.dragonet.cloudland.net.protocol.GUI;
import org.dragonet.cloudland.server.entity.PlayerEntity;
import org.dragonet.cloudland.server.gui.BaseGUIWindow;
import org.dragonet.cloudland.server.gui.element.InventoryElement;
import org.dragonet.cloudland.server.item.Item;
import org.dragonet.cloudland.server.item.Items;
import lombok.Getter;

/**
 * Created on 2017/1/15.
 *
 * THIS DOES NOT MAINTAIN THE ELEMENTS STUFF
 */
public abstract class BaseInventory extends BaseGUIWindow implements Inventory {

    @Getter
    private InventoryElement inventoryElement = new InventoryElement();

    public BaseInventory(String title, int size){
        this(title, new Item[size]);
    }

    public BaseInventory(String title, Item[] items){
        super(title);
        inventoryElement.items = items;
    }

    @Override
    public int getSize() {
        return inventoryElement.items.length;
    }

    @Override
    public Item[] getItems() {
        return inventoryElement.items;
    }

    @Override
    public void setItems(Item[] items) {
        if(items == null) inventoryElement.items = null;
        if(inventoryElement.items.length != items.length) {
            throw new IllegalStateException("different item array size");
        }
        inventoryElement.items = items;
        sendContents();
    }

    @Override
    public int addItem(Item item) {
        int c = item.getCount();
        Items type = Items.get(item.getId(), item.getMeta());
        for (int i = 0; i < inventoryElement.items.length; i++) {
            if(c <= 0) break;
            if (inventoryElement.items[i] == null) {
                int slotCount = c > type.getMaxStack() ? type.getMaxStack() : c;
                inventoryElement.items[i] = item.clone();
                inventoryElement.items[i].setCount(slotCount);
                c -= slotCount;
            } else {
                if(!inventoryElement.items[i].equals(item)) continue;
                // Same type, try to merge
                int slotCount = inventoryElement.items[i].getCount() + c > type.getMaxStack() ? type.getMaxStack() - inventoryElement.items[i].getCount() : c;
                inventoryElement.items[i].setCount(inventoryElement.items[i].getCount() + slotCount);
                c -= slotCount;
            }
        }
        if(c > 0) {
            System.out.println("Didn't add all items");
        }
        sendContents();
        return item.getCount() - c;
    }

    @Override
    public void setSlot(int slot, Item item) {
        inventoryElement.items[slot] = item;
        sendContents();
    }

    @Override
    public void onAction(PlayerEntity player, int elementId, GUI.ClientWindowInteractMessage.WindowAction action, int param1, int param2) {
        if(elementId == 0) {
            inventoryElement.onAction(player, elementId, action, param1, param2);
            sendContents();
        }
    }
}

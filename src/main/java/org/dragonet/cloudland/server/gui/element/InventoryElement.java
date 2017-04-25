package org.dragonet.cloudland.server.gui.element;

import org.dragonet.cloudland.net.protocol.GUI;
import org.dragonet.cloudland.net.protocol.Metadata;
import org.dragonet.cloudland.server.entity.PlayerEntity;
import org.dragonet.cloudland.server.item.Item;
import org.dragonet.cloudland.server.item.Items;
import lombok.Getter;
import lombok.Setter;

/**
 * Created on 2017/3/14.
 */
public class InventoryElement extends BaseGUIElement {

    @Getter
    @Setter
    public Item[] items;

    @Override
    public GUI.GUIElementType getType() {
        return GUI.GUIElementType.INVENTORY;
    }

    @Override
    public GUI.GUIElement serialize() {
        GUI.GUIElement.Builder ele = createBuilder();
        Metadata.SerializedMetadata.Builder meta = Metadata.SerializedMetadata.newBuilder();
        for(int i = 0; i < items.length; i ++) {
            Metadata.SerializedMetadata.MetadataEntry.Builder ent = Metadata.SerializedMetadata.MetadataEntry.newBuilder();
            ent.setType(Metadata.SerializedMetadata.MetadataEntry.DataType.META);
            if(items[i] != null) {
                ent.setMetaValue(items[i].serializeToBinary());
            } else {
                ent.setMetaValue(Item.AIR_SERIALIZED);
            }
            meta.putEntries(i, ent.build());
        }
        ele.setValue(meta.build());
        return ele.build();
    }

    @Override
    public void onAction(PlayerEntity player, int elementId, GUI.ClientWindowInteractMessage.WindowAction action, int param1, int param2) {
        if(player.getCursorItem() != null && player.getCursorItem().getId() != 0) {
            // there is something on his/her hand
            if(param1 < items.length && items[param1] != null && items[param1].getId() != 0) {
                Items slotItem = Items.get(items[param1]);
                if(player.getCursorItem().isMergeableWith(items[param1])) {
                    // try to merge
                    if(items[param1].getCount() + player.getCursorItem().getCount() <= slotItem.getMaxStack()) {
                        // merge all
                        items[param1].setCount(items[param1].getCount() + player.getCursorItem().getCount());
                        player.setCursorItem(null);
                        //sendContents();
                    } else {
                        // merge partially
                        int canBeMerged = slotItem.getMaxStack() - items[param1].getCount();
                        items[param1].setCount(slotItem.getMaxStack());
                        Item cursor = player.getCursorItem();
                        cursor.setCount(cursor.getCount() - canBeMerged);
                        player.setCursorItem(cursor);
                        //sendContents();
                    }
                } else {
                    // there is also something on that slot, so we swap them
                    Item toSwap = items[param1];
                    items[param1] = player.getCursorItem();
                    player.setCursorItem(toSwap);
                    //sendContents();
                }
            } else {
                // there is NOTHING on the slot, so we just place the item there
                items[param1] = player.getCursorItem();
                player.setCursorItem(null);
                //sendContents();
            }
        } else {
            // there is NOTHING on his/her hand
            if(param1 < items.length && items[param1] != null && items[param1].getId() != 0) {
                // there is something on that slot, so we just pick that slot up
                player.setCursorItem(items[param1]);
                items[param1] = null;
                //sendContents();
            }
        }
    }
}

package org.dragonet.cloudland.server.gui.element;

import org.dragonet.cloudland.net.protocol.GUI;
import org.dragonet.cloudland.net.protocol.Metadata;
import org.dragonet.cloudland.server.entity.PlayerEntity;
import org.dragonet.cloudland.server.item.Item;
import org.dragonet.cloudland.server.item.ItemPrototype;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

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
                ItemPrototype slotItem = ItemPrototype.get(items[param1]);
                if(player.getCursorItem().isMergeableWith(items[param1])) {
                    if(!onItemMerge(param1, player)) return; // call event
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
                    if(!onItemSwap(param1, player)) return; // call event
                    Item toSwap = items[param1];
                    items[param1] = player.getCursorItem();
                    player.setCursorItem(toSwap);
                    //sendContents();
                }
            } else {
                // there is NOTHING on the slot, so we just place the item there
                if(!onItemPlacedInside(param1, player)) return; // call event
                items[param1] = player.getCursorItem();
                player.setCursorItem(null);
                //sendContents();
            }
        } else {
            // there is NOTHING on his/her hand
            if(param1 < items.length && items[param1] != null && items[param1].getId() != 0) {
                if(!onItemPickedUp(param1, player)) return; // call event

                // there is something on that slot, so we just pick that slot up
                player.setCursorItem(items[param1]);
                items[param1] = null;
                //sendContents();
            }
        }
    }

    /**
     * Try to remove some items, if there aren't sufficient items it will recover
     * @param remove what to remove?
     * @return success or not?
     */
    public boolean removeItems(Item[] remove) {
        Item[] operation = cloneItems();
        for(int removeIndex = 0; removeIndex < remove.length; removeIndex++) {
            Item removeItem = remove[removeIndex];
            if(removeItem == null || removeItem.getCount() == 0) continue;
            for(int i = 0; i < operation.length; i++) {
                Item currentItem = operation[i];
                if(currentItem == null || currentItem.getId() != removeItem.getId()) continue;
                if(currentItem.getCount() > removeItem.getCount()) {
                    currentItem.setCount(currentItem.getCount() - removeItem.getCount());
                    remove[removeIndex] = null;
                } else if(currentItem.getCount() == removeItem.getCount()) {
                    operation[i] = null;
                    remove[removeIndex] = null;
                } else if(currentItem.getCount() < removeItem.getCount()) {
                    removeItem.setCount(removeItem.getCount() - currentItem.getCount());
                    operation[i] = null;
                }
            }
        }
        for(Item check : remove) {
            if(check != null && check.getCount() > 0) {
                return false;
            }
        }
        items = operation; // finally update the items
        return true;
    }

    public Item[] cloneItems() {
        Item[] clone = new Item[items.length];
        for(int i = 0; i < items.length; i ++) {
            if(items[i] != null) {
                clone[i] = items[i].clone();
            }
        }
        return clone;
    }

    public boolean onItemMerge(int slot, PlayerEntity player) {
        // can be used for listeners, eg. crafting
        return onChange(slot, player);
    }

    public boolean onItemPickedUp(int slot, PlayerEntity player) {
        // can be used for listeners, eg. crafting
        return onChange(slot, player);
    }

    public boolean onItemPlacedInside(int slot, PlayerEntity player) {
        // can be used for listeners, eg. crafting
        return onChange(slot, player);
    }

    public boolean onItemSwap(int slot, PlayerEntity player) {
        // can be used for listeners, eg. crafting
        return onChange(slot, player);
    }

    public boolean onChange(int slot, PlayerEntity player) {
        // can be used for listeners, eg. crafting
        return true;
    }
}

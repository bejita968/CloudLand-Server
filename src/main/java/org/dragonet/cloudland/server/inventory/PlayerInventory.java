package org.dragonet.cloudland.server.inventory;

import org.dragonet.cloudland.net.protocol.GUI;
import org.dragonet.cloudland.server.entity.PlayerEntity;
import org.dragonet.cloudland.server.gui.InternalGUIElement;
import org.dragonet.cloudland.server.gui.element.InventoryElement;
import org.dragonet.cloudland.server.item.Item;
import lombok.Getter;
import org.dragonet.cloudland.server.item.crafting.CraftingHandler;
import org.dragonet.cloudland.server.item.crafting.CraftingManager;
import org.dragonet.cloudland.server.item.crafting.recipe.Recipe;
import org.dragonet.cloudland.server.item.crafting.recipe.ShapedRecipe;

/**
 *
 * Only hot bars, backpack is needed in order to get extra spaces.
 *
 * Created on 2017/1/17.
 */
public class PlayerInventory extends BaseInventory {

    public final static int PLAYER_INVENTORY_SIZE = 9 + 30;

    @Getter
    private final PlayerEntity player;

    private InventoryElement craftingInput = new InventoryElement(){
        @Override
        public void onAction(PlayerEntity player, int elementId, GUI.ClientWindowInteractMessage.WindowAction action, int param1, int param2) {
            super.onAction(player, elementId, action, param1, param2);

            crafting.detectCrafting();
        }
    };
    private InventoryElement craftingOutput = new InventoryElement() {
        @Override
        public boolean onChange(int slot, PlayerEntity player) {
            return false; // only pick up
        }

        @Override
        public boolean onItemPickedUp(int slot, PlayerEntity player) {
            return crafting.finishCrafting();
        }
    };

    private final CraftingHandler crafting;

    @Getter
    private int selectedSlot;

    public PlayerInventory(PlayerEntity player) {
        super(player.getProfile().getUsername() + "'s Inventory", PLAYER_INVENTORY_SIZE);
        this.player = player;
        elements = new InternalGUIElement[1];
        width = 1200;
        height = 900;
        windowId = 0;
        craftingInput.items = new Item[4];
        craftingOutput.items = new Item[1];

        crafting = new CraftingHandler(player, craftingInput, craftingOutput){
            @Override
            public void sendContents() {
                PlayerInventory.this.sendContents();
            }
        };
    }

    @Override
    public void openTo(PlayerEntity owner) {
        // Can't be opened by others
    }

    @Override
    public void close(PlayerEntity owner) {
        // Can't be opened by others so....
    }

    public void setSelectedSlot(int selectedSlot) {
        this.selectedSlot = selectedSlot;
        // TODO: Send changes
    }

    @Override
    public void refresh() {
        sendContents();
    }

    @Override
    public void sendContents() {
        GUI.ServerUpdateWindowMessage.Builder msg = GUI.ServerUpdateWindowMessage.newBuilder();

        // Inventory
        GUI.ServerUpdateWindowElementMessage.Builder inv = GUI.ServerUpdateWindowElementMessage.newBuilder();
        inv.setWindowId(0);
        inv.setElementIndex(0);
        inv.setNewElement(getInventoryElement().serialize());
        msg.addRecords(inv);

        // Crafting Inputs
        GUI.ServerUpdateWindowElementMessage.Builder msgCraftIn = GUI.ServerUpdateWindowElementMessage.newBuilder();
        msgCraftIn.setWindowId(0);
        msgCraftIn.setElementIndex(1);
        msgCraftIn.setNewElement(craftingInput.serialize());
        msg.addRecords(msgCraftIn);

        GUI.ServerUpdateWindowElementMessage.Builder msgCraftOut = GUI.ServerUpdateWindowElementMessage.newBuilder();
        msgCraftOut.setWindowId(0);
        msgCraftOut.setElementIndex(2);
        msgCraftOut.setNewElement(craftingOutput.serialize());
        msg.addRecords(msgCraftOut);

        player.getSession().sendNetworkMessage(msg.build());
    }

    public Item getHoldingItem(){
        return getItems()[selectedSlot];
    }

    @Override
    public void onAction(PlayerEntity player, int elementId, GUI.ClientWindowInteractMessage.WindowAction action, int param1, int param2) {
        if(elementId == 0) {
            // we don't use super.onAction() here OR it will WASTE bandwidth and performance
            getInventoryElement().onAction(player, elementId, action, param1, param2);
        } else if(elementId == 1) {
            craftingInput.onAction(player, elementId, action, param1, param2);
        } else if(elementId == 2) {
            craftingOutput.onAction(player, elementId, action, param1, param2);
        }
        sendContents();
    }
}

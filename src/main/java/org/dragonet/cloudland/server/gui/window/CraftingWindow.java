package org.dragonet.cloudland.server.gui.window;

import org.dragonet.cloudland.net.protocol.GUI;
import org.dragonet.cloudland.server.entity.PlayerEntity;
import org.dragonet.cloudland.server.gui.BaseGUIWindow;
import org.dragonet.cloudland.server.gui.InternalGUIElement;
import org.dragonet.cloudland.server.gui.element.InventoryElement;
import org.dragonet.cloudland.server.gui.element.TextElement;
import org.dragonet.cloudland.server.item.Item;
import org.dragonet.cloudland.server.item.crafting.CraftingHandler;

/**
 * Created on 2017/10/26.
 */
public class CraftingWindow extends BaseGUIWindow {

    private InventoryElement craftingIn;
    private InventoryElement craftingOut;

    private CraftingHandler crafting;

    public CraftingWindow(String title) {
        super(title);

        craftingIn = new InventoryElement() {
            @Override
            public void onAction(PlayerEntity player, int elementId, GUI.ClientWindowInteractMessage.WindowAction action, int param1, int param2) {
                super.onAction(player, elementId, action, param1, param2);

                crafting.detectCrafting();
                sendContents();
            }
        };
        craftingOut = new InventoryElement() {
            @Override
            public void onAction(PlayerEntity player, int elementId, GUI.ClientWindowInteractMessage.WindowAction action, int param1, int param2) {
                super.onAction(player, elementId, action, param1, param2);

                crafting.detectCrafting();
                sendContents();
            }

            @Override
            public boolean onChange(int slot, PlayerEntity player) {
                return false; // only pick up
            }

            @Override
            public boolean onItemPickedUp(int slot, PlayerEntity player) {
                return crafting.finishCrafting();
            }
        };
        craftingIn.items = new Item[9]; // 3x3
        craftingOut.items = new Item[1]; // output

        TextElement text = new TextElement("=>");

        setRawSize(450, 360);
        elements = new InternalGUIElement[] {
            craftingIn.setRawDimensions(60, 60, 180, 180),
            text.setRawDimensions(300, 100, 60, 40),
            craftingOut.setRawDimensions(260, 60, 70, 70)
        };

        crafting = new CraftingHandler(craftingIn, craftingOut);
    }

    @Override
    public void onAction(PlayerEntity player, int elementId, GUI.ClientWindowInteractMessage.WindowAction action, int param1, int param2) {
        elements[elementId].onAction(player, elementId, action, param1, param2);
    }

    public void sendContents() {
        GUI.ServerUpdateWindowMessage.Builder updateWindow = GUI.ServerUpdateWindowMessage.newBuilder();
        for(int i = 0; i < elements.length; i++) {
            GUI.ServerUpdateWindowElementMessage window = GUI.ServerUpdateWindowElementMessage.newBuilder()
                    .setWindowId(uniqueId)
                    .setElementIndex(i)
                    .setNewElement(elements[i].serialize())
                    .build();
            updateWindow.addRecords(window);
        }
        GUI.ServerUpdateWindowMessage m = updateWindow.build();
        viewers.forEach(p -> p.getSession().sendNetworkMessage(m));
    }
}

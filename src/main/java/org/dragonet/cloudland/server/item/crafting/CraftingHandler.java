package org.dragonet.cloudland.server.item.crafting;

import org.dragonet.cloudland.server.entity.PlayerEntity;
import org.dragonet.cloudland.server.gui.element.InventoryElement;
import org.dragonet.cloudland.server.item.Item;
import org.dragonet.cloudland.server.item.crafting.recipe.Recipe;
import org.dragonet.cloudland.server.item.crafting.recipe.ShapedRecipe;
import org.dragonet.cloudland.server.item.crafting.recipe.ShapelessRecipe;

/**
 * Created on 2017/5/19.
 */
public abstract class CraftingHandler {

    public final PlayerEntity player;

    public final InventoryElement input;
    public final InventoryElement output;

    public CraftingHandler(PlayerEntity player, InventoryElement input, InventoryElement output) {
        this.player = player;
        this.input = input;
        this.output = output;
    }

    public void detectCrafting(){
        Recipe r = CraftingManager.get().getCraftingRecipe(input.getItems());
        if(r == null) {
            output.items[0] = null;
        } else {
            output.items[0] = r.getResult();
        }
    }

    public boolean finishCrafting(){
        Recipe r = CraftingManager.get().getCraftingRecipe(input.getItems());
        if(r == null) {
            output.items[0] = null;
            return false;
        }
        if(ShapedRecipe.class.isAssignableFrom(r.getClass())) {
            /*
            ShapedRecipe shaped = (ShapedRecipe)r;
            if(shaped.getShape().length * shaped.getShape()[0].length() != input.items.length) {
                return false; // not likely to be possible
            }
            int len = shaped.getShape()[0].length();
            for(int row = 0; row < shaped.getShape().length; row++) {
                String sRow = shaped.getShape()[row];
                for(int col = 0; col < len; col++) {
                    int pos = row * len + col;
                    char ingredientChar = sRow.charAt(col);
                    Item ingredient = shaped.getIngredientMap().get(ingredientChar);
                    // Since we already checked before, we do like this to prevent from having errors
                    if(input.items[pos].getCount() > 0 && input.items[pos].getCount() >= ingredient.getCount()) {
                        input.items[pos].setCount(input.items[pos].getCount() - ingredient.getCount());
                    } else {
                        input.items[pos] = null;
                        return false;
                    }
                }
            }*/
            CraftingManager.get().removeItems(input.items);
        } else if(ShapelessRecipe.class.isAssignableFrom(r.getClass())) {
            ShapelessRecipe shapeless = (ShapelessRecipe)r;
            if(!input.removeItems(shapeless.getIngredientList().toArray(new Item[0]))){
                output.items[0] = null;
                return false;
            }
        }

        return true;
    }

    public abstract void sendContents();
}

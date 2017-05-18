package org.dragonet.cloudland.server.item.crafting.recipe;

import org.apache.commons.lang.Validate;
import org.dragonet.cloudland.server.item.Item;
import org.dragonet.cloudland.server.item.ItemPrototype;
import org.dragonet.cloudland.server.util.Keyed;
import org.dragonet.cloudland.server.util.NamespacedKey;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a shapeless recipe, where the arrangement of the ingredients on
 * the crafting grid does not matter.
 * -- BORROWED FROM SPIGOT BUKKIT
 */
public class ShapelessRecipe implements Recipe, Keyed {
    private final NamespacedKey key;
    private final Item output;
    private final List<Item> ingredients = new ArrayList<>();

    @Deprecated
    public ShapelessRecipe(Item result) {
        this.key = NamespacedKey.randomKey();
        this.output = result.clone();
    }

    /**
     * Create a shapeless recipe to craft the specified Item. The
     * constructor merely determines the result and type; to set the actual
     * recipe, you'll need to call the appropriate methods.
     *
     * @param key the unique recipe key
     * @param result The item you want the recipe to create.
     */
    public ShapelessRecipe(NamespacedKey key, Item result) {
        this.key = key;
        this.output = result.clone();
    }

    /**
     * Adds multiples of the specified ingredient.
     *
     * @param count How many to add (can't be more than 9!)
     * @param ingredient The ingredient to add.
     * @return The changed recipe, so you can chain calls.
     * @deprecated Magic value
     */
    @Deprecated
    public ShapelessRecipe addIngredient(int count, ItemPrototype ingredient) {
        Validate.isTrue(ingredients.size() + count <= 9, "Shapeless recipes cannot have more than 9 ingredients");

        while (count-- > 0) {
            ingredients.add(ingredient.newItemInstance(1));
        }
        return this;
    }

    /**
     * Removes multiple instances of an ingredient from the list. If there are
     * less instances then specified, all will be removed. If the data value
     * is -1, only ingredients with a -1 data value will be removed.
     *
     * @param count The number of copies to remove.
     * @param ingredient The ingredient to remove.
     * @return The changed recipe.
     * @deprecated Magic value
     */
    @Deprecated
    public ShapelessRecipe removeIngredient(int count, ItemPrototype ingredient) {
        Iterator<Item> iterator = ingredients.iterator();
        while (count > 0 && iterator.hasNext()) {
            Item stack = iterator.next();
            if (stack.getId() == ingredient.getId()) {
                iterator.remove();
                count--;
            }
        }
        return this;
    }

    /**
     * Get the result of this recipe.
     *
     * @return The result stack.
     */
    public Item getResult() {
        return output.clone();
    }

    /**
     * Get the list of ingredients used for this recipe.
     *
     * @return The input list
     */
    public List<Item> getIngredientList() {
        ArrayList<Item> result = new ArrayList<Item>(ingredients.size());
        for (Item ingredient : ingredients) {
            result.add(ingredient.clone());
        }
        return result;
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }
}

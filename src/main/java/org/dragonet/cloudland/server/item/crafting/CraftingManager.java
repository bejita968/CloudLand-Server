package org.dragonet.cloudland.server.item.crafting;

import com.google.common.collect.Iterators;
import org.dragonet.cloudland.server.gui.element.InventoryElement;
import org.dragonet.cloudland.server.inventory.Inventory;
import org.dragonet.cloudland.server.item.Item;
import org.dragonet.cloudland.server.item.ItemPrototype;
import org.dragonet.cloudland.server.item.crafting.recipe.*;
import org.dragonet.cloudland.server.util.NamespacedKey;

import java.util.*;

/**
 * Manager for crafting and smelting recipes
 *
 * -- BORROWED FROM GLOWSTONE IMPLEMENTATION
 */
public final class CraftingManager implements Iterable<Recipe> {

    private final static CraftingManager INSTANCE = new CraftingManager();

    public static CraftingManager get() {
        return INSTANCE;
    }

    private final ArrayList<ShapedRecipe> shapedRecipes = new ArrayList<>();
    private final ArrayList<ShapelessRecipe> shapelessRecipes = new ArrayList<>();
    private final ArrayList<DynamicRecipe> dynamicRecipes = new ArrayList<>();

    private boolean initialized = false;

    public void init() {
        if(initialized) return;

        resetRecipes();
        initialized = true;
    }

    /**
     * Adds a recipe to the crafting manager.
     *
     * @param recipe The recipe to add.
     * @return Whether adding the recipe was successful.
     */
    public boolean addRecipe(Recipe recipe) {
        if (recipe instanceof ShapedRecipe) {
            return shapedRecipes.add((ShapedRecipe) recipe);
        }
        if (recipe instanceof ShapelessRecipe) {
            return shapelessRecipes.add((ShapelessRecipe) recipe);
        }
        if (recipe instanceof DynamicRecipe) {
            return dynamicRecipes.add((DynamicRecipe) recipe);
        }
        return false;
    }

    /**
     * Remove a layer of items from the crafting matrix and recipe result.
     *
     * @param items The items to remove the ingredients from.
     */
    public void removeItems(Item[] items) {
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null && items[i].getId() != 0 && items[i].getCount() > 0) {
                int amount = items[i].getCount();
                if (amount > 1) {
                    items[i].setCount(amount - 1);
                } else {
                    items[i] = null;
                }
            }
        }
    }

    /**
     * Get the amount of layers in the crafting matrix.
     * This assumes all Minecraft recipes have an item stack of 1 for all items in the recipe.
     *
     * @param items The items in the crafting matrix.
     * @return The number of stacks for a recipe.
     */
    public static int getLayers(Item... items) {
        int layers = 0;
        for (Item item : items) {
            if (item != null && (item.getCount() < layers || layers == 0)) {
                layers = item.getCount();
            }
        }
        return layers;
    }

    /**
     * Get a crafting recipe from the crafting manager.
     *
     * @param items An array of items with null being empty slots. Length should be a perfect square.
     * @return The Recipe that matches the input, or null if none match.
     */
    public Recipe getCraftingRecipe(Item... items) {
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null && (items[i].getCount() <= 0 || items[i].getId() == 0)) {
                // TODO: rewrite recipe matcher to respect empty items
                items[i] = null;
            }
        }


        int size = (int) Math.sqrt(items.length);

        if (size * size != items.length) {
            throw new IllegalArgumentException("Item list was not square (was " + items.length + ")");
        }

        ShapedRecipe result = getShapedRecipe(size, items);
        if (result != null) {
            return result;
        }

        Item[] reversedItems = new Item[items.length];
        for (int row = 0; row < size; ++row) {
            for (int col = 0; col < size; ++col) {
                int col2 = size - 1 - col;
                reversedItems[row * size + col] = items[row * size + col2];
            }
        }

        // this check saves the trouble of iterating through all the recipes again
        if (!Arrays.equals(items, reversedItems)) {
            result = getShapedRecipe(size, reversedItems);
            if (result != null) {
                return result;
            }
        }

        for (DynamicRecipe dynamicRecipe : dynamicRecipes) {
            if (dynamicRecipe.matches(items)) {
                return dynamicRecipe;
            }
        }

        return getShapelessRecipe(items);
    }

    private ShapedRecipe getShapedRecipe(int size, Item... items) {
        for (ShapedRecipe recipe : shapedRecipes) {
            Map<Character, Item> ingredients = recipe.getIngredientMap();
            String[] shape = recipe.getShape();

            int rows = shape.length, cols = 0;
            for (String row : shape) {
                if (row.length() > cols) {
                    cols = row.length();
                }
            }

            if (rows == 0 || cols == 0) continue;

            // outer loop: try at each possible starting position
            for (int rStart = 0; rStart <= size - rows; ++rStart) {
                position:
                for (int cStart = 0; cStart <= size - cols; ++cStart) {
                    // inner loop: verify recipe against this position
                    for (int row = 0; row < rows; ++row) {
                        for (int col = 0; col < cols; ++col) {
                            Item given = items[(rStart + row) * size + cStart + col];
                            char ingredientChar = shape[row].length() > col ? shape[row].charAt(col) : ' ';
                            Item expected = ingredients.get(ingredientChar);

                            // check for mismatch in presence of an item in that slot at all
                            if (expected == null) {
                                if (given != null) {
                                    continue position;
                                } else {
                                    continue; // good match
                                }
                            } else if (given == null) {
                                continue position;
                            }

                            // check for type and data match
                            if (!matchesWildcard(expected, given)) {
                                continue position;
                            }
                        }
                    }

                    // also check that no items outside the recipe size are present
                    for (int row = 0; row < size; row++) {
                        for (int col = 0; col < size; col++) {
                            // if this position is outside the recipe and non-null, fail
                            if ((row < rStart || row >= rStart + rows || col < cStart || col >= cStart + cols) &&
                                    items[row * size + col] != null) {
                                continue position;
                            }
                        }
                    }

                    // recipe matches and zero items outside the recipe part.
                    return recipe;
                }
            } // end position loop
        } // end recipe loop

        return null;
    }

    private ShapelessRecipe getShapelessRecipe(Item... items) {
        recipe:
        for (ShapelessRecipe recipe : shapelessRecipes) {
            boolean[] accountedFor = new boolean[items.length];

            // Mark empty item slots accounted for
            for (int i = 0; i < items.length; ++i) {
                accountedFor[i] = items[i] == null;
            }

            // Make sure each ingredient in the recipe exists in the inventory
            ingredient:
            for (Item ingredient : recipe.getIngredientList()) {
                for (int i = 0; i < items.length; ++i) {
                    // if this item is not already used and it matches this ingredient...
                    if (!accountedFor[i] && matchesWildcard(ingredient, items[i])) {
                        // ... this item is accounted for and this ingredient is found.
                        accountedFor[i] = true;
                        continue ingredient;
                    }
                }
                // no item matched this ingredient, so the recipe fails
                continue recipe;
            }

            // Make sure inventory has no leftover items
            for (int i = 0; i < items.length; ++i) {
                if (!accountedFor[i]) {
                    continue recipe;
                }
            }

            return recipe;
        }

        return null;
    }

    @Override
    public Iterator<Recipe> iterator() {
        return Iterators.concat(shapedRecipes.iterator(), shapelessRecipes.iterator(), dynamicRecipes.iterator());
    }

    /**
     * CL: Just for compatibility, no wildcard actually
     * @param expected
     * @param actual
     * @return
     */
    private boolean matchesWildcard(Item expected, Item actual) {
        return expected != null && actual != null && (expected == actual || expected.getId() == actual.getId());
    }

    /**
     * Get a list of all recipes for a given item. The stack size is ignored
     * in comparisons. If the durability is -1, it will match any data value.
     *
     * @param result The item whose recipes you want
     * @return The list of recipes
     */
    public List<Recipe> getRecipesFor(Item result) {
        // handling for old-style wildcards

        List<Recipe> recipes = new LinkedList<>();
        for (Recipe recipe : this) {
            if (matchesWildcard(result, recipe.getResult())) {
                recipes.add(recipe);
            }
        }
        return recipes;
    }

    /**
     * Clear all recipes.
     */
    public void clearRecipes() {
        shapedRecipes.clear();
        shapelessRecipes.clear();
        dynamicRecipes.clear();
    }

    /**
     * Reset the crafting recipe lists to their default states.
     */
    public void resetRecipes() {
        clearRecipes();

        addRecipe(
                new ShapelessRecipe(NamespacedKey.cloudland("log_to_plank"), ItemPrototype.get("cloudland:plank").newItemInstance(4))
                .addIngredient(1, ItemPrototype.get("cloudland:log"))
        );

        addRecipe(
                new ShapedRecipe(NamespacedKey.cloudland("plank_to_stick"), ItemPrototype.get("cloudland:wood_stick").newItemInstance(1))
                .shape("p ", "p ")
                .setIngredient('p', ItemPrototype.get("cloudland:plank"))
        );

        addRecipe(new ShapedRecipe(NamespacedKey.cloudland("to_torch"), ItemPrototype.get("cloudland:torch").newItemInstance(1))
                .shape("w ", "s ")
                .setIngredient('w', ItemPrototype.get("cloudland:log"))
                .setIngredient('s', ItemPrototype.get("cloudland:wood_stick"))
        );
    }
}

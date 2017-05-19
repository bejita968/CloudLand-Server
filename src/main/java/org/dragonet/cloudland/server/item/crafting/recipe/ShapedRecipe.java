package org.dragonet.cloudland.server.item.crafting.recipe;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.Validate;
import org.dragonet.cloudland.server.item.Item;
import org.dragonet.cloudland.server.item.ItemPrototype;
import org.dragonet.cloudland.server.util.Keyed;
import org.dragonet.cloudland.server.util.NamespacedKey;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a shaped (ie normal) crafting recipe.
 * -- BORROWED FROM SPIGOT BUKKIT
 */
public class ShapedRecipe implements Recipe, Keyed {
    private final NamespacedKey key;
    private final Item output;
    private String[] rows;
    private Map<Character, Item> ingredients = new HashMap<>();

    @Deprecated
    public ShapedRecipe(Item result) {
        this.key = NamespacedKey.randomKey();
        this.output = result.clone();
    }

    /**
     * Create a shaped recipe to craft the specified Item. The
     * constructor merely determines the result and type; to set the actual
     * recipe, you'll need to call the appropriate methods.
     *
     * @param key the unique recipe key
     * @param result The item you want the recipe to create.
     * @see ShapedRecipe#shape(String...)
     */
    public ShapedRecipe(NamespacedKey key, Item result) {
        Preconditions.checkArgument(key != null, "key");

        this.key = key;
        this.output = result.clone();
    }

    /**
     * Set the shape of this recipe to the specified rows. Each character
     * represents a different ingredient; exactly what each character
     * represents is set separately. The first row supplied corresponds with
     * the upper most part of the recipe on the workbench e.g. if all three
     * rows are supplies the first string represents the top row on the
     * workbench.
     *
     * @param shape The rows of the recipe (up to 3 rows).
     * @return The changed recipe, so you can chain calls.
     */
    public ShapedRecipe shape(final String... shape) {
        Validate.notNull(shape, "Must provide a shape");
        Validate.isTrue(shape.length > 0 && shape.length < 4, "Crafting recipes should be 1, 2, 3 rows, not ", shape.length);

        int lastLen = -1;
        for (String row : shape) {
            Validate.notNull(row, "Shape cannot have null rows");
            Validate.isTrue(row.length() > 0 && row.length() < 4, "Crafting rows should be 1, 2, or 3 characters, not ", row.length());

            Validate.isTrue(lastLen == -1 || lastLen == row.length(), "Crafting recipes must be rectangular");
            lastLen = row.length();
        }
        this.rows = new String[shape.length];
        for (int i = 0; i < shape.length; i++) {
            this.rows[i] = shape[i];
        }

        // Remove character mappings for characters that no longer exist in the shape
        HashMap<Character, Item> newIngredients = new HashMap<Character, Item>();
        for (String row : shape) {
            for (Character c : row.toCharArray()) {
                newIngredients.put(c, ingredients.get(c));
            }
        }
        this.ingredients = newIngredients;

        return this;
    }


    /**
     * Sets the material that a character in the recipe shape refers to.
     *
     * @param key The character that represents the ingredient in the shape.
     * @param item The ingredient.
     * @return The changed recipe, so you can chain calls.
     */
    public ShapedRecipe setIngredient(char key, Item item) {
        return setIngredient(key, ItemPrototype.get(item));
    }

    /**
     * Sets the material that a character in the recipe shape refers to.
     *
     * @param key The character that represents the ingredient in the shape.
     * @param ingredient The ingredient.
     * @return The changed recipe, so you can chain calls.
     */
    public ShapedRecipe setIngredient(char key, ItemPrototype ingredient) {
        Validate.isTrue(ingredients.containsKey(key), "Symbol does not appear in the shape:", key);

        ingredients.put(key, ingredient.newItemInstance(1));
        return this;
    }

    /**
     * Get a copy of the ingredients map.
     *
     * @return The mapping of character to ingredients.
     */
    public Map<Character, Item> getIngredientMap() {
        HashMap<Character, Item> result = new HashMap<>();
        for (Map.Entry<Character, Item> ingredient : ingredients.entrySet()) {
            if (ingredient.getValue() == null) {
                result.put(ingredient.getKey(), null);
            } else {
                result.put(ingredient.getKey(), ingredient.getValue().clone());
            }
        }
        return result;
    }

    /**
     * Get the shape.
     *
     * @return The recipe's shape.
     */
    public String[] getShape() {
        return rows.clone();
    }

    /**
     * Get the result.
     *
     * @return The result stack.
     */
    public Item getResult() {
        return output.clone();
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }
}

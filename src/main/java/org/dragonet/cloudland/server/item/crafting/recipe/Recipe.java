package org.dragonet.cloudland.server.item.crafting.recipe;

import org.dragonet.cloudland.server.item.Item;

/**
 * Represents some type of crafting recipe.
 * -- BORROWED FROM SPIGOT BUKKIT
 */
public interface Recipe {

    /**
     * Get the result of this recipe.
     *
     * @return The result stack
     */
    Item getResult();
}

package org.dragonet.cloudland.server.util;

/**
 * Represents an object which has a {@link NamespacedKey} attached to it.
 */
public interface Keyed {

    /**
     * Return the namespaced identifier for this object.
     *
     * @return this object's key
     */
    NamespacedKey getKey();
}

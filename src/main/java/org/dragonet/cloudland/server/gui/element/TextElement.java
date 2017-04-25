package org.dragonet.cloudland.server.gui.element;

import org.dragonet.cloudland.net.protocol.GUI;
import org.dragonet.cloudland.net.protocol.Metadata;
import lombok.Getter;
import lombok.Setter;

/**
 * Created on 2017/3/14.
 */
public class TextElement extends BaseGUIElement {

    @Getter
    @Setter
    public String value;

    public TextElement() {
        value = "";
    }

    public TextElement(String value) {
        this.value = value;
    }

    @Override
    public GUI.GUIElementType getType() {
        return GUI.GUIElementType.TEXT;
    }

    @Override
    public GUI.GUIElement serialize() {
        return createBuilder()
                .setValue(Metadata.SerializedMetadata.newBuilder()
                .putEntries(0, Metadata.SerializedMetadata.MetadataEntry.newBuilder().setStringValue(value).build()).build()
                ).build();
    }
}

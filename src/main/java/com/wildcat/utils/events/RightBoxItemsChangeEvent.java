package com.wildcat.utils.events;

import com.vaadin.flow.component.ComponentEvent;
import com.wildcat.ListBoxGroup;

public class RightBoxItemsChangeEvent extends ComponentEvent<ListBoxGroup> {
    private boolean hasItems;
    public RightBoxItemsChangeEvent(ListBoxGroup source, boolean fromClient, boolean hasItems) {
        super(source, fromClient);
        this.hasItems = hasItems;
    }

    public boolean hasRightItems() {
        return hasItems;
    }
}

package com.wildcat.views.dialog;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.wildcat.persistence.model.IngestedDocument;
import com.wildcat.utils.dto.MetadataAttr;

import java.util.List;

public class MetadataDialog extends Dialog {
    public MetadataDialog(IngestedDocument ingestedDocument) {

        List<MetadataAttr> metadataAttrList = ingestedDocument.getMetadata().entrySet()
                .stream().map(MetadataAttr::new).toList();

        Grid<MetadataAttr>  metadataAttrGrid = new Grid<>(MetadataAttr.class, false);
        metadataAttrGrid.addColumn(MetadataAttr::getKey).setHeader("Key");
        metadataAttrGrid.addColumn(MetadataAttr::getValue).setHeader("Value");
        metadataAttrGrid.setAllRowsVisible(true);
        metadataAttrGrid.setItems(metadataAttrList);

        Span description = new Span(ingestedDocument.getDescription());
        description.setSizeFull();

        this.setHeaderTitle("Document Metadata");
        this.add(description);
        this.add(metadataAttrGrid);
   }
}

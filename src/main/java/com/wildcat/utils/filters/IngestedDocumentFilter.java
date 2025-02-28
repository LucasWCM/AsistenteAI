package com.wildcat.utils.filters;

import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.wildcat.persistence.model.IngestedDocument;

import java.time.LocalDateTime;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class IngestedDocumentFilter {
    private GridListDataView<IngestedDocument> dataView;

    private Long id;
    private String fileName;
    private String description;
    private LocalDateTime uploadDate;

    public IngestedDocumentFilter(GridListDataView<IngestedDocument> dataView) {
        this.dataView = dataView;
        this.dataView.addFilter(this::test);
    }

    public void setId(String id) {
        this.id = nonNull(id) ? Long.parseLong(id) : null;
        this.dataView.refreshAll();
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        this.dataView.refreshAll();
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
        this.dataView.refreshAll();
    }

    public void setDescription(String description) {
        this.description = description;
        this.dataView.refreshAll();
    }

    public boolean test(IngestedDocument ingestedDocument) {
        boolean matchesId = matches(ingestedDocument.getId().toString(), nonNull(this.id) ? this.id.toString() : null);
        boolean matchesFilename = matches(ingestedDocument.getFileName(), this.fileName);
        boolean matchesDescription = matches(ingestedDocument.getDescription(), this.description);
        boolean matchesUploadDate = isNull(this.uploadDate) || ingestedDocument.getUploadDate().toLocalDate().isEqual(this.uploadDate.toLocalDate());

        return matchesId && matchesFilename && matchesDescription && matchesUploadDate;
    }

    private boolean matches(String value, String searchTerm) {
        return searchTerm == null || searchTerm.isEmpty()
                || value.toLowerCase().contains(searchTerm.toLowerCase());
    }
}

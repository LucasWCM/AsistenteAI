package com.wildcat.views.dialog;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.wildcat.ai.services.DocumentLoader;
import com.wildcat.persistence.model.IngestedDocument;
import com.wildcat.persistence.service.ingesteddocument.IngestedDocumentService;
import com.wildcat.utils.dto.IngestResult;
import org.springframework.util.concurrent.ListenableFuture;

import static com.wildcat.utils.AiUtils.buildAndShowConfirmDialog;
import static com.wildcat.utils.AiUtils.executeCommand;
import static com.wildcat.utils.enums.IngestionType.LINK;
import static java.util.Objects.nonNull;

public class IngestLinksDialog extends Dialog {

    private Button btnIngest;
    private Button btnCancel;

    private TextField txtLink;
    private TextArea descriptionArea;

    private DocumentLoader documentLoader;
    private IngestedDocumentService ingestedDocumentService;
    private ListenableFuture<IngestResult> ingestLinkResult;

    public IngestLinksDialog(DocumentLoader documentLoader,
                             IngestedDocumentService ingestedDocumentService){
        this.documentLoader = documentLoader;
        this.ingestedDocumentService = ingestedDocumentService;

        this.setWidth("450px");
        this.setHeight("600px");

        VerticalLayout formContainer = new VerticalLayout();
        formContainer.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
        formContainer.setSizeFull();


        initializeHeaderComponent();
        initializeFooterComponents();

        initializeForms(formContainer);


        this.add(formContainer);
    }

    private void initializeForms(VerticalLayout formContainer) {
        this.txtLink = new TextField("Link");
        this.txtLink.setValueChangeMode(ValueChangeMode.EAGER);
        this.txtLink.setClearButtonVisible(true);
        this.txtLink.setWidthFull();
        this.txtLink.addValueChangeListener(evt -> {
            executeInputValidation();
        });
        formContainer.add(this.txtLink);

        this.descriptionArea = new TextArea("Descripcion");
        this.descriptionArea.setValueChangeMode(ValueChangeMode.EAGER);
        this.descriptionArea.setClearButtonVisible(true);
        this.descriptionArea.setWidthFull();
        this.descriptionArea.addValueChangeListener(evt -> {
            executeInputValidation();
        });
        formContainer.add(this.descriptionArea);
    }

    private void executeInputValidation() {
        boolean isAreaValid = !this.descriptionArea.getValue().strip().isEmpty();
        boolean isLinkValid = !this.txtLink.getValue().strip().isEmpty();

        btnIngest.setEnabled(isLinkValid && isAreaValid);
    }

    private void initializeHeaderComponent() {
        H3 headerTitle = new H3("Ingestar Links");
        HorizontalLayout headerContainer = new HorizontalLayout();
        headerContainer.add(headerTitle);
        headerContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        headerContainer.setWidthFull();
        this.getHeader().add(headerContainer);
    }

    private void initializeFooterComponents() {
        UI ui = UI.getCurrent();

        btnIngest = new Button("Save");
        btnIngest.setEnabled(false);
        btnIngest.addClickListener(evt -> {
            btnIngest.setEnabled(false);
            btnIngest.setText("Guardando...");

            String link = txtLink.getValue();
            String description = descriptionArea.getValue();
            this.ingestLinkResult = this.documentLoader.executeAndRetrieveLinkIngestionProcess(link, description);
            this.ingestLinkResult.addCallback((ingestResult) -> {
                IngestedDocument ingestedDocument = IngestedDocument.builder()
                        .associatedUuid(ingestResult.getUuid())
                        .description(description)
                        .link(link)
                        .ingestionType(LINK)
                        .uploadDate(ingestResult.getUploadDate())
                        .build();
                this.ingestedDocumentService.save(ingestedDocument);

                executeCommand(ui, () -> {
                    Notification.show("El link fue ingestado correctamente", 2000, Notification.Position.TOP_CENTER);
                    this.close();
                });
                    }, (ex) -> {
                        executeCommand(ui, () -> {
                            btnIngest.setEnabled(true);
                            btnIngest.setText("Save");
                            Notification.show(ex.getMessage(), 2000, Notification.Position.TOP_CENTER);
                        });

                });
        });
        btnCancel = new Button("Cancel");
        btnCancel.addClickListener(evt -> {
            if(nonNull(this.ingestLinkResult) && !this.ingestLinkResult.isDone()) {
                ConfirmDialog confirmDialog = buildAndShowConfirmDialog("Confirmar Operacion", "Desea Interrumpir El Proceso ?");
                confirmDialog.open();
                confirmDialog.addConfirmListener(closeEvt -> {
                    this.ingestLinkResult.cancel(true);
                    this.ingestLinkResult = null;
                    this.close();
                });
            } else {
                this.close();
            }
        });
        HorizontalLayout spacer = new HorizontalLayout();
        HorizontalLayout container = new HorizontalLayout();
        container.add(btnIngest, spacer, btnCancel);
        spacer.setWidthFull();
        container.setWidthFull();
        this.getFooter().add(container);
    }
}

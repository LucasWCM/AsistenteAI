package com.wildcat.views.dialog;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.wildcat.ListBoxGroup;
import com.wildcat.ai.services.DocumentLoader;
import com.wildcat.ai.services.ui.UiOperations;
import com.wildcat.persistence.model.IngestedDocument;
import com.wildcat.persistence.service.ingesteddocument.IngestedDocumentService;
import com.wildcat.utils.dto.IngestResult;
import com.wildcat.utils.dto.MetadataAttr;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.concurrent.ListenableFuture;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.wildcat.utils.AiUtils.buildAndShowConfirmDialog;
import static com.wildcat.utils.AiUtils.executeCommand;
import static com.wildcat.utils.AiUtils.generateMostUsed;
import static com.wildcat.utils.enums.IngestionType.DOCUMENT;
import static java.util.Objects.nonNull;

public class IngestedDocumentDialog extends Dialog {

    private List<MetadataAttr> metadataAttrList = new ArrayList<>();

    // Componentes UI
    private Button btnIngest;
    private Button btnCancel;
    private Grid<MetadataAttr> metadataAttrGrid;
    private Upload documentUploadComponent;
    private MultiFileMemoryBuffer buffer;
    private TextArea documentDescription;
    private TextArea fileContentArea;
    private UploadResult uploadResult;

    private DocumentLoader documentLoader;
    private IngestedDocumentService ingestedDocumentService;
    private ListenableFuture<IngestResult> ingestProcessResult;

    private ListBoxGroup listBoxGroup;

    public IngestedDocumentDialog(DocumentLoader documentLoader, IngestedDocumentService ingestedDocumentService, UiOperations uiOperations) {
        this.documentLoader = documentLoader;
        this.ingestedDocumentService = ingestedDocumentService;

        this.setWidth("950px");
        this.setHeight("600px");

        // Header
        initializeHeaderComponent();
        // Footer
        initializeFooterComponents();

        this.setSizeFull();
        HorizontalLayout formContainerWrapper = new HorizontalLayout();
        formContainerWrapper.setSizeFull();

        VerticalLayout formContainer = new VerticalLayout();
        formContainer.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
        formContainer.setWidth("350px");
        // Description
        initializeDescription(formContainer);
        // Key Words
        this.listBoxGroup = new ListBoxGroup(uiOperations);
        this.listBoxGroup.setSizeFull();
        this.listBoxGroup.setEnabled(false, false);
        this.listBoxGroup.addRightBoxItemsChangeListener(evt -> {
            verifyIfSaveButtonIsEnabled();
        });
        formContainer.add(this.listBoxGroup);
        // Upload
        initializeDocumentUploadComponent(formContainer);
        // Grid
        initializeMetadataGrid(formContainer);
        formContainer.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        formContainerWrapper.add(formContainer);

        this.fileContentArea = new TextArea("PDF Contenido");
        fileContentArea.setSizeFull();
        formContainerWrapper.add(fileContentArea);


        this.add(formContainerWrapper);
    }

    private void initializeDescription(VerticalLayout formContainer) {
        Span spacer = new Span();
        spacer.setHeight("25px");
        formContainer.add(spacer);
        Label descriptionLbl = new Label("Description");
        formContainer.add(descriptionLbl);
        this.documentDescription = new TextArea();
        this.documentDescription.setValueChangeMode(ValueChangeMode.EAGER);
        this.documentDescription.addValueChangeListener(evt -> {
            verifyIfSaveButtonIsEnabled();
        });
        this.documentDescription.setWidthFull();
        formContainer.add(this.documentDescription);
    }

    private void initializeDocumentUploadComponent(VerticalLayout formContainer) {
        Label documentoLbl = new Label("Documento");
        this.buffer = new MultiFileMemoryBuffer();
        this.documentUploadComponent = new Upload(this.buffer);
        this.documentUploadComponent.setAutoUpload(true);
        this.documentUploadComponent.setHeight("50px");
        UI ui = UI.getCurrent();
        this.documentUploadComponent.addSucceededListener(evt -> {
            String fileName = evt.getFileName();
            InputStream fileStream = buffer.getInputStream(fileName);
            this.uploadResult = new UploadResult(fileName, fileStream);

            this.fileContentArea.setValue("Cargando Contenido PDF...");
            this.fileContentArea.setEnabled(false);

            this.documentLoader.retrieveFileContent(this.uploadResult.fileStream())
                            .addCallback(result -> {
                                executeCommand(ui, () -> {
                                    this.fileContentArea.setEnabled(true);

                                    List<String> mostUsedWords = generateMostUsed(result).stream().filter(word -> word.strip().length() >= 3).toList();
                                    this.listBoxGroup.setItemsOnLeft(mostUsedWords);
                                    this.listBoxGroup.setEnabled(true, true);

                                    this.fileContentArea.setValue(result);
                                    Notification.show("El documento fue cargado correctamente.", 2000, Notification.Position.TOP_CENTER);
                                    verifyIfSaveButtonIsEnabled();
                                });
                            }, (ex) -> {
                                executeCommand(ui, () -> {
                                    this.fileContentArea.setValue("");
                                    this.fileContentArea.setEnabled(true);
                                    Notification.show("Hubo errores al cargar el documento.", 2000, Notification.Position.TOP_CENTER);
                                });
                            });
        });
        formContainer.add(documentoLbl, this.documentUploadComponent);
    }

    private void initializeMetadataGrid(VerticalLayout formContainer) {
        /*
         * MetaData Section
         */
        Label metadataLbl = new Label("Metadata");
        formContainer.add(metadataLbl);

        TextField keyTextField = new TextField("Key");
        TextField valueTextField = new TextField("Value");

        keyTextField.setClearButtonVisible(true);
        keyTextField.addKeyPressListener(Key.ENTER, evt -> {
            updateOrAddAttribute(keyTextField, valueTextField);
        });
        valueTextField.setClearButtonVisible(true);
        valueTextField.addKeyPressListener(Key.ENTER, evt -> {
            updateOrAddAttribute(keyTextField, valueTextField);
        });

        formContainer.add(keyTextField, valueTextField);
        keyTextField.setWidthFull();
        valueTextField.setWidthFull();

        metadataAttrGrid = new Grid<>(MetadataAttr.class, false);
        metadataAttrGrid.addColumn(MetadataAttr::getKey).setHeader("Key");
        metadataAttrGrid.addColumn(MetadataAttr::getValue).setHeader("Value");
        metadataAttrGrid.addColumn(
                new ComponentRenderer<>(Button::new, (button, metadataAttr) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON,
                            ButtonVariant.LUMO_ERROR,
                            ButtonVariant.LUMO_TERTIARY);
                    button.addClickListener(e -> {

                        ConfirmDialog confirmDialog = buildAndShowConfirmDialog("Confirmar Operacion", "Desea Borrar El Atributo Ingresado ?");
                        confirmDialog.open();
                        confirmDialog.addConfirmListener(evt -> {
                            metadataAttrList.removeIf(attr -> attr.getKey().equals(metadataAttr.getKey()));
                            metadataAttrGrid.getDataProvider().refreshAll();

                            verifyIfSaveButtonIsEnabled();
                        });

                    });
                    button.setIcon(new Icon(VaadinIcon.TRASH));
                }));
        metadataAttrGrid.setAllRowsVisible(true);
        metadataAttrGrid.setItems(metadataAttrList);
        formContainer.add(metadataAttrGrid);
        formContainer.add(new Span());
    }

    private void updateOrAddAttribute(TextField keyTextField, TextField valueTextField){
        String key = keyTextField.getValue().strip();
        String value = valueTextField.getValue().strip();
        if(key.isEmpty() || value.isEmpty()){
            Notification.show("Es Necesario Proveer Un Valor", 1500, Notification.Position.TOP_CENTER);
        } else {
            Optional<MetadataAttr> metadataAttrOpt = metadataAttrList.stream()
                    .filter(metadataAttr -> metadataAttr.getKey().equals(key))
                    .findAny();
            metadataAttrOpt.ifPresentOrElse(metadataAttr -> {
                metadataAttr.setValue(value.strip());
                metadataAttrGrid.getDataProvider().refreshAll();
            }, () -> {
                MetadataAttr attr = new MetadataAttr(key, value);
                metadataAttrList.add(attr);
                metadataAttrGrid.getDataProvider().refreshAll();
            });
            keyTextField.setValue(StringUtils.EMPTY);
            valueTextField.setValue(StringUtils.EMPTY);

            verifyIfSaveButtonIsEnabled();
        }
    }

    private void verifyIfSaveButtonIsEnabled() {
        boolean isDescriptionValid = !this.documentDescription.getValue().strip().isEmpty();
        boolean hasMetadataAttrs = !this.metadataAttrList.isEmpty();
        boolean hasUploadedFile = !this.fileContentArea.getValue().strip().isEmpty();
        boolean hasItemsOnRight = !listBoxGroup.getItemsOnRight().isEmpty();
        btnIngest.setEnabled(isDescriptionValid && hasMetadataAttrs && hasUploadedFile && hasItemsOnRight);
    }

    private void initializeHeaderComponent() {
        H3 headerTitle = new H3("Ingestar Documentos");
        HorizontalLayout headerContainer = new HorizontalLayout();
        headerContainer.add(headerTitle);
        headerContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        headerContainer.setWidthFull();
        this.getHeader().add(headerContainer);
    }

    private void changeComponentStatus(boolean status) {

    }

    private void initializeFooterComponents() {
        UI ui = UI.getCurrent();

        btnIngest = new Button("Save");
        btnIngest.setEnabled(false);
        btnIngest.addClickListener(evt -> {
            btnIngest.setEnabled(false);
            btnIngest.setText("En Proceso...");

            String fileName = this.uploadResult.fileName();
            String textAreaValue = this.fileContentArea.getValue();
            String docDescription = documentDescription.getValue().strip();
            List<String> keyWords = this.listBoxGroup.getItemsOnRight();
            this.ingestProcessResult = this.documentLoader.executeAndRetrieveIngestionProcess(textAreaValue, metadataAttrList, keyWords, docDescription);
            this.ingestProcessResult.addCallback(ingestResult -> {
                Map<String, String> metadataMap = metadataAttrList.stream().collect(Collectors.toMap(MetadataAttr::getKey, MetadataAttr::getValue));
                IngestedDocument ingestedDocument = IngestedDocument.builder()
                        .associatedUuid(ingestResult.getUuid())
                        .fileName(fileName)
                        .metadata(metadataMap)
                        .keyWords(keyWords)
                        .ingestionType(DOCUMENT)
                        .description(docDescription)
                        .uploadDate(ingestResult.getUploadDate())
                        .build();
                this.ingestedDocumentService.save(ingestedDocument);

                executeCommand(ui, () -> {
                    Notification.show("El documento fue ingestado correctamente", 2000, Notification.Position.TOP_CENTER);
                    this.close();
                });
            }, (err) -> {
                executeCommand(ui, () -> {
                    btnIngest.setEnabled(true);
                    btnIngest.setText("Save");
                    Notification.show(err.getMessage(), 2000, Notification.Position.TOP_CENTER);
                });

            });

        });
        btnCancel = new Button("Cancel");
        btnCancel.addClickListener(evt -> {
            if(nonNull(ingestProcessResult) && !ingestProcessResult.isDone()) {
                ConfirmDialog confirmDialog = buildAndShowConfirmDialog("Confirmar Operacion", "Desea Interrumpir El Proceso ?");
                confirmDialog.open();
                confirmDialog.addConfirmListener(closeEvt -> {
                    ingestProcessResult.cancel(true);
                    ingestProcessResult = null;
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

    record UploadResult(String fileName, InputStream fileStream) {}
}
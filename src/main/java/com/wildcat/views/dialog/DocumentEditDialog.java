package com.wildcat.views.dialog;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.wildcat.ai.services.DocumentLoader;
import com.wildcat.persistence.model.IngestedDocument;
import com.wildcat.persistence.service.ingesteddocument.IngestedDocumentService;
import com.wildcat.utils.dto.MetadataAttr;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.wildcat.utils.AiUtils.buildProcessDialog;
import static com.wildcat.utils.AiUtils.executeCommand;
import static com.wildcat.utils.enums.IngestionType.DOCUMENT;

public class DocumentEditDialog extends Dialog {

    // Left Container Components
    private TextArea txtDescription;
    private TextField newKeywordTxt;
    private ListBox<String> keyWordsListBox;
    private TextField keyTxt;
    private TextField valueTxt;
    private Grid<MetadataAttr> metadataAttrGrid;
    private List<MetadataAttr> metadataAttrList = new ArrayList<>();

    // Right Container Components
    private TextArea documentContent;

    // Footer
    private Button btnGrabar;
    private Button btnCancelar;

    private DocumentLoader documentLoader;
    private IngestedDocument ingestedDocument;
    private IngestedDocumentService ingestedDocumentService;

    public DocumentEditDialog(IngestedDocument ingestedDocument,
                              String textContent,
                              DocumentLoader documentLoader,
                              IngestedDocumentService ingestedDocumentService) {
        this.documentLoader = documentLoader;
        this.ingestedDocument = ingestedDocument;
        this.ingestedDocumentService = ingestedDocumentService;

        this.setWidth("850px");

        HorizontalLayout dialogContainer = new HorizontalLayout();
        dialogContainer.setSizeFull();

        initHeader();
        initFooter();
        initLeftContainer(dialogContainer);
        initRightContainer(dialogContainer);

        initComponentValues(ingestedDocument, textContent);

        performSaveStatusValidation();

        this.add(dialogContainer);
    }

    private void initComponentValues(IngestedDocument ingestedDocument, String textContent) {
        this.txtDescription.setValue(ingestedDocument.getDescription());

        this.keyWordsListBox.setItems(ingestedDocument.getKeyWords());

        List<MetadataAttr> metadataAttrs = ingestedDocument.getMetadata().entrySet()
                .stream().map(entry -> new MetadataAttr(entry.getKey(), entry.getValue()))
                .toList();
        this.metadataAttrList.addAll(metadataAttrs);
        this.metadataAttrGrid.setItems(this.metadataAttrList);

        this.documentContent.setValue(textContent);
    }

    private void initHeader() {
        H1 headerCmp = new H1();
        headerCmp.setText("Edicion Del Documento");
        this.getHeader().add(headerCmp);
        headerCmp.setWidthFull();
        headerCmp.getStyle().set("text-align", "center");
    }

    private void initFooter() {
        HorizontalLayout footerContainer = new HorizontalLayout();
        HorizontalLayout spacer = new HorizontalLayout();

        UI ui = UI.getCurrent();

        this.btnGrabar = new Button("Grabar");
        this.btnGrabar.addClickListener(evt -> {

            Dialog processDialog = buildProcessDialog();
            this.add(processDialog);
            processDialog.open();
            processDialog.addOpenedChangeListener(openedChangeEvent -> {
                if (!openedChangeEvent.isOpened()) {
                    this.remove(processDialog);
                }
            });

            ingestedDocumentService.executeRedisRecordRemoval(this.ingestedDocument.getAssociatedUuid())
                    .addCallback(result -> {
                        String textAreaValue = documentContent.getValue();
                        List<String> keyWords = this.keyWordsListBox.getListDataView().getItems().toList();
                        String description = txtDescription.getValue();
                        executeDocumentUpdate(textAreaValue, keyWords, description, processDialog, ui);
                    }, (ex) -> {
                        executeCommand(ui, () -> {
                            Notification.show("Se Ha Producido Un Error.", 2000, Notification.Position.TOP_CENTER);
                        });
                    });
        });

        this.btnCancelar = new Button("Cancelar");
        this.btnCancelar.addClickListener(evt -> {
           this.close();
        });

        footerContainer.add(this.btnGrabar, spacer, this.btnCancelar);
        spacer.setWidthFull();
        footerContainer.setWidthFull();
        this.getFooter().add(footerContainer);
    }

    private void executeDocumentUpdate(String textAreaValue, List<String> keyWords, String description, Dialog processDialog, UI ui) {
        this.documentLoader.executeAndRetrieveIngestionProcess(textAreaValue, this.metadataAttrList, keyWords, description)
                .addCallback(result -> {

                    executeCommand(ui, () -> {
                        processDialog.close();
                    });

                    Map<String, String> metadataMap = metadataAttrList.stream().collect(Collectors.toMap(MetadataAttr::getKey, MetadataAttr::getValue));
                    IngestedDocument ingestedDocument = IngestedDocument.builder()
                            .associatedUuid(result.getUuid())
                            .fileName(this.ingestedDocument.getFileName())
                            .metadata(metadataMap)
                            .keyWords(keyWords)
                            .ingestionType(DOCUMENT)
                            .description(description)
                            .uploadDate(this.ingestedDocument.getUploadDate())
                            .id(this.ingestedDocument.getId())
                            .build();
                    this.ingestedDocumentService.save(ingestedDocument);

                    executeCommand(ui, () -> {
                        Notification.show("El documento fue actualizado correctamente.", 2000, Notification.Position.TOP_CENTER);
                        this.close();
                    });
                }, (ex) -> {
                    executeCommand(ui, () -> {
                        Notification.show("Se Ha Producido Un Error.", 2000, Notification.Position.TOP_CENTER);
                    });
                });
    }

    private void initLeftContainer(HorizontalLayout dialogContainer) {
        VerticalLayout leftContainer = new VerticalLayout();

        this.txtDescription = new TextArea("Descripcion");
        this.txtDescription.setWidthFull();
        this.txtDescription.addKeyPressListener(Key.ENTER, (evt) -> {
            performSaveStatusValidation();
        });

        this.newKeywordTxt = new TextField("Palabra Clave");
        this.newKeywordTxt.setWidthFull();
        this.newKeywordTxt.setClearButtonVisible(true);
        this.newKeywordTxt.addKeyPressListener(Key.ENTER, evt -> {
            String value = this.newKeywordTxt.getValue();
            if(value.strip().isEmpty()) {
                Notification.show("Debe Ingresar Un Valor !.", 2000, Notification.Position.TOP_CENTER);
                return;
            }

            boolean hasEnteredWord = this.keyWordsListBox.getListDataView().getItems()
                    .anyMatch(word -> word.equals(value.strip()));

            if(hasEnteredWord) {
                Notification.show("La Palabra Ingresada Ya Se Disponible", 2000, Notification.Position.TOP_CENTER);
                return;
            }

            List<String> currentKeyWordsItem = new ArrayList<>(this.keyWordsListBox.getListDataView().getItems().toList());
            currentKeyWordsItem.add(value);
            this.keyWordsListBox.setItems(currentKeyWordsItem);

            this.newKeywordTxt.setValue(StringUtils.EMPTY);

            performSaveStatusValidation();
        });

        this.keyWordsListBox = new ListBox<>();
        this.keyWordsListBox.getStyle().set("border", "1px solid black");
        this.keyWordsListBox.getStyle().set("padding", "5px");
        this.keyWordsListBox.setHeight("150px");
        this.keyWordsListBox.setWidthFull();
        this.keyWordsListBox.addValueChangeListener(evt -> {
            String selectedValue = evt.getValue();
            List<String> currentKeyWords = this.keyWordsListBox.getListDataView().getItems()
                    .filter(keyWord -> !keyWord.equals(selectedValue))
                    .toList();
            this.keyWordsListBox.setItems(currentKeyWords);
            this.keyWordsListBox.getDataProvider().refreshAll();

            performSaveStatusValidation();
        });

        HorizontalLayout inputContainer = new HorizontalLayout();
        this.keyTxt = new TextField("Key");
        this.keyTxt.addKeyPressListener(Key.ENTER, evt -> {
            updateOrAddAttribute(this.keyTxt, this.valueTxt);
        });
        this.valueTxt = new TextField("Value");
        this.valueTxt.addKeyPressListener(Key.ENTER, evt -> {
            updateOrAddAttribute(this.keyTxt, this.valueTxt);
        });
        inputContainer.add(this.keyTxt, this.valueTxt);
        inputContainer.setWidthFull();
        inputContainer.setFlexGrow(1, this.keyTxt);
        inputContainer.setFlexGrow(1, this.valueTxt);

        this.metadataAttrGrid = new Grid<>(MetadataAttr.class, false);
        this.metadataAttrGrid.setWidthFull();
        this.metadataAttrGrid.addColumn(MetadataAttr::getKey).setHeader("Key");
        this.metadataAttrGrid.addColumn(MetadataAttr::getValue).setHeader("Value");
        this.metadataAttrGrid.addItemDoubleClickListener(evt -> {
            MetadataAttr item = evt.getItem();
            this.metadataAttrList.removeIf(metadataAttr -> metadataAttr.getKey().equals(item.getKey()));
            this.metadataAttrGrid.getDataProvider().refreshAll();
        });
        leftContainer.add(this.txtDescription, this.newKeywordTxt, this.keyWordsListBox, inputContainer, this.metadataAttrGrid);

        dialogContainer.add(leftContainer);
}

    private void initRightContainer(HorizontalLayout dialogContainer) {
        VerticalLayout rightContainer = new VerticalLayout();

        this.documentContent = new TextArea();
        this.documentContent.setSizeFull();
        rightContainer.add(this.documentContent);

        dialogContainer.add(rightContainer);
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

            performSaveStatusValidation();
        }
    }

    private void performSaveStatusValidation() {
        boolean hasDescription = !this.txtDescription.getValue().strip().isEmpty();
        boolean hasKeywords = !keyWordsListBox.getListDataView().getItems().toList().isEmpty();
        boolean hasMetadata = !metadataAttrList.isEmpty();
        boolean hasContent = !documentContent.getValue().strip().isEmpty();
        this.btnGrabar.setEnabled(hasDescription && hasKeywords && hasMetadata && hasContent);
    }
}

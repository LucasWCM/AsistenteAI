package com.wildcat.views.pages;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.wildcat.ai.services.DocumentLoader;
import com.wildcat.ai.services.ui.UiOperations;
import com.wildcat.persistence.model.Configuration;
import com.wildcat.persistence.model.IngestedDocument;
import com.wildcat.persistence.service.configuration.ConfigurationService;
import com.wildcat.persistence.service.ingesteddocument.IngestedDocumentService;
import com.wildcat.utils.ChatLanguageModelService;
import com.wildcat.utils.enums.IngestionType;
import com.wildcat.utils.providers.PagedDataProvider;
import com.wildcat.views.dialog.DocumentEditDialog;
import com.wildcat.views.dialog.IngestLinksDialog;
import com.wildcat.views.dialog.IngestedDocumentDialog;
import com.wildcat.views.dialog.MetadataDialog;
import com.wildcat.views.layout.AiAssistantLayout;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.tika.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import static com.wildcat.utils.AiUtils.*;
import static com.wildcat.utils.AiUtils.executeCommand;
import static com.wildcat.utils.Constants.CURRENT_MODEL_NAME;
import static java.util.Objects.nonNull;

@Route(value = "configuration", layout = AiAssistantLayout.class)
@RouteAlias(value = "", layout = AiAssistantLayout.class)
public class AiConfiguration extends VerticalLayout {

    private IngestedDocumentService ingestedDocumentService;
    private Grid<IngestedDocument> ingestedDocumentGrid;
    private UiOperations uiOperations;
    private DocumentLoader documentLoader;

    public AiConfiguration(IngestedDocumentService ingestedDocumentService,
                           DocumentLoader documentLoader,
                           ConfigurationService configurationService,
                           ChatLanguageModelService chatLanguageModelService,
                           @Value("${assistant-ia-integration.open-ai.apiKey}") String openAiApìKey,
                           UiOperations uiOperations){
        this.ingestedDocumentService = ingestedDocumentService;
        this.uiOperations = uiOperations;
        this.documentLoader = documentLoader;

        H1 documentosIngestados = new H1("Documentos Ingestados");
        this.add(documentosIngestados);

        Button ingestarPdfsDialogBtn = new Button("Ingestar PDFs");
        ingestarPdfsDialogBtn.addClickListener(evt -> {
            IngestedDocumentDialog ingestedDocumentDialog = new IngestedDocumentDialog(documentLoader, ingestedDocumentService, uiOperations);
            ingestedDocumentDialog.setCloseOnOutsideClick(false);
            ingestedDocumentDialog.setCloseOnEsc(false);

            ingestedDocumentDialog.addOpenedChangeListener(event -> {
                if (!event.isOpened()) {
                    // TODO: Que pasa si esta en procesa de ingestacion ? validar eso
                    this.remove(ingestedDocumentDialog);
                    this.ingestedDocumentGrid.getDataProvider().refreshAll();
                }
            });

            this.add(ingestedDocumentDialog);
            ingestedDocumentDialog.open();
        });
        Button ingestarLinksDialogBtn = new Button("Ingestar Links");
        ingestarLinksDialogBtn.addClickListener(evt -> {
            IngestLinksDialog ingestLinksDialog = new IngestLinksDialog(documentLoader, ingestedDocumentService);
            ingestLinksDialog.setCloseOnOutsideClick(false);
            ingestLinksDialog.setCloseOnEsc(false);

            ingestLinksDialog.addOpenedChangeListener(event -> {
                if (!event.isOpened()) {
                    // TODO: Que pasa si esta en procesa de ingestacion ? validar eso
                    this.remove(ingestLinksDialog);
                    this.ingestedDocumentGrid.getDataProvider().refreshAll();
                }
            });

            this.add(ingestLinksDialog);
            ingestLinksDialog.open();
        });

        Optional<Configuration> currentModelOpt = configurationService.findByPropertyName(CURRENT_MODEL_NAME);
        ComboBox<String> modelNameToken = new ComboBox<>("Model Name");
        modelNameToken.setWidthFull();
        modelNameToken.setItems(List.of("gpt-4o", "gpt-4o-mini", "gpt-4"));
        modelNameToken.setItemLabelGenerator(String::valueOf);
        modelNameToken.setClearButtonVisible(true);
        modelNameToken.addValueChangeListener(evt -> {
            String modelValue = evt.getValue();

            OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                    .apiKey(openAiApìKey)
                    .modelName(modelValue)
                    .temperature(0.3)
                    .build();

            Configuration newModelConfig = Configuration.builder()
                    .propertyName(CURRENT_MODEL_NAME)
                    .propertyValue(modelValue)
                    .build();
            configurationService.save(newModelConfig);

            chatLanguageModelService.restartContext("chatLanguageModel", openAiChatModel);
        });
        currentModelOpt.ifPresent(currentModelConfig -> {
            modelNameToken.setValue(currentModelConfig.getPropertyValue());
        });

        HorizontalLayout ingestarBtnContainer = new HorizontalLayout(ingestarPdfsDialogBtn, ingestarLinksDialogBtn, modelNameToken);
        this.add(ingestarBtnContainer);

        VerticalLayout ingestedDocumentContainerGrid = buildIngestedDocumentContainerGrid();
        this.add(ingestedDocumentContainerGrid);

    }

    private VerticalLayout buildIngestedDocumentContainerGrid() {
        PagedDataProvider pagedDataProvider = new PagedDataProvider(ingestedDocumentService);
        UI ui = UI.getCurrent();

        VerticalLayout container = new VerticalLayout();

        this.ingestedDocumentGrid = new Grid<>(IngestedDocument.class, false);
        this.ingestedDocumentGrid.setPageSize(10);
        this.ingestedDocumentGrid.setAllRowsVisible(false);
        this.ingestedDocumentGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        this.ingestedDocumentGrid.addColumn(IngestedDocument::getId).setHeader("ID").setSortable(true).setKey("id");
        this.ingestedDocumentGrid.addColumn(IngestedDocument::getDescription).setHeader("Description").setSortable(true).setKey("description");
        this.ingestedDocumentGrid.addColumn(IngestedDocument::getFileName).setHeader("Archivo").setSortable(true).setKey("fileName");
        this.ingestedDocumentGrid.addColumn(ingestedDocument -> {
            if(nonNull(ingestedDocument.getIngestionType())) {
                return ingestedDocument.getIngestionType();
            }
            return StringUtils.EMPTY;
        }).setHeader("Tipo").setSortable(true).setKey("ingestionType");
        this.ingestedDocumentGrid.addColumn(ingestedDocument ->
                fromDataToString(ingestedDocument.getUploadDate())).setHeader("Fecha Subida").setSortable(true).setKey("uploadDate");
        this.ingestedDocumentGrid.addColumn(LitRenderer.<IngestedDocument>of(
                        """
                                    <vaadin-horizontal-layout theme="spacing padding">
                                      <vaadin-button theme="primary" 
                                                     @click="${showDocumentMetadata}" 
                                                     title="Mostrar Metadata">
                                                     <vaadin-icon icon="vaadin:abacus"></vaadin-icon>
                                      </vaadin-button>
                                      <vaadin-button theme="primary error"
                                                     @click="${removeIngestedDocument}" 
                                                     title="Remover Documento">
                                                        <vaadin-icon icon="vaadin:close-small"></vaadin-icon>
                                                     </vaadin-button>   
                                      <vaadin-button theme="primary error"
                                                     @click="${editIndexedDocument}" 
                                                     title="Check Docs">
                                                        <vaadin-icon icon="vaadin:close-small"></vaadin-icon>
                                                     </vaadin-button>                                                                                                        
                                    </vaadin-horizontal-layout>                                   
                                """)
                .withFunction("showDocumentMetadata", ingestedDocument -> {
                    if(ingestedDocument.isLink()) {
                        Notification.show("Esta ingestacion no posee metadata.", 2000, Notification.Position.TOP_CENTER);
                        return;
                    }

                    if(ingestedDocument.getMetadata().entrySet().isEmpty()){
                        Notification.show("No Se Posee Metadata Para Mostrar.", 2000, Notification.Position.TOP_CENTER);
                        return;
                    }
                    MetadataDialog metadataDialog = new MetadataDialog(ingestedDocument);
                    metadataDialog.setCloseOnEsc(true);
                    metadataDialog.setCloseOnOutsideClick(true);
                    metadataDialog.setWidth("500px");
                    metadataDialog.setHeight("300px");
                    this.add(metadataDialog);
                    metadataDialog.open();
                    metadataDialog.addOpenedChangeListener(evt -> {
                        if (!evt.isOpened()) {
                            this.remove(metadataDialog);
                        }
                    });

                }).withFunction("editIndexedDocument", ingestedDocument -> {

                    Dialog processDialog = buildProcessDialog();
                    this.add(processDialog);
                    processDialog.open();
                    processDialog.addOpenedChangeListener(openedChangeEvent -> {
                        if (!openedChangeEvent.isOpened()) {
                            this.remove(processDialog);
                        }
                    });

                    ingestedDocumentService.retrieveFullDocument(ingestedDocument.getAssociatedUuid())
                            .addCallback(textContent -> {
                                executeCommand(ui, () -> {
                                    processDialog.close();

                                    DocumentEditDialog documentEditDialog = new DocumentEditDialog(ingestedDocument, textContent, this.documentLoader, this.ingestedDocumentService);
                                    documentEditDialog.setCloseOnEsc(false);
                                    documentEditDialog.setCloseOnOutsideClick(false);
                                    documentEditDialog.setDraggable(true);
                                    documentEditDialog.addOpenedChangeListener(evt -> {
                                        if(!evt.isOpened()) {
                                            this.remove(documentEditDialog);
                                        }
                                    });
                                    this.add(documentEditDialog);
                                    documentEditDialog.open();
                                });
                            }, (ex) -> {
                                executeCommand(ui, () -> {
                                    processDialog.close();
                                });
                            });

                })
                .withFunction("removeIngestedDocument", ingestedDocument -> {
                    ConfirmDialog confirmDialog = buildAndShowConfirmDialog("Confirmar Operacion", "Desea Borrar Documento Ingestado ?.");
                    confirmDialog.open();
                    confirmDialog.addConfirmListener(evt -> {
                        Dialog processDialog = buildProcessDialog();
                        this.add(processDialog);
                        processDialog.open();
                        processDialog.addOpenedChangeListener(openedChangeEvent -> {
                            if (!openedChangeEvent.isOpened()) {
                                this.remove(processDialog);
                            }
                        });
                        ingestedDocumentService.executeRedisRecordRemoval(ingestedDocument.getAssociatedUuid())
                                .addCallback(empty -> {
                                    executeCommand(ui,  () -> {
                                        ingestedDocumentService.executeMongoRecordRemoval(ingestedDocument.getId());
                                        this.ingestedDocumentGrid.getDataProvider().refreshAll();
                                        processDialog.close();
                                        Notification.show("El documento fue borrado exitosamente.", 2000, Notification.Position.TOP_CENTER);
                                    });
                                }, (ex) -> {
                                    executeCommand(ui, () -> {
                                        Notification.show("Se Ha Producido Un Error Al Remover El Registro.", 2000, Notification.Position.TOP_CENTER);
                                    });
                                });
                    });
                })).setHeader("Acciones");

        this.ingestedDocumentGrid.addSortListener(evt -> {
            List<GridSortOrder<IngestedDocument>> sortOrder = evt.getSortOrder();
            String sortProperty = sortOrder.get(0).getSorted().getKey();
            SortDirection direction = sortOrder.getFirst().getDirection();
            pagedDataProvider.getIngestedDocumentQuery().setSortData(sortProperty, direction);
            pagedDataProvider.refreshAll();
        });

        ingestedDocumentGrid.setDataProvider(pagedDataProvider);

        ingestedDocumentGrid.getHeaderRows().clear();
        HeaderRow headerRow = ingestedDocumentGrid.appendHeaderRow();

        Grid.Column<IngestedDocument> uploadDateColumn = ingestedDocumentGrid.getColumnByKey("uploadDate");
        headerRow.getCell(uploadDateColumn).setComponent(createDateFieldComponent(pagedDataProvider.retrieveCustomFilter()));

        Grid.Column<IngestedDocument> idColumn = ingestedDocumentGrid.getColumnByKey("id");
        headerRow.getCell(idColumn).setComponent(createFilterHeader(true, "id", pagedDataProvider.retrieveCustomFilter()));

        Grid.Column<IngestedDocument> decriptionColumn = ingestedDocumentGrid.getColumnByKey("description");
        headerRow.getCell(decriptionColumn).setComponent(createFilterHeader(false, "description", pagedDataProvider.retrieveCustomFilter()));

        Grid.Column<IngestedDocument> archivoColumn = ingestedDocumentGrid.getColumnByKey("fileName");
        headerRow.getCell(archivoColumn).setComponent(createFilterHeader(false, "fileName", pagedDataProvider.retrieveCustomFilter()));

        Grid.Column<IngestedDocument> ingestionTypeColumn = ingestedDocumentGrid.getColumnByKey("ingestionType");
        headerRow.getCell(ingestionTypeColumn).setComponent(createDropDownComponent(pagedDataProvider.retrieveCustomFilter()));

        container.add(ingestedDocumentGrid);

        return container;
    }

    private ComboBox<IngestionType> createDropDownComponent(BiConsumer<String, Object> filterIngestionTypeConsumer) {
        ComboBox<IngestionType> comboBox = new ComboBox<>();
        comboBox.setItems(IngestionType.values());
        comboBox.setItemLabelGenerator(IngestionType::name);
        comboBox.setClearButtonVisible(true);
        comboBox.addValueChangeListener(evt -> {
            if(nonNull(evt.getValue())) {
                filterIngestionTypeConsumer.accept("ingestionType", evt.getValue());
            } else {
                filterIngestionTypeConsumer.accept("ingestionType", null);
            }
        });
        return comboBox;
    }

    private DatePicker createDateFieldComponent(BiConsumer<String, Object> filterUploadDateConsumer) {
        DatePicker uploadDateFilterPicker = new DatePicker();
        uploadDateFilterPicker.setClearButtonVisible(true);
        uploadDateFilterPicker.setWidthFull();
        uploadDateFilterPicker.addValueChangeListener(evt -> {
            if(nonNull(evt.getValue())) {
                filterUploadDateConsumer.accept("uploadDate", evt.getValue().atStartOfDay());
            } else {
                filterUploadDateConsumer.accept("uploadDate", null);
            }
        });
        return uploadDateFilterPicker;
    }

    private static Component createFilterHeader(boolean numberValidation,
                                                String property,
                                                BiConsumer<String, Object> filterBiConsumer) {
        TextField textField = new TextField();
        textField.setValueChangeMode(ValueChangeMode.EAGER);
        textField.setClearButtonVisible(true);
        textField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        textField.setWidthFull();
        textField.getStyle().set("max-width", "100%");
        textField.addValueChangeListener(
                evt -> {
                    if(nonNull(evt.getValue()) && !evt.getValue().strip().isBlank()){
                        if(numberValidation) {
                            if(NumberUtils.isParsable(evt.getValue().strip())) {
                                filterBiConsumer.accept(property, evt.getValue().strip());
                            }
                            return;
                        }
                        filterBiConsumer.accept(property, evt.getValue().strip());
                    } else {
                        filterBiConsumer.accept(property, null);
                    }
                });
        return textField;
    }
}
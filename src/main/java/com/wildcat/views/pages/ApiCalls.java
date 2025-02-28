package com.wildcat.views.pages;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.wildcat.persistence.model.ApiCall;
import com.wildcat.persistence.model.IngestedDocument;
import com.wildcat.persistence.service.apicall.ApiCallService;
import com.wildcat.persistence.service.cost.ApiCostService;
import com.wildcat.utils.enums.IngestionType;
import com.wildcat.utils.enums.SearchType;
import com.wildcat.utils.providers.ApiCallDataProvider;
import com.wildcat.utils.providers.PagedDataProvider;
import com.wildcat.views.dialog.ApiCostsDialog;
import com.wildcat.views.layout.AiAssistantLayout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.isParsable;

@Route(value = "apì-calls", layout = AiAssistantLayout.class)
public class ApiCalls extends VerticalLayout {
    private ApiCostService apiCostService;
    private ApiCallService apiCallService;
    public ApiCalls(ApiCostService apiCostService, ApiCallService apiCallService) {
        this.apiCostService = apiCostService;
        this.apiCallService = apiCallService;

        // Documentos Ingestados
        H1 documentosIngestados = new H1("Api Calls");
        documentosIngestados.setWidthFull();
        documentosIngestados.getStyle().set("text-align", "center");
        this.add(documentosIngestados);

        Button apiCostsBtn = new Button("Costos");
        apiCostsBtn.addClickListener(evt -> {
            ApiCostsDialog apiCostsDialog = new ApiCostsDialog(this.apiCostService);
            this.add(apiCostsDialog);
            apiCostsDialog.open();
            apiCostsDialog.addOpenedChangeListener(openedEvt -> {
                if(!openedEvt.isOpened()) {
                    this.remove(apiCostsDialog);
                }
            });
        });
        this.add(apiCostsBtn);


        ApiCallDataProvider pagedDataProvider = new ApiCallDataProvider(this.apiCallService);

        Grid<ApiCall> apiCallGrid = new Grid<>(ApiCall.class, false);
        apiCallGrid.setPageSize(10);
        apiCallGrid.addColumn(ApiCall::getId).setHeader("Id").setKey("id").setSortable(true);
        apiCallGrid.addColumn(ApiCall::getModelName).setHeader("Modelo").setKey("modelName").setSortable(true);
        apiCallGrid.addColumn(ApiCall::getInputTokens).setHeader("Input #").setKey("inputTokens").setSortable(true);
        apiCallGrid.addColumn(ApiCall::getOutputTokens).setHeader("Output #").setKey("outputTokens").setSortable(true);
        apiCallGrid.addColumn(ApiCall::getCreatedDate).setHeader("Fecha Creacion").setKey("createdDate").setSortable(true);
        apiCallGrid.addColumn(apiCall -> apiCall.getInputTotal().setScale(3, RoundingMode.HALF_UP)).setHeader("Input Total").setKey("inputTotal").setSortable(true);
        apiCallGrid.addColumn(apiCall -> apiCall.getOutputTotal().setScale(3, RoundingMode.HALF_UP)).setHeader("Output Total").setKey("outputTotal").setSortable(true);
        apiCallGrid.addColumn(apiCall ->  apiCall.getTotal().setScale(3, RoundingMode.HALF_UP)).setHeader("Total").setSortable(false);
        apiCallGrid.addColumn(ApiCall::getSearchType).setHeader("Tipo Busqueda").setKey("searchType").setSortable(true);
        apiCallGrid.setWidthFull();
        apiCallGrid.setAllRowsVisible(true);
        this.add(apiCallGrid);

        apiCallGrid.setDataProvider(pagedDataProvider);

        apiCallGrid.getHeaderRows().clear();
        HeaderRow headerRow = apiCallGrid.appendHeaderRow();

        apiCallGrid.addSortListener(evt -> {
            List<GridSortOrder<ApiCall>> sortOrder = evt.getSortOrder();
            String sortProperty = sortOrder.get(0).getSorted().getKey();
            if(nonNull(sortProperty)) {
                SortDirection direction = sortOrder.getFirst().getDirection();
                pagedDataProvider.getApiCallQuery().setSortData(sortProperty, direction);
                pagedDataProvider.refreshAll();
            }
        });

        // id
        Grid.Column<ApiCall> idColumn = apiCallGrid.getColumnByKey("id");
        headerRow.getCell(idColumn).setComponent(createInputText("id", true, pagedDataProvider.retrieveCustomFilter()));
        // modelName
        Grid.Column<ApiCall> modelNameColumn = apiCallGrid.getColumnByKey("modelName");
        headerRow.getCell(modelNameColumn).setComponent(createInputText("modelName", false, pagedDataProvider.retrieveCustomFilter()));
        // inputTokens
        Grid.Column<ApiCall> inputTokensColumn = apiCallGrid.getColumnByKey("inputTokens");
        headerRow.getCell(inputTokensColumn).setComponent(createTokenHeader("inputTokens", pagedDataProvider.retrieveCustomFilter()));
        // outputTokens
        Grid.Column<ApiCall> outputTokensColumn = apiCallGrid.getColumnByKey("outputTokens");
        headerRow.getCell(outputTokensColumn).setComponent(createTokenHeader("outputTokens", pagedDataProvider.retrieveCustomFilter()));
        // createdDate
        Grid.Column<ApiCall> createdDateColumn = apiCallGrid.getColumnByKey("createdDate");
        headerRow.getCell(createdDateColumn).setComponent(createDateHeader("createdDate", pagedDataProvider.retrieveCustomFilter()));
        // inputTotal
        Grid.Column<ApiCall> inputTotalColumn = apiCallGrid.getColumnByKey("inputTotal");
        headerRow.getCell(inputTotalColumn).setComponent(createTotalHeader("inputTotal", pagedDataProvider.retrieveCustomFilter()));
        // outputTotal
        Grid.Column<ApiCall> outputTotalColumn = apiCallGrid.getColumnByKey("outputTotal");
        headerRow.getCell(outputTotalColumn).setComponent(createTotalHeader("outputTotal", pagedDataProvider.retrieveCustomFilter()));
        // searchType
        Grid.Column<ApiCall> searchTypeColumn = apiCallGrid.getColumnByKey("searchType");
        headerRow.getCell(searchTypeColumn).setComponent(createSearchTypeHeader("searchType", pagedDataProvider.retrieveCustomFilter()));

    }

    private VerticalLayout createInputText(String property, boolean withNumberValidation, BiConsumer<String, Object> filterConsumer) {
        VerticalLayout headerContainer = new VerticalLayout();
        TextField inputText = new TextField();
        inputText.getStyle().set("border", "1px solid blue");
        inputText.setWidthFull();
        headerContainer.add(inputText);
        headerContainer.setSizeFull();
        headerContainer.getStyle().set("border", "1px solid black");

        inputText.setClearButtonVisible(true);
        inputText.setValueChangeMode(ValueChangeMode.EAGER);
        inputText.addValueChangeListener(evt -> {
            String value = inputText.getValue();
            if(isNull(value) || value.strip().isEmpty()) {
                filterConsumer.accept(property, null);
            } else {
                if(withNumberValidation) {
                    if(isParsable(value.strip())) {
                        filterConsumer.accept(property, value.strip());
                    } else {
                        filterConsumer.accept(property, null);
                    }
                } else {
                    filterConsumer.accept(property, value.strip());
                }
            }
        });
        return headerContainer;
    }

    private VerticalLayout createTotalHeader(String property, BiConsumer<String, Object> filterConsumer) {
        VerticalLayout headerContainer = new VerticalLayout();
        headerContainer.getStyle().set("border", "1px solid black");
        headerContainer.setSizeFull();

        TextField tokenStart = new TextField();
        tokenStart.getStyle().set("border", "1px solid blue");
        tokenStart.setClearButtonVisible(true);
        tokenStart.setWidthFull();
        tokenStart.setValueChangeMode(ValueChangeMode.EAGER);

        TextField tokenEnd = new TextField();
        tokenEnd.getStyle().set("border", "1px solid blue");
        tokenEnd.setClearButtonVisible(true);
        tokenEnd.setWidthFull();
        tokenEnd.setValueChangeMode(ValueChangeMode.EAGER);

        headerContainer.add(tokenStart, tokenEnd);

        tokenStart.addValueChangeListener(evt -> {
            String value = evt.getValue();
            if(isNull(value) || value.strip().isEmpty()) {
                filterConsumer.accept(property, null);
            } else {
                String endValue = tokenEnd.getValue();
                if(isParsable(value.strip()) && nonNull(endValue) && isParsable(endValue.strip())) {
                    BigDecimal start = new BigDecimal(value.strip());
                    BigDecimal end = new BigDecimal(endValue.strip());
                    BigDecimal tokens[] = {start, end};
                    filterConsumer.accept(property, tokens);
                } else {
                    filterConsumer.accept(property, null);
                }
            }
        });

        tokenEnd.addValueChangeListener(evt -> {
            String value = evt.getValue();
            if(isNull(value) || value.strip().isEmpty()) {
                filterConsumer.accept(property, null);
            } else {
                String startValue = tokenStart.getValue();
                if(isParsable(value.strip()) && nonNull(startValue) && isParsable(startValue.strip())) {
                    BigDecimal start = new BigDecimal(startValue.strip());
                    BigDecimal end = new BigDecimal(value.strip());
                    BigDecimal tokens[] = {start, end};
                    filterConsumer.accept(property, tokens);
                } else {
                    filterConsumer.accept(property, null);
                }
            }
        });

        return headerContainer;
    }

    private VerticalLayout createTokenHeader(String property, BiConsumer<String, Object> filterConsumer) {
        VerticalLayout headerContainer = new VerticalLayout();
        headerContainer.getStyle().set("border", "1px solid black");
        headerContainer.setSizeFull();

        TextField tokenStart = new TextField();
        tokenStart.getStyle().set("border", "1px solid blue");
        tokenStart.setClearButtonVisible(true);
        tokenStart.setWidthFull();
        tokenStart.setValueChangeMode(ValueChangeMode.EAGER);

        TextField tokenEnd = new TextField();
        tokenEnd.getStyle().set("border", "1px solid blue");
        tokenEnd.setClearButtonVisible(true);
        tokenEnd.setWidthFull();
        tokenEnd.setValueChangeMode(ValueChangeMode.EAGER);

        headerContainer.add(tokenStart, tokenEnd);

        tokenStart.addValueChangeListener(evt -> {
            String value = evt.getValue();
            if(isNull(value) || value.strip().isEmpty()) {
                filterConsumer.accept(property, null);
            } else {
                String endValue = tokenEnd.getValue();
                if(isParsable(value.strip()) && nonNull(endValue) && isParsable(endValue.strip())) {
                    Long start = Long.parseLong(value);
                    Long end = Long.parseLong(endValue);
                    Long tokens[] = {start, end};
                    filterConsumer.accept(property, tokens);
                } else {
                    filterConsumer.accept(property, null);
                }
            }
        });

        tokenEnd.addValueChangeListener(evt -> {
            String value = evt.getValue();
            if(isNull(value) || value.strip().isEmpty()) {
                filterConsumer.accept(property, null);
            } else {
                String startValue = tokenStart.getValue();
                if(isParsable(value.strip()) && nonNull(startValue) && isParsable(startValue.strip())) {
                    Long start = Long.parseLong(startValue);
                    Long end = Long.parseLong(value);
                    Long tokens[] = {start, end};
                    filterConsumer.accept(property, tokens);
                } else {
                    filterConsumer.accept(property, null);
                }
            }
        });

        return headerContainer;
    }

    private VerticalLayout createDateHeader(String property, BiConsumer<String, Object> filterConsumer) {
        VerticalLayout headerContainer = new VerticalLayout();
        headerContainer.getStyle().set("border", "1px solid black");
        headerContainer.setSizeFull();

        DatePicker datePicker = new DatePicker();
        headerContainer.add(datePicker);
        datePicker.getStyle().set("border", "1px solid blue");
        datePicker.setWidthFull();
        datePicker.setClearButtonVisible(true);
        datePicker.addValueChangeListener(evt -> {
            LocalDate value = evt.getValue();
            filterConsumer.accept(property, value);
        });

        return headerContainer;
    }

    private ComboBox<SearchType> createSearchTypeHeader(String property, BiConsumer<String, Object> filterIngestionTypeConsumer) {
        ComboBox<SearchType> comboBox = new ComboBox<>();
        comboBox.setItems(SearchType.values());
        comboBox.setItemLabelGenerator(SearchType::name);
        comboBox.setClearButtonVisible(true);
        comboBox.addValueChangeListener(evt -> {
            filterIngestionTypeConsumer.accept(property, evt.getValue());
        });
        return comboBox;
    }
}

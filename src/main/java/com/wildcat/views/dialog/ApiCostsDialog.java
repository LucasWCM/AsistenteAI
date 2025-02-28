package com.wildcat.views.dialog;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.wildcat.persistence.model.ApiCost;
import com.wildcat.persistence.service.cost.ApiCostService;
import com.wildcat.utils.enums.Token;
import org.apache.commons.lang3.math.NumberUtils;

import static com.wildcat.utils.AiUtils.buildAndShowConfirmDialog;
import static java.util.Objects.nonNull;


public class ApiCostsDialog extends Dialog {
    private Binder<ApiCost> registrationFormBinder = new BeanValidationBinder<>(ApiCost.class);
    private Button btnSave;
    private Button btnCancel;
    private boolean initialized = false;
    private ApiCost apiCost = new ApiCost();
    private Grid<ApiCost> apiCostGrid;
    public ApiCostsDialog(ApiCostService apiCostService) {
        this.setWidth("600px");
        this.setHeight("550px");
        initializeHeader();
        initializeFooter(apiCostService);
        initialized = true;
        initializeForm(apiCostService);
    }

    private void initializeForm(ApiCostService apiCostService) {
        TextField modelNameTxt = new TextField("Modelo");
        modelNameTxt.setValueChangeMode(ValueChangeMode.EAGER);
        modelNameTxt.setWidthFull();

        TextField amountTxt = new TextField("Cantidad");
        amountTxt.setValueChangeMode(ValueChangeMode.EAGER);
        amountTxt.setWidthFull();

        TextField moneyQuantity = new TextField("Costo");
        moneyQuantity.setValueChangeMode(ValueChangeMode.EAGER);
        moneyQuantity.setWidthFull();

        ComboBox<Token> comboToken = new ComboBox<>("Tipo");
        comboToken.setWidthFull();
        comboToken.setItems(Token.values());
        comboToken.setItemLabelGenerator(Token::name);
        comboToken.setClearButtonVisible(true);

        this.add(modelNameTxt, moneyQuantity, amountTxt, comboToken);

        registrationFormBinder.forField(modelNameTxt)
                .withValidator(val -> nonNull(val) && !val.strip().isBlank(), "Debe Ingresar Un Modelo.")
                .withValidator(val -> val.strip().length() < 25, "Debe Ingresar Un Modelo De Menor Longitud.")
                .bind(ApiCost::getModelName, ApiCost::setModelName);

        registrationFormBinder.forField(amountTxt)
                .withValidator(val -> nonNull(val) && !val.strip().isBlank(), "Debe Ingresar Una Cantidad.")
                .withValidator(val -> NumberUtils.isParsable(val.strip()), "Debe Ingresar Una Cantidad Valida.")
                .withConverter(Long::valueOf,
                        String::valueOf,
                        // Text to use instead of the
                        // NumberFormatException message
                        "Cantidad No Valida")
                .bind(ApiCost::getAmount, ApiCost::setAmount);

        registrationFormBinder.forField(moneyQuantity)
                .withValidator(val -> nonNull(val) && !val.strip().isBlank(), "Debe Ingresar Una Costo.")
                .withValidator(val -> NumberUtils.isParsable(val.strip()), "Debe Ingresar Un Costo Valido.")
                .withConverter(Long::valueOf,
                        String::valueOf,
                        // Text to use instead of the
                        // NumberFormatException message
                        "Costo No Valido")
                .bind(ApiCost::getQuantity, ApiCost::setQuantity);

        registrationFormBinder.forField(comboToken)
                .withValidator(val -> nonNull(val), "Debe Ingresar Un Token.")
                .bind(ApiCost::getToken, ApiCost::setToken);

        registrationFormBinder.addStatusChangeListener(evt -> {
            if(initialized) {
                btnSave.setEnabled(registrationFormBinder.isValid());
            }
        });

        this.apiCostGrid = new Grid<>(ApiCost.class, false);
        this.apiCostGrid.setWidthFull();
        this.apiCostGrid.setHeight("200px");
        this.apiCostGrid.setAllRowsVisible(true);
        this.apiCostGrid.addColumn(ApiCost::getId).setHeader("Id");
        this.apiCostGrid.addColumn(ApiCost::getModelName).setHeader("Modelo");
        this.apiCostGrid.addColumn(ApiCost::getAmount).setHeader("Monto");
        this.apiCostGrid.addColumn(ApiCost::getQuantity).setHeader("Cantidad");
        this.apiCostGrid.addColumn(ApiCost::getToken).setHeader("Token");
        this.apiCostGrid.addColumn(new ComponentRenderer<>(apiCost -> {
            Button removeBtn = new Button();
            removeBtn.setIcon(VaadinIcon.DEL.create());
            removeBtn.addClickListener(evt -> {
                ConfirmDialog confirmDialog = buildAndShowConfirmDialog("Confirmar Operacion", "Desea Borrar El Costo ?.");
                confirmDialog.open();
                confirmDialog.addConfirmListener(confirmEvt -> {
                    apiCostService.removeById(apiCost.getId());
                    this.apiCostGrid.setItems(apiCostService.getAllApiCosts());
                });
            });
            return removeBtn;
        })).setHeader("Acciones");
        this.apiCostGrid.setItems(apiCostService.getAllApiCosts());
        this.add(this.apiCostGrid);
    }

    private void initializeFooter(ApiCostService apiCostService) {
        btnSave = new Button("Guardar");
        btnSave.setEnabled(false);
        btnSave.addClickListener(evt -> {
            if(registrationFormBinder.writeBeanIfValid(apiCost)){
                // TODO: Poner el ID del usuario
                apiCostService.save(apiCost);
                registrationFormBinder.readBean(null);
                this.apiCostGrid.setItems(apiCostService.getAllApiCosts());
                apiCost.resetValues();
            }
        });
        btnCancel = new Button("Cancelar");
        btnCancel.addClickListener(evt -> {
            this.close();
        });
        Span spacer = new Span();
        HorizontalLayout footerContainer = new HorizontalLayout(btnSave, spacer, btnCancel);
        footerContainer.setFlexGrow(1, spacer);
        this.getFooter().add(footerContainer);
        footerContainer.setWidthFull();
    }

    private void initializeHeader() {
        H2 headerTitle = new H2("Api Costs");
        headerTitle.setWidthFull();
        headerTitle.getStyle().set("text-align", "center");
        this.getHeader().add(headerTitle);
    }
}

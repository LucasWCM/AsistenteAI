package com.wildcat;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import com.wildcat.ai.services.ui.UiOperations;
import com.wildcat.utils.dto.UiResult;
import com.wildcat.utils.events.RightBoxItemsChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static com.wildcat.utils.AiUtils.executeCommand;

public class ListBoxGroup extends HorizontalLayout {
    private MultiSelectListBox<String> leftListBox;
    private MultiSelectListBox<String> rightListBox;
    private Button btnLeft;
    private Button btnAllLeft;
    private Button btnRight;
    private Button btnAllRight;
    private ComboBox<Integer> cmbFiltering;
    private List<String> backUpList;

    public ListBoxGroup(UiOperations uiOperations) {
        UI ui = UI.getCurrent();

        leftListBox = new MultiSelectListBox<>();
        leftListBox.getStyle().set("border", "1px solid blue");
        leftListBox.getStyle().set("padding", "3px");
        leftListBox.setWidth("100px");
        leftListBox.setHeight("300px");

        rightListBox = new MultiSelectListBox<>();
        rightListBox.getStyle().set("border", "1px solid blue");
        rightListBox.getStyle().set("padding", "3px");
        rightListBox.setWidth("100px");
        rightListBox.setHeight("300px");


        List<Integer> lengthFilteringList = IntStream.rangeClosed(3, 100).boxed().toList();
        this.cmbFiltering = new ComboBox<>();
        this.cmbFiltering.setSizeFull();
        this.cmbFiltering.setItems(lengthFilteringList);
        this.cmbFiltering.setItemLabelGenerator(String::valueOf);
        this.cmbFiltering.addValueChangeListener(evt -> {
            List<String> tempList = this.backUpList.stream()
                    .filter(word -> word.length() <= evt.getValue().intValue())
                    .toList();
            executeCommand(ui, () -> {
                leftListBox.setItems(tempList);
            });
        });


        btnAllRight = new Button();
        btnAllRight.setIcon(VaadinIcon.FORWARD.create());
        btnAllRight.addClickListener(evt -> {
            Map<String, Boolean> resetStatusMap = Map.of("right", btnRight.isEnabled(), "allRight", btnAllRight.isEnabled(),
                    "left", btnLeft.isEnabled(), "allLeft", btnAllLeft.isEnabled());
            setEnabled(false, false);
            uiOperations.executeUiOperation(() -> {
                List<String> currentItemsFromRight = rightListBox.getListDataView().getItems().toList();
                List<String> currentItemsFromLeft = leftListBox.getListDataView().getItems().toList();
                List<String> currentFromRight = new ArrayList<>();
                currentFromRight.addAll(currentItemsFromRight);
                currentFromRight.addAll(currentItemsFromLeft);
                return new UiResult(List.of(), currentFromRight);
            }).addCallback(result -> {
                executeCommand(ui, () -> {
                    leftListBox.setItems(result.getLeftComboItems());
                    rightListBox.setItems(result.getRightComboItems());

                    // UI Status Update
                    btnRight.setEnabled(false);
                    btnAllRight.setEnabled(false);
                    btnLeft.setEnabled(true);
                    btnAllLeft.setEnabled(true);
                    leftListBox.setEnabled(true);
                    rightListBox.setEnabled(true);
                    cmbFiltering.setEnabled(true);

                    this.fireEvent(new RightBoxItemsChangeEvent(this, false, true));
                });
            }, (ex) -> {
                executeCommand(ui, () -> {
                    btnRight.setEnabled(resetStatusMap.get("right"));
                    btnAllRight.setEnabled(resetStatusMap.get("allRight"));
                    btnLeft.setEnabled(resetStatusMap.get("left"));
                    btnAllLeft.setEnabled(resetStatusMap.get("allLeft"));
                    leftListBox.setEnabled(true);
                    rightListBox.setEnabled(true);
                    cmbFiltering.setEnabled(true);
                    Notification.show("Se Ha Producido Un Error Al Efectuar La Operacion: %s.".formatted(ex.getMessage()), 2000, Notification.Position.TOP_CENTER);
                });
            });

        });
        btnAllLeft = new Button();
        btnAllLeft.setIcon(VaadinIcon.BACKWARDS.create());
        btnAllLeft.addClickListener(evt -> {

            Map<String, Boolean> resetStatusMap = Map.of("right", btnRight.isEnabled(), "allRight", btnAllRight.isEnabled(),
                    "left", btnLeft.isEnabled(), "allLeft", btnAllLeft.isEnabled());
            setEnabled(false, false);

            uiOperations.executeUiOperation(() -> {
                List<String> currentItemsFromRight = rightListBox.getListDataView().getItems().toList();
                List<String> currentItemsFromLeft = leftListBox.getListDataView().getItems().toList();
                List<String> currentFromRight = new ArrayList<>();
                currentFromRight.addAll(currentItemsFromRight);
                currentFromRight.addAll(currentItemsFromLeft);
                return new UiResult(currentFromRight, List.of());
            }).addCallback(result -> {
                executeCommand(ui, () -> {
                    leftListBox.setItems(result.getLeftComboItems());
                    rightListBox.setItems(result.getRightComboItems());

                    // UI Status Update
                    btnRight.setEnabled(true);
                    btnAllRight.setEnabled(true);
                    btnLeft.setEnabled(false);
                    btnAllLeft.setEnabled(false);
                    leftListBox.setEnabled(true);
                    rightListBox.setEnabled(true);
                    cmbFiltering.setEnabled(true);

                    this.fireEvent(new RightBoxItemsChangeEvent(this, false, false));
                });
            }, (ex) -> {
                executeCommand(ui, () -> {
                    btnRight.setEnabled(resetStatusMap.get("right"));
                    btnAllRight.setEnabled(resetStatusMap.get("allRight"));
                    btnLeft.setEnabled(resetStatusMap.get("left"));
                    btnAllLeft.setEnabled(resetStatusMap.get("allLeft"));
                    leftListBox.setEnabled(true);
                    rightListBox.setEnabled(true);
                    cmbFiltering.setEnabled(true);
                    Notification.show("Se Ha Producido Un Error Al Efectuar La Operacion: %s.".formatted(ex.getMessage()), 2000, Notification.Position.TOP_CENTER);
                });
            });
        });

        btnLeft = new Button();
        btnLeft.setIcon(VaadinIcon.LEVEL_LEFT.create());
        btnLeft.addClickListener(evt -> {

            Set<String> rightSelectedItems = rightListBox.getSelectedItems();
            if(rightSelectedItems.isEmpty()) {
                Notification.show("Se Debe Seleccionar Un Item/s Del ListBox De La Derecha.", 2000, Notification.Position.TOP_CENTER);
                return;
            }

            Map<String, Boolean> resetStatusMap = Map.of("right", btnRight.isEnabled(), "allRight", btnAllRight.isEnabled(),
                    "left", btnLeft.isEnabled(), "allLeft", btnAllLeft.isEnabled());
            setEnabled(false, false);

            uiOperations.executeUiOperation(() -> {
                List<String> currentFromRight = new ArrayList<>(rightListBox.getListDataView().getItems().toList());
                currentFromRight.removeIf(word -> rightSelectedItems.contains(word));

                List<String> currentFromLeft = leftListBox.getListDataView().getItems().toList();

                List<String> finalCurrentFromLeft = new ArrayList<>();
                finalCurrentFromLeft.addAll(currentFromLeft);
                finalCurrentFromLeft.addAll(rightSelectedItems);

                return new UiResult(finalCurrentFromLeft, currentFromRight);
            }).addCallback(result -> {
                executeCommand(ui, () -> {
                    leftListBox.setItems(result.getLeftComboItems());
                    rightListBox.setItems(result.getRightComboItems());

                    if(result.getRightComboItems().isEmpty()) {
                        btnLeft.setEnabled(false);
                        btnAllLeft.setEnabled(false);

                        this.fireEvent(new RightBoxItemsChangeEvent(this, false, false));
                    } else {
                        btnLeft.setEnabled(true);
                        btnAllLeft.setEnabled(true);
                    }
                    // UI Status Update
                    btnRight.setEnabled(true);
                    btnAllRight.setEnabled(true);
                    leftListBox.setEnabled(true);
                    rightListBox.setEnabled(true);
                    cmbFiltering.setEnabled(true);
                });
            }, (ex) -> {
                executeCommand(ui, () -> {
                    btnRight.setEnabled(resetStatusMap.get("right"));
                    btnAllRight.setEnabled(resetStatusMap.get("allRight"));
                    btnLeft.setEnabled(resetStatusMap.get("left"));
                    btnAllLeft.setEnabled(resetStatusMap.get("allLeft"));
                    leftListBox.setEnabled(true);
                    rightListBox.setEnabled(true);
                    cmbFiltering.setEnabled(true);
                    Notification.show("Se Ha Producido Un Error Al Efectuar La Operacion: %s.".formatted(ex.getMessage()), 2000, Notification.Position.TOP_CENTER);
                });
            });
        });
        btnRight = new Button();
        btnRight.setIcon(VaadinIcon.LEVEL_RIGHT.create());
        btnRight.addClickListener(evt -> {
            Set<String> leftSelectedItems = leftListBox.getSelectedItems();
            if(leftSelectedItems.isEmpty()) {
                Notification.show("Se Debe Seleccionar Un Item/s Del ListBox De La Izquierda.", 2000, Notification.Position.TOP_CENTER);
                return;
            }

            Map<String, Boolean> resetStatusMap = Map.of("right", btnRight.isEnabled(), "allRight", btnAllRight.isEnabled(),
                    "left", btnLeft.isEnabled(), "allLeft", btnAllLeft.isEnabled());
            setEnabled(false, false);


            uiOperations.executeUiOperation(() -> {
                List<String> currentFromLeft = new ArrayList<>(leftListBox.getListDataView().getItems().toList());
                currentFromLeft.removeIf(word -> leftSelectedItems.contains(word));

                List<String> currentFromRight = rightListBox.getListDataView().getItems().toList();

                List<String> finalCurrentFromLeft = new ArrayList<>();
                finalCurrentFromLeft.addAll(currentFromRight);
                finalCurrentFromLeft.addAll(leftSelectedItems);

                return new UiResult(currentFromLeft, finalCurrentFromLeft);
            }).addCallback(result -> {
                executeCommand(ui, () -> {
                    leftListBox.setItems(result.getLeftComboItems());
                    rightListBox.setItems(result.getRightComboItems());

                    if(result.getLeftComboItems().isEmpty()) {
                        btnRight.setEnabled(false);
                        btnAllRight.setEnabled(false);
                    } else {
                        btnRight.setEnabled(true);
                        btnAllRight.setEnabled(true);
                    }

                    // UI Status Update
                    btnLeft.setEnabled(true);
                    btnAllLeft.setEnabled(true);
                    leftListBox.setEnabled(true);
                    rightListBox.setEnabled(true);
                    cmbFiltering.setEnabled(true);

                    this.fireEvent(new RightBoxItemsChangeEvent(this, false, true));
                });
            }, (ex) -> {
                executeCommand(ui, () -> {
                    btnRight.setEnabled(resetStatusMap.get("right"));
                    btnAllRight.setEnabled(resetStatusMap.get("allRight"));
                    btnLeft.setEnabled(resetStatusMap.get("left"));
                    btnAllLeft.setEnabled(resetStatusMap.get("allLeft"));
                    leftListBox.setEnabled(true);
                    rightListBox.setEnabled(true);
                    cmbFiltering.setEnabled(true);
                    Notification.show("Se Ha Producido Un Error Al Efectuar La Operacion: %s.".formatted(ex.getMessage()), 2000, Notification.Position.TOP_CENTER);
                });
            });

        });

        VerticalLayout buttonContainers = new VerticalLayout(cmbFiltering, btnLeft, btnRight, btnAllLeft, btnAllRight);
        buttonContainers.setAlignItems(FlexComponent.Alignment.CENTER);
        buttonContainers.setWidth("100px");

        this.add(leftListBox, buttonContainers, rightListBox);
    }

    public void setEnabled(boolean pdfLoading, boolean enabled) {
        leftListBox.setEnabled(enabled);
        rightListBox.setEnabled(enabled);
        btnRight.setEnabled(enabled);
        btnAllRight.setEnabled(enabled);
        cmbFiltering.setEnabled(enabled);

        btnLeft.setEnabled(pdfLoading ? false : enabled);
        btnAllLeft.setEnabled(pdfLoading ? false : enabled);

    }

    public void setItemsOnLeft(List<String> items) {
        this.backUpList = items;
        leftListBox.setItems(items);
    }

    public List<String> getItemsOnRight() {
        return this.rightListBox.getListDataView().getItems().toList();
    }

    public Registration addRightBoxItemsChangeListener(ComponentEventListener<RightBoxItemsChangeEvent> listener) {
        return addListener(RightBoxItemsChangeEvent.class, listener);
    }

}

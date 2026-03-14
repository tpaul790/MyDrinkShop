package drinkshop.ui;

import drinkshop.domain.*;
import drinkshop.service.DrinkShopService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class DrinkShopController {

    private DrinkShopService service;

    // ---------- PRODUCT ----------
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> colProdId;
    @FXML private TableColumn<Product, String> colProdName;
    @FXML private TableColumn<Product, Double> colProdPrice;
    @FXML private TableColumn<Product, CategorieBautura> colProdCategorie;
    @FXML private TableColumn<Product, TipBautura> colProdTip;
    @FXML private TextField txtProdName, txtProdPrice;
    @FXML private ComboBox<CategorieBautura> comboProdCategorie;
    @FXML private ComboBox<TipBautura> comboProdTip;

    // ---------- RETETE ----------
    @FXML private TableView<Reteta> retetaTable;
    @FXML private TableColumn<Reteta, Integer> colRetetaId;
    @FXML private TableColumn<Reteta, String> colRetetaDesc;

    @FXML private TableView<IngredientReteta> newRetetaTable;
    @FXML private TableColumn<IngredientReteta, String> colNewIngredName;
    @FXML private TableColumn<IngredientReteta, Double> colNewIngredCant;
    @FXML private TextField txtNewIngredName, txtNewIngredCant;

    // ---------- ORDER (CURRENT) ----------
    @FXML private TableView<OrderItem> currentOrderTable;
    @FXML private TableColumn<OrderItem, String> colOrderProdName;
    @FXML private TableColumn<OrderItem, Integer> colOrderQty;

    @FXML private ComboBox<Integer> comboQty;
    @FXML private Label lblOrderTotal;
    @FXML private TextArea txtReceipt;

    @FXML private Label lblTotalRevenue;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ObservableList<Reteta> retetaList = FXCollections.observableArrayList();
    private ObservableList<IngredientReteta> newRetetaList = FXCollections.observableArrayList();
    private ObservableList<OrderItem> currentOrderItems = FXCollections.observableArrayList();

    private Order currentOrder = new Order(1);

    public void setService(DrinkShopService service) {
        this.service = service;
        this.currentOrder = new Order(getNextOrderId());
        initData();
    }

    @FXML
    private void initialize() {

        // PRODUCTS
        colProdId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProdName.setCellValueFactory(new PropertyValueFactory<>("nume"));
        colProdPrice.setCellValueFactory(new PropertyValueFactory<>("pret"));
        colProdCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colProdTip.setCellValueFactory(new PropertyValueFactory<>("tip"));
        productTable.setItems(productList);

        comboProdCategorie.getItems().setAll(CategorieBautura.values());
        comboProdTip.getItems().setAll(TipBautura.values());

        // RETETE
        colRetetaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRetetaDesc.setCellValueFactory(data -> {
            Reteta r = data.getValue();
            String desc = r.getIngrediente().stream()
                    .map(i -> i.getDenumire() + " (" + i.getCantitate() + ")")
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(desc);
        });
        retetaTable.setItems(retetaList);

        colNewIngredName.setCellValueFactory(new PropertyValueFactory<>("denumire"));
        colNewIngredCant.setCellValueFactory(new PropertyValueFactory<>("cantitate"));
        newRetetaTable.setItems(newRetetaList);

        // CURRENT ORDER TABLE
        colOrderProdName.setCellValueFactory(data -> {
            int prodId = data.getValue().getProduct().getId();
            Product p = productList.stream().filter(pr -> pr.getId() == prodId).findFirst().orElse(null);
            return new SimpleStringProperty(p != null ? p.getNume() : "N/A");
        });
        colOrderQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        currentOrderTable.setItems(currentOrderItems);

        comboQty.setItems(FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10));
    }

    private void initData() {
        productList.setAll(service.getAllProducts());
        retetaList.setAll(service.getAllRetete());
        lblTotalRevenue.setText("Daily Revenue: " + service.getDailyRevenue());
        updateOrderTotal();
    }

    // ---------- PRODUCT ----------
    @FXML
    private void onAddProduct() {
        Reteta r = retetaTable.getSelectionModel().getSelectedItem();

        if (r == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Selectati o reteta pentru care adugati un produs");
            alert.showAndWait();
            return;
        }
        if (service.getAllProducts().stream().anyMatch(p -> p.getId() == r.getId())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Error");
            alert.setHeaderText("Exista un produs cu reteta adaugata.");
            alert.showAndWait();
            return;
        }

        if (txtProdName.getText() == null || txtProdName.getText().isBlank()) {
            showError("Numele produsului este obligatoriu.");
            return;
        }
        if (comboProdCategorie.getValue() == null || comboProdTip.getValue() == null) {
            showError("Categoria si tipul produsului sunt obligatorii.");
            return;
        }

        final double price;
        try {
            price = Double.parseDouble(txtProdPrice.getText().trim());
        } catch (NumberFormatException ex) {
            showError("Pret invalid. Introdu un numar valid.");
            return;
        }

        Product p = new Product(
                r.getId(),
                txtProdName.getText().trim(),
                price,
                comboProdCategorie.getValue(),
                comboProdTip.getValue()
        );
        service.addProduct(p);
        initData();
    }

    @FXML
    private void onUpdateProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selecteaza produsul pe care vrei sa il actualizezi.");
            return;
        }

        if (txtProdName.getText() == null || txtProdName.getText().isBlank()) {
            showError("Numele produsului este obligatoriu.");
            return;
        }
        if (comboProdCategorie.getValue() == null || comboProdTip.getValue() == null) {
            showError("Categoria si tipul produsului sunt obligatorii.");
            return;
        }

        final double price;
        try {
            price = Double.parseDouble(txtProdPrice.getText().trim());
        } catch (NumberFormatException ex) {
            showError("Pret invalid. Introdu un numar valid.");
            return;
        }

        service.updateProduct(
                selected.getId(),
                txtProdName.getText().trim(),
                price,
                comboProdCategorie.getValue(),
                comboProdTip.getValue()
        );
        initData();
    }

    @FXML
    private void onDeleteProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        service.deleteProduct(selected.getId());
        initData();
    }

    @FXML
    private void onFilterCategorie() {
        productList.setAll(service.filtreazaDupaCategorie(comboProdCategorie.getValue()));
    }

    @FXML
    private void onFilterTip() {
        productList.setAll(service.filtreazaDupaTip(comboProdTip.getValue()));
    }

    // ---------- RETETA NOUA ----------
    @FXML
    private void onAddNewIngred() {
        if (txtNewIngredName.getText() == null || txtNewIngredName.getText().isBlank()) {
            showError("Numele ingredientului este obligatoriu.");
            return;
        }

        final double cantitate;
        try {
            cantitate = Double.parseDouble(txtNewIngredCant.getText().trim());
        } catch (NumberFormatException ex) {
            showError("Cantitate invalida. Introdu un numar valid.");
            return;
        }

        if (cantitate <= 0) {
            showError("Cantitatea trebuie sa fie mai mare decat 0.");
            return;
        }

        newRetetaList.add(new IngredientReteta(txtNewIngredName.getText().trim(), cantitate));
    }

    @FXML
    private void onDeleteNewIngred() {
        IngredientReteta sel = newRetetaTable.getSelectionModel().getSelectedItem();
        if (sel != null) newRetetaList.remove(sel);
    }

    @FXML
    private void onAddNewReteta() {
        if (newRetetaList.isEmpty()) {
            showError("Reteta trebuie sa contina cel putin un ingredient.");
            return;
        }

        Reteta r = new Reteta(getNextRetetaId(), new ArrayList<>(newRetetaList));
        service.addReteta(r);
        newRetetaList.clear();
        initData();
    }

    @FXML
    private void onClearNewRetetaIngredients() {
        newRetetaTable.getItems().clear();
        txtNewIngredName.clear();
        txtNewIngredCant.clear();
    }

    // ---------- CURRENT ORDER ----------
    @FXML
    private void onAddOrderItem() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        Integer qty = comboQty.getValue();

        if (selected == null) {
            showError("Selectează un produs din listă.");
            return;
        }
        if (qty == null) {
            showError("Selectează cantitatea.");
            return;
        }

        currentOrderItems.add(new OrderItem(selected, qty));
        updateOrderTotal();
    }

    @FXML
    private void onDeleteOrderItem() {
        OrderItem sel = currentOrderTable.getSelectionModel().getSelectedItem();
        if (sel != null) {
            currentOrderItems.remove(sel);
            updateOrderTotal();
        }
    }

    @FXML
    private void onFinalizeOrder() {
        if (currentOrderItems.isEmpty()) {
            showError("Comanda curenta este goala.");
            return;
        }

        try {
            for (OrderItem item : currentOrderItems) {
                for (int i = 0; i < item.getQuantity(); i++) {
                    service.comandaProdus(item.getProduct());
                }
            }
        } catch (IllegalStateException ex) {
            showError(ex.getMessage());
            return;
        }

        currentOrder.getItems().clear();
        currentOrder.getItems().addAll(currentOrderItems);
        currentOrder.computeTotalPrice();

        service.addOrder(currentOrder);
        txtReceipt.setText(service.generateReceipt(currentOrder));

        currentOrderItems.clear();
        currentOrder = new Order(getNextOrderId());
        updateOrderTotal();
        onDailyRevenue();
    }

    private void updateOrderTotal() {
        currentOrder.getItems().clear();
        currentOrder.getItems().addAll(currentOrderItems);
        double total = service.computeTotal(currentOrder);
        lblOrderTotal.setText("Total: " + total);
    }

    private int getNextOrderId() {
        return service.getAllOrders().stream()
                .mapToInt(Order::getId)
                .max()
                .orElse(0) + 1;
    }

    private int getNextRetetaId() {
        return service.getAllRetete().stream()
                .mapToInt(Reteta::getId)
                .max()
                .orElse(0) + 1;
    }

    // ---------- EXPORT + REVENUE ----------
    @FXML
    private void onExportOrdersCsv() {
        service.exportCsv("orders.csv");
    }

    @FXML
    private void onDailyRevenue() {
        lblTotalRevenue.setText("Daily Revenue: " + service.getDailyRevenue());
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
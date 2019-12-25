package Controller;

import Db.DBConnection;
import Model.*;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static jdk.nashorn.internal.parser.TokenType.WHILE;


/**
 * Created by Singhabahu-PC on 8/18/2019.
 */
public class PlaceOrderFormController implements Initializable {


    public TableColumn<OrderTM, String> colItemId;
    public TableColumn<OrderTM, String> colDescription;
    public TableColumn<OrderTM, Double> colQuantity;
    public TableColumn<OrderTM, Double> colUnitPrice;
    public TableColumn<OrderTM, Double> colTotal;
    public Label lblOrderCount;
    public Label lblCount;
    public Button btnAdd;
    public TableView<OrderTM> tblOrder;
    public Label lblOrderNo;
    public Label lblNetTotal;
    public TableColumn colDelete;
    public Button btnOrder;
    public AnchorPane placeOrderForm;
    public JFXTextField txtOrderNo;
    public JFXButton btnPrint;
    public JFXTextField txtCash;
    double netTotal = 0;


    @FXML
    private ComboBox<String> cmbId;
    @FXML
    private TextField txtDate;
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtAddress;

    @FXML
    private ComboBox<String> cmbItem;
    @FXML
    private TextField txtDescription;
    @FXML
    private TextField txtPrice;
    @FXML
    private TextField txtQtyOnHand;
    @FXML
    private TextField txtQty;

    Connection connection;
    PreparedStatement insertQuery;
    PreparedStatement selectQuery;
    PreparedStatement updateQuery;

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Date curDate = new Date();


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        btnAdd.setDisable(true);
        txtDate.setDisable(true);
        txtName.setDisable(true);
        txtAddress.setDisable(true);
        txtDescription.setDisable(true);
        txtPrice.setDisable(true);
        txtQtyOnHand.setDisable(true);
        btnOrder.setDisable(true);
        cmbItem.setDisable(true);
        cmbId.setDisable(true);

        connection = DBConnection.getInstance().getConnection();

        txtDate.setText(dateFormat.format(curDate));


        colItemId.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colDelete.setCellValueFactory(new PropertyValueFactory<>("delete"));


        // I created  temporary tables instead of temporary array list.

        try {

            loadingCustomerId();
            loadingItemId();
            lblNetTotal.setText("0.00");

            PreparedStatement createQuery1 = connection.prepareStatement("DROP TABLE IF EXISTS temp_item");
            createQuery1.executeUpdate();

            PreparedStatement createQuery2 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS temp_item AS SELECT * FROM item");
            createQuery2.executeUpdate();



            PreparedStatement selectQuery1 = connection.prepareStatement("SELECT table_name FROM information_schema.tables ");
            ResultSet resultSet = selectQuery1.executeQuery();


            while (resultSet.next()) {

                if (resultSet.getString(1).equals("temp_order_detail")) {

                    txtOrderNo.setText(generateOrderNo());
                    loadTable();
                    displayNetTotal();
                    cmbItem.setDisable(false);
                    cmbId.setDisable(false);
                    break;
                }
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        cmbItem.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String itemId = cmbItem.getSelectionModel().getSelectedItem();

                try {

                    selectQuery = connection.prepareStatement("SELECT * FROM temp_item  WHERE item_id=?");
                    selectQuery.setString(1, itemId);
                    ResultSet resultSet = selectQuery.executeQuery();

                    while (resultSet.next()) {
                        txtDescription.setText(resultSet.getString(2));
                        txtQtyOnHand.setText(resultSet.getDouble(3) + "");
                        txtPrice.setText(resultSet.getDouble(4) + "");
                    }
                    checkButtonAdd();
                    checkPlaceOrder();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        cmbId.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String customerId = cmbId.getSelectionModel().getSelectedItem();
                try {
                    selectQuery = connection.prepareStatement("SELECT * FROM customer WHERE customer_id=?");
                    selectQuery.setString(1, customerId);
                    ResultSet resultSet = selectQuery.executeQuery();
                    while (resultSet.next()) {
                        txtName.setText(resultSet.getString(2));
                        txtAddress.setText(resultSet.getString(3));
                    }
                    checkPlaceOrder();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        tblOrder.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<OrderTM>() {
            @Override
            public void changed(ObservableValue<? extends OrderTM> observable, OrderTM oldValue, OrderTM newValue) {

                OrderTM selectedItem = tblOrder.getSelectionModel().getSelectedItem();

                if (selectedItem == null) {
                    btnAdd.setText("Add");
                    return;
                } else {
                    cmbItem.setValue(selectedItem.getItemId());
                    txtQtyOnHand.setText((Double.parseDouble(txtQtyOnHand.getText()) + selectedItem.getQuantity()) + "");
                    btnAdd.setText("Update");
                    txtQty.requestFocus();
                    txtQty.selectAll();
                }

            }
        });
    }

    void loadingCustomerId() throws SQLException {
        selectQuery = connection.prepareStatement("SELECT customer_id FROM customer");
        ResultSet resultSet = selectQuery.executeQuery();

        ObservableList<String> items = cmbId.getItems();
        items.clear();
        while (resultSet.next()) {

            items.add(resultSet.getString(1));
        }
        cmbId.setItems(items);

    }

    void loadingItemId() throws SQLException {
        selectQuery = connection.prepareStatement("SELECT item_id FROM item");
        ResultSet resultSet = selectQuery.executeQuery();
        ObservableList<String> items = cmbItem.getItems();
        items.clear();
        while (resultSet.next()) {

            items.add(resultSet.getString(1));
        }
        cmbItem.setItems(items);
    }

    public void btnAdd_OnAction(ActionEvent actionEvent) throws SQLException {
        if (!txtQty.getText().matches("(\\d+[.]\\d+|(\\d+))")) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Quantity...", ButtonType.OK);
            alert.show();
            txtQty.requestFocus();
            txtQty.selectAll();
            return;
        } else if ((Double.parseDouble(txtQtyOnHand.getText()) < Double.parseDouble(txtQty.getText())) || Double.parseDouble(txtQty.getText()) <= 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Quantity is not Available", ButtonType.OK);
            alert.show();
            txtQty.requestFocus();
            txtQty.selectAll();
            return;
        }

        String orderId = txtOrderNo.getText();
        String item_id = cmbItem.getSelectionModel().getSelectedItem();
        Double qty = Double.parseDouble(txtQty.getText());
        Double price = Double.parseDouble(txtPrice.getText());
        boolean flag = false;


        if (btnAdd.getText().equals("Add")) {
            updateQuery = connection.prepareStatement("UPDATE temp_item SET item_qty=item_qty-? WHERE item_id=?");
            updateQuery.setDouble(1, qty);
            updateQuery.setString(2, item_id);
            updateQuery.executeUpdate();
        } else {
            updateQuery = connection.prepareStatement("UPDATE temp_item SET item_qty=?-? WHERE item_id=?");
            updateQuery.setDouble(1, Double.parseDouble(txtQtyOnHand.getText()));
            updateQuery.setDouble(2, qty);
            updateQuery.setString(3, item_id);
            updateQuery.executeUpdate();
            flag = true;
        }

        if (flag == true) {
            updateQuery = connection.prepareStatement("UPDATE temp_order_detail SET item_qty=? WHERE item_id=?");
            updateQuery.setDouble(1, qty);
            updateQuery.setString(2, item_id);
            updateQuery.executeUpdate();
        } else {
            insertQuery = connection.prepareStatement("INSERT INTO temp_order_detail VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE item_qty = item_qty+?");
            insertQuery.setString(1, orderId);
            insertQuery.setString(2, item_id);
            insertQuery.setDouble(3, qty);
            insertQuery.setDouble(4, price);
            insertQuery.setDouble(5, qty);
            insertQuery.executeUpdate();
        }
        loadTable();
        displayNetTotal();
        checkPlaceOrder();
        cmbItem.getSelectionModel().clearSelection();
        txtQty.clear();
        txtDescription.clear();
        txtPrice.clear();
        txtQtyOnHand.clear();
        txtQty.requestFocus();
        txtQty.selectAll();

    }

    public void loadTable() throws SQLException {

        selectQuery = connection.prepareStatement("SELECT tod.item_id,tod.item_price,tod.item_qty,item_des FROM temp_order_detail tod JOIN item ON tod.item_id = item.item_id WHERE order_id=?");
        selectQuery.setString(1, txtOrderNo.getText());
        ResultSet resultSet = selectQuery.executeQuery();

        ObservableList<OrderTM> items = tblOrder.getItems();
        items.clear();

        while (resultSet.next()) {
            String itemId = resultSet.getString(1);
            Double qty = resultSet.getDouble(3);
            Double price = resultSet.getDouble(2);
            String des = resultSet.getString(4);

            Button btn = new Button("Delete");
            btn.setId(itemId);
            btn.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, null, null)));
            btn.setOnAction(event ->
            {
                try {
                    updateQuery = connection.prepareStatement("UPDATE temp_item SET item_qty=item_qty+? WHERE item_id=?");
                    updateQuery.setDouble(1, qty);
                    updateQuery.setString(2, btn.getId());
                    updateQuery.executeUpdate();

                    PreparedStatement deleteQuery = connection.prepareStatement("DELETE FROM temp_order_detail WHERE order_id=? AND item_id=?");
                    deleteQuery.setString(1, txtOrderNo.getText());
                    deleteQuery.setString(2, btn.getId());
                    deleteQuery.executeUpdate();

                    loadTable();
                    loadingItemId();
                    displayNetTotal();
                } catch (SQLException e) {
                    e.printStackTrace();
                }


            });
            items.add(new OrderTM(itemId, des, qty, price, qty * price, btn));
        }


    }

    public void btnPlaceOrder_OnAction(ActionEvent actionEvent) {

        if (!txtCash.getText().matches("(\\d+[.]\\d+|(\\d+))")) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Amount...", ButtonType.OK);
            alert.show();
            txtCash.requestFocus();
            txtCash.selectAll();
            return;
        } else if (Double.parseDouble(txtCash.getText()) < Double.parseDouble(lblNetTotal.getText().replace(",",""))) {

            Alert alert = new Alert(Alert.AlertType.ERROR, "Cash amount is not enough... ", ButtonType.OK);
            alert.show();
            txtCash.requestFocus();
            txtCash.selectAll();
            return;
        }

        try {

            connection.setAutoCommit(false);

            insertQuery = connection.prepareStatement("INSERT INTO customer_order VALUES(?,?,?)");
            insertQuery.setString(1, txtOrderNo.getText());
            insertQuery.setString(2, cmbId.getSelectionModel().getSelectedItem());
            insertQuery.setString(3, txtDate.getText());
            int i = insertQuery.executeUpdate();

            if (i == 0) {
                connection.rollback();
                throw new RuntimeException("Something, something went wrong");
            }


            PreparedStatement insertQuery2 = connection.prepareStatement("INSERT  INTO order_detail SELECT * FROM temp_order_detail ");
            i = insertQuery2.executeUpdate();

            if (i == 0) {
                connection.rollback();
                throw new RuntimeException("Run time Exception...");
            }

            PreparedStatement insertQuery3 = connection.prepareStatement("INSERT INTO item SELECT * FROM temp_item ON DUPLICATE KEY UPDATE item.item_qty = temp_item.item_qty");
            i = insertQuery3.executeUpdate();
            if (i == 0) {
                connection.rollback();
                throw new RuntimeException("Run time Exception...");
            }

            PreparedStatement createQuery1 = connection.prepareStatement("DROP TABLE IF EXISTS temp_item,temp_order_detail");
            createQuery1.executeUpdate();
            if (i == 0) {
                connection.rollback();
                throw new RuntimeException("Run time Exception...");
            }
            connection.commit();

            printBill();

            cmbItem.getSelectionModel().clearSelection();
            cmbId.getSelectionModel().clearSelection();
            txtQty.clear();
            txtQtyOnHand.clear();
            txtPrice.clear();
            lblNetTotal.setText("0.00");
            txtDescription.clear();
            txtName.clear();
            txtOrderNo.clear();
            txtAddress.clear();
            tblOrder.getItems().clear();
            txtCash.clear();

        } catch (Throwable e) {

            try {
                connection.rollback();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void displayNetTotal() throws SQLException {

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);

        double total = 0.00;

        selectQuery = connection.prepareStatement("SELECT SUM(item_qty * item_price) FROM temp_order_detail");
        ResultSet resultSet = selectQuery.executeQuery();
        if (resultSet.next()) {
            total = resultSet.getDouble(1);
        }
        lblNetTotal.setText(nf.format(total));
    }


    public void txtQty_OnAction(ActionEvent actionEvent) throws SQLException {

        btnAdd_OnAction(actionEvent);
    }


    public void preview(String orderId) throws SQLException {

        cmbItem.setDisable(true);
        txtPrice.setDisable(true);
        txtQtyOnHand.setDisable(true);
        txtQty.setDisable(true);

        cmbId.setDisable(true);
        txtAddress.setDisable(true);
        txtDate.setDisable(true);
        txtName.setDisable(true);
        txtOrderNo.setDisable(true);
        txtOrderNo.setText(orderId);

        btnAdd.setDisable(true);
        btnOrder.setDisable(true);
        tblOrder.setDisable(true);


        selectQuery = connection.prepareStatement("SELECT customer_id,order_date FROM customer_order where order_id=?");
        selectQuery.setString(1, orderId);
        ResultSet resultSet1 = selectQuery.executeQuery();

        String cid = "";
        if (resultSet1.next()) {
            cid = resultSet1.getString(1);
            txtDate.setText(resultSet1.getString(2));
        }
        cmbId.setValue(cid);
        selectQuery = connection.prepareStatement("SELECT i.item_id,i.item_des,order_detail.item_qty,order_detail.item_price FROM order_detail LEFT JOIN item i on order_detail.item_id = i.item_id WHERE order_id=?");
        selectQuery.setString(1, orderId);
        ResultSet resultSet = selectQuery.executeQuery();

        ObservableList<OrderTM> items = tblOrder.getItems();
        items.clear();

        double total = 0.00;


        while (resultSet.next()) {
            String item_id = resultSet.getString(1);
            String item_des = resultSet.getString(2);
            double qty = resultSet.getDouble(3);
            double price = resultSet.getDouble(4);
            total = total + (qty * price);
            Button btn = new Button("Delete");
            items.add(new OrderTM(item_id, item_des, qty, price, qty * price, btn));


        }
        lblNetTotal.setText(total + "");
        lblNetTotal.setDisable(true);


    }

    private void checkPlaceOrder() {
        if (tblOrder.getItems().size() > 0 && (cmbId.getSelectionModel().getSelectedItem() != null)) {
            btnOrder.setDisable(false);
        }

    }

    private void checkButtonAdd() {
        if (cmbItem.getSelectionModel().getSelectedIndex() >= 0)
            btnAdd.setDisable(false);
    }


    public String generateOrderNo() throws SQLException {

        int maxId = 0;
        PreparedStatement getMaxIdQuery = connection.prepareStatement("SELECT order_id FROM customer_order ORDER BY order_id DESC LIMIT 1");
        ResultSet resultSet = getMaxIdQuery.executeQuery();
        if (resultSet.next()) {
            String maxIdString = resultSet.getString(1);
            int num = Integer.parseInt(maxIdString.replace("OD", ""));
            if (num > maxId) {
                maxId = num;
            }
        }
        maxId = maxId + 1;
        String id = "";
        if (maxId < 10) {
            id = "OD00" + maxId;
        } else if (maxId < 100) {
            id = "OD0" + maxId;
        } else {
            id = "OD" + maxId;
        }
        return id;

    }

    public void imgHome_OnMouseClicked(MouseEvent mouseEvent) throws IOException {
        URL resource = this.getClass().getResource("/View/DashBoardForm.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);

        Stage primaryStage = (Stage) this.placeOrderForm.getScene().getWindow();
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public void imgAdd_OnMouseClicked(MouseEvent mouseEvent) throws SQLException {

        PreparedStatement dropQuery1 = connection.prepareStatement("DROP TABLE IF EXISTS temp_item");
        dropQuery1.executeUpdate();

        PreparedStatement createQuery2 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS temp_item AS SELECT * FROM item");
        createQuery2.executeUpdate();

        PreparedStatement dropQuery2 = connection.prepareStatement("DROP TABLE IF EXISTS temp_order_detail");
        dropQuery2.executeUpdate();
        PreparedStatement createQuery3 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS temp_order_detail(order_id VARCHAR(5),item_id VARCHAR(4) ,item_qty DECIMAL(10,2),item_price DECIMAL(10,2),CONSTRAINT PRIMARY KEY(order_id,item_id))");
        createQuery3.executeUpdate();
        cmbId.getSelectionModel().clearSelection();
        cmbItem.getSelectionModel().clearSelection();
        cmbId.setDisable(false);
        cmbItem.setDisable(false);
        tblOrder.setDisable(false);
        txtDate.setText(dateFormat.format(curDate));
        txtOrderNo.setText(generateOrderNo());
        txtName.clear();
        lblNetTotal.setText("0.00");
        txtQty.setDisable(false);
        txtAddress.clear();
        tblOrder.getItems().clear();
    }


    public void printBill() throws JRException {
        JasperReport mainJasperReport = (JasperReport) JRLoader.loadObject(this.getClass().getResourceAsStream("/Report/pos_bill_main.jasper"));
        Map<String, Object> params = new HashMap<>();
        BigDecimal cash = new BigDecimal(txtCash.getText());
        params.put("orderNo", txtOrderNo.getText());
        params.put("cash", cash);
        JasperPrint jasperPrint = JasperFillManager.fillReport(mainJasperReport, params, DBConnection.getInstance().getConnection());
        JasperViewer.viewReport(jasperPrint, false);
    }
}

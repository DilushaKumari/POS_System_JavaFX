package Controller;

import Db.DBConnection;
import Model.CustomerTM;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;

import javax.naming.ldap.PagedResultsControl;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * Created by Singhabahu-PC on 9/11/2019.
 */
public class CustomerFormController {
    public Button btnSave;
    public AnchorPane customerForm;
    public Button btnDelete;
    public JFXTextField txtCustomerId;
    public JFXTextField txtCustomerAddress;
    public JFXTextField txtCustomerName;
    public JFXTextField txtSearch;
    public TableView<CustomerTM> tblCustomer;
    public JFXButton btnPrint;

    Connection connection;
    PreparedStatement selectQuery;
    PreparedStatement insertQuery;
    PreparedStatement updateQuery;

    public void initialize() {

        tblCustomer.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("code"));
        tblCustomer.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblCustomer.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("address"));

        txtCustomerId.setDisable(true);
        btnSave.setDisable(true);
        btnDelete.setDisable(true);
        Platform.runLater(() ->
        {
            txtSearch.requestFocus();

        });

        connection = DBConnection.getInstance().getConnection();
        try {
            insertQuery = connection.prepareStatement("INSERT INTO customer VALUES(?,?,?)");
            selectQuery = connection.prepareStatement("SELECT * FROM customer WHERE customer_id LIKE ? OR customer_name LIKE ? OR customer_address LIKE ?");
            updateQuery = connection.prepareStatement("UPDATE customer SET customer_name=?,customer_address=? WHERE customer_id=?");
            loadCustomerDetail();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        txtSearch.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    loadCustomerDetail();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        tblCustomer.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<CustomerTM>() {
            @Override
            public void changed(ObservableValue<? extends CustomerTM> observable, CustomerTM oldValue, CustomerTM newValue) {

                CustomerTM selectedCustomer = tblCustomer.getSelectionModel().getSelectedItem();

                if (selectedCustomer == null)
                    return;

                txtCustomerId.setText(selectedCustomer.getCode());
                txtCustomerName.setText(selectedCustomer.getName());
                txtCustomerAddress.setText(selectedCustomer.getAddress());
                btnSave.setText("Update");
                btnSave.setDisable(false);
                btnDelete.setDisable(false);

            }
        });


    }

    public void imgHome_OnMouseClicked(MouseEvent mouseEvent) throws IOException {
        URL resource = this.getClass().getResource("/View/DashBoardForm.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);

        Stage primaryStage = (Stage) this.customerForm.getScene().getWindow();
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public void imgAddUser_OnMouseClicked(MouseEvent mouseEvent) throws SQLException {
        btnSave.setDisable(false);
        txtCustomerId.setText(generateCustomerId());
    }

    public void btnSave_OnAction(ActionEvent actionEvent) throws SQLException {

        if (!txtCustomerName.getText().matches("^[A-Za-z][A-Za-z. ]+$")) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Member Name");
            alert.show();
            txtCustomerName.requestFocus();
            txtCustomerName.selectAll();
            return;
        } else if (!txtCustomerAddress.getText().matches("^\\w[ A-Za-z,.-/0-9\\\\]+$")) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Address");
            alert.show();
            txtCustomerAddress.requestFocus();
            txtCustomerAddress.selectAll();
            return;
        }

        if (btnSave.getText().equals("Save")) {
            insertQuery.setString(1, txtCustomerId.getText());
            insertQuery.setString(2, txtCustomerName.getText());
            insertQuery.setString(3, txtCustomerAddress.getText());
            insertQuery.executeUpdate();
        } else {
            updateQuery.setString(1, txtCustomerName.getText());
            updateQuery.setString(2, txtCustomerAddress.getText());
            updateQuery.setString(3, txtCustomerId.getText());
            updateQuery.executeUpdate();
        }

        loadCustomerDetail();
        btnSave.setDisable(true);
        btnDelete.setDisable(true);
        txtCustomerId.clear();
        txtCustomerName.clear();
        txtCustomerAddress.clear();
        txtSearch.requestFocus();
    }

    public void btnDelete_OnAction(ActionEvent event) throws SQLException {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure whether you want to delete this customer?",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> buttonType = alert.showAndWait();

        if (buttonType.get() == ButtonType.YES) {
            try {
                PreparedStatement deleteQuery = connection.prepareStatement("DELETE FROM customer WHERE customer_id=?");
                deleteQuery.setString(1, txtCustomerId.getText());
                deleteQuery.executeUpdate();
                loadCustomerDetail();

            } catch (Exception e) {
                Alert alert2 = new Alert(Alert.AlertType.CONFIRMATION,
                        "Cannot Delete this customer because this customer has placed an order...");
                alert2.show();
            }
            txtCustomerId.clear();
            txtCustomerAddress.clear();
            txtSearch.clear();
            txtCustomerName.clear();
            btnSave.setText("Save");
            btnSave.setDisable(true);
            btnDelete.setDisable(true);
        }

    }

    public String generateCustomerId() throws SQLException {

        int maxId = 0;
        PreparedStatement getMaxIdQuery = connection.prepareStatement("SELECT customer_id FROM customer ORDER BY customer_id DESC LIMIT 1");
        ResultSet resultSet = getMaxIdQuery.executeQuery();
        if (resultSet.next()) {
            String maxIdString = resultSet.getString(1);
            int num = Integer.parseInt(maxIdString.replace("C", ""));
            if (num > maxId) {
                maxId = num;
            }
        }
        maxId = maxId + 1;
        String id = "";
        if (maxId < 10) {
            id = "C00" + maxId;
        } else if (maxId < 100) {
            id = "C0" + maxId;
        } else {
            id = "C" + maxId;
        }
        return id;

    }

    public void loadCustomerDetail() throws SQLException {

        tblCustomer.getItems().clear();
        selectQuery.setString(1, "%" + txtSearch.getText() + "%");
        selectQuery.setString(2, "%" + txtSearch.getText() + "%");
        selectQuery.setString(3, "%" + txtSearch.getText() + "%");
        ResultSet rst = selectQuery.executeQuery();

        ObservableList<CustomerTM> customers = tblCustomer.getItems();

        while (rst.next()) {
            String id = rst.getString(1);
            String name = rst.getString(2);
            String address = rst.getString(3);

            CustomerTM customer = new CustomerTM(id, name, address);
            customers.add(customer);
        }
    }

    public void btnPrint_OnAction(ActionEvent actionEvent) throws JRException {
        JasperDesign jasperDesign = JRXmlLoader.load(this.getClass().getResourceAsStream("/Report/customer.jrxml"));
        JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
        Map<String, Object> parms = new HashMap<>();

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parms, new JRBeanCollectionDataSource(tblCustomer.getItems()));
        JasperViewer.viewReport(jasperPrint, false);
    }
}

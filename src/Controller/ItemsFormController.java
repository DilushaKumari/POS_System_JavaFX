package Controller;

import Db.DBConnection;
import Model.ItemTM;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by Singhabahu-PC on 8/19/2019.
 */
public class ItemsFormController implements Initializable {

    public Button btnSave;
    public TextField txtItemCode;
    public TextField txtDescription;
    public TextField txtQty;
    public Button btnDelete;
    public AnchorPane itemForm;
    public TableView<ItemTM> tblItem;
    public TextField txtUnitPrice;
    public JFXTextField txtSearch;
    public JFXButton btnPrint;


    Connection connection;
    PreparedStatement selectQuery;
    PreparedStatement insertQuery;
    PreparedStatement updateQuery;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tblItem.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("code"));
        tblItem.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("description"));
        tblItem.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("qty"));
        tblItem.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("price"));

        txtItemCode.setDisable(true);
        btnSave.setDisable(true);
        btnDelete.setDisable(true);

        Platform.runLater(() ->
        {
            txtSearch.requestFocus();

        });

        txtSearch.requestFocus();

        connection = DBConnection.getInstance().getConnection();
        try {
            insertQuery = connection.prepareStatement("INSERT INTO item VALUES(?,?,?,?)");
            selectQuery = connection.prepareStatement("SELECT * FROM item WHERE item_id LIKE ? OR item_des LIKE ?");
            updateQuery = connection.prepareStatement("UPDATE item SET item_des=?,item_qty=?,item_price=? WHERE item_id=?");
            loadItemDetail();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tblItem.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ItemTM>() {
            @Override
            public void changed(ObservableValue<? extends ItemTM> observable, ItemTM oldValue, ItemTM newValue) {
                ItemTM selectedItem = tblItem.getSelectionModel().getSelectedItem();

                if (selectedItem == null) {
                    btnSave.setText("Save");
                    btnDelete.setDisable(true);
                    return;
                }
                txtItemCode.setText(selectedItem.getCode());
                txtDescription.setText(selectedItem.getDescription());
                txtQty.setText(Double.toString(selectedItem.getQty()));
                txtUnitPrice.setText(Double.toString(selectedItem.getPrice()));
                btnSave.setText("Update");
                btnDelete.setDisable(false);
                btnSave.setDisable(false);


            }
        });

        txtSearch.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    loadItemDetail();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @FXML
    private void btnSave_OnAction(ActionEvent event) throws SQLException {
        if (btnSave.getText().equals("Save")) {
            insertQuery.setString(1, txtItemCode.getText());
            insertQuery.setString(2, txtDescription.getText());
            insertQuery.setDouble(3, Double.parseDouble(txtQty.getText()));
            insertQuery.setDouble(4, Double.parseDouble(txtUnitPrice.getText()));
            insertQuery.executeUpdate();

        } else if (btnSave.getText().equals("Update")) {
            updateQuery.setString(1, txtDescription.getText());
            updateQuery.setDouble(2, Double.parseDouble(txtQty.getText()));
            updateQuery.setDouble(3, Double.parseDouble(txtUnitPrice.getText()));
            updateQuery.setString(4, txtItemCode.getText());
            updateQuery.executeUpdate();
        }

        loadItemDetail();
        btnSave.setDisable(true);
        txtQty.clear();
        txtDescription.clear();
        txtItemCode.clear();
        txtUnitPrice.clear();
        txtSearch.requestFocus();

    }

    @FXML
    private void btnDelete_OnAction(ActionEvent event) throws SQLException {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure whether you want to delete this item?",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> buttonType = alert.showAndWait();

        if (buttonType.get() == ButtonType.YES) {
            try {
                PreparedStatement deleteQuery = connection.prepareStatement("DELETE FROM item WHERE item_id=?");
                deleteQuery.setString(1, txtItemCode.getText());
                deleteQuery.executeUpdate();
                loadItemDetail();
            } catch (Exception e) {
                Alert alert2 = new Alert(Alert.AlertType.CONFIRMATION,
                        "Cannot Delete this item because this item has purchased...");
                alert2.show();
            }
            txtQty.clear();
            txtUnitPrice.clear();
            txtItemCode.clear();
            txtDescription.clear();
            btnSave.setText("Save");
            btnSave.setDisable(true);
            btnDelete.setDisable(true);
        }
    }

    public void imgHome_OnMouseClicked(MouseEvent mouseEvent) throws IOException {
        URL resource = this.getClass().getResource("/View/DashBoardForm.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);

        Stage primaryStage = (Stage) this.itemForm.getScene().getWindow();
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }


    public String generateItemId() throws SQLException {

        int maxId = 0;
        PreparedStatement getMaxIdQuery = connection.prepareStatement("SELECT item_id FROM item ORDER BY item_id DESC LIMIT 1");
        ResultSet resultSet = getMaxIdQuery.executeQuery();
        if (resultSet.next()) {
            String maxIdString = resultSet.getString(1);
            int num = Integer.parseInt(maxIdString.replace("I", ""));
            if (num > maxId) {
                maxId = num;
            }
        }
        maxId = maxId + 1;
        String id = "";
        if (maxId < 10) {
            id = "I00" + maxId;
        } else if (maxId < 100) {
            id = "I0" + maxId;
        } else {
            id = "I" + maxId;
        }
        return id;

    }

    public void imgAddItem_OnMouseClicked(MouseEvent mouseEvent) throws SQLException {
        btnSave.setDisable(false);
        txtItemCode.setText(generateItemId());
    }

    public void loadItemDetail() throws SQLException {

        tblItem.getItems().clear();
        selectQuery.setString(1, "%" + txtSearch.getText() + "%");
        selectQuery.setString(2, "%" + txtSearch.getText() + "%");
        ResultSet rst = selectQuery.executeQuery();

        ObservableList<ItemTM> items = tblItem.getItems();

        while (rst.next()) {
            String code = rst.getString(1);
            String description = rst.getString(2);
            double qty = rst.getDouble(3);
            double price = rst.getDouble(4);

            ItemTM item = new ItemTM(code, description, qty, price);
            items.add(item);
        }
    }

    public void btnPrint_OnAction(ActionEvent actionEvent) throws JRException {
        JasperDesign jasperDesign = JRXmlLoader.load(this.getClass().getResourceAsStream("/Report/item.jrxml"));
        JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
        Map<String, Object> parms = new HashMap<>();
        parms.put("word",txtSearch.getText());

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parms, DBConnection.getInstance().getConnection());
        JasperViewer.viewReport(jasperPrint, false);
    }
}

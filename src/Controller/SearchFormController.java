package Controller;

import Db.DBConnection;
import Model.SearchTM;
import com.jfoenix.controls.JFXButton;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by Singhabahu-PC on 8/21/2019.
 */
public class SearchFormController implements Initializable {
    public AnchorPane searchForm;
    public JFXButton btnPrint;
    @FXML
    private TextField txtSearch;

    @FXML
    private TableView<SearchTM> viewTbl;


    Connection connection;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        viewTbl.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("orderId"));
        viewTbl.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("date"));
        viewTbl.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("total"));
        viewTbl.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("customerId"));
        viewTbl.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("customerName"));

        connection = DBConnection.getInstance().getConnection();

        ordersLoading();

        txtSearch.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                ordersLoading();
            }
        });


    }

    private void ordersLoading() {
        ObservableList<SearchTM> tblData = viewTbl.getItems();
        tblData.clear();

        try {
            PreparedStatement selectQuery = connection.prepareStatement("SELECT cod.order_id,cod.order_date,SUM(o.item_price*o.item_qty) AS total,cod.customer_id,customer.customer_name" +
                    " FROM customer_order cod left join order_detail o on cod.order_id = o.order_id left join customer ON cod.customer_id = customer.customer_id " +
                    "WHERE cod.order_id LIKE ? OR customer.customer_name LIKE ? OR customer.customer_id LIKE ?" +
                    " GROUP BY  o.order_id");

            selectQuery.setString(1, "%" + txtSearch.getText() + "%");
            selectQuery.setString(2, "%" + txtSearch.getText() + "%");
            selectQuery.setString(3, "%" + txtSearch.getText() + "%");
            ResultSet resultSet = selectQuery.executeQuery();

            while (resultSet.next()) {
                String orderId = resultSet.getString(1);
                String date = resultSet.getString(2);
                Double total = resultSet.getDouble(3);
                String customerId = resultSet.getString(4);
                String name = resultSet.getString(5);
                tblData.add(new SearchTM(orderId, date, total, customerId, name));

            }
            viewTbl.setItems(tblData);


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    public void viewtbl_OnMouseClicked(MouseEvent mouseEvent) throws IOException, SQLException {
        if (mouseEvent.getClickCount() == 2) {
            SearchTM selectedItem = viewTbl.getSelectionModel().getSelectedItem();
            if (selectedItem == null)
                return;

            URL resource2 = this.getClass().getResource("/View/PlaceOrderForm.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(resource2);
            Parent root = fxmlLoader.load();
            PlaceOrderFormController controller = fxmlLoader.getController();
            controller.preview(selectedItem.getOrderId());
            Stage primaryStage = (Stage) this.searchForm.getScene().getWindow();
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Order Details");
            primaryStage.show();
            primaryStage.centerOnScreen();
        }

    }

    public void imgHome_OnMouseClicked(MouseEvent mouseEvent) throws IOException {
        URL resource = this.getClass().getResource("/View/DashBoardForm.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);

        Stage primaryStage = (Stage) this.searchForm.getScene().getWindow();
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public void btnPrint_OnAction(ActionEvent actionEvent) throws JRException {
        JasperReport mainJasperReport = (JasperReport) JRLoader.loadObject(this.getClass().getResourceAsStream("/Report/pos_search.jasper"));
        Map<String, Object> params = new HashMap<>();
        params.put("word", txtSearch.getText());
        JasperPrint jasperPrint = JasperFillManager.fillReport(mainJasperReport, params, DBConnection.getInstance().getConnection());
        JasperViewer.viewReport(jasperPrint, false);
    }

}

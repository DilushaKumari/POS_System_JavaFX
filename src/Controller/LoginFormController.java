package Controller;

import Db.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class LoginFormController {
    public TextField txtUserName;
    public TextField txtPassword;
    public Button btnLogin;

    public void btnLogin_OnAction(ActionEvent actionEvent) throws ClassNotFoundException {

        try {
            Connection connection = DBConnection.getInstance().getConnection();
            PreparedStatement query = connection.prepareStatement("SELECT * FROM user WHERE user_id=? AND password=?");

            query.setString(1, txtUserName.getText());
            query.setString(2, txtPassword.getText());
            ResultSet resultSet = query.executeQuery();

            if (resultSet.next()) {
                Stage primaryStage = (Stage) txtUserName.getScene().getWindow();
                Parent root = FXMLLoader.load(this.getClass().getResource("/View/DashBoardForm.fxml"));
                Scene scene = new Scene(root);
                primaryStage.setScene(scene);
                primaryStage.centerOnScreen();
                primaryStage.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid User Name or Password", ButtonType.OK);
                alert.show();
                txtPassword.clear();
                txtUserName.requestFocus();
                txtUserName.selectAll();

            }

        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
    }

    public void txtPassword_OnAction(ActionEvent actionEvent) throws ClassNotFoundException {
        btnLogin_OnAction(actionEvent);
    }

}

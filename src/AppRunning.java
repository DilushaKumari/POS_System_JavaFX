/**
 * Created by Singhabahu-PC on 8/19/2019.
 */

import Db.DBConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

public class AppRunning extends Application
{

    public static void main(String[] args) throws SQLException {

        launch(args);
        DBConnection.getInstance().getConnection().close();
    }

    @Override
    public void start(Stage primaryStage) throws IOException
    {

        URL resource = this.getClass().getResource("/View/LoginForm.fxml");
        Parent root = FXMLLoader.load(resource);

        Scene scene =new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("POS System");
        primaryStage.show();
    }
}

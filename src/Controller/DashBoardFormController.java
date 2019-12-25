package Controller;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Singhabahu-PC on 8/19/2019.
 */

public class DashBoardFormController  implements Initializable {
    public AnchorPane mainWindow;
    public ImageView imgCustomer;
    public Label lblCustomer;
    public ImageView imgItem;
    public Label lblItem;
    public ImageView imgPlaceOrder;
    public Label lblPlaceOrder;
    public ImageView imgSearchOrder;
    public Label lblSearchOrder;
    public ImageView imgExit;
    public Label lblExit;


    public void initialize(URL url, ResourceBundle rb) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(2000), mainWindow);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();


    }

    public void playExitAction(MouseEvent mouseEvent) {
        if (mouseEvent.getSource() instanceof ImageView) {
            ImageView icon = (ImageView) mouseEvent.getSource();
            ScaleTransition scaleT = new ScaleTransition(Duration.millis(200), icon);
            scaleT.setToX(1);
            scaleT.setToY(1);
            scaleT.play();
        }
    }

    public void playEnterAction(MouseEvent mouseEvent) {
        if (mouseEvent.getSource() instanceof ImageView) {
            ImageView icon = (ImageView) mouseEvent.getSource();
            ScaleTransition scaleT = new ScaleTransition(Duration.millis(200), icon);
            scaleT.setToX(1.2);
            scaleT.setToY(1.2);
            scaleT.play();


            DropShadow glow = new DropShadow();
            glow.setColor(Color.CORNFLOWERBLUE);
            glow.setWidth(15);
            glow.setHeight(15);
            glow.setRadius(15);
            icon.setEffect(glow);
        }
    }

    public void navigate(MouseEvent event) throws IOException {
        if (event.getSource() instanceof ImageView) {
            ImageView icon = (ImageView) event.getSource();

            Parent root = null;

            switch (icon.getId()) {
                case "imgCustomer":
                    root = FXMLLoader.load(this.getClass().getResource("/View/CustomerForm.fxml"));
                    break;
                case "imgItem":
                    root = FXMLLoader.load(this.getClass().getResource("/View/ItemForm.fxml"));
                    break;
                case "imgPlaceOrder":
                    root = FXMLLoader.load(this.getClass().getResource("/View/PlaceOrderForm.fxml"));
                    break;
                case "imgSearchOrder":
                    root = FXMLLoader.load(this.getClass().getResource("/View/SearchForm.fxml"));
                    break;

                case "imgExit":
                    root = FXMLLoader.load(this.getClass().getResource("/View/LoginForm.fxml"));
                    break;
            }
            if (root != null) {
                Scene subScene = new Scene(root);
                Stage primaryStage = (Stage) this.mainWindow.getScene().getWindow();
                primaryStage.setScene(subScene);
                primaryStage.centerOnScreen();

                TranslateTransition tt = new TranslateTransition(Duration.millis(350), subScene.getRoot());
                tt.setFromX(-subScene.getWidth());
                tt.setToX(0);
                tt.play();

            }
        }


    }

    public void imgCustomer_OnMouseMoved(MouseEvent mouseEvent) {
        lblCustomer.setText("Manage \n Customer");
        lblItem.setText("");
        lblPlaceOrder.setText("");
        lblSearchOrder.setText("");
        lblExit.setText("");
    }

    public void imgItem_OnMouseMoved(MouseEvent mouseEvent) {
        lblItem.setText("Manage \n Item");
        lblCustomer.setText("");
        lblPlaceOrder.setText("");
        lblSearchOrder.setText("");
        lblExit.setText("");
    }

    public void imgPlaceOrder_OnMouseMoved(MouseEvent mouseEvent) {
        lblPlaceOrder.setText("Place\nOrder");
        lblCustomer.setText("");
        lblItem.setText("");
        lblSearchOrder.setText("");
        lblExit.setText("");
    }

    public void imgSearchOrder_OnMouseMoved(MouseEvent mouseEvent) {
        lblSearchOrder.setText("Search\nOrder");
        lblCustomer.setText("");
        lblItem.setText("");
        lblPlaceOrder.setText("");
        lblExit.setText("");
    }

    public void imgExit_OnMouseMoved(MouseEvent mouseEvent) {
        lblCustomer.setText("");
        lblItem.setText("");
        lblPlaceOrder.setText("");
        lblSearchOrder.setText("");
        lblExit.setText("Logout");

    }

}

package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

    Controller controller = new Controller();

    @Override
    public void start(Stage primaryStage) throws Exception{
        controller.init();
        Parent root = controller.getRoot();

        primaryStage.setTitle("Device Manager");
        primaryStage.setScene(new Scene(root, 900, 400));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

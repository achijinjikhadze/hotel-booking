package hotel;

import javafx.application.Application;
import javafx.stage.Stage;

public class main extends Application {

    @Override
    public void start(Stage primaryStage) {
        guestwindow.show(primaryStage); // This opens your guest panel
    }

    public static void main(String[] args) {
        launch(args); // Start JavaFX
    }
}

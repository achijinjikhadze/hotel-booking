package hotel;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class login extends Application {

    @Override
    public void start(Stage primaryStage) {
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button loginButton = new Button("Admin Login");
        Button guestButton = new Button("As Guest");

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (loginAsAdmin(username, password)) {
            	 adminwindow.show();
                primaryStage.close();
            } else {
                errorLabel.setText("Invalid admin credentials.");
            }
        });
        
        guestButton.setOnAction(e->{
        	guestwindow.show(primaryStage);
        	
        });
        
       
        VBox vbox = new VBox(10, usernameField, passwordField, new HBox(10, loginButton, guestButton), errorLabel);
        vbox.setPadding(new Insets(20));

        Scene scene2 = new Scene(vbox, 300, 200);
        scene2.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        primaryStage.setScene(scene2);
        primaryStage.setTitle("Admin Login");
        primaryStage.show();

       
    }

    private boolean loginAsAdmin(String username, String password) {
        try (Connection conn = dbcn.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND isadmin = 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            return rs.next(); 
           

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void openAdminPage() {
        Stage adminStage = new Stage();
        Label label = new Label("Welcome to the Admin Page!");
        VBox vbox = new VBox(20, label);
        vbox.setPadding(new Insets(20));
        adminStage.setScene(new Scene(vbox, 400, 300));
        adminStage.setTitle("Admin Panel");
        adminStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

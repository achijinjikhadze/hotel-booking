package hotel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;

public class adminwindow {

    public static void show() {
        Stage stage = new Stage();
        stage.setTitle("Hotel Admin Panel");

        Label title = new Label("Admin Dashboard");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            stage.close();
            guestwindow.show(stage);
        });

        HBox topBar = new HBox(title, new Region(), logoutButton);
        HBox.setHgrow(topBar.getChildren().get(1), Priority.ALWAYS);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox navigation = new VBox(10);
        navigation.setPadding(new Insets(10));
       // navigation.setStyle("-fx-background-color: #f0f0f0;");
        navigation.setPrefWidth(200);
        navigation.setId("navg");
        Button viewRoomsBtn = new Button("View Rooms");
        Button viewGuestsBtn = new Button("View Guests");
        Button viewPaymentsBtn = new Button("View Payments");
        Button viewRevenueBtn = new Button("View Revenue");
        for (Button btn : new Button[]{viewRoomsBtn, viewGuestsBtn, viewPaymentsBtn, viewRevenueBtn})
            btn.setMaxWidth(Double.MAX_VALUE);

        navigation.getChildren().addAll(viewRoomsBtn, viewGuestsBtn, viewPaymentsBtn, viewRevenueBtn);

        VBox contentArea = new VBox(10);
        contentArea.setPadding(new Insets(10));
       ;

        TableView<ObservableList<String>> table = new TableView<>();
        contentArea.getChildren().add(table);

        BorderPane layout = new BorderPane();
        layout.setTop(topBar);
        layout.setLeft(navigation);
        layout.setCenter(contentArea);

        Scene scene = new Scene(layout, 1000, 600);
        stage.setScene(scene);
        stage.show();

        scene.getStylesheets().add(
                guestwindow.class.getResource("style.css").toExternalForm()
        );
        
        // ---------- VIEW ROOMS ----------
        viewRoomsBtn.setOnAction(ev -> {
            contentArea.getChildren().clear();

            HBox filterBar = new HBox(10);
            filterBar.setAlignment(Pos.CENTER_LEFT);

            ComboBox<String> filterType = new ComboBox<>();
            filterType.getItems().addAll("All", "Available", "Booked");
            filterType.setValue("All");

            DatePicker fromDate = new DatePicker();
            DatePicker toDate = new DatePicker();
            fromDate.setPromptText("From");
            toDate.setPromptText("To");

            ComboBox<String> sortBy = new ComboBox<>();
            sortBy.getItems().addAll("Price", "Beds");
            sortBy.setValue("Price");

            Button searchBtn = new Button("Search");

            filterBar.getChildren().addAll(
                    new Label("Status:"), filterType,
                    new Label("From:"), fromDate,
                    new Label("To:"), toDate,
                    new Label("Sort by:"), sortBy,
                    searchBtn
            );

            contentArea.getChildren().addAll(filterBar, table);

            searchBtn.setOnAction(e -> {
                table.getColumns().clear();
                table.getItems().clear();
                try (Connection conn = dbcn.getConnection()) {
                    String query = """
                        SELECT r.roomid, r.roomnumber, r.roombed, r.roomprice,
                               CASE 
                                 WHEN EXISTS (
                                   SELECT 1 FROM bookings b 
                                   WHERE b.roomid = r.roomid AND 
                                         NOT (b.checkoutdate <= ? OR b.checkindate >= ?)
                                 ) THEN 'Booked' ELSE 'Available'
                               END AS status
                        FROM rooms r
                    """;

                    if (sortBy.getValue().equals("Price")) {
                        query += " ORDER BY r.roomprice";
                    } else {
                        query += " ORDER BY r.roombed";
                    }

                    PreparedStatement stmt = conn.prepareStatement(query);

                    LocalDate from = fromDate.getValue() != null ? fromDate.getValue() : LocalDate.of(2000, 1, 1);
                    LocalDate to = toDate.getValue() != null ? toDate.getValue() : LocalDate.of(2099, 12, 31);

                    stmt.setDate(1, Date.valueOf(from));
                    stmt.setDate(2, Date.valueOf(to));

                    ResultSet rs = stmt.executeQuery();
                    ResultSetMetaData meta = rs.getMetaData();

                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        final int colIndex = i - 1;
                        TableColumn<ObservableList<String>, String> col = new TableColumn<>(meta.getColumnName(i));
                        col.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(colIndex)));
                        table.getColumns().add(col);
                    }

                    while (rs.next()) {
                        String status = rs.getString("status");
                        if (!filterType.getValue().equals("All") && !status.equalsIgnoreCase(filterType.getValue())) continue;

                        ObservableList<String> row = FXCollections.observableArrayList();
                        for (int i = 1; i <= meta.getColumnCount(); i++) row.add(rs.getString(i));
                        table.getItems().add(row);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            searchBtn.fire(); 
        });

        // ---------- VIEW GUESTS ----------
        viewGuestsBtn.setOnAction(ev -> {
            table.getColumns().clear();
            table.getItems().clear();
            contentArea.getChildren().setAll(table);

            try (Connection conn = dbcn.getConnection()) {
                String query = "SELECT guestid, firstname, lastname, phone FROM guests";
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();

                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    final int colIndex = i - 1;
                    TableColumn<ObservableList<String>, String> col = new TableColumn<>(meta.getColumnName(i));
                    col.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(colIndex)));
                    table.getColumns().add(col);
                }

                while (rs.next()) {
                    ObservableList<String> row = FXCollections.observableArrayList();
                    for (int i = 1; i <= meta.getColumnCount(); i++) row.add(rs.getString(i));
                    table.getItems().add(row);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // ---------- VIEW PAYMENTS ----------
        viewPaymentsBtn.setOnAction(ev -> {
            table.getColumns().clear();
            table.getItems().clear();
            contentArea.getChildren().setAll(table);

            try (Connection conn = dbcn.getConnection()) {
                String query = """
                    SELECT p.payid, g.firstname + ' ' + g.lastname AS guest, p.amount, p.paydate
                    FROM payments p
                    JOIN guests g ON p.guestid = g.guestid
                    ORDER BY p.paydate DESC
                """;
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();

                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    final int colIndex = i - 1;
                    TableColumn<ObservableList<String>, String> col = new TableColumn<>(meta.getColumnName(i));
                    col.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(colIndex)));
                    table.getColumns().add(col);
                }

                while (rs.next()) {
                    ObservableList<String> row = FXCollections.observableArrayList();
                    for (int i = 1; i <= meta.getColumnCount(); i++) row.add(rs.getString(i));
                    table.getItems().add(row);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // ---------- VIEW REVENUE ----------
        viewRevenueBtn.setOnAction(ev -> {
            contentArea.getChildren().clear();

            HBox revBar = new HBox(10);
            revBar.setAlignment(Pos.CENTER_LEFT);

            DatePicker fromRev = new DatePicker();
            DatePicker toRev = new DatePicker();
            fromRev.setPromptText("From");
            toRev.setPromptText("To");

            Button calcBtn = new Button("Calculate Revenue");
            Label totalLabel = new Label("Total: $0.00");

            revBar.getChildren().addAll(new Label("From:"), fromRev, new Label("To:"), toRev, calcBtn, totalLabel);
            contentArea.getChildren().addAll(revBar, table);

            calcBtn.setOnAction(e -> {
                table.getColumns().clear();
                table.getItems().clear();

                try (Connection conn = dbcn.getConnection()) {
                    String query = """
                        SELECT p.payid, g.firstname + ' ' + g.lastname AS guest, p.amount, p.paydate
                        FROM payments p
                        JOIN guests g ON p.guestid = g.guestid
                        WHERE p.paydate BETWEEN ? AND ?
                    """;

                    LocalDate from = fromRev.getValue() != null ? fromRev.getValue() : LocalDate.of(2000, 1, 1);
                    LocalDate to = toRev.getValue() != null ? toRev.getValue() : LocalDate.of(2099, 12, 31);

                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setDate(1, Date.valueOf(from));
                    stmt.setDate(2, Date.valueOf(to));

                    ResultSet rs = stmt.executeQuery();
                    ResultSetMetaData meta = rs.getMetaData();

                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        final int colIndex = i - 1;
                        TableColumn<ObservableList<String>, String> col = new TableColumn<>(meta.getColumnName(i));
                        col.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(colIndex)));
                        table.getColumns().add(col);
                    }

                    double total = 0;

                    while (rs.next()) {
                        ObservableList<String> row = FXCollections.observableArrayList();
                        for (int i = 1; i <= meta.getColumnCount(); i++) row.add(rs.getString(i));
                        table.getItems().add(row);
                        total += rs.getDouble("amount");
                    }

                    totalLabel.setText("Total: $" + String.format("%.2f", total));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        });
    }
}

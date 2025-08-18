package hotel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;

public class guestwindow {

    private static final TableView<Room> tableView = new TableView<>();

    public static void show(Stage stage) {
        HBox topBar = new HBox();
        Button loginButton = new Button("Login");
        
        loginButton.setOnAction(e -> {
            // Login window
            login login = new login();
            try {
                login.start(new Stage());
                stage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_RIGHT);
        topBar.getChildren().add(loginButton);

        ComboBox<String> bedFilter = new ComboBox<>();
        bedFilter.getItems().addAll("All", "1", "2", "3");
        bedFilter.setValue("All");
       
        
        DatePicker checkinPicker = new DatePicker();
        checkinPicker.setPromptText("Check-in");
        checkinPicker.setStyle("-fx-text-fill: green;");

        DatePicker checkoutPicker = new DatePicker();
        checkoutPicker.setPromptText("Check-out");

        TextField minPriceField = new TextField();
        minPriceField.setPromptText("Min Price");

        TextField maxPriceField = new TextField();
        maxPriceField.setPromptText("Max Price");

        Button searchButton = new Button("Search");

        HBox filterBox = new HBox(10,
                new Label("Beds:"), bedFilter,
                new Label("Check-in:"), checkinPicker,
                new Label("Check-out:"), checkoutPicker,
                new Label("Min Price:"), minPriceField,
                new Label("Max Price:"), maxPriceField,
                searchButton);
        filterBox.setAlignment(Pos.CENTER);
        filterBox.setPadding(new Insets(10));

        // Setup table columns once (reuse on reload)
        setupTableColumns();

        // Initial load
        loadRooms("All", "", "", "", "");

        searchButton.setOnAction(e -> {
            String beds = bedFilter.getValue();
            String min = minPriceField.getText().trim();
            String max = maxPriceField.getText().trim();
            String checkin = checkinPicker.getValue() != null ? checkinPicker.getValue().toString() : "";
            String checkout = checkoutPicker.getValue() != null ? checkoutPicker.getValue().toString() : "";
            loadRooms(beds, min, max, checkin, checkout);
        });

        VBox mainLayout = new VBox(10, topBar, filterBox, tableView);
        mainLayout.setPadding(new Insets(10));

        Scene scene = new Scene(mainLayout, 1000, 600);

        // ✅ Load CSS file
        scene.getStylesheets().add(
                guestwindow.class.getResource("style.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setTitle("Hotel Guest View");
        stage.show();

        filterBox.setId("filterBox");
        mainLayout.setId("mainLayout");

        
    }
    

    private static void setupTableColumns() {
        tableView.getColumns().clear();

        TableColumn<Room, Integer> roomIdCol = new TableColumn<>("Room ID");
        roomIdCol.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        roomIdCol.setPrefWidth(80);

        TableColumn<Room, Integer> roomNumberCol = new TableColumn<>("Room Number");
        roomNumberCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        roomNumberCol.setPrefWidth(140);

        TableColumn<Room, Integer> bedsCol = new TableColumn<>("Beds");
        bedsCol.setCellValueFactory(new PropertyValueFactory<>("beds"));
        bedsCol.setPrefWidth(70);

        TableColumn<Room, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(80);

        TableColumn<Room, Void> bookCol = new TableColumn<>("Book");
        bookCol.setPrefWidth(100);
        bookCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Book");

            {
                btn.setOnAction(event -> {
                    Room room = getTableView().getItems().get(getIndex());
                    openBookingDialog(room);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tableView.getColumns().addAll(roomIdCol, roomNumberCol, bedsCol, priceCol, bookCol);
    }

    private static void loadRooms(String beds, String minPrice, String maxPrice, String checkinDate, String checkoutDate) {
        ObservableList<Room> data = FXCollections.observableArrayList();

        StringBuilder query = new StringBuilder(
                "SELECT roomid, roomnumber, roombed, roomprice FROM rooms WHERE 1=1"
        );
        ObservableList<Object> params = FXCollections.observableArrayList();

        if (!beds.equalsIgnoreCase("All")) {
            query.append(" AND roombed = ?");
            params.add(Integer.parseInt(beds));
        }

        if (!minPrice.isEmpty()) {
            try {
                query.append(" AND roomprice >= ?");
                params.add(Double.parseDouble(minPrice));
            } catch (NumberFormatException ignored) {}
        }

        if (!maxPrice.isEmpty()) {
            try {
                query.append(" AND roomprice <= ?");
                params.add(Double.parseDouble(maxPrice));
            } catch (NumberFormatException ignored) {}
        }

        if (!checkinDate.isEmpty() && !checkoutDate.isEmpty()) {
            query.append(" AND roomid NOT IN (" +
                    "SELECT roomid FROM bookings " +
                    "WHERE NOT (checkoutdate <= ? OR checkindate >= ?))");
            params.add(Date.valueOf(checkinDate));
            params.add(Date.valueOf(checkoutDate));
        }

        try (Connection conn = dbcn.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int roomId = rs.getInt("roomid");
                int roomNumber = rs.getInt("roomnumber");
                int bedsCount = rs.getInt("roombed");
                double price = rs.getDouble("roomprice");

                data.add(new Room(roomId, roomNumber, bedsCount, price));
            }

            tableView.setItems(data);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //dialog 
    private static void openBookingDialog(Room room) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Book Room " + room.getRoomNumber());

        Label lblName = new Label("First Name:");
        TextField tfName = new TextField();

        Label lblLastName = new Label("Last Name:");
        TextField tfLastName = new TextField();

        Label lblPhone = new Label("Phone:");
        TextField tfPhone = new TextField();

        DatePicker dpCheckIn = new DatePicker();
        dpCheckIn.setValue(LocalDate.now());

        DatePicker dpCheckOut = new DatePicker();
        dpCheckOut.setValue(LocalDate.now().plusDays(1));

        Button btnBook = new Button("Confirm Booking");
        Label lblMessage = new Label();

        btnBook.setOnAction(e -> {
            String fname = tfName.getText().trim();
            String lname = tfLastName.getText().trim();
            String phone = tfPhone.getText().trim();
            LocalDate checkIn = dpCheckIn.getValue();
            LocalDate checkOut = dpCheckOut.getValue();

            if (fname.isEmpty() || lname.isEmpty() || phone.isEmpty() || checkIn == null || checkOut == null) {
                lblMessage.setText("Please fill all fields.");
                return;
            }
            if (!checkOut.isAfter(checkIn)) {
                lblMessage.setText("Check-out date must be after check-in date.");
                return;
            }

            // stumrebis bazashi chasma
            try (Connection conn = dbcn.getConnection()) {
                conn.setAutoCommit(false);

                //inserti
                String guestInsert = "INSERT INTO guests (firstname, lastname, phone) VALUES (?, ?, ?)";
                PreparedStatement guestStmt = conn.prepareStatement(guestInsert, Statement.RETURN_GENERATED_KEYS);
                guestStmt.setString(1, fname);
                guestStmt.setString(2, lname);
                guestStmt.setString(3, phone);
                guestStmt.executeUpdate();

                ResultSet generatedKeys = guestStmt.getGeneratedKeys();
                int guestId;
                if (generatedKeys.next()) {
                    guestId = generatedKeys.getInt(1);
                } else {
                    lblMessage.setText("Failed to retrieve guest ID.");
                    conn.rollback();
                    return;
                }

                // tu aris otaxi tavisufali
                String checkBooking = "SELECT COUNT(*) FROM bookings WHERE roomid = ? AND NOT (checkoutdate <= ? OR checkindate >= ?)";
                PreparedStatement checkStmt = conn.prepareStatement(checkBooking);
                checkStmt.setInt(1, room.getRoomId());
                checkStmt.setDate(2, Date.valueOf(checkIn));
                checkStmt.setDate(3, Date.valueOf(checkOut));
                ResultSet checkRs = checkStmt.executeQuery();
                if (checkRs.next() && checkRs.getInt(1) > 0) {
                    lblMessage.setText("Sorry, this room is no longer available for selected dates.");
                    conn.rollback();
                    return;
                }

                // insert book
                String bookingInsert = "INSERT INTO bookings (roomid, guestid, checkindate, checkoutdate) VALUES (?, ?, ?, ?)";
                PreparedStatement bookingStmt = conn.prepareStatement(bookingInsert);
                bookingStmt.setInt(1, room.getRoomId());
                bookingStmt.setInt(2, guestId);
                bookingStmt.setDate(3, Date.valueOf(checkIn));
                bookingStmt.setDate(4, Date.valueOf(checkOut));
                bookingStmt.executeUpdate();

                conn.commit();

                lblMessage.setText("Booking confirmed!");
                loadRooms("All", "", "", "", "");
                btnBook.setDisable(true);

            } catch (SQLException ex) {
                lblMessage.setText("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        VBox dialogLayout = new VBox(10,
                new HBox(10, lblName, tfName),
                new HBox(10, lblLastName, tfLastName),
                new HBox(10, lblPhone, tfPhone),
                new HBox(10, new Label("Check-in:"), dpCheckIn),
                new HBox(10, new Label("Check-out:"), dpCheckOut),
                btnBook,
                lblMessage
        );
        dialogLayout.setPadding(new Insets(10));
        dialogLayout.setAlignment(Pos.CENTER_LEFT);

        Scene dialogScene = new Scene(dialogLayout, 400, 300);
        dialogScene.getStylesheets().add(
                guestwindow.class.getResource("style.css").toExternalForm()
        );
        
        dialog.setScene(dialogScene);
        dialog.showAndWait();
        
    }

    
    
    public static class Room {
        private final int roomId;
        private final int roomNumber;
        private final int beds;
        private final double price;

        public Room(int roomId, int roomNumber, int beds, double price) {
            this.roomId = roomId;
            this.roomNumber = roomNumber;
            this.beds = beds;
            this.price = price;
        }

        public int getRoomId() {
            return roomId;
        }

        public int getRoomNumber() {
            return roomNumber;
        }

        public int getBeds() {
            return beds;
        }

        public double getPrice() {
            return price;
        }
    }
}

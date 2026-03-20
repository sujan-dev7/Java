package view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.EquipmentModel;
import model.MaintenanceModel;
import model.Session;
import model.dbconnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MaintenancePanel {

    private final dbconnection db;
    private final Stage        owner;

    private TableView<MaintenanceModel> maintTable;
    private VBox                        pane;

    public MaintenancePanel(dbconnection db, Stage owner) {
        this.db = db; this.owner = owner;
        build();
    }

    @SuppressWarnings("unchecked")
    private void build() {
        pane = new VBox(14);
        pane.setId("pane-maintenance");
        pane.getChildren().add(UIHelper.sectionTitle("Maintenance Schedule"));

        Button addBtn = UIHelper.primaryBtn("+ Schedule Maintenance");
        addBtn.setOnAction(e -> openDialog());
        HBox toolbar = new HBox(addBtn);
        toolbar.setAlignment(Pos.CENTER_RIGHT);

        maintTable = new TableView<>();
        VBox.setVgrow(maintTable, Priority.ALWAYS);

        TableColumn<MaintenanceModel, String> colEq   = new TableColumn<>("Equipment");
        TableColumn<MaintenanceModel, String> colType = new TableColumn<>("Type");
        TableColumn<MaintenanceModel, String> colDesc = new TableColumn<>("Description");
        TableColumn<MaintenanceModel, String> colDate = new TableColumn<>("Scheduled");
        TableColumn<MaintenanceModel, String> colBy   = new TableColumn<>("Assigned To");
        TableColumn<MaintenanceModel, String> colStat = new TableColumn<>("Status");

        colEq.setCellValueFactory(new PropertyValueFactory<>("equipmentName"));    colEq.setPrefWidth(140);
        colType.setCellValueFactory(new PropertyValueFactory<>("maintenanceType")); colType.setPrefWidth(120);
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));    colDesc.setPrefWidth(180);
        colDate.setCellValueFactory(new PropertyValueFactory<>("scheduledDate"));  colDate.setPrefWidth(110);
        colBy.setCellValueFactory(new PropertyValueFactory<>("performedByName"));  colBy.setPrefWidth(130);
        colStat.setCellValueFactory(new PropertyValueFactory<>("status"));         colStat.setPrefWidth(110);

        // status — coloured text only
        colStat.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); return; }
                setGraphic(UIHelper.maintBadge(s));
                setText(null);
            }
        });

        maintTable.getColumns().addAll(colEq, colType, colDesc, colDate, colStat, colBy);
        maintTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        pane.getChildren().addAll(toolbar, maintTable);
    }

    public void refresh() {
        List<MaintenanceModel> list = new ArrayList<>();
        try {
            ResultSet rs = db.getAllMaintenanceLogs();
            if (rs == null) return;
            while (rs.next()) {
                MaintenanceModel m = new MaintenanceModel();
                m.setId(rs.getInt("id"));
                m.setEquipmentId(rs.getInt("equipment_id"));
                m.setEquipmentName(rs.getString("equipment_name"));
                m.setPerformedBy(rs.getInt("performed_by"));
                m.setPerformedByName(rs.getString("performed_by_name"));
                m.setMaintenanceType(rs.getString("maintenance_type"));
                m.setDescription(rs.getString("description"));
                m.setScheduledDate(rs.getString("scheduled_date"));
                m.setStatus(rs.getString("status"));
                list.add(m);
            }
            rs.close();
        } catch (SQLException e) { e.printStackTrace(); }
        maintTable.setItems(FXCollections.observableArrayList(list));
    }

    private void openDialog() {
        // Fetch equipment list
        List<EquipmentModel> equipList = new ArrayList<>();
        try {
            ResultSet rs = db.getAllEquipment();
            if (rs != null) {
                while (rs.next()) {
                    EquipmentModel eq = new EquipmentModel();
                    eq.setId(rs.getInt("id"));
                    eq.setName(rs.getString("name"));
                    equipList.add(eq);
                }
                rs.close();
            }
        } catch (SQLException e) { e.printStackTrace(); }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Schedule Maintenance");
        dialog.setResizable(false);

        VBox root = new VBox(12);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color:#0d1b2a;");
        root.setPrefWidth(420);

        Label titleLbl = new Label("Schedule Maintenance");
        titleLbl.setFont(Font.font("SansSerif", FontWeight.BOLD, 18));
        titleLbl.setTextFill(Color.web("#e0e8f0"));

        ComboBox<String> eqBox = new ComboBox<>();
        equipList.forEach(e -> eqBox.getItems().add(e.getId() + " — " + e.getName()));
        eqBox.setPromptText("Select equipment");
        eqBox.setPrefWidth(Double.MAX_VALUE);
        eqBox.setPrefHeight(38);
        eqBox.setStyle("-fx-background-color:#1b2a3b;-fx-border-color:#2e4057;"
            + "-fx-border-width:1.5px;-fx-border-radius:6px;-fx-background-radius:6px;"
            + "-fx-text-fill:#e0e8f0;");

        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList(
            "Routine Check", "Repair", "Replacement", "Deep Clean", "Calibration", "Other"));
        typeBox.setPromptText("Maintenance type");
        typeBox.setPrefWidth(Double.MAX_VALUE);
        typeBox.setPrefHeight(38);
        typeBox.setStyle("-fx-background-color:#1b2a3b;-fx-border-color:#2e4057;"
            + "-fx-border-width:1.5px;-fx-border-radius:6px;-fx-background-radius:6px;"
            + "-fx-text-fill:#e0e8f0;");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Describe the work to be done…");
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);
        descArea.setStyle("-fx-background-color:#1b2a3b;-fx-border-color:#2e4057;"
            + "-fx-text-fill:#e0e8f0;-fx-prompt-text-fill:#4a6080;");

        TextField dateField = UIHelper.dlgField("Scheduled date  (yyyy-MM-dd)");

        Label errLbl = new Label("");
        errLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#e74c3c;");
        errLbl.setVisible(false);

        Button saveBtn   = UIHelper.primaryBtn("Schedule");
        Button cancelBtn = UIHelper.secondaryBtn("Cancel");
        cancelBtn.setOnAction(e -> dialog.close());

        saveBtn.setOnAction(e -> {
            if (eqBox.getValue() == null || typeBox.getValue() == null) {
                errLbl.setText("Please select equipment and maintenance type.");
                errLbl.setVisible(true);
                return;
            }
            int eqId = Integer.parseInt(eqBox.getValue().split(" — ")[0]);
            boolean ok = db.addMaintenanceLog(
                eqId, Session.getCurrentUser().getId(),
                typeBox.getValue(), descArea.getText().trim(),
                dateField.getText().trim(), "Scheduled");
            if (ok) {
                db.logActivity(Session.getCurrentUser().getId(),
                    "SCHEDULE_MAINTENANCE", "Scheduled " + typeBox.getValue() + " for equipment ID: " + eqId);
                dialog.close();
                refresh();
                UIHelper.showInfo(owner, "Maintenance scheduled successfully.");
            } else {
                errLbl.setText("Failed to schedule maintenance. Please try again.");
                errLbl.setVisible(true);
            }
        });

        root.getChildren().addAll(
            titleLbl,
            UIHelper.dlgGroup("Equipment",    eqBox),
            UIHelper.dlgGroup("Type",         typeBox),
            UIHelper.dlgGroup("Description",  descArea),
            UIHelper.dlgGroup("Scheduled Date", dateField),
            errLbl,
            UIHelper.dialogBtnRow(saveBtn, cancelBtn)
        );

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
            getClass().getResource("/application/application.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    public VBox getPane() { return pane; }
}

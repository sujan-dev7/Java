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
import model.Session;
import model.dbconnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * EquipmentPanel — equipment table with search, status filter, CRUD.
 * When status is set to "Retired" a popup shows the equipment name.
 * Notifies OverviewPanel via Runnable callback after any data change.
 */
public class EquipmentPanel {

    private final dbconnection db;
    private final Stage        owner;
    private final Runnable     onDataChanged;

    private TableView<EquipmentModel> equipTable;
    private TextField                 searchField;
    private ComboBox<String>          statusFilter;
    private VBox                      pane;

    public EquipmentPanel(dbconnection db, Stage owner, Runnable onDataChanged) {
        this.db = db; this.owner = owner; this.onDataChanged = onDataChanged;
        build();
    }

    @SuppressWarnings("unchecked")
    private void build() {
        pane = new VBox(14);
        pane.setId("pane-equipment");
        pane.getChildren().add(UIHelper.sectionTitle("Equipment Management"));

        // ── toolbar ──────────────────────────────────────────────────────────────
        searchField = new TextField();
        searchField.setPromptText("Search by name, category or serial…");
        searchField.setPrefWidth(280);
        searchField.setPrefHeight(38);
        searchField.setStyle("-fx-background-color:#1b2a3b;-fx-border-color:#2e4057;"
            + "-fx-border-width:1.5px;-fx-border-radius:6px;-fx-background-radius:6px;"
            + "-fx-text-fill:#e0e8f0;-fx-prompt-text-fill:#4a6080;-fx-font-size:13px;");
        searchField.textProperty().addListener((o, ov, nv) -> applyFilter());

        statusFilter = new ComboBox<>(
            FXCollections.observableArrayList("All", "Active", "Maintenance", "Retired"));
        statusFilter.setValue("All");
        statusFilter.setPrefHeight(38);
        statusFilter.setStyle("-fx-background-color:#1b2a3b;-fx-border-color:#2e4057;"
            + "-fx-border-width:1.5px;-fx-border-radius:6px;-fx-background-radius:6px;"
            + "-fx-text-fill:#e0e8f0;");
        statusFilter.valueProperty().addListener((o, ov, nv) -> applyFilter());

        Button addBtn = UIHelper.primaryBtn("+ Add Equipment");
        addBtn.setOnAction(e -> openDialog(null));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox toolbar = new HBox(10, searchField, statusFilter, spacer, addBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        // ── table ─────────────────────────────────────────────────────────────────
        equipTable = new TableView<>();
        VBox.setVgrow(equipTable, Priority.ALWAYS);

        TableColumn<EquipmentModel, Integer> colId   = new TableColumn<>("ID");
        TableColumn<EquipmentModel, String>  colName = new TableColumn<>("Name");
        TableColumn<EquipmentModel, String>  colCat  = new TableColumn<>("Category");
        TableColumn<EquipmentModel, String>  colSer  = new TableColumn<>("Serial No.");
        TableColumn<EquipmentModel, String>  colDate = new TableColumn<>("Purchase Date");
        TableColumn<EquipmentModel, String>  colStat = new TableColumn<>("Status");
        TableColumn<EquipmentModel, String>  colAct  = new TableColumn<>("Actions");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));         colId.setPrefWidth(55);
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));     colName.setPrefWidth(160);
        colCat.setCellValueFactory(new PropertyValueFactory<>("category"));  colCat.setPrefWidth(110);
        colSer.setCellValueFactory(new PropertyValueFactory<>("serialNumber")); colSer.setPrefWidth(120);
        colDate.setCellValueFactory(new PropertyValueFactory<>("purchaseDate")); colDate.setPrefWidth(110);
        colStat.setCellValueFactory(new PropertyValueFactory<>("status"));   colStat.setPrefWidth(110);
        colAct.setPrefWidth(190);

        // Status cell — coloured TEXT only
        colStat.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); setText(null); return; }
                setGraphic(UIHelper.statusBadge(s));
                setText(null);
            }
        });

        // Actions cell
        colAct.setCellFactory(c -> new TableCell<>() {
            final Button editBtn   = UIHelper.secondaryBtn("Edit");
            final Button deleteBtn = UIHelper.dangerBtn("Delete");
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                EquipmentModel eq = getTableView().getItems().get(getIndex());
                editBtn.setOnAction(e -> openDialog(eq));
                deleteBtn.setDisable(!Session.isAdmin());
                deleteBtn.setOnAction(e -> handleDelete(eq));
                HBox box = new HBox(6, editBtn, deleteBtn);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        equipTable.getColumns().addAll(colId, colName, colCat, colSer, colDate, colStat, colAct);
        equipTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        pane.getChildren().addAll(toolbar, equipTable);
    }

    // ── Refresh / filter ──────────────────────────────────────────────────────────
    public void refresh() {
        equipTable.setItems(FXCollections.observableArrayList(fetchAll()));
    }

    private void applyFilter() {
        String kw     = searchField.getText().trim().toLowerCase();
        String status = statusFilter.getValue();
        List<EquipmentModel> filtered = new ArrayList<>();
        for (EquipmentModel eq : fetchAll()) {
            boolean matchKw = kw.isEmpty()
                || eq.getName().toLowerCase().contains(kw)
                || eq.getCategory().toLowerCase().contains(kw)
                || (eq.getSerialNumber() != null && eq.getSerialNumber().toLowerCase().contains(kw));
            boolean matchSt = status == null || status.equals("All") || eq.getStatus().equals(status);
            if (matchKw && matchSt) filtered.add(eq);
        }
        equipTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private List<EquipmentModel> fetchAll() {
        List<EquipmentModel> list = new ArrayList<>();
        try {
            ResultSet rs = db.getAllEquipment();
            if (rs == null) return list;
            while (rs.next()) {
                EquipmentModel eq = new EquipmentModel();
                eq.setId(rs.getInt("id"));
                eq.setName(rs.getString("name"));
                eq.setCategory(rs.getString("category"));
                eq.setSerialNumber(rs.getString("serial_number"));
                eq.setPurchaseDate(rs.getString("purchase_date"));
                eq.setStatus(rs.getString("status"));
                eq.setNotes(rs.getString("condition_notes"));
                eq.setAddedBy(rs.getInt("added_by"));
                eq.setAddedByName(rs.getString("added_by_name"));
                list.add(eq);
            }
            rs.close();
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── Add / Edit Dialog ─────────────────────────────────────────────────────────
    private void openDialog(EquipmentModel existing) {
        boolean isNew = (existing == null);

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle(isNew ? "Add Equipment" : "Edit Equipment");
        dialog.setResizable(false);

        VBox root = new VBox(12);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color:#0d1b2a;");
        root.setPrefWidth(440);

        Label titleLbl = new Label(isNew ? "Add New Equipment" : "Edit Equipment");
        titleLbl.setFont(Font.font("SansSerif", FontWeight.BOLD, 18));
        titleLbl.setTextFill(Color.web("#e0e8f0"));

        TextField nameField   = UIHelper.dlgField("Equipment name");
        TextField catField    = UIHelper.dlgField("Category  (e.g. Cardio, Strength)");
        TextField serialField = UIHelper.dlgField("Serial number  (e.g. SN-001)");
        TextField dateField   = UIHelper.dlgField("Purchase date  (yyyy-MM-dd)");

        ComboBox<String> statusBox = new ComboBox<>(
            FXCollections.observableArrayList("Active", "Maintenance", "Retired"));
        statusBox.setPrefWidth(Double.MAX_VALUE);
        statusBox.setPrefHeight(38);
        statusBox.setStyle("-fx-background-color:#1b2a3b;-fx-border-color:#2e4057;"
            + "-fx-border-width:1.5px;-fx-border-radius:6px;-fx-background-radius:6px;"
            + "-fx-text-fill:#e0e8f0;");

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Condition notes (optional)");
        notesArea.setPrefRowCount(3);
        notesArea.setWrapText(true);
        notesArea.setStyle("-fx-background-color:#1b2a3b;-fx-border-color:#2e4057;"
            + "-fx-text-fill:#e0e8f0;-fx-prompt-text-fill:#4a6080;");

        // pre-fill for edit
        if (!isNew) {
            nameField.setText(existing.getName());
            catField.setText(existing.getCategory());
            serialField.setText(existing.getSerialNumber() != null ? existing.getSerialNumber() : "");
            dateField.setText(existing.getPurchaseDate()   != null ? existing.getPurchaseDate()   : "");
            statusBox.setValue(existing.getStatus());
            notesArea.setText(existing.getNotes()          != null ? existing.getNotes()           : "");
        } else {
            statusBox.setValue("Active");
        }

        // Track previous status for change detection
        final String[] prevStatus = { isNew ? "Active" : (existing.getStatus() != null ? existing.getStatus() : "Active") };

        Label errLbl = new Label("");
        errLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#e74c3c;");
        errLbl.setVisible(false);

        Button saveBtn   = UIHelper.primaryBtn(isNew ? "Add Equipment" : "Save Changes");
        Button cancelBtn = UIHelper.secondaryBtn("Cancel");
        cancelBtn.setOnAction(e -> dialog.close());

        saveBtn.setOnAction(e -> {
            String name   = nameField.getText().trim();
            String cat    = catField.getText().trim();
            String serial = serialField.getText().trim();
            String date   = dateField.getText().trim();
            String status = statusBox.getValue() != null ? statusBox.getValue() : "Active";
            String notes  = notesArea.getText().trim();

            // Validation
            if (name.isEmpty() || cat.isEmpty() || serial.isEmpty()) {
                errLbl.setText("Name, category and serial number are required.");
                errLbl.setVisible(true);
                return;
            }

            boolean success;
            if (isNew) {
                success = db.addEquipment(name, cat, serial, date, status, notes,
                                          Session.getCurrentUser().getId());
                if (success) db.logActivity(Session.getCurrentUser().getId(),
                    "ADD_EQUIPMENT", "Added: " + name + " (" + serial + ")");
            } else {
                success = db.updateEquipment(existing.getId(), name, cat, serial, date, status, notes);
                if (success) db.logActivity(Session.getCurrentUser().getId(),
                    "UPDATE_EQUIPMENT", "Updated: " + name + " → status: " + status);
            }

            if (success) {
                dialog.close();
                refresh();
                onDataChanged.run();

                // ── RETIRED POPUP ─────────────────────────────────────────────────
                boolean statusChangedToRetired = "Retired".equals(status) && !"Retired".equals(prevStatus[0]);
                boolean isNewAndRetired        = isNew && "Retired".equals(status);

                if (statusChangedToRetired || isNewAndRetired) {
                    Alert retiredAlert = new Alert(Alert.AlertType.INFORMATION);
                    retiredAlert.setTitle("Equipment Retired");
                    retiredAlert.setHeaderText("Equipment Added to Retired");
                    retiredAlert.setContentText(
                        "\"" + name + "\" (Serial: " + serial + ")\n"
                        + "has been added to the Retired equipment list.\n\n"
                        + "Category: " + cat + "\n"
                        + (date.isEmpty() ? "" : "Purchase Date: " + date + "\n")
                        + (notes.isEmpty() ? "" : "Notes: " + notes));
                    retiredAlert.initOwner(owner);
                    retiredAlert.showAndWait();
                } else {
                    UIHelper.showInfo(owner, isNew ? "Equipment added successfully." : "Equipment updated successfully.");
                }

            } else {
                errLbl.setText("Save failed. Serial number may already be in use.");
                errLbl.setVisible(true);
            }
        });

        // Update prevStatus when combo changes (to detect transition)
        statusBox.valueProperty().addListener((o, ov, nv) -> {
            if (ov != null) prevStatus[0] = ov;
        });

        root.getChildren().addAll(
            titleLbl,
            UIHelper.dlgGroup("Name",          nameField),
            UIHelper.dlgGroup("Category",      catField),
            UIHelper.dlgGroup("Serial Number", serialField),
            UIHelper.dlgGroup("Purchase Date", dateField),
            UIHelper.dlgGroup("Status",        statusBox),
            UIHelper.dlgGroup("Notes",         notesArea),
            errLbl,
            UIHelper.dialogBtnRow(saveBtn, cancelBtn)
        );

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
            getClass().getResource("/application/application.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // ── Delete ────────────────────────────────────────────────────────────────────
    private void handleDelete(EquipmentModel eq) {
        if (!Session.isAdmin()) {
            UIHelper.showAlert(owner, "Access Denied",
                "Only administrators can delete equipment.", Alert.AlertType.WARNING);
            return;
        }
        if (UIHelper.confirm(owner, "Delete Equipment",
                "Are you sure you want to delete \"" + eq.getName() + "\"?\nThis cannot be undone.")) {
            if (db.deleteEquipment(eq.getId())) {
                db.logActivity(Session.getCurrentUser().getId(),
                    "DELETE_EQUIPMENT", "Deleted: " + eq.getName());
                refresh();
                onDataChanged.run();
                UIHelper.showInfo(owner, "\"" + eq.getName() + "\" has been deleted.");
            } else {
                UIHelper.showAlert(owner, "Error",
                    "Failed to delete equipment.", Alert.AlertType.ERROR);
            }
        }
    }

    public VBox getPane() { return pane; }
}

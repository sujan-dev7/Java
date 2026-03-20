package view;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Session;
import model.UserModel;
import model.dbconnection;

/**
 * UsersPanel — admin-only panel.
 * Lists all registered users. Admin can delete any user except themselves.
 */
public class UsersPanel {

    private final dbconnection db;
    private final Stage        owner;

    private TableView<UserModel> usersTable;
    private VBox                 pane;

    public UsersPanel(dbconnection db, Stage owner) {
        this.db = db; this.owner = owner;
        build();
    }

    @SuppressWarnings("unchecked")
    private void build() {
        pane = new VBox(14);
        pane.setId("pane-users");
        pane.getChildren().add(UIHelper.sectionTitle("User Management"));

        // info label
        Label infoLbl = new Label("Usernames are case-sensitive.  \"Admin\" and \"admin\" are separate accounts.");
        infoLbl.setStyle("-fx-font-size:11px;-fx-text-fill:" + UIHelper.COL_TEXT_MUTED + ";");

        usersTable = new TableView<>();
        VBox.setVgrow(usersTable, Priority.ALWAYS);

        TableColumn<UserModel, Integer> colId    = new TableColumn<>("ID");
        TableColumn<UserModel, String>  colUname = new TableColumn<>("Username");
        TableColumn<UserModel, String>  colFname = new TableColumn<>("Full Name");
        TableColumn<UserModel, String>  colEmail = new TableColumn<>("Email");
        TableColumn<UserModel, String>  colRole  = new TableColumn<>("Role");
        TableColumn<UserModel, String>  colAct   = new TableColumn<>("Actions");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));         colId.setPrefWidth(55);
        colUname.setCellValueFactory(new PropertyValueFactory<>("username")); colUname.setPrefWidth(130);
        colFname.setCellValueFactory(new PropertyValueFactory<>("fullName")); colFname.setPrefWidth(160);
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));    colEmail.setPrefWidth(200);
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));      colRole.setPrefWidth(90);
        colAct.setPrefWidth(110);

        // Role cell — coloured TEXT only
        colRole.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) { setGraphic(null); return; }
                setGraphic(UIHelper.roleBadge(role));
                setText(null);
            }
        });

        // Actions cell — Delete button
        colAct.setCellFactory(c -> new TableCell<>() {
            final Button deleteBtn = UIHelper.dangerBtn("Delete");
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                UserModel u = getTableView().getItems().get(getIndex());
                boolean isSelf = u.getId() == Session.getCurrentUser().getId();
                deleteBtn.setDisable(isSelf);
                deleteBtn.setTooltip(new Tooltip(isSelf
                    ? "You cannot delete your own account"
                    : "Delete this user permanently"));
                deleteBtn.setOnAction(e -> handleDeleteUser(u));
                HBox box = new HBox(deleteBtn);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        usersTable.getColumns().addAll(colId, colUname, colFname, colEmail, colRole, colAct);
        usersTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        pane.getChildren().addAll(infoLbl, usersTable);
    }

    public void refresh() {
        List<UserModel> list = new ArrayList<>();
        try {
            ResultSet rs = db.getAllUsers();
            if (rs == null) return;
            while (rs.next()) {
                UserModel u = new UserModel();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setFullName(rs.getString("full_name"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("role"));
                list.add(u);
            }
            rs.close();
        } catch (SQLException e) { e.printStackTrace(); }
        usersTable.setItems(FXCollections.observableArrayList(list));
    }

    private void handleDeleteUser(UserModel u) {
        if (!Session.isAdmin()) {
            UIHelper.showAlert(owner, "Access Denied",
                "Only administrators can delete users.", Alert.AlertType.WARNING);
            return;
        }
        if (u.getId() == Session.getCurrentUser().getId()) {
            UIHelper.showAlert(owner, "Not Allowed",
                "You cannot delete your own account.", Alert.AlertType.WARNING);
            return;
        }
        boolean confirmed = UIHelper.confirm(owner, "Delete User",
            "Delete user \"" + u.getUsername() + "\" (" + u.getFullName() + ")?\n\n"
            + "This will permanently remove their account.\nThis action cannot be undone.");
        if (!confirmed) return;

        boolean success = db.deleteUser(u.getUsername());
        if (success) {
            db.logActivity(Session.getCurrentUser().getId(),
                "DELETE_USER", "Deleted user: " + u.getUsername() + " (ID: " + u.getId() + ")");
            refresh();
            UIHelper.showInfo(owner,
                "User \"" + u.getUsername() + "\" (" + u.getFullName() + ") has been removed.");
        } else {
            UIHelper.showAlert(owner, "Error",
                "Failed to delete user. They may have linked equipment or maintenance records.",
                Alert.AlertType.ERROR);
        }
    }

    public VBox getPane() { return pane; }
}

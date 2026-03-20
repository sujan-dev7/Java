package view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.dbconnection;

/**
 * UserPage — original CRUD test page preserved.
 * Updated to use the full createUser() signature.
 */
public class UserPage {

    private Stage         stage;
    private TextField     usernameField;
    private PasswordField passwordField;
    private Label         resultLabel;

    public UserPage(Stage stage) {
        this.stage = stage;
        dbconnection connect = new dbconnection();

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-background-color:#0d1b2a;");

        Label userLabel = new Label("Username:");
        userLabel.setStyle("-fx-text-fill:#7ec8e3;");
        usernameField = new TextField();
        usernameField.setStyle("-fx-background-color:#1b2a3b;-fx-text-fill:#e0e8f0;"
            + "-fx-border-color:#2e4057;-fx-border-width:1px;");
        grid.add(userLabel, 0, 0);
        grid.add(usernameField, 1, 0);

        Label passLabel = new Label("Password:");
        passLabel.setStyle("-fx-text-fill:#7ec8e3;");
        passwordField = new PasswordField();
        passwordField.setStyle("-fx-background-color:#1b2a3b;-fx-text-fill:#e0e8f0;"
            + "-fx-border-color:#2e4057;-fx-border-width:1px;");
        grid.add(passLabel, 0, 1);
        grid.add(passwordField, 1, 1);

        Button createBtn = UIHelper.primaryBtn("Create");
        Button readBtn   = UIHelper.secondaryBtn("Read");
        Button updateBtn = UIHelper.primaryBtn("Update");
        Button deleteBtn = UIHelper.dangerBtn("Delete");
        Button testBtn   = UIHelper.secondaryBtn("Test Connection");

        resultLabel = new Label();
        resultLabel.setStyle("-fx-text-fill:#2ecc71;-fx-font-size:12px;");
        grid.add(resultLabel, 0, 5, 2, 1);

        createBtn.setOnAction(e -> {
            boolean success = connect.createUser(
                usernameField.getText(), passwordField.getText(), "", "", "staff");
            resultLabel.setText(success ? "✓ User created!" : "✗ Failed to create user.");
        });

        readBtn.setOnAction(e -> {
            String pw = connect.readUser(usernameField.getText());
            resultLabel.setText(pw != null ? "Password: " + pw : "✗ User not found.");
        });

        updateBtn.setOnAction(e -> {
            boolean success = connect.updateUserPassword(
                usernameField.getText(), passwordField.getText());
            resultLabel.setText(success ? "✓ Password updated!" : "✗ Update failed.");
        });

        deleteBtn.setOnAction(e -> {
            boolean success = connect.deleteUser(usernameField.getText());
            resultLabel.setText(success ? "✓ User deleted!" : "✗ Delete failed.");
        });

        testBtn.setOnAction(e -> {
            connect.getConnection();
            resultLabel.setText("✓ Connection test complete — check console.");
        });

        grid.add(createBtn, 0, 2);
        grid.add(readBtn,   1, 2);
        grid.add(updateBtn, 0, 3);
        grid.add(deleteBtn, 1, 3);
        grid.add(testBtn,   0, 4);

        Scene scene = new Scene(grid, 380, 320);
        stage.setTitle("User Page — CRUD Test");
        stage.setScene(scene);
    }

    public void show() { stage.show(); }
}

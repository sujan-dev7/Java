package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.Session;
import model.UserModel;
import model.Validator;
import model.dbconnection;

/**
 * LoginPage — dark-blue split panel.
 * Left: gym image | Right: login form.
 * Shows popup Alert dialogs for all validation and auth errors.
 * Username comparison is CASE-SENSITIVE.
 */
public class LoginPage {

    private Stage         stage;
    private TextField     usernameField;
    private PasswordField passwordField;
    private Button        loginBtn;
    private Hyperlink     registerLink;

    private final dbconnection db = new dbconnection();

    public LoginPage(Stage stage) { this.stage = stage; }

    public void show() {
        HBox root = new HBox();
        root.setPrefSize(960, 640);
        root.getChildren().addAll(buildImagePanel(), buildFormPanel());

        Scene scene = new Scene(root, 960, 640);
        scene.getStylesheets().add(
            getClass().getResource("/application/application.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("GymLogix — Login");
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();

        wireActions();
    }

    // ── LEFT: gym image ──────────────────────────────────────────────────────────
    private StackPane buildImagePanel() {
        StackPane panel = new StackPane();
        panel.setPrefSize(480, 640);
        panel.setMinWidth(480);

        try {
            Image img = new Image(
                getClass().getResourceAsStream("/application/gym_bg.jpg"),
                480, 640, false, true);
            ImageView iv = new ImageView(img);
            iv.setFitWidth(480);
            iv.setFitHeight(640);
            iv.setPreserveRatio(false);
            panel.getChildren().add(iv);
        } catch (Exception e) {
            panel.setStyle("-fx-background-color:linear-gradient(to bottom,#0d0d0d,#0d1b2a);");
        }

        Pane overlay = new Pane();
        overlay.setPrefSize(480, 640);
        overlay.setStyle("-fx-background-color:rgba(0,0,0,0.55);");

        VBox brand = new VBox(6);
        brand.setPadding(new Insets(0, 0, 40, 36));
        Label appName = new Label("GymLogix");
        appName.setFont(Font.font("SansSerif", FontWeight.BOLD, 36));
        appName.setTextFill(Color.web("#00a8e8"));
        Label tagline = new Label("Equipment Management System");
        tagline.setFont(Font.font("SansSerif", 14));
        tagline.setTextFill(Color.web("#7ec8e3"));
        brand.getChildren().addAll(appName, tagline);
        StackPane.setAlignment(brand, Pos.BOTTOM_LEFT);

        panel.getChildren().addAll(overlay, brand);
        return panel;
    }

    // ── RIGHT: form panel ────────────────────────────────────────────────────────
    private VBox buildFormPanel() {
        VBox panel = new VBox();
        panel.setPrefSize(480, 640);
        panel.setStyle("-fx-background-color:#0d1b2a;");
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(0, 60, 0, 60));

        // Title
        Label title = new Label("Welcome Back");
        title.setFont(Font.font("SansSerif", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#e0e8f0"));

        Label subtitle = new Label("Sign in to your account");
        subtitle.setFont(Font.font("SansSerif", 13));
        subtitle.setTextFill(Color.web("#4a6080"));
        VBox.setMargin(subtitle, new Insets(4, 0, 32, 0));

        // Username field
        Label userLbl = fieldLabel("Username");
        usernameField = new TextField();
        usernameField.setPromptText("Enter username (case-sensitive)");
        usernameField.setPrefHeight(42);
        styleInput(usernameField);
        VBox usernameGroup = new VBox(6, userLbl, usernameField);

        // Password field
        Label passLbl = fieldLabel("Password");
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setPrefHeight(42);
        stylePassInput(passwordField);
        VBox passwordGroup = new VBox(6, passLbl, passwordField);
        VBox.setMargin(passwordGroup, new Insets(14, 0, 0, 0));

        // Login button
        loginBtn = new Button("Sign In");
        loginBtn.setStyle("-fx-background-color:#00a8e8;-fx-text-fill:white;"
            + "-fx-font-size:14px;-fx-font-weight:bold;-fx-background-radius:8px;"
            + "-fx-padding:11px 20px;-fx-cursor:hand;");
        loginBtn.setPrefWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(44);
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle(
            "-fx-background-color:#007bb5;-fx-text-fill:white;"
            + "-fx-font-size:14px;-fx-font-weight:bold;-fx-background-radius:8px;"
            + "-fx-padding:11px 20px;-fx-cursor:hand;"));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle(
            "-fx-background-color:#00a8e8;-fx-text-fill:white;"
            + "-fx-font-size:14px;-fx-font-weight:bold;-fx-background-radius:8px;"
            + "-fx-padding:11px 20px;-fx-cursor:hand;"));
        VBox.setMargin(loginBtn, new Insets(22, 0, 0, 0));

        // Register link
        HBox regRow = new HBox(4);
        regRow.setAlignment(Pos.CENTER);
        VBox.setMargin(regRow, new Insets(16, 0, 0, 0));
        Label regPrompt = new Label("Don't have an account?");
        regPrompt.setFont(Font.font("SansSerif", 12));
        regPrompt.setTextFill(Color.web("#4a6080"));
        registerLink = new Hyperlink("Register here");
        registerLink.setStyle("-fx-text-fill:#00a8e8;-fx-border-color:transparent;-fx-font-size:12px;");
        regRow.getChildren().addAll(regPrompt, registerLink);

        panel.getChildren().addAll(
            title, subtitle,
            usernameGroup, passwordGroup,
            loginBtn, regRow
        );
        return panel;
    }

    //  Actions
    private void wireActions() {
        loginBtn.setOnAction(e -> handleLogin());
        passwordField.setOnAction(e -> handleLogin());
        registerLink.setOnAction(e -> new RegisterPage(stage).show());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Step 1: local validation (popup) 
        String err = Validator.validateLogin(username, password);
        if (err != null) {
            showPopup("Validation Error", err, Alert.AlertType.WARNING);
            return;
        }

        // Step 2: authenticate (case-sensitive) 
        boolean valid = db.loginUser(username, password);
        if (!valid) {
            showPopup("Login Failed",
                "Invalid username or password.\n\nNote: Username is case-sensitive.\n"
                + "\"Admin\" and \"admin\" are different accounts.",
                Alert.AlertType.ERROR);
            usernameField.requestFocus();
            return;
        }

        // Step 3: load full user and start session 
        UserModel user = db.getUserByUsername(username);
        if (user == null) {
            showPopup("Error", "Could not load user data. Please try again.", Alert.AlertType.ERROR);
            return;
        }

        Session.setCurrentUser(user);
        db.logActivity(user.getId(), "LOGIN", "Logged in: " + username);

        // ── Step 4: success popup then navigate ───────────────────────────────────
        showPopup("Welcome!",
            "Welcome back, " + user.getFullName() + "!\nRole: " + user.getRole().toUpperCase(),
            Alert.AlertType.INFORMATION);

        new DashboardPage(stage).show();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────
    private void showPopup(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.initOwner(stage);
        a.showAndWait();
    }

    private Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#7ec8e3;");
        return lbl;
    }

    private void styleInput(TextField tf) {
        tf.setStyle("-fx-background-color:#1b2a3b;-fx-border-color:#2e4057;"
            + "-fx-border-width:1.5px;-fx-border-radius:6px;-fx-background-radius:6px;"
            + "-fx-text-fill:#e0e8f0;-fx-prompt-text-fill:#4a6080;-fx-font-size:13px;");
        tf.focusedProperty().addListener((o, ov, f) ->
            tf.setStyle(f
                ? "-fx-background-color:#1b2a3b;-fx-border-color:#00a8e8;"
                  + "-fx-border-width:2px;-fx-border-radius:6px;-fx-background-radius:6px;"
                  + "-fx-text-fill:#e0e8f0;-fx-prompt-text-fill:#4a6080;-fx-font-size:13px;"
                : "-fx-background-color:#1b2a3b;-fx-border-color:#2e4057;"
                  + "-fx-border-width:1.5px;-fx-border-radius:6px;-fx-background-radius:6px;"
                  + "-fx-text-fill:#e0e8f0;-fx-prompt-text-fill:#4a6080;-fx-font-size:13px;"));
    }

    private void stylePassInput(PasswordField pf) {
        pf.setStyle("-fx-background-color:#1b2a3b;-fx-border-color:#2e4057;"
            + "-fx-border-width:1.5px;-fx-border-radius:6px;-fx-background-radius:6px;"
            + "-fx-text-fill:#e0e8f0;-fx-prompt-text-fill:#4a6080;-fx-font-size:13px;");
        pf.focusedProperty().addListener((o, ov, f) ->
            pf.setStyle(f
                ? "-fx-background-color:#1b2a3b;-fx-border-color:#00a8e8;"
                  + "-fx-border-width:2px;-fx-border-radius:6px;-fx-background-radius:6px;"
                  + "-fx-text-fill:#e0e8f0;-fx-prompt-text-fill:#4a6080;-fx-font-size:13px;"
                : "-fx-background-color:#1b2a3b;-fx-border-color:#2e4057;"
                  + "-fx-border-width:1.5px;-fx-border-radius:6px;-fx-background-radius:6px;"
                  + "-fx-text-fill:#e0e8f0;-fx-prompt-text-fill:#4a6080;-fx-font-size:13px;"));
    }
}

package view;

import javafx.application.Platform;
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
 * RegisterPage — dark-blue split panel.
 * Real-time inline hints on each field AS you type.
 * Final popup alert on submit for all errors and success.
 * Username is CASE-SENSITIVE: "Admin" and "admin" are different.
 */
public class RegisterPage {

    private Stage stage;

    private TextField     fullNameField;
    private TextField     usernameField;
    private TextField     emailField;
    private PasswordField passwordField;
    private PasswordField confirmField;
    private ComboBox<String> roleBox;

    // per-field inline hint labels
    private Label usernameHint;
    private Label emailHint;
    private Label passwordHint;
    private Label confirmHint;

    private Button    registerBtn;
    private Hyperlink loginLink;

    private final dbconnection db = new dbconnection();

    public RegisterPage(Stage stage) { this.stage = stage; }

    public void show() {
        HBox root = new HBox();
        root.setPrefSize(960, 720);
        root.getChildren().addAll(buildImagePanel(), buildFormPanel());

        Scene scene = new Scene(root, 960, 720);
        scene.getStylesheets().add(
            getClass().getResource("/application/application.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("GymLogix — Register");
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();

        wireActions();
    }

    // ── LEFT: gym image ──────────────────────────────────────────────────────────
    private StackPane buildImagePanel() {
        StackPane panel = new StackPane();
        panel.setPrefSize(480, 720);
        panel.setMinWidth(480);

        try {
            Image img = new Image(
                getClass().getResourceAsStream("/application/gym_bg.jpg"),
                480, 720, false, true);
            ImageView iv = new ImageView(img);
            iv.setFitWidth(480);
            iv.setFitHeight(720);
            iv.setPreserveRatio(false);
            panel.getChildren().add(iv);
        } catch (Exception e) {
            panel.setStyle("-fx-background-color:linear-gradient(to bottom,#0d0d0d,#0d1b2a);");
        }

        Pane overlay = new Pane();
        overlay.setPrefSize(480, 720);
        overlay.setStyle("-fx-background-color:rgba(0,0,0,0.55);");

        VBox brand = new VBox(6);
        brand.setPadding(new Insets(0, 0, 40, 36));
        Label appName = new Label("GymLogix");
        appName.setFont(Font.font("SansSerif", FontWeight.BOLD, 36));
        appName.setTextFill(Color.web("#00a8e8"));
        Label tagline = new Label("Create your account");
        tagline.setFont(Font.font("SansSerif", 14));
        tagline.setTextFill(Color.web("#7ec8e3"));
        brand.getChildren().addAll(appName, tagline);
        StackPane.setAlignment(brand, Pos.BOTTOM_LEFT);

        panel.getChildren().addAll(overlay, brand);
        return panel;
    }

    // ── RIGHT: form panel ────────────────────────────────────────────────────────
    private ScrollPane buildFormPanel() {
        VBox inner = new VBox(0);
        inner.setPrefWidth(480);
        inner.setStyle("-fx-background-color:#0d1b2a;");
        inner.setPadding(new Insets(40, 60, 40, 60));

        Label title = new Label("Create Account");
        title.setFont(Font.font("SansSerif", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#e0e8f0"));

        Label subtitle = new Label("All fields are required");
        subtitle.setFont(Font.font("SansSerif", 12));
        subtitle.setTextFill(Color.web("#4a6080"));
        VBox.setMargin(subtitle, new Insets(4, 0, 24, 0));

        // ── Full Name ─────────────────────────────────────────────────────────────
        fullNameField = styledField("e.g. Juan Dela Cruz");
        VBox fullNameGroup = fieldGroup("Full Name", fullNameField, null);

        // ── Username + inline hint ────────────────────────────────────────────────
        usernameField = styledField("3–20 chars, letters/digits/underscore");
        usernameHint  = hintLabel();
        usernameField.textProperty().addListener((o, ov, nv) -> {
            if (nv.isEmpty()) { usernameHint.setVisible(false); return; }
            if (!Validator.isValidUsername(nv))
                hint(usernameHint, "✗  3–20 alphanumeric characters only", false);
            else
                hint(usernameHint, "✓  Valid username", true);
        });
        VBox usernameGroup = fieldGroup("Username (case-sensitive)", usernameField, usernameHint);

        // ── Email + inline hint ───────────────────────────────────────────────────
        emailField = styledField("you@email.com");
        emailHint  = hintLabel();
        emailField.textProperty().addListener((o, ov, nv) -> {
            if (nv.isEmpty()) { emailHint.setVisible(false); return; }
            if (!Validator.isValidEmail(nv))
                hint(emailHint, "✗  Enter a valid email address", false);
            else
                hint(emailHint, "✓  Valid email", true);
        });
        VBox emailGroup = fieldGroup("Email Address", emailField, emailHint);

        // ── Password + inline hint ────────────────────────────────────────────────
        passwordField = styledPassField("At least 6 characters");
        passwordHint  = hintLabel();
        passwordField.textProperty().addListener((o, ov, nv) -> {
            if (nv.isEmpty()) { passwordHint.setVisible(false); return; }
            if (!Validator.isValidPassword(nv))
                hint(passwordHint, "✗  At least 6 characters required", false);
            else
                hint(passwordHint, "✓  Strong enough", true);
        });
        VBox passwordGroup = fieldGroup("Password", passwordField, passwordHint);

        // ── Confirm Password + inline hint ────────────────────────────────────────
        confirmField = styledPassField("Re-enter your password");
        confirmHint  = hintLabel();
        confirmField.textProperty().addListener((o, ov, nv) -> {
            if (nv.isEmpty()) { confirmHint.setVisible(false); return; }
            if (!nv.equals(passwordField.getText()))
                hint(confirmHint, "✗  Passwords do not match", false);
            else
                hint(confirmHint, "✓  Passwords match", true);
        });
        VBox confirmGroup = fieldGroup("Confirm Password", confirmField, confirmHint);

        // ── Role ──────────────────────────────────────────────────────────────────
        roleBox = new ComboBox<>();
        roleBox.getItems().addAll("staff", "admin");
        roleBox.setPromptText("Select role");
        roleBox.setPrefWidth(Double.MAX_VALUE);
        roleBox.setPrefHeight(40);
        roleBox.setStyle("-fx-background-color:#1b2a3b;-fx-border-color:#2e4057;"
            + "-fx-border-width:1.5px;-fx-border-radius:6px;-fx-background-radius:6px;"
            + "-fx-text-fill:#e0e8f0;");
        VBox roleGroup = fieldGroup("Role", roleBox, null);

        // ── Register button ───────────────────────────────────────────────────────
        registerBtn = new Button("Create Account");
        registerBtn.setStyle("-fx-background-color:#00a8e8;-fx-text-fill:white;"
            + "-fx-font-size:14px;-fx-font-weight:bold;-fx-background-radius:8px;"
            + "-fx-padding:11px 20px;-fx-cursor:hand;");
        registerBtn.setPrefWidth(Double.MAX_VALUE);
        registerBtn.setPrefHeight(44);
        registerBtn.setOnMouseEntered(e -> registerBtn.setStyle(
            "-fx-background-color:#007bb5;-fx-text-fill:white;"
            + "-fx-font-size:14px;-fx-font-weight:bold;-fx-background-radius:8px;"
            + "-fx-padding:11px 20px;-fx-cursor:hand;"));
        registerBtn.setOnMouseExited(e -> registerBtn.setStyle(
            "-fx-background-color:#00a8e8;-fx-text-fill:white;"
            + "-fx-font-size:14px;-fx-font-weight:bold;-fx-background-radius:8px;"
            + "-fx-padding:11px 20px;-fx-cursor:hand;"));
        VBox.setMargin(registerBtn, new Insets(18, 0, 0, 0));

        // ── Back to login ─────────────────────────────────────────────────────────
        HBox loginRow = new HBox(4);
        loginRow.setAlignment(Pos.CENTER);
        VBox.setMargin(loginRow, new Insets(14, 0, 0, 0));
        Label loginPrompt = new Label("Already have an account?");
        loginPrompt.setFont(Font.font("SansSerif", 12));
        loginPrompt.setTextFill(Color.web("#4a6080"));
        loginLink = new Hyperlink("Sign in");
        loginLink.setStyle("-fx-text-fill:#00a8e8;-fx-border-color:transparent;-fx-font-size:12px;");
        loginRow.getChildren().addAll(loginPrompt, loginLink);

        inner.getChildren().addAll(
            title, subtitle,
            fullNameGroup,   spacer(12),
            usernameGroup,   spacer(12),
            emailGroup,      spacer(12),
            passwordGroup,   spacer(12),
            confirmGroup,    spacer(12),
            roleGroup,
            registerBtn,
            loginRow
        );

        ScrollPane scroll = new ScrollPane(inner);
        scroll.setPrefSize(480, 720);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color:#0d1b2a;-fx-background:#0d1b2a;");
        return scroll;
    }

    // ── Actions ──────────────────────────────────────────────────────────────────
    private void wireActions() {
        loginLink.setOnAction(e -> new LoginPage(stage).show());
        registerBtn.setOnAction(e -> handleRegister());
    }

    private void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm  = confirmField.getText();
        String role     = roleBox.getValue();

        // ── Step 1: full validation (popup on any error) ──────────────────────────
        String err = Validator.validateRegistration(username, password, confirm, email, fullName, role);
        if (err != null) {
            showPopup("Validation Error", err, Alert.AlertType.WARNING);
            return;
        }

        // ── Step 2: case-sensitive duplicate username check ───────────────────────
        if (db.usernameExists(username)) {
            showPopup("Username Taken",
                "The username \"" + username + "\" is already registered.\n\n"
                + "Note: Usernames are case-sensitive.\n"
                + "\"" + username + "\" and \"" + username.toUpperCase()
                + "\" are treated as different usernames.",
                Alert.AlertType.WARNING);
            usernameField.requestFocus();
            return;
        }

        // ── Step 3: email duplicate check ────────────────────────────────────────
        if (db.emailExists(email)) {
            showPopup("Email Already Used",
                "An account with the email \"" + email + "\" already exists.\n"
                + "Please use a different email address.",
                Alert.AlertType.WARNING);
            emailField.requestFocus();
            return;
        }

        // ── Step 4: save ─────────────────────────────────────────────────────────
        boolean saved = db.createUser(username, password, email, fullName, role);
        if (!saved) {
            showPopup("Registration Failed",
                "Account could not be created due to a database error.\n"
                + "Please try again.", Alert.AlertType.ERROR);
            return;
        }

        // ── Step 5: success popup ─────────────────────────────────────────────────
        showPopup("Account Created!",
            "Welcome to GymLogix, " + fullName + "!\n\n"
            + "Your account has been created with the role: " + role.toUpperCase() + "\n"
            + "You will now be redirected to the dashboard.",
            Alert.AlertType.INFORMATION);

        registerBtn.setDisable(true);

        // ── Step 6: auto-login and redirect ──────────────────────────────────────
        new Thread(() -> {
            try { Thread.sleep(400); } catch (InterruptedException ignored) {}
            Platform.runLater(() -> {
                UserModel user = db.getUserByUsername(username);
                if (user != null) {
                    Session.setCurrentUser(user);
                    db.logActivity(user.getId(), "REGISTER", "New account: " + username + " (" + role + ")");
                    new DashboardPage(stage).show();
                }
            });
        }).start();
    }

    // ── UI helpers ────────────────────────────────────────────────────────────────
    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefHeight(40);
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
        return tf;
    }

    private PasswordField styledPassField(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setPrefHeight(40);
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
        return pf;
    }

    private Label hintLabel() {
        Label l = new Label("");
        l.setVisible(false);
        l.setStyle("-fx-font-size:11px;");
        return l;
    }

    private void hint(Label lbl, String msg, boolean ok) {
        lbl.setText(msg);
        lbl.setStyle(ok
            ? "-fx-font-size:11px;-fx-text-fill:#2ecc71;"
            : "-fx-font-size:11px;-fx-text-fill:#e74c3c;");
        lbl.setVisible(true);
    }

    private VBox fieldGroup(String labelText, Control field, Label hint) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#7ec8e3;");
        VBox group = new VBox(5, lbl, field);
        if (hint != null) group.getChildren().add(hint);
        return group;
    }

    private Region spacer(double h) {
        Region r = new Region();
        r.setPrefHeight(h);
        return r;
    }

    private void showPopup(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.initOwner(stage);
        a.showAndWait();
    }
}

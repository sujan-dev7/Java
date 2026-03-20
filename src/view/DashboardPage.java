package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.Session;
import model.UserModel;
import model.dbconnection;

import java.util.ArrayList;
import java.util.List;

/**
 * DashboardPage — dark-blue shell.
 * Delegates every section to its own panel class.
 */
public class DashboardPage {

    private final Stage        stage;
    private final dbconnection db = new dbconnection();

    // sidebar buttons
    private Button btnOverview, btnEquipment, btnMaintenance;
    private Button btnUsers, btnReports;
    private Button btnLogout;

    // content area + panels
    private StackPane        contentArea;
    private OverviewPanel    overviewPanel;
    private EquipmentPanel   equipmentPanel;
    private MaintenancePanel maintenancePanel;
    private UsersPanel       usersPanel;
    private ReportsPanel     reportsPanel;
    private ActivityPanel    activityPanel;

    public DashboardPage(Stage stage) { this.stage = stage; }

    // ── show ──────────────────────────────────────────────────────────────────────
    public void show() {
        overviewPanel    = new OverviewPanel(db);
        equipmentPanel   = new EquipmentPanel(db, stage, () -> overviewPanel.refresh());
        maintenancePanel = new MaintenancePanel(db, stage);
        activityPanel    = new ActivityPanel(db);

        BorderPane root = new BorderPane();
        root.setTop(buildTopBar());
        root.setLeft(buildSidebar());
        root.setCenter(buildContentArea());

        Scene scene = new Scene(root, 1200, 720);
        scene.getStylesheets().add(
            getClass().getResource("/application/application.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("GymLogix — Equipment Management");
        stage.setResizable(true);
        stage.setMinWidth(1000);
        stage.setMinHeight(620);
        stage.show();

        overviewPanel.refresh();
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  TOP BAR
    // ════════════════════════════════════════════════════════════════════════════
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setPrefHeight(58);
        bar.setStyle("-fx-background-color:#091524;"
            + "-fx-border-color:#1e3a5f;-fx-border-width:0 0 1 0;");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 24, 0, 24));

        Label logo = new Label("⚡ GymLogix");
        logo.setFont(Font.font("SansSerif", FontWeight.BOLD, 19));
        logo.setTextFill(Color.web("#00a8e8"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        UserModel user = Session.getCurrentUser();
        String displayName = (user.getFullName() != null && !user.getFullName().isEmpty())
            ? user.getFullName() : user.getUsername();

        Label userName = new Label(displayName);
        userName.setFont(Font.font("SansSerif", 13));
        userName.setTextFill(Color.web("#7ec8e3"));

        Label roleLbl = UIHelper.roleBadge(user.getRole());

        btnLogout = new Button("Sign Out");
        btnLogout.setStyle("-fx-background-color:transparent;-fx-text-fill:#4a6080;"
            + "-fx-border-color:#1e3a5f;-fx-border-width:1px;-fx-border-radius:6px;"
            + "-fx-padding:5px 14px;-fx-font-size:12px;-fx-cursor:hand;");
        btnLogout.setOnMouseEntered(e -> btnLogout.setStyle(
            "-fx-background-color:#1b2a3b;-fx-text-fill:#e0e8f0;"
            + "-fx-border-color:#2e4057;-fx-border-width:1px;-fx-border-radius:6px;"
            + "-fx-padding:5px 14px;-fx-font-size:12px;-fx-cursor:hand;"));
        btnLogout.setOnMouseExited(e -> btnLogout.setStyle(
            "-fx-background-color:transparent;-fx-text-fill:#4a6080;"
            + "-fx-border-color:#1e3a5f;-fx-border-width:1px;-fx-border-radius:6px;"
            + "-fx-padding:5px 14px;-fx-font-size:12px;-fx-cursor:hand;"));
        btnLogout.setOnAction(e -> handleLogout());

        HBox right = new HBox(14, userName, roleLbl, btnLogout);
        right.setAlignment(Pos.CENTER);
        bar.getChildren().addAll(logo, spacer, right);
        return bar;
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ════════════════════════════════════════════════════════════════════════════
    private VBox buildSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(215);
        sidebar.setStyle("-fx-background-color:#091524;"
            + "-fx-border-color:#1e3a5f;-fx-border-width:0 1 0 0;");
        sidebar.setPadding(new Insets(16, 0, 16, 0));

        btnOverview    = navBtn("📊   Dashboard");
        btnEquipment   = navBtn("🏋   Equipment");
        btnMaintenance = navBtn("🔧   Maintenance");

        sidebar.getChildren().addAll(
            navSection("OVERVIEW"),   btnOverview,
            navSection("MANAGEMENT"), btnEquipment, btnMaintenance
        );

        if (Session.isAdmin()) {
            btnUsers   = navBtn("👤   Users");
            btnReports = navBtn("📈   Reports");
            sidebar.getChildren().addAll(navSection("ADMIN"), btnUsers, btnReports);
        }

        Region grow = new Region();
        VBox.setVgrow(grow, Priority.ALWAYS);
        sidebar.getChildren().add(grow);

        // wire nav
        btnOverview.setOnAction(e    -> navigate("overview",    btnOverview));
        btnEquipment.setOnAction(e   -> navigate("equipment",   btnEquipment));
        btnMaintenance.setOnAction(e -> navigate("maintenance", btnMaintenance));
        if (Session.isAdmin()) {
            btnUsers.setOnAction(e   -> navigate("users",   btnUsers));
            btnReports.setOnAction(e -> navigate("reports", btnReports));
        }

        setActive(btnOverview);
        return sidebar;
    }

    // Navigate: switch pane + refresh data
    private void navigate(String pane, Button btn) {
        setActive(btn);
        showPane(pane);
        switch (pane) {
            case "overview"    -> overviewPanel.refresh();
            case "equipment"   -> equipmentPanel.refresh();
            case "maintenance" -> maintenancePanel.refresh();
            case "users"       -> { if (usersPanel   != null) usersPanel.refresh(); }
            case "reports"     -> { if (reportsPanel  != null) reportsPanel.refresh(); }
            case "activity"    -> activityPanel.refresh();
        }
    }

    private Button navBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle(navStyle(false));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnMouseEntered(e -> { if (!btn.getStyle().contains("0.18")) btn.setStyle(navHover()); });
        btn.setOnMouseExited(e  -> { if (!btn.getStyle().contains("0.18")) btn.setStyle(navStyle(false)); });
        return btn;
    }

    private Label navSection(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("SansSerif", FontWeight.BOLD, 10));
        lbl.setTextFill(Color.web("#2e4057"));
        lbl.setPadding(new Insets(18, 20, 5, 20));
        return lbl;
    }

    private void setActive(Button active) {
        List<Button> all = new ArrayList<>();
        all.add(btnOverview); all.add(btnEquipment); all.add(btnMaintenance);
        if (Session.isAdmin() && btnUsers != null) { all.add(btnUsers); all.add(btnReports); }
        for (Button b : all) {
            if (b != null) b.setStyle(b == active ? navActive() : navStyle(false));
        }
    }

    private String navStyle(boolean active) {
        return "-fx-background-color:transparent;-fx-text-fill:#4a6080;"
            + "-fx-font-size:13px;-fx-alignment:CENTER-LEFT;"
            + "-fx-padding:10px 20px;-fx-cursor:hand;";
    }

    private String navActive() {
        return "-fx-background-color:rgba(0,168,232,0.18);-fx-text-fill:#00a8e8;"
            + "-fx-font-size:13px;-fx-font-weight:bold;-fx-alignment:CENTER-LEFT;"
            + "-fx-padding:10px 20px;-fx-cursor:hand;"
            + "-fx-border-color:#00a8e8;-fx-border-width:0 0 0 3;";
    }

    private String navHover() {
        return "-fx-background-color:#1b2a3b;-fx-text-fill:#7ec8e3;"
            + "-fx-font-size:13px;-fx-alignment:CENTER-LEFT;"
            + "-fx-padding:10px 20px;-fx-cursor:hand;";
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  CONTENT AREA
    // ════════════════════════════════════════════════════════════════════════════
    private StackPane buildContentArea() {
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color:#0d1b2a;");
        contentArea.setPadding(new Insets(24));

        contentArea.getChildren().addAll(
            overviewPanel.getPane(),
            equipmentPanel.getPane(),
            maintenancePanel.getPane(),
            activityPanel.getPane()
        );

        if (Session.isAdmin()) {
            usersPanel   = new UsersPanel(db, stage);
            reportsPanel = new ReportsPanel(db, stage);
            contentArea.getChildren().addAll(usersPanel.getPane(), reportsPanel.getPane());
        }

        showPane("overview");
        return contentArea;
    }

    private void showPane(String name) {
        contentArea.getChildren().forEach(n -> n.setVisible(false));
        String id = switch (name) {
            case "equipment"   -> "pane-equipment";
            case "maintenance" -> "pane-maintenance";
            case "users"       -> "pane-users";
            case "reports"     -> "pane-reports";
            case "activity"    -> "pane-activity";
            default            -> "pane-overview";
        };
        contentArea.getChildren().stream()
            .filter(n -> id.equals(n.getId()))
            .findFirst()
            .ifPresent(n -> n.setVisible(true));
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  LOGOUT
    // ════════════════════════════════════════════════════════════════════════════
    private void handleLogout() {
        boolean confirmed = UIHelper.confirm(stage, "Sign Out",
            "Are you sure you want to sign out?");
        if (!confirmed) return;

        db.logActivity(Session.getCurrentUser().getId(), "LOGOUT",
            "Logged out: " + Session.getCurrentUser().getUsername());
        Session.logout();
        new LoginPage(stage).show();
    }
}

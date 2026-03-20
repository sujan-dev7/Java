package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * UIHelper — shared static widget builders.
 *
 * ════════════════════════════════════════════════
 *  HOW TO CHANGE TEXT COLOURS IN THIS FILE:
 * ════════════════════════════════════════════════
 *  Section titles (e.g. "Equipment Management")
 *    → sectionTitle()        COL_TEXT_PRIMARY
 *
 *  Stat card numbers (the big 0/12/5 etc.)
 *    → statCard()            the `color` parameter passed in
 *    → OverviewPanel.java    pass a different hex e.g. "#ffffff"
 *
 *  Stat card captions (e.g. "Total Equipment")
 *    → statCard()            COL_TEXT_MUTED  (change the string below)
 *
 *  Status text (Active / Maintenance / Retired)
 *    → statusBadge()         each case in the switch statement
 *
 *  Role text (ADMIN / STAFF)
 *    → roleBadge()           "admin" and "staff" branches
 *
 *  Maintenance status text (Scheduled / Completed etc.)
 *    → maintBadge()          each case in the switch statement
 *
 *  Dialog field labels (Name / Category / Serial Number etc.)
 *    → dlgGroup()            COL_LABEL
 *
 *  Report row text
 *    → rowLabel()            COL_ROW_TEXT
 *
 *  Sub-headings in reports
 *    → subHeading()          COL_TEXT_PRIMARY
 * ════════════════════════════════════════════════
 */
public class UIHelper {

    // ════════════════════════════════════════════════════════════════════════
    //  COLOUR CONSTANTS  — change any of these to update the whole UI at once
    // ════════════════════════════════════════════════════════════════════════

    /** Main body text — used for titles, section headings, sub-headings */
    public static final String COL_TEXT_PRIMARY = "#e8f4ff";

    /** Secondary text — column headers, captions, card labels */
    public static final String COL_TEXT_SECONDARY = "#7ec8e3";

    /** Muted text — small hints, stat card captions */
    public static final String COL_TEXT_MUTED = "#5a8aaa";

    /** Dialog field label colour */
    public static final String COL_LABEL = "#90c8e8";

    /** Report / activity log row text */
    public static final String COL_ROW_TEXT = "#b8d8f0";

    /** Accent (links, buttons, active nav) */
    public static final String COL_ACCENT = "#00a8e8";

    /** Status: Active */
    public static final String COL_SUCCESS = "#2ecc71";

    /** Status: Maintenance / Warning */
    public static final String COL_WARNING = "#f9c74f";

    /** Status: Retired / Danger */
    public static final String COL_DANGER = "#ff6b6b";

    /** Card / panel background */
    public static final String COL_PANEL = "#0e1f30";
    public static final String COL_BORDER = "#1e3a5f";

    // ════════════════════════════════════════════════════════════════════════
    //  WIDGETS
    // ════════════════════════════════════════════════════════════════════════

    // ── Section title ─────────────────────────────────────────────────────────
    public static Label sectionTitle(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("SansSerif", FontWeight.BOLD, 20));
        lbl.setTextFill(Color.web(COL_TEXT_PRIMARY));   // ← section title colour
        return lbl;
    }

    // ── Stat card ─────────────────────────────────────────────────────────────
    public static VBox statCard(Label valueLabel, String caption, String numberColor) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color:" + COL_PANEL + ";"
            + "-fx-border-color:" + COL_BORDER + ";"
            + "-fx-border-width:1px;-fx-border-radius:10px;-fx-background-radius:10px;"
            + "-fx-padding:18px;");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label cap = new Label(caption);
        cap.setStyle("-fx-font-size:12px;-fx-text-fill:" + COL_TEXT_MUTED + ";"); // ← caption colour

        valueLabel.setFont(Font.font("SansSerif", FontWeight.BOLD, 36));
        valueLabel.setTextFill(Color.web(numberColor));  // ← number colour (passed by caller)

        card.getChildren().addAll(cap, valueLabel);
        return card;
    }

    public static VBox reportCard(String caption, String value, String color) {
        Label val = new Label(value);
        return statCard(val, caption, color);
    }

    // ── Status badge (text colour only) ───────────────────────────────────────
    public static Label statusBadge(String status) {
        Label badge = new Label(status);
        String textColor = switch (status) {
            case "Active"      -> COL_SUCCESS;   // ← Active colour    (#2ecc71 green)
            case "Maintenance" -> COL_WARNING;   // ← Maintenance colour (#f9c74f yellow)
            case "Retired"     -> COL_DANGER;    // ← Retired colour   (#ff6b6b red)
            default            -> COL_TEXT_MUTED;
        };
        badge.setStyle("-fx-text-fill:" + textColor + ";"
            + "-fx-font-size:12px;-fx-font-weight:bold;");
        return badge;
    }

    // ── Role badge (text colour only) ─────────────────────────────────────────
    public static Label roleBadge(String role) {
        Label badge = new Label(role.toUpperCase());
        String textColor = "admin".equals(role)
            ? COL_WARNING    // ← Admin role colour  (#f9c74f yellow)
            : COL_SUCCESS;   // ← Staff role colour  (#2ecc71 green)
        badge.setStyle("-fx-text-fill:" + textColor + ";"
            + "-fx-font-size:12px;-fx-font-weight:bold;");
        return badge;
    }

    // ── Maintenance status badge (text colour only) ───────────────────────────
    public static Label maintBadge(String status) {
        Label badge = new Label(status);
        String textColor = switch (status) {
            case "Completed"  -> COL_SUCCESS;   // ← Completed colour
            case "Scheduled"  -> COL_WARNING;   // ← Scheduled colour
            case "Cancelled"  -> COL_DANGER;    // ← Cancelled colour
            default           -> COL_ACCENT;    // ← In Progress colour
        };
        badge.setStyle("-fx-text-fill:" + textColor + ";"
            + "-fx-font-size:12px;-fx-font-weight:bold;");
        return badge;
    }

    // ── Primary button ────────────────────────────────────────────────────────
    public static Button primaryBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color:#00a8e8;-fx-text-fill:#ffffff;"
            + "-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:6px;"
            + "-fx-padding:9px 18px;-fx-cursor:hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color:#007bb5;-fx-text-fill:#ffffff;"
            + "-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:6px;"
            + "-fx-padding:9px 18px;-fx-cursor:hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color:#00a8e8;-fx-text-fill:#ffffff;"
            + "-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:6px;"
            + "-fx-padding:9px 18px;-fx-cursor:hand;"));
        return btn;
    }

    // ── Secondary (outline) button ────────────────────────────────────────────
    public static Button secondaryBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color:transparent;-fx-text-fill:#00d4ff;"
            + "-fx-border-color:#00a8e8;-fx-border-width:1.5px;"
            + "-fx-border-radius:6px;-fx-background-radius:6px;"
            + "-fx-font-size:12px;-fx-padding:7px 14px;-fx-cursor:hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color:rgba(0,168,232,0.12);-fx-text-fill:#00d4ff;"
            + "-fx-border-color:#00a8e8;-fx-border-width:1.5px;"
            + "-fx-border-radius:6px;-fx-background-radius:6px;"
            + "-fx-font-size:12px;-fx-padding:7px 14px;-fx-cursor:hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color:transparent;-fx-text-fill:#00d4ff;"
            + "-fx-border-color:#00a8e8;-fx-border-width:1.5px;"
            + "-fx-border-radius:6px;-fx-background-radius:6px;"
            + "-fx-font-size:12px;-fx-padding:7px 14px;-fx-cursor:hand;"));
        return btn;
    }

    // ── Danger (red outline) button ───────────────────────────────────────────
    public static Button dangerBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color:transparent;-fx-text-fill:#ff6b6b;"
            + "-fx-border-color:#e74c3c;-fx-border-width:1.5px;"
            + "-fx-border-radius:6px;-fx-background-radius:6px;"
            + "-fx-font-size:12px;-fx-padding:5px 12px;-fx-cursor:hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color:rgba(231,76,60,0.12);-fx-text-fill:#ff8e8e;"
            + "-fx-border-color:#e74c3c;-fx-border-width:1.5px;"
            + "-fx-border-radius:6px;-fx-background-radius:6px;"
            + "-fx-font-size:12px;-fx-padding:5px 12px;-fx-cursor:hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color:transparent;-fx-text-fill:#ff6b6b;"
            + "-fx-border-color:#e74c3c;-fx-border-width:1.5px;"
            + "-fx-border-radius:6px;-fx-background-radius:6px;"
            + "-fx-font-size:12px;-fx-padding:5px 12px;-fx-cursor:hand;"));
        return btn;
    }

    // ── Dialog text field ─────────────────────────────────────────────────────
    public static TextField dlgField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefHeight(38);
        tf.setPrefWidth(Double.MAX_VALUE);
        tf.setStyle("-fx-background-color:#1b2a3b;-fx-border-color:#2e4057;"
            + "-fx-border-width:1.5px;-fx-border-radius:6px;-fx-background-radius:6px;"
            + "-fx-text-fill:#ffffff;"            // ← dialog input text colour
            + "-fx-prompt-text-fill:#4a6080;"     // ← dialog placeholder colour
            + "-fx-font-size:13px;");
        tf.focusedProperty().addListener((o, ov, f) ->
            tf.setStyle(f
                ? "-fx-background-color:#1b2a3b;-fx-border-color:#00a8e8;"
                  + "-fx-border-width:2px;-fx-border-radius:6px;-fx-background-radius:6px;"
                  + "-fx-text-fill:#ffffff;-fx-prompt-text-fill:#4a6080;-fx-font-size:13px;"
                : "-fx-background-color:#1b2a3b;-fx-border-color:#2e4057;"
                  + "-fx-border-width:1.5px;-fx-border-radius:6px;-fx-background-radius:6px;"
                  + "-fx-text-fill:#ffffff;-fx-prompt-text-fill:#4a6080;-fx-font-size:13px;"));
        return tf;
    }

    // ── Dialog label + control group ──────────────────────────────────────────
    public static VBox dlgGroup(String labelText, Control control) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:"
            + COL_LABEL + ";");                    // ← field label colour
        return new VBox(5, lbl, control);
    }

    // ── Button row ────────────────────────────────────────────────────────────
    public static HBox dialogBtnRow(Button save, Button cancel) {
        HBox row = new HBox(10, save, cancel);
        row.setAlignment(Pos.CENTER_RIGHT);
        return row;
    }

    // ── Alert dialogs ─────────────────────────────────────────────────────────
    public static void showAlert(Stage owner, String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        if (owner != null) a.initOwner(owner);
        a.showAndWait();
    }

    public static void showInfo(Stage owner, String msg) {
        showAlert(owner, "Success", msg, Alert.AlertType.INFORMATION);
    }

    public static boolean confirm(Stage owner, String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        if (owner != null) a.initOwner(owner);
        return a.showAndWait().map(r -> r == ButtonType.OK).orElse(false);
    }

    // ── Row / sub-heading labels ──────────────────────────────────────────────
    public static Label rowLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:12px;-fx-text-fill:" + COL_ROW_TEXT + ";"); // ← row text colour
        l.setWrapText(true);
        return l;
    }

    public static Label subHeading(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("SansSerif", FontWeight.BOLD, 14));
        lbl.setTextFill(Color.web(COL_TEXT_PRIMARY));   // ← sub-heading colour
        VBox.setMargin(lbl, new Insets(14, 0, 6, 0));
        return lbl;
    }
}
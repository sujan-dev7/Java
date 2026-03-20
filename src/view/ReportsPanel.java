package view;

import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.dbconnection;

/**
 * ReportsPanel — admin-only analytics panel (dark theme).
 */
public class ReportsPanel {

    private final dbconnection db;
    private final Stage        owner;
    private VBox               pane;

    public ReportsPanel(dbconnection db, Stage owner) {
        this.db = db; this.owner = owner;
        build();
    }

    private void build() {
        pane = new VBox(16);
        pane.setId("pane-reports");
        pane.getChildren().add(UIHelper.sectionTitle("Reports & Analytics"));
    }

    public void refresh() {
        while (pane.getChildren().size() > 1)
            pane.getChildren().remove(1);

        // ── stat cards ────────────────────────────────────────────────────────────
        HBox stats = new HBox(16,
            UIHelper.reportCard("Total Equipment",
                String.valueOf(db.countAllEquipment()),                 "#00a8e8"),
            UIHelper.reportCard("Active",
                String.valueOf(db.countEquipmentByStatus("Active")),    "#2ecc71"),
            UIHelper.reportCard("Maintenance",
                String.valueOf(db.countEquipmentByStatus("Maintenance")),"#f39c12"),
            UIHelper.reportCard("Retired",
                String.valueOf(db.countEquipmentByStatus("Retired")),   "#e74c3c")
        );
        pane.getChildren().add(stats);

        // ── recent maintenance ────────────────────────────────────────────────────
        pane.getChildren().add(UIHelper.subHeading("Recent Maintenance Logs"));
        try {
            ResultSet rs = db.getAllMaintenanceLogs();
            boolean any = false;
            if (rs != null) {
                int count = 0;
                while (rs.next() && count++ < 8) {
                    any = true;
                    pane.getChildren().add(UIHelper.rowLabel(
                        "•  " + rs.getString("equipment_name")
                        + "   [" + rs.getString("maintenance_type") + "]   "
                        + rs.getString("status")
                        + "   —   " + rs.getString("scheduled_date")));
                }
                rs.close();
            }
            if (!any) pane.getChildren().add(UIHelper.rowLabel("No maintenance logs yet."));
        } catch (SQLException e) { e.printStackTrace(); }

        // ── recent activity ───────────────────────────────────────────────────────
        pane.getChildren().add(UIHelper.subHeading("Recent Activity"));
        try {
            ResultSet rs = db.getRecentActivityLogs(15);
            boolean any = false;
            if (rs != null) {
                while (rs.next()) {
                    any = true;
                    String detail = rs.getString("details");
                    pane.getChildren().add(UIHelper.rowLabel(
                        "•  " + rs.getTimestamp("created_at")
                        + "   " + rs.getString("full_name")
                        + "   →   " + rs.getString("action")
                        + (detail != null ? ":  " + detail : "")));
                }
                rs.close();
            }
            if (!any) pane.getChildren().add(UIHelper.rowLabel("No activity recorded yet."));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public VBox getPane() { return pane; }

	public Stage getOwner() {
		return owner;
	}
}

package view;

import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.dbconnection;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ActivityPanel — shows the most recent 50 activity log entries (dark theme).
 */
public class ActivityPanel {

    private final dbconnection db;
    private ListView<String>   logList;
    private VBox               pane;

    public ActivityPanel(dbconnection db) {
        this.db = db;
        build();
    }

    private void build() {
        pane = new VBox(14);
        pane.setId("pane-activity");
        pane.getChildren().add(UIHelper.sectionTitle("Activity Log"));

        logList = new ListView<>();
        VBox.setVgrow(logList, Priority.ALWAYS);
        pane.getChildren().add(logList);
    }

    public void refresh() {
        logList.getItems().clear();
        try {
            ResultSet rs = db.getRecentActivityLogs(50);
            if (rs == null) return;
            while (rs.next()) {
                String detail = rs.getString("details");
                logList.getItems().add(
                    rs.getTimestamp("created_at") + "   |   "
                    + rs.getString("full_name")   + "   →   "
                    + rs.getString("action")
                    + (detail != null ? ":   " + detail : ""));
            }
            rs.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public VBox getPane() { return pane; }
}

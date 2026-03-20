package view;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.dbconnection;

public class OverviewPanel {
    private final dbconnection db;
    private Label statTotal, statActive, statMaint, statRetired;
    private VBox pane;

    public OverviewPanel(dbconnection db) {
        this.db = db;
        build();
    }

    private void build() {
        pane = new VBox(20);
        pane.setId("pane-overview");
        pane.getChildren().add(UIHelper.sectionTitle("Dashboard Overview"));

        statTotal   = new Label("0");
        statActive  = new Label("0");
        statMaint   = new Label("0");
        statRetired = new Label("0");

        HBox stats = new HBox(16,
            UIHelper.statCard(statTotal,   "Total Equipment", "#00a8e8"),
            UIHelper.statCard(statActive,  "Active",          "#2ecc71"),
            UIHelper.statCard(statMaint,   "Maintenance",     "#f39c12"),
            UIHelper.statCard(statRetired, "Retired",         "#e74c3c")
        );
        pane.getChildren().add(stats);
    }

    public void refresh() {
        statTotal.setText(String.valueOf(db.countAllEquipment()));
        statActive.setText(String.valueOf(db.countEquipmentByStatus("Active")));
        statMaint.setText(String.valueOf(db.countEquipmentByStatus("Maintenance")));
        statRetired.setText(String.valueOf(db.countEquipmentByStatus("Retired")));
    }

    public VBox getPane() { return pane; }
}

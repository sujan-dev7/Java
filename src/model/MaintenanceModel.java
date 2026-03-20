package model;

public class MaintenanceModel {
    private int    id;
    private int    equipmentId;
    private String equipmentName;
    private int    performedBy;
    private String performedByName;
    private String maintenanceType;
    private String description;
    private String scheduledDate;
    private String completedDate;
    private String status;

    public MaintenanceModel() {}

    public MaintenanceModel(int equipmentId, int performedBy, String maintenanceType,
                            String description, String scheduledDate, String status) {
        this.equipmentId = equipmentId; this.performedBy = performedBy;
        this.maintenanceType = maintenanceType; this.description = description;
        this.scheduledDate = scheduledDate; this.status = status;
    }

    public int    getId()               { return id; }
    public int    getEquipmentId()      { return equipmentId; }
    public String getEquipmentName()    { return equipmentName; }
    public int    getPerformedBy()      { return performedBy; }
    public String getPerformedByName()  { return performedByName; }
    public String getMaintenanceType()  { return maintenanceType; }
    public String getDescription()      { return description; }
    public String getScheduledDate()    { return scheduledDate; }
    public String getCompletedDate()    { return completedDate; }
    public String getStatus()           { return status; }

    public void setId(int id)                    { this.id = id; }
    public void setEquipmentId(int e)            { this.equipmentId = e; }
    public void setEquipmentName(String n)       { this.equipmentName = n; }
    public void setPerformedBy(int p)            { this.performedBy = p; }
    public void setPerformedByName(String n)     { this.performedByName = n; }
    public void setMaintenanceType(String t)     { this.maintenanceType = t; }
    public void setDescription(String d)         { this.description = d; }
    public void setScheduledDate(String d)       { this.scheduledDate = d; }
    public void setCompletedDate(String d)       { this.completedDate = d; }
    public void setStatus(String s)              { this.status = s; }
}

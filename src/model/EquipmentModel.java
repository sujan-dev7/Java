package model;

public class EquipmentModel {
    private int    id;
    private String name;
    private String category;
    private String serialNumber;
    private String purchaseDate;
    private String status;
    private String notes;
    private int    addedBy;
    private String addedByName;

    public EquipmentModel() {}

    public EquipmentModel(String name, String category, String serialNumber,
                          String purchaseDate, String status, String notes, int addedBy) {
        this.name = name; this.category = category; this.serialNumber = serialNumber;
        this.purchaseDate = purchaseDate; this.status = status;
        this.notes = notes; this.addedBy = addedBy;
    }

    public int    getId()           { return id; }
    public String getName()         { return name; }
    public String getCategory()     { return category; }
    public String getSerialNumber() { return serialNumber; }
    public String getPurchaseDate() { return purchaseDate; }
    public String getStatus()       { return status; }
    public String getNotes()        { return notes; }
    public int    getAddedBy()      { return addedBy; }
    public String getAddedByName()  { return addedByName; }

    public void setId(int id)                { this.id = id; }
    public void setName(String n)            { this.name = n; }
    public void setCategory(String c)        { this.category = c; }
    public void setSerialNumber(String s)    { this.serialNumber = s; }
    public void setPurchaseDate(String d)    { this.purchaseDate = d; }
    public void setStatus(String s)          { this.status = s; }
    public void setNotes(String n)           { this.notes = n; }
    public void setAddedBy(int a)            { this.addedBy = a; }
    public void setAddedByName(String n)     { this.addedByName = n; }

    @Override public String toString() { return id + " — " + name; }
}

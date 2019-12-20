package org.altbeacon.beacon;

public class iBeaconData {
    private String Name;
    private String UUID;
    private int level;

    public iBeaconData() {
    }

    public iBeaconData(String name, String UUID) {
        Name = name;
        this.UUID = UUID;
    }

    public iBeaconData(String UUID, int level) {
        this.UUID = UUID;
        this.level = level;
    }

    public iBeaconData(String name, String UUID, int level) {
        Name = name;
        this.UUID = UUID;
        this.level = level;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "iBeaconData{" +
                "Name='" + Name + '\'' +
                ", UUID='" + UUID + '\'' +
                ", level=" + level +
                '}';
    }
}

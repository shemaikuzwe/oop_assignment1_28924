package com.auca.classroom;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;

public class Classroom implements Identifiable, Registrable {
    private final String classroomId;
    private String buildingName;
    private String roomNumber;
    private int capacity;
    private int floorNumber;
    private boolean available;

    public Classroom(String classroomId, String buildingName, String roomNumber, int capacity, int floorNumber,
            boolean available) {
        this.classroomId = classroomId;
        this.buildingName = buildingName;
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.floorNumber = floorNumber;
        this.available = available;
    }

    @Override
    public String getId() {
        return classroomId;
    }

    @Override
    public String getDisplayName() {
        return buildingName + " " + roomNumber;
    }

    @Override
    public boolean isActive() {
        return available;
    }

    @Override
    public void validate() throws ValidationException {
        Registrable.id(classroomId, "Classroom ID");
        Registrable.text(buildingName, "Building name", 2, 60);
        Registrable.text(roomNumber, "Room number", 1, 10);
        Registrable.range(capacity, 1, 1000, "Capacity");
        Registrable.range(floorNumber, 0, 100, "Floor number");
    }

    @Override
    public void printDetails() {
        System.out.println(this);
    }

    @Override
    public String getRegistrationSummary() {
        return Identifiable.label(this) + " - " + capacity + " seats, " + (available ? "available" : "unavailable");
    }

    @Override
    public String toString() {
        return String.format("""
                CLASSROOM
                  Classroom ID       : %s
                  Building           : %s
                  Room number        : %s
                  Capacity           : %d
                  Floor number       : %d
                  Available          : %s
                """, classroomId, buildingName, roomNumber, capacity, floorNumber, available ? "YES" : "NO");
    }

    public String getClassroomId() {
        return classroomId;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public boolean getAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}

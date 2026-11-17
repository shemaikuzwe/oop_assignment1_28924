package com.auca.department;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.teacher.Teacher;

public class Department implements Identifiable, Registrable {
    private final String departmentId;
    private String departmentName;
    private String departmentCode;
    private String officeLocation;
    private String contactEmail;
    private Teacher headTeacher; // optional: assigned once a Teacher exists

    public Department(String departmentId, String departmentName, String departmentCode, String officeLocation,
            String contactEmail, Teacher headTeacher) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.departmentCode = departmentCode;
        this.officeLocation = officeLocation;
        this.contactEmail = contactEmail;
        this.headTeacher = headTeacher;
    }

    @Override
    public String getId() {
        return departmentId;
    }

    @Override
    public String getDisplayName() {
        return departmentName;
    }

    @Override
    public boolean isActive() {
        return true; // departments have no status attribute
    }

    @Override
    public void validate() throws ValidationException {
        Registrable.id(departmentId, "Department ID");
        Registrable.name(departmentName, "Department name");
        Registrable.text(departmentCode, "Department code", 2, 10);
        Registrable.text(officeLocation, "Office location", 2, 60);
        Registrable.email(contactEmail, "Contact email");
    }

    @Override
    public void printDetails() {
        System.out.println(this);
    }

    @Override
    public String getRegistrationSummary() {
        return Identifiable.label(this) + " (" + departmentCode + ")";
    }

    @Override
    public String toString() {
        return String.format("""
                DEPARTMENT
                  Department ID      : %s
                  Name               : %s
                  Code               : %s
                  Office location    : %s
                  Contact email      : %s
                  Head teacher       : %s
                """, departmentId, departmentName, departmentCode, officeLocation, contactEmail,
                Identifiable.label(headTeacher));
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getOfficeLocation() {
        return officeLocation;
    }

    public void setOfficeLocation(String officeLocation) {
        this.officeLocation = officeLocation;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public Teacher getHeadTeacher() {
        return headTeacher;
    }

    public void setHeadTeacher(Teacher headTeacher) {
        this.headTeacher = headTeacher;
    }
}

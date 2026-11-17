package com.auca.administrator;

import com.auca.abstracts.Person;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Registrable;

import java.time.LocalDate;

public class Administrator extends Person {
    private final String staffNumber;
    private String jobTitle;
    private String assignedOffice;
    private LocalDate employmentDate;
    private int accessLevel;
    private boolean active;

    public Administrator(String personId, String firstName, String lastName, String email, String phoneNumber,
            LocalDate dateOfBirth, String staffNumber, String jobTitle, String assignedOffice,
            LocalDate employmentDate, int accessLevel, boolean active) {
        super(personId, firstName, lastName, email, phoneNumber, dateOfBirth);
        this.staffNumber = staffNumber;
        this.jobTitle = jobTitle;
        this.assignedOffice = assignedOffice;
        this.employmentDate = employmentDate;
        this.accessLevel = accessLevel;
        this.active = active;
    }

    @Override
    public String getRoleDescription() {
        return "Administrator - " + jobTitle;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        Registrable.id(staffNumber, "Staff number");
        Registrable.text(jobTitle, "Job title", 2, 60);
        Registrable.text(assignedOffice, "Assigned office", 1, 60);
        Registrable.notFuture(employmentDate, "Employment date");
        Registrable.range(accessLevel, 1, 5, "Access level");
    }

    @Override
    public String toString() {
        return super.toString() + String.format("""
                ADMINISTRATOR
                  Staff number       : %s
                  Job title          : %s
                  Assigned office    : %s
                  Employment date    : %s
                  Access level       : %d
                  Active             : %s
                """, staffNumber, jobTitle, assignedOffice, employmentDate, accessLevel, active ? "YES" : "NO");
    }

    public String getStaffNumber() {
        return staffNumber;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getAssignedOffice() {
        return assignedOffice;
    }

    public void setAssignedOffice(String assignedOffice) {
        this.assignedOffice = assignedOffice;
    }

    public LocalDate getEmploymentDate() {
        return employmentDate;
    }

    public void setEmploymentDate(LocalDate employmentDate) {
        this.employmentDate = employmentDate;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

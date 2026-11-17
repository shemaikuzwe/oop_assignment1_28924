package com.auca.abstracts;

import com.auca.department.Department;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;

import java.time.LocalDate;
import java.util.List;


public abstract class AcademicMember extends Person {
    public static final List<String> STATUSES = List.of("ACTIVE", "INACTIVE");

    private final String memberNumber;
    private Department department;
    private LocalDate joinDate;
    private String academicStatus;
    private String institutionalEmail;
    private String advisorName;

    protected AcademicMember(String personId, String firstName, String lastName, String email, String phoneNumber,
            LocalDate dateOfBirth, String memberNumber, Department department, LocalDate joinDate,
            String academicStatus, String institutionalEmail, String advisorName) {
        super(personId, firstName, lastName, email, phoneNumber, dateOfBirth);
        this.memberNumber = memberNumber;
        this.department = department;
        this.joinDate = joinDate;
        this.academicStatus = academicStatus;
        this.institutionalEmail = institutionalEmail;
        this.advisorName = advisorName;
    }

    public abstract String getAcademicCategory();

    @Override
    public boolean isActive() {
        return DEFAULT_STATUS.equals(academicStatus);
    }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        Registrable.id(memberNumber, "Member number");
        Registrable.required(department, "Department");
        Registrable.notFuture(joinDate, "Join date");
        Registrable.oneOf(academicStatus, STATUSES, "Academic status");
        Registrable.email(institutionalEmail, "Institutional email");
        Registrable.text(advisorName, "Advisor name", 2, 120);
    }

    @Override
    public String toString() {
        return super.toString() + String.format("""
                ACADEMIC MEMBER
                  Member number      : %s
                  Department         : %s
                  Join date          : %s
                  Academic status    : %s
                  Institutional email: %s
                  Advisor name       : %s
                  Category           : %s
                """, memberNumber, Identifiable.label(department), joinDate, academicStatus, institutionalEmail,
                advisorName, getAcademicCategory());
    }

    public String getMemberNumber() {
        return memberNumber;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public String getAcademicStatus() {
        return academicStatus;
    }

    public void setAcademicStatus(String academicStatus) {
        this.academicStatus = academicStatus;
    }

    public String getInstitutionalEmail() {
        return institutionalEmail;
    }

    public void setInstitutionalEmail(String institutionalEmail) {
        this.institutionalEmail = institutionalEmail;
    }

    public String getAdvisorName() {
        return advisorName;
    }

    public void setAdvisorName(String advisorName) {
        this.advisorName = advisorName;
    }
}

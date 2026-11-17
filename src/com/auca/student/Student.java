package com.auca.student;

import com.auca.abstracts.AcademicMember;
import com.auca.department.Department;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Registrable;

import java.time.LocalDate;

public class Student extends AcademicMember {
    private final String studentNumber;
    private String programName;
    private LocalDate admissionDate;
    private int studyYear;
    private String guardianPhone;
    private boolean scholarshipStatus;

    public Student(String personId, String firstName, String lastName, String email, String phoneNumber,
            LocalDate dateOfBirth, String memberNumber, Department department, LocalDate joinDate,
            String academicStatus, String institutionalEmail, String advisorName, String studentNumber,
            String programName, LocalDate admissionDate, int studyYear, String guardianPhone,
            boolean scholarshipStatus) {
        super(personId, firstName, lastName, email, phoneNumber, dateOfBirth, memberNumber, department, joinDate,
                academicStatus, institutionalEmail, advisorName);
        this.studentNumber = studentNumber;
        this.programName = programName;
        this.admissionDate = admissionDate;
        this.studyYear = studyYear;
        this.guardianPhone = guardianPhone;
        this.scholarshipStatus = scholarshipStatus;
    }

    @Override
    public String getRoleDescription() {
        return "Student of " + programName;
    }

    @Override
    public String getAcademicCategory() {
        return "Undergraduate - Year " + studyYear + (scholarshipStatus ? " (scholarship)" : "");
    }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        Registrable.id(studentNumber, "Student number");
        Registrable.text(programName, "Program name", 2, 120);
        Registrable.notFuture(admissionDate, "Admission date");
        Registrable.range(studyYear, 1, 6, "Study year");
        Registrable.phone(guardianPhone, "Guardian phone");
    }

    @Override
    public String toString() {
        return super.toString() + String.format("""
                STUDENT
                  Student number     : %s
                  Program name       : %s
                  Admission date     : %s
                  Study year         : %d
                  Guardian phone     : %s
                  Scholarship        : %s
                """, studentNumber, programName, admissionDate, studyYear, guardianPhone,
                scholarshipStatus ? "YES" : "NO");
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public LocalDate getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(LocalDate admissionDate) {
        this.admissionDate = admissionDate;
    }

    public int getStudyYear() {
        return studyYear;
    }

    public void setStudyYear(int studyYear) {
        this.studyYear = studyYear;
    }

    public String getGuardianPhone() {
        return guardianPhone;
    }

    public void setGuardianPhone(String guardianPhone) {
        this.guardianPhone = guardianPhone;
    }

    public boolean isScholarshipStatus() {
        return scholarshipStatus;
    }

    public void setScholarshipStatus(boolean scholarshipStatus) {
        this.scholarshipStatus = scholarshipStatus;
    }
}

package com.auca.teacher;

import com.auca.abstracts.AcademicMember;
import com.auca.department.Department;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Registrable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Teacher extends AcademicMember {
    public static final List<String> EMPLOYMENT_TYPES = List.of("FULL_TIME", "PART_TIME", "CONTRACT");

    private final String employeeNumber;
    private String specialization;
    private LocalDate hireDate;
    private BigDecimal salary;
    private String officeNumber;
    private String employmentType;

    public Teacher(String personId, String firstName, String lastName, String email, String phoneNumber,
            LocalDate dateOfBirth, String memberNumber, Department department, LocalDate joinDate,
            String academicStatus, String institutionalEmail, String advisorName, String employeeNumber,
            String specialization, LocalDate hireDate, BigDecimal salary, String officeNumber,
            String employmentType) {
        super(personId, firstName, lastName, email, phoneNumber, dateOfBirth, memberNumber, department, joinDate,
                academicStatus, institutionalEmail, advisorName);
        this.employeeNumber = employeeNumber;
        this.specialization = specialization;
        this.hireDate = hireDate;
        this.salary = salary;
        this.officeNumber = officeNumber;
        this.employmentType = employmentType;
    }

    @Override
    public String getRoleDescription() {
        return "Teacher of " + specialization;
    }

    @Override
    public String getAcademicCategory() {
        return "Faculty - " + employmentType;
    }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        Registrable.id(employeeNumber, "Employee number");
        Registrable.text(specialization, "Specialization", 2, 60);
        Registrable.notFuture(hireDate, "Hire date");
        Registrable.positive(salary, "Salary");
        Registrable.text(officeNumber, "Office number", 1, 20);
        Registrable.oneOf(employmentType, EMPLOYMENT_TYPES, "Employment type");
    }

    @Override
    public String toString() {
        return super.toString() + String.format("""
                TEACHER
                  Employee number    : %s
                  Specialization     : %s
                  Hire date          : %s
                  Salary             : %s
                  Office number      : %s
                  Employment type    : %s
                """, employeeNumber, specialization, hireDate, salary.toPlainString(), officeNumber, employmentType);
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public String getOfficeNumber() {
        return officeNumber;
    }

    public void setOfficeNumber(String officeNumber) {
        this.officeNumber = officeNumber;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }
}

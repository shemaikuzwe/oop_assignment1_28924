package com.auca.course;

import com.auca.department.Department;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.teacher.Teacher;

public class Course implements Identifiable, Registrable {
    private final String courseId;
    private String courseCode;
    private String courseTitle;
    private int creditHours;
    private Department department;
    private Teacher assignedTeacher;

    public Course(String courseId, String courseCode, String courseTitle, int creditHours, Department department,
            Teacher assignedTeacher) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.creditHours = creditHours;
        this.department = department;
        this.assignedTeacher = assignedTeacher;
    }

    @Override
    public String getId() {
        return courseId;
    }

    @Override
    public String getDisplayName() {
        return courseCode + " " + courseTitle;
    }

    @Override
    public boolean isActive() {
        return assignedTeacher != null && assignedTeacher.isActive();
    }

    @Override
    public void validate() throws ValidationException {
        Registrable.id(courseId, "Course ID");
        Registrable.text(courseCode, "Course code", 2, 15);
        Registrable.text(courseTitle, "Course title", 2, 120);
        Registrable.range(creditHours, 1, 6, "Credit hours");
        Registrable.required(department, "Department");
        Registrable.required(assignedTeacher, "Assigned teacher");
    }

    @Override
    public void printDetails() {
        System.out.println(this);
    }

    @Override
    public String getRegistrationSummary() {
        return Identifiable.label(this) + " - " + creditHours + " credits, " + assignedTeacher.getDisplayName();
    }

    @Override
    public String toString() {
        return String.format("""
                COURSE
                  Course ID          : %s
                  Code               : %s
                  Title              : %s
                  Credit hours       : %d
                  Department         : %s
                  Assigned teacher   : %s
                """, courseId, courseCode, courseTitle, creditHours, Identifiable.label(department),
                Identifiable.label(assignedTeacher));
    }

    public String getCourseId() {
        return courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public int getCreditHours() {
        return creditHours;
    }

    public void setCreditHours(int creditHours) {
        this.creditHours = creditHours;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Teacher getAssignedTeacher() {
        return assignedTeacher;
    }

    public void setAssignedTeacher(Teacher assignedTeacher) {
        this.assignedTeacher = assignedTeacher;
    }
}

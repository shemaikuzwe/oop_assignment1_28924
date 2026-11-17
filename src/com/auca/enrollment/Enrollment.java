package com.auca.enrollment;

import com.auca.course.Course;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.student.Student;

import java.time.LocalDate;
import java.util.List;

public class Enrollment implements Identifiable, Registrable {
    public static final List<String> SEMESTERS = List.of("FALL", "SPRING", "SUMMER");
    public static final List<String> STATUSES = List.of("ACTIVE", "COMPLETED", "DROPPED");

    private final String enrollmentId;
    private Student student;
    private Course course;
    private LocalDate enrollmentDate;
    private String semester;
    private String enrollmentStatus;

    public Enrollment(String enrollmentId, Student student, Course course, LocalDate enrollmentDate, String semester,
            String enrollmentStatus) {
        this.enrollmentId = enrollmentId;
        this.student = student;
        this.course = course;
        this.enrollmentDate = enrollmentDate;
        this.semester = semester;
        this.enrollmentStatus = enrollmentStatus;
    }

    @Override
    public String getId() {
        return enrollmentId;
    }

    @Override
    public String getDisplayName() {
        return student.getDisplayName() + " -> " + course.getCourseCode() + " (" + semester + ")";
    }

    @Override
    public boolean isActive() {
        return DEFAULT_STATUS.equals(enrollmentStatus);
    }

    @Override
    public void validate() throws ValidationException {
        Registrable.id(enrollmentId, "Enrollment ID");
        Registrable.required(student, "Student");
        Registrable.required(course, "Course");
        Registrable.notFuture(enrollmentDate, "Enrollment date");
        Registrable.oneOf(semester, SEMESTERS, "Semester");
        Registrable.oneOf(enrollmentStatus, STATUSES, "Enrollment status");
    }

    @Override
    public void printDetails() {
        System.out.println(this);
    }

    @Override
    public String getRegistrationSummary() {
        return Identifiable.label(this) + " - " + enrollmentStatus;
    }

    @Override
    public String toString() {
        return String.format("""
                ENROLLMENT
                  Enrollment ID      : %s
                  Student            : %s
                  Course             : %s
                  Enrollment date    : %s
                  Semester           : %s
                  Status             : %s
                """, enrollmentId, Identifiable.label(student), Identifiable.label(course), enrollmentDate, semester,
                enrollmentStatus);
    }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public void setEnrollmentStatus(String enrollmentStatus) {
        this.enrollmentStatus = enrollmentStatus;
    }
}

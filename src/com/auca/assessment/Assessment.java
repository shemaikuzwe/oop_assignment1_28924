package com.auca.assessment;

import com.auca.enrollment.Enrollment;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;

import java.time.LocalDate;
import java.util.List;

public class Assessment implements Identifiable, Registrable {
    public static final List<String> TYPES = List.of("QUIZ", "ASSIGNMENT", "MIDTERM", "FINAL", "PROJECT");

    private final String assessmentId;
    private Enrollment enrollment;
    private String assessmentType;
    private double maximumScore;
    private double obtainedScore;
    private LocalDate assessmentDate;

    public Assessment(String assessmentId, Enrollment enrollment, String assessmentType, double maximumScore,
            double obtainedScore, LocalDate assessmentDate) {
        this.assessmentId = assessmentId;
        this.enrollment = enrollment;
        this.assessmentType = assessmentType;
        this.maximumScore = maximumScore;
        this.obtainedScore = obtainedScore;
        this.assessmentDate = assessmentDate;
    }

    @Override
    public String getId() {
        return assessmentId;
    }

    @Override
    public String getDisplayName() {
        return assessmentType + " - " + enrollment.getCourse().getCourseCode();
    }

    @Override
    public boolean isActive() {
        return enrollment.isActive();
    }

    @Override
    public void validate() throws ValidationException {
        Registrable.id(assessmentId, "Assessment ID");
        Registrable.required(enrollment, "Enrollment");
        Registrable.oneOf(assessmentType, TYPES, "Assessment type");
        if (!(Double.isFinite(maximumScore) && maximumScore > 0)) {
            throw new ValidationException("Maximum score must be greater than zero.");
        }
        if (!(obtainedScore >= 0 && obtainedScore <= maximumScore)) {
            throw new ValidationException("Obtained score must be between 0 and " + maximumScore + ".");
        }
        Registrable.notFuture(assessmentDate, "Assessment date");
    }

    @Override
    public void printDetails() {
        System.out.println(this);
    }

    @Override
    public String getRegistrationSummary() {
        return Identifiable.label(this) + " - " + obtainedScore + "/" + maximumScore;
    }

    @Override
    public String toString() {
        return String.format("""
                ASSESSMENT
                  Assessment ID      : %s
                  Enrollment         : %s
                  Type               : %s
                  Maximum score      : %s
                  Obtained score     : %s
                  Assessment date    : %s
                """, assessmentId, Identifiable.label(enrollment), assessmentType, maximumScore, obtainedScore,
                assessmentDate);
    }

    public String getAssessmentId() {
        return assessmentId;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

    public String getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(String assessmentType) {
        this.assessmentType = assessmentType;
    }

    public double getMaximumScore() {
        return maximumScore;
    }

    public void setMaximumScore(double maximumScore) {
        this.maximumScore = maximumScore;
    }

    public double getObtainedScore() {
        return obtainedScore;
    }

    public void setObtainedScore(double obtainedScore) {
        this.obtainedScore = obtainedScore;
    }

    public LocalDate getAssessmentDate() {
        return assessmentDate;
    }

    public void setAssessmentDate(LocalDate assessmentDate) {
        this.assessmentDate = assessmentDate;
    }
}

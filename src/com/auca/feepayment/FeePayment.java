package com.auca.feepayment;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.student.Student;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class FeePayment implements Identifiable, Registrable {
    public static final List<String> METHODS = List.of("CASH", "BANK_TRANSFER", "MOBILE_MONEY", "CARD");

    private final String paymentId;
    private Student student;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String receiptNumber;

    public FeePayment(String paymentId, Student student, BigDecimal amount, LocalDate paymentDate,
            String paymentMethod, String receiptNumber) {
        this.paymentId = paymentId;
        this.student = student;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.receiptNumber = receiptNumber;
    }

    @Override
    public String getId() {
        return paymentId;
    }

    @Override
    public String getDisplayName() {
        return "Receipt " + receiptNumber + " - " + student.getDisplayName();
    }

    @Override
    public boolean isActive() {
        return true; // a recorded payment is always valid
    }

    @Override
    public void validate() throws ValidationException {
        Registrable.id(paymentId, "Payment ID");
        Registrable.required(student, "Student");
        Registrable.positive(amount, "Amount");
        Registrable.notFuture(paymentDate, "Payment date");
        Registrable.oneOf(paymentMethod, METHODS, "Payment method");
        Registrable.text(receiptNumber, "Receipt number", 1, 30);
    }

    @Override
    public void printDetails() {
        System.out.println(this);
    }

    @Override
    public String getRegistrationSummary() {
        return Identifiable.label(this) + " - " + amount.toPlainString() + " via " + paymentMethod + " on "
                + paymentDate;
    }

    @Override
    public String toString() {
        return String.format("""
                FEE PAYMENT
                  Payment ID         : %s
                  Student            : %s
                  Amount             : %s
                  Payment date       : %s
                  Payment method     : %s
                  Receipt number     : %s
                """, paymentId, Identifiable.label(student), amount.toPlainString(), paymentDate, paymentMethod,
                receiptNumber);
    }

    public String getPaymentId() {
        return paymentId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }
}

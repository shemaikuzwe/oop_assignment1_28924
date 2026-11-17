package com.auca.bookloan;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.librarybook.LibraryBook;
import com.auca.student.Student;

import java.time.LocalDate;

public class BookLoan implements Identifiable, Registrable {
    private final String loanId;
    private Student student;
    private LibraryBook book;
    private LocalDate borrowedDate;
    private LocalDate dueDate;
    private LocalDate returnedDate; // null until the book is returned

    public BookLoan(String loanId, Student student, LibraryBook book, LocalDate borrowedDate, LocalDate dueDate,
            LocalDate returnedDate) {
        this.loanId = loanId;
        this.student = student;
        this.book = book;
        this.borrowedDate = borrowedDate;
        this.dueDate = dueDate;
        this.returnedDate = returnedDate;
    }

    @Override
    public String getId() {
        return loanId;
    }

    @Override
    public String getDisplayName() {
        return book.getTitle() + " -> " + student.getDisplayName();
    }

    /** A loan is active while the book has not been returned. */
    @Override
    public boolean isActive() {
        return returnedDate == null;
    }

    @Override
    public void validate() throws ValidationException {
        Registrable.id(loanId, "Loan ID");
        Registrable.required(student, "Student");
        Registrable.required(book, "Book");
        Registrable.notFuture(borrowedDate, "Borrowed date");
        Registrable.required(dueDate, "Due date");
        if (!dueDate.isAfter(borrowedDate)) {
            throw new ValidationException("Due date must be after the borrowed date (" + borrowedDate + ").");
        }
        if (returnedDate != null) {
            Registrable.notFuture(returnedDate, "Returned date");
            if (returnedDate.isBefore(borrowedDate)) {
                throw new ValidationException("Returned date must not be before the borrowed date (" + borrowedDate + ").");
            }
        }
    }

    @Override
    public void printDetails() {
        System.out.println(this);
    }

    @Override
    public String getRegistrationSummary() {
        return Identifiable.label(this) + " - due " + dueDate + (isActive() ? " (on loan)" : " (returned " + returnedDate + ")");
    }

    @Override
    public String toString() {
        return String.format("""
                BOOK LOAN
                  Loan ID            : %s
                  Student            : %s
                  Book               : %s
                  Borrowed date      : %s
                  Due date           : %s
                  Returned date      : %s
                """, loanId, Identifiable.label(student), Identifiable.label(book), borrowedDate, dueDate,
                returnedDate == null ? "- (not returned)" : returnedDate);
    }

    public String getLoanId() {
        return loanId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public LibraryBook getBook() {
        return book;
    }

    public void setBook(LibraryBook book) {
        this.book = book;
    }

    public LocalDate getBorrowedDate() {
        return borrowedDate;
    }

    public void setBorrowedDate(LocalDate borrowedDate) {
        this.borrowedDate = borrowedDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnedDate() {
        return returnedDate;
    }

    public void setReturnedDate(LocalDate returnedDate) {
        this.returnedDate = returnedDate;
    }
}

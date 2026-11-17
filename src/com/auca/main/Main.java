package com.auca.main;

import com.auca.abstracts.AcademicMember;
import com.auca.abstracts.Person;
import com.auca.administrator.Administrator;
import com.auca.assessment.Assessment;
import com.auca.bookloan.BookLoan;
import com.auca.classroom.Classroom;
import com.auca.course.Course;
import com.auca.department.Department;
import com.auca.enrollment.Enrollment;
import com.auca.exceptions.BookUnavailableException;
import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.RecordNotFoundException;
import com.auca.exceptions.ValidationException;
import com.auca.feepayment.FeePayment;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.librarybook.LibraryBook;
import com.auca.student.Student;
import com.auca.teacher.Teacher;

import java.math.BigDecimal;
import java.time.format.DateTimeParseException;
import java.util.List;

public class Main {
    private static final List<Class<? extends Registrable>> TYPES = List.of(Department.class, Student.class,
            Teacher.class, Administrator.class, Course.class, Classroom.class, Enrollment.class, Assessment.class,
            FeePayment.class, LibraryBook.class, BookLoan.class);

    private static final Store store = new Store();
    private static final Forms forms = new Forms(store);

    public static void main(String[] args) {
        while (true) {
            printMenu();
            int choice = Console.choose("Select an option: ", 0, 19);
            if (choice == 0) {
                System.out.println("Goodbye.");
                return;
            }
            // Workflow boundary: every failure is reported and the menu continues.
            try {
                route(choice);
            } catch (PrerequisiteMissingException | RecordNotFoundException | ValidationException
                    | BookUnavailableException e) {
                System.out.println("! " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("! Unexpected problem (" + e.getClass().getSimpleName() + "): " + e.getMessage()
                        + ". Returning to the main menu.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("""

                ============== AUCA_ID SCHOOL MANAGEMENT ==============
                 1. Register Department       11. Borrow Library Book
                 2. Register Student          12. Return Library Book
                 3. Register Teacher          13. View All Records
                 4. Register Administrator    14. Search Record by ID
                 5. Register Course           15. Update Existing Record
                 6. Register Classroom        16. Student Academic Summary
                 7. Enroll Student            17. Student Payment History
                 8. Record Assessment         18. View Available Books
                 9. Record Fee Payment        19. Demonstrate Polymorphism
                10. Register Library Book      0. Exit""");
    }

    private static void route(int choice) throws PrerequisiteMissingException, RecordNotFoundException,
            ValidationException, BookUnavailableException {
        switch (choice) {
            case 1 -> register(forms.department);
            case 2 -> register(forms.student);
            case 3 -> register(forms.teacher);
            case 4 -> register(forms.administrator);
            case 5 -> register(forms.course);
            case 6 -> register(forms.classroom);
            case 7 -> register(forms.enrollment);
            case 8 -> register(forms.assessment);
            case 9 -> register(forms.feePayment);
            case 10 -> register(forms.libraryBook);
            case 11 -> register(forms.bookLoan);
            case 12 -> returnBook();
            case 13 -> viewAll();
            case 14 -> search();
            case 15 -> update();
            case 16 -> academicSummary();
            case 17 -> paymentHistory();
            case 18 -> availableBooks();
            case 19 -> demonstratePolymorphism();
            default -> System.out.println("! Unknown option " + choice + ".");
        }
    }

    private static void register(Form<?> form) throws PrerequisiteMissingException {
        while (true) {
            var saved = form.create();
            if (saved == null) {
                return;
            }
            int next = Console.choose("[1] Register another  [2] View " + form.name + "  [0] Main menu : ", 0, 2);
            if (next == 2) {
                saved.printDetails();
            }
            if (next != 1) {
                return;
            }
        }
    }

    
    private static void returnBook() throws RecordNotFoundException {
        List<BookLoan> active = store.all(BookLoan.class).stream().filter(BookLoan::isActive).toList();
        if (active.isEmpty()) {
            throw new RecordNotFoundException("No book is currently on loan. Borrow a book first (option 11).");
        }
        BookLoan loan = Console.pick("RETURN LIBRARY BOOK", active, BookLoan::getRegistrationSummary);
        if (loan == null) {
            return;
        }
        while (true) {
            String raw = Console.read("Returned date [LocalDate | YYYY-MM-DD] (CANCEL to abort) : ");
            if (raw.equalsIgnoreCase("CANCEL")) {
                return;
            }
            try {
                loan.setReturnedDate(Registrable.parseDate(raw));
                loan.validate();
                break;
            } catch (DateTimeParseException e) {
                System.out.println("! Invalid date: use YYYY-MM-DD (example: 2026-11-16).");
            } catch (ValidationException e) {
                System.out.println("! " + e.getMessage());
            }
            loan.setReturnedDate(null);
        }
        LibraryBook book = loan.getBook();
        if (!Console.confirm("Return '" + book.getTitle() + "' on " + loan.getReturnedDate() + "?")) {
            loan.setReturnedDate(null);
            System.out.println("x Return cancelled. Nothing changed.");
            return;
        }
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        System.out.println("+ Loan " + loan.getId() + " returned. Available copies: " + book.getAvailableCopies() + "/"
                + book.getTotalCopies());
    }

    private static void viewAll() {
        Console.title("ALL RECORDS");
        if (store.all(Identifiable.class).isEmpty()) {
            System.out.println("No records registered yet.");
        }
        for (Class<? extends Registrable> type : TYPES) {
            List<? extends Registrable> records = store.all(type);
            if (!records.isEmpty()) {
                System.out.println(type.getSimpleName() + " (" + records.size() + ")");
                records.forEach(r -> System.out.println("  " + r.getRegistrationSummary()));
            }
        }
        pause();
    }

    private static void search() throws RecordNotFoundException {
        Identifiable record = store.findById(Console.read("Enter record ID: "));
        if (record instanceof Registrable details) {
            details.printDetails();
        }
        pause();
    }

    private static void update() throws RecordNotFoundException {
        Identifiable record = store.findById(Console.read("Enter the ID of the record to update: "));
        System.out.println("Locked fields (IDs and fixed links) cannot be changed.");
        forms.formFor(record).update(record);
    }

    private static Student pickStudent(String title) throws RecordNotFoundException {
        List<Student> students = store.all(Student.class);
        if (students.isEmpty()) {
            throw new RecordNotFoundException("No student exists. Register a student first (option 2).");
        }
        return Console.pick(title, students, Student::getRegistrationSummary);
    }

    private static void academicSummary() throws RecordNotFoundException {
        Student student = pickStudent("STUDENT ACADEMIC SUMMARY");
        if (student == null) {
            return;
        }
        System.out.println(student.getRegistrationSummary() + " | " + student.getAcademicCategory());
        List<Enrollment> enrollments = store.all(Enrollment.class).stream()
                .filter(e -> e.getStudent() == student).toList();
        if (enrollments.isEmpty()) {
            System.out.println("  No enrollments yet.");
        }
        for (Enrollment e : enrollments) {
            System.out.println("  " + Identifiable.label(e.getCourse()) + " | " + e.getSemester() + " | "
                    + e.getEnrollmentStatus());
            List<Assessment> results = store.all(Assessment.class).stream()
                    .filter(a -> a.getEnrollment() == e).toList();
            double obtained = 0;
            double maximum = 0;
            for (Assessment a : results) {
                System.out.printf("      %-10s %6.1f / %-6.1f (%s)%n", a.getAssessmentType(), a.getObtainedScore(),
                        a.getMaximumScore(), a.getAssessmentDate());
                obtained += a.getObtainedScore();
                maximum += a.getMaximumScore();
            }
            System.out.println(results.isEmpty() ? "      No assessments yet."
                    : String.format("      Course score: %.1f%%", obtained * 100 / maximum));
        }
        pause();
    }

    private static void paymentHistory() throws RecordNotFoundException {
        Student student = pickStudent("STUDENT PAYMENT HISTORY");
        if (student == null) {
            return;
        }
        List<FeePayment> payments = store.all(FeePayment.class).stream()
                .filter(p -> p.getStudent() == student).toList();
        System.out.println(student.getRegistrationSummary());
        payments.forEach(p -> System.out.println("  " + p.getRegistrationSummary() + " | receipt "
                + p.getReceiptNumber()));
        BigDecimal total = payments.stream().map(FeePayment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println(payments.isEmpty() ? "  No payments recorded." : "  Total paid: " + total.toPlainString());
        pause();
    }

    private static void availableBooks() {
        Console.title("AVAILABLE BOOKS");
        List<LibraryBook> books = store.all(LibraryBook.class).stream().filter(LibraryBook::isActive).toList();
        if (books.isEmpty()) {
            System.out.println("No books with available copies.");
        }
        books.forEach(b -> System.out.println("  " + b.getRegistrationSummary()));
        pause();
    }

    private static void demonstratePolymorphism() {
        Console.title("SUPERCLASS POLYMORPHISM: List<Person>");
        List<Person> people = store.all(Person.class);
        if (people.isEmpty()) {
            System.out.println("Register a Student, Teacher or Administrator to see this part.");
        }
        for (Person p : people) {
            // getRoleDescription() resolves to the Student/Teacher/Administrator override at runtime.
            String category = p instanceof AcademicMember m ? " | " + m.getAcademicCategory() : "";
            System.out.printf("  %-14s %-22s -> %s%s%n", p.getClass().getSimpleName(), p.getDisplayName(),
                    p.getRoleDescription(), category);
        }

        Console.title("INTERFACE POLYMORPHISM: List<Identifiable>");
        List<Identifiable> records = store.all(Identifiable.class);
        if (records.isEmpty()) {
            System.out.println("No records registered yet.");
        }
        for (Identifiable r : records) {
            System.out.printf("  %-14s %-12s %-35s active=%s%n", r.getClass().getSimpleName(), r.getId(),
                    r.getDisplayName(), r.isActive());
        }

        System.out.println("""

                Why Person and AcademicMember cannot be instantiated:
                  They are abstract. Person leaves getRoleDescription() and AcademicMember leaves
                  getAcademicCategory() without a body, so only concrete subclasses (Student, Teacher,
                  Administrator) that supply those behaviours can be created. Their constructors are
                  protected and run through super(...) chaining: Student -> AcademicMember -> Person.""");
        pause();
    }

    private static void pause() {
        Console.read("\nPress Enter to return to the main menu...");
    }
}

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
import com.auca.exceptions.DuplicateRecordException;
import com.auca.exceptions.ValidationException;
import com.auca.feepayment.FeePayment;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.librarybook.LibraryBook;
import com.auca.student.Student;
import com.auca.teacher.Teacher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


final class Forms {
    private static final String ID = "String | unique | 4-30 chars";
    private static final String NAME = "String | 2-60 chars";
    private static final String DATE = "LocalDate | YYYY-MM-DD";
    private static final String PHONE = "String | 10-15 digits";

    private static final Form.Parser PAST = (raw, f) -> Registrable.past(Registrable.parseDate(raw), f);
    private static final Form.Parser NOT_FUTURE = (raw, f) -> Registrable.notFuture(Registrable.parseDate(raw), f);
    private static final Form.Parser ANY_DATE = (raw, f) -> Registrable.parseDate(raw);
    private static final Form.Parser MONEY = (raw, f) -> Registrable.positive(new BigDecimal(raw), f);
    private static final Form.Parser SCORE = (raw, f) -> {
        double score = Double.parseDouble(raw);
        if (!Double.isFinite(score) || score < 0) {
            throw new ValidationException(f + " must be zero or greater.");
        }
        return score;
    };

    private final Store store;
    final Form<Department> department;
    final Form<Student> student;
    final Form<Teacher> teacher;
    final Form<Administrator> administrator;
    final Form<Course> course;
    final Form<Classroom> classroom;
    final Form<Enrollment> enrollment;
    final Form<Assessment> assessment;
    final Form<FeePayment> feePayment;
    final Form<LibraryBook> libraryBook;
    final Form<BookLoan> bookLoan;

    Forms(Store store) {
        this.store = store;
        department = departmentForm();
        student = studentForm();
        teacher = teacherForm();
        administrator = administratorForm();
        course = courseForm();
        classroom = classroomForm();
        enrollment = enrollmentForm();
        assessment = assessmentForm();
        feePayment = feePaymentForm();
        libraryBook = libraryBookForm();
        bookLoan = bookLoanForm();
    }

    Form<?> formFor(Identifiable record) {
        return List.of(department, student, teacher, administrator, course, classroom, enrollment, assessment,
                feePayment, libraryBook, bookLoan).stream().filter(f -> f.handles(record)).findFirst().orElseThrow();
    }

    private static Form.Parser intRange(int min, int max) {
        return (raw, f) -> Registrable.range(Integer.parseInt(raw), min, max, f);
    }

    // ---- shared steps for the Person hierarchy -------------------------------

    private <T extends Person> void personStep(Form<T> f) {
        f.step("Personal details")
                .field("Person ID", ID, Registrable::id, Person::getPersonId, null)
                .unique(Identifiable.class, Identifiable::getId)
                .field("First name", NAME, Registrable::name, Person::getFirstName, (p, v) -> p.setFirstName((String) v))
                .field("Last name", NAME, Registrable::name, Person::getLastName, (p, v) -> p.setLastName((String) v))
                .field("Email", "String | unique | email", Registrable::email, Person::getEmail,
                        (p, v) -> p.setEmail((String) v))
                .unique(Person.class, Person::getEmail)
                .field("Phone", PHONE, Registrable::phone, Person::getPhoneNumber, (p, v) -> p.setPhoneNumber((String) v))
                .field("Date of birth", DATE + " | past", PAST, Person::getDateOfBirth,
                        (p, v) -> p.setDateOfBirth((LocalDate) v));
    }

    private <T extends AcademicMember> void academicStep(Form<T> f) {
        f.step("Academic details")
                .field("Member number", ID, Registrable::id, AcademicMember::getMemberNumber, null)
                .unique(AcademicMember.class, AcademicMember::getMemberNumber)
                .select("Department", departments(), AcademicMember::getDepartment,
                        (m, v) -> m.setDepartment((Department) v))
                .field("Join date", DATE + " | not future", NOT_FUTURE, AcademicMember::getJoinDate,
                        (m, v) -> m.setJoinDate((LocalDate) v))
                .choice("Academic status", AcademicMember.STATUSES, AcademicMember::getAcademicStatus,
                        (m, v) -> m.setAcademicStatus((String) v))
                .field("Institutional email", "String | unique | email", Registrable::email,
                        AcademicMember::getInstitutionalEmail, (m, v) -> m.setInstitutionalEmail((String) v))
                .unique(AcademicMember.class, AcademicMember::getInstitutionalEmail)
                .field("Advisor name", "String | 2-120 chars", Form.text(2, 120), AcademicMember::getAdvisorName,
                        (m, v) -> m.setAdvisorName((String) v));
    }

    // ---- related-record selectors --------------------------------------------

    private Select<Department> departments() {
        return new Select<>("Department", () -> store.all(Department.class), () -> department, true);
    }

    private Select<Student> students() {
        return new Select<>("Student", () -> store.all(Student.class), () -> student, true);
    }

    // ---- entity forms ----------------------------------------------------------

    private Form<Department> departmentForm() {
        // Head teacher is optional and offers no nested registration (a Teacher needs a Department first).
        Select<Teacher> heads = new Select<>("Teacher", () -> store.all(Teacher.class), null, false);
        return new Form<>(Department.class, "Department", store)
                .step("Department details")
                .field("Department ID", ID, Registrable::id, Department::getDepartmentId, null)
                .unique(Identifiable.class, Identifiable::getId)
                .field("Department name", NAME, Registrable::name, Department::getDepartmentName,
                        (d, v) -> d.setDepartmentName((String) v))
                .field("Department code", "String | unique | 2-10 chars", Form.text(2, 10),
                        Department::getDepartmentCode, (d, v) -> d.setDepartmentCode((String) v))
                .unique(Department.class, Department::getDepartmentCode)
                .field("Office location", "String | 2-60 chars", Form.text(2, 60), Department::getOfficeLocation,
                        (d, v) -> d.setOfficeLocation((String) v))
                .field("Contact email", "String | email", Registrable::email, Department::getContactEmail,
                        (d, v) -> d.setContactEmail((String) v))
                .step("Leadership")
                .select("Head teacher", heads, Department::getHeadTeacher, (d, v) -> d.setHeadTeacher((Teacher) v))
                .creator(v -> new Department(v.get(0), v.get(1), v.get(2), v.get(3), v.get(4), v.get(5)));
    }

    private Form<Student> studentForm() {
        Form<Student> f = new Form<>(Student.class, "Student", store);
        personStep(f);
        academicStep(f);
        return f.step("Student details")
                .field("Student number", ID, Registrable::id, Student::getStudentNumber, null)
                .unique(Student.class, Student::getStudentNumber)
                .field("Program name", "String | 2-120 chars", Form.text(2, 120), Student::getProgramName,
                        (s, v) -> s.setProgramName((String) v))
                .field("Admission date", DATE + " | not future", NOT_FUTURE, Student::getAdmissionDate,
                        (s, v) -> s.setAdmissionDate((LocalDate) v))
                .field("Study year", "int | 1-6", intRange(1, 6), Student::getStudyYear,
                        (s, v) -> s.setStudyYear((Integer) v))
                .field("Guardian phone", PHONE, Registrable::phone, Student::getGuardianPhone,
                        (s, v) -> s.setGuardianPhone((String) v))
                .yesNo("Scholarship", Student::isScholarshipStatus, (s, v) -> s.setScholarshipStatus((Boolean) v))
                .creator(v -> new Student(v.get(0), v.get(1), v.get(2), v.get(3), v.get(4), v.get(5), v.get(6),
                        v.get(7), v.get(8), v.get(9), v.get(10), v.get(11), v.get(12), v.get(13), v.get(14),
                        v.get(15), v.get(16), v.get(17)));
    }

    private Form<Teacher> teacherForm() {
        Form<Teacher> f = new Form<>(Teacher.class, "Teacher", store);
        personStep(f);
        academicStep(f);
        return f.step("Teacher details")
                .field("Employee number", ID, Registrable::id, Teacher::getEmployeeNumber, null)
                .unique(Teacher.class, Teacher::getEmployeeNumber)
                .field("Specialization", NAME, Form.text(2, 60), Teacher::getSpecialization,
                        (t, v) -> t.setSpecialization((String) v))
                .field("Hire date", DATE + " | not future", NOT_FUTURE, Teacher::getHireDate,
                        (t, v) -> t.setHireDate((LocalDate) v))
                .field("Salary", "BigDecimal | > 0", MONEY, Teacher::getSalary, (t, v) -> t.setSalary((BigDecimal) v))
                .field("Office number", "String | 1-20 chars", Form.text(1, 20), Teacher::getOfficeNumber,
                        (t, v) -> t.setOfficeNumber((String) v))
                .choice("Employment type", Teacher.EMPLOYMENT_TYPES, Teacher::getEmploymentType,
                        (t, v) -> t.setEmploymentType((String) v))
                .creator(v -> new Teacher(v.get(0), v.get(1), v.get(2), v.get(3), v.get(4), v.get(5), v.get(6),
                        v.get(7), v.get(8), v.get(9), v.get(10), v.get(11), v.get(12), v.get(13), v.get(14),
                        v.get(15), v.get(16), v.get(17)));
    }

    private Form<Administrator> administratorForm() {
        Form<Administrator> f = new Form<>(Administrator.class, "Administrator", store);
        personStep(f);
        return f.step("Administrator details")
                .field("Staff number", ID, Registrable::id, Administrator::getStaffNumber, null)
                .unique(Administrator.class, Administrator::getStaffNumber)
                .field("Job title", NAME, Form.text(2, 60), Administrator::getJobTitle,
                        (a, v) -> a.setJobTitle((String) v))
                .field("Assigned office", "String | 1-60 chars", Form.text(1, 60), Administrator::getAssignedOffice,
                        (a, v) -> a.setAssignedOffice((String) v))
                .field("Employment date", DATE + " | not future", NOT_FUTURE, Administrator::getEmploymentDate,
                        (a, v) -> a.setEmploymentDate((LocalDate) v))
                .field("Access level", "int | 1-5", intRange(1, 5), Administrator::getAccessLevel,
                        (a, v) -> a.setAccessLevel((Integer) v))
                .yesNo("Active", Administrator::getActive, (a, v) -> a.setActive((Boolean) v))
                .creator(v -> new Administrator(v.get(0), v.get(1), v.get(2), v.get(3), v.get(4), v.get(5),
                        v.get(6), v.get(7), v.get(8), v.get(9), v.get(10), v.get(11)));
    }

    private Form<Course> courseForm() {
        Select<Teacher> teachers = new Select<>("Teacher", () -> store.all(Teacher.class), () -> teacher, true);
        return new Form<>(Course.class, "Course", store)
                .step("Course details")
                .field("Course ID", ID, Registrable::id, Course::getCourseId, null)
                .unique(Identifiable.class, Identifiable::getId)
                .field("Course code", "String | unique | 2-15 chars", Form.text(2, 15), Course::getCourseCode,
                        (c, v) -> c.setCourseCode((String) v))
                .unique(Course.class, Course::getCourseCode)
                .field("Course title", "String | 2-120 chars", Form.text(2, 120), Course::getCourseTitle,
                        (c, v) -> c.setCourseTitle((String) v))
                .field("Credit hours", "int | 1-6", intRange(1, 6), Course::getCreditHours,
                        (c, v) -> c.setCreditHours((Integer) v))
                .step("Assignment")
                .select("Department", departments(), Course::getDepartment, (c, v) -> c.setDepartment((Department) v))
                .select("Assigned teacher", teachers, Course::getAssignedTeacher,
                        (c, v) -> c.setAssignedTeacher((Teacher) v))
                .creator(v -> new Course(v.get(0), v.get(1), v.get(2), v.get(3), v.get(4), v.get(5)));
    }

    private Form<Classroom> classroomForm() {
        return new Form<>(Classroom.class, "Classroom", store)
                .step("Room details")
                .field("Classroom ID", ID, Registrable::id, Classroom::getClassroomId, null)
                .unique(Identifiable.class, Identifiable::getId)
                .field("Building name", "String | 2-60 chars", Form.text(2, 60), Classroom::getBuildingName,
                        (c, v) -> c.setBuildingName((String) v))
                .field("Room number", "String | 1-10 chars", Form.text(1, 10), Classroom::getRoomNumber,
                        (c, v) -> c.setRoomNumber((String) v))
                .step("Capacity & status")
                .field("Capacity", "int | 1-1000", intRange(1, 1000), Classroom::getCapacity,
                        (c, v) -> c.setCapacity((Integer) v))
                .field("Floor number", "int | 0-100", intRange(0, 100), Classroom::getFloorNumber,
                        (c, v) -> c.setFloorNumber((Integer) v))
                .yesNo("Available", Classroom::getAvailable, (c, v) -> c.setAvailable((Boolean) v))
                .creator(v -> new Classroom(v.get(0), v.get(1), v.get(2), v.get(3), v.get(4), v.get(5)));
    }

    private Form<Enrollment> enrollmentForm() {
        Select<Course> courses = new Select<>("Course", () -> store.all(Course.class), () -> course, true);
        return new Form<>(Enrollment.class, "Enrollment", store)
                .step("Student & course")
                .field("Enrollment ID", ID, Registrable::id, Enrollment::getEnrollmentId, null)
                .unique(Identifiable.class, Identifiable::getId)
                .select("Student", students(), Enrollment::getStudent, null)
                .select("Course", courses, Enrollment::getCourse, null)
                .step("Term")
                .field("Enrollment date", DATE + " | not future", NOT_FUTURE, Enrollment::getEnrollmentDate,
                        (e, v) -> e.setEnrollmentDate((LocalDate) v))
                .choice("Semester", Enrollment.SEMESTERS, Enrollment::getSemester, (e, v) -> e.setSemester((String) v))
                .choice("Enrollment status", Enrollment.STATUSES, Enrollment::getEnrollmentStatus,
                        (e, v) -> e.setEnrollmentStatus((String) v))
                .creator(v -> new Enrollment(v.get(0), v.get(1), v.get(2), v.get(3), v.get(4), v.get(5)))
                .check(this::checkDuplicateEnrollment);
    }

    private void checkDuplicateEnrollment(Enrollment e) throws DuplicateRecordException {
        boolean duplicate = e.isActive() && store.find(Enrollment.class, other -> other != e && other.isActive()
                && other.getStudent() == e.getStudent() && other.getCourse() == e.getCourse()
                && other.getSemester().equals(e.getSemester())).isPresent();
        if (duplicate) {
            throw new DuplicateRecordException(e.getStudent().getDisplayName() + " already has an active enrollment in "
                    + e.getCourse().getCourseCode() + " for " + e.getSemester() + ".");
        }
    }

    private Form<Assessment> assessmentForm() {
        Select<Enrollment> active = new Select<>("Active enrollment",
                () -> store.all(Enrollment.class).stream().filter(Enrollment::isActive).toList(), () -> enrollment, true);
        return new Form<>(Assessment.class, "Assessment", store)
                .step("Assessment details")
                .field("Assessment ID", ID, Registrable::id, Assessment::getAssessmentId, null)
                .unique(Identifiable.class, Identifiable::getId)
                .select("Enrollment", active, Assessment::getEnrollment, null)
                .choice("Assessment type", Assessment.TYPES, Assessment::getAssessmentType,
                        (a, v) -> a.setAssessmentType((String) v))
                .step("Result")
                .field("Maximum score", "double | > 0", SCORE, Assessment::getMaximumScore,
                        (a, v) -> a.setMaximumScore((Double) v))
                .field("Obtained score", "double | 0 to maximum", SCORE, Assessment::getObtainedScore,
                        (a, v) -> a.setObtainedScore((Double) v))
                .field("Assessment date", DATE + " | not future", NOT_FUTURE, Assessment::getAssessmentDate,
                        (a, v) -> a.setAssessmentDate((LocalDate) v))
                .creator(v -> new Assessment(v.get(0), v.get(1), v.get(2), v.get(3), v.get(4), v.get(5)))
                .check(a -> {
                    if (!a.getEnrollment().isActive()) {
                        throw new ValidationException("Only an active enrollment can be assessed.");
                    }
                });
    }

    private Form<FeePayment> feePaymentForm() {
        return new Form<>(FeePayment.class, "Fee payment", store)
                .step("Payer")
                .field("Payment ID", ID, Registrable::id, FeePayment::getPaymentId, null)
                .unique(Identifiable.class, Identifiable::getId)
                .select("Student", students(), FeePayment::getStudent, null)
                .step("Payment details")
                .field("Amount", "BigDecimal | > 0", MONEY, FeePayment::getAmount, (p, v) -> p.setAmount((BigDecimal) v))
                .field("Payment date", DATE + " | not future", NOT_FUTURE, FeePayment::getPaymentDate,
                        (p, v) -> p.setPaymentDate((LocalDate) v))
                .choice("Payment method", FeePayment.METHODS, FeePayment::getPaymentMethod,
                        (p, v) -> p.setPaymentMethod((String) v))
                .field("Receipt number", "String | unique | 1-30 chars", Form.text(1, 30), FeePayment::getReceiptNumber,
                        (p, v) -> p.setReceiptNumber((String) v))
                .unique(FeePayment.class, FeePayment::getReceiptNumber)
                .creator(v -> new FeePayment(v.get(0), v.get(1), v.get(2), v.get(3), v.get(4), v.get(5)));
    }

    private Form<LibraryBook> libraryBookForm() {
        return new Form<>(LibraryBook.class, "Library book", store)
                .step("Book details")
                .field("Book ID", ID, Registrable::id, LibraryBook::getBookId, null)
                .unique(Identifiable.class, Identifiable::getId)
                .field("ISBN", "String | unique | 10 or 13 digits", Registrable::isbn, LibraryBook::getIsbn,
                        (b, v) -> b.setIsbn((String) v))
                .unique(LibraryBook.class, LibraryBook::getIsbn)
                .field("Title", "String | 1-120 chars", Form.text(1, 120), LibraryBook::getTitle,
                        (b, v) -> b.setTitle((String) v))
                .field("Author", NAME, Registrable::name, LibraryBook::getAuthor, (b, v) -> b.setAuthor((String) v))
                .step("Stock")
                .field("Total copies", "int | >= 0", intRange(0, 10_000), LibraryBook::getTotalCopies,
                        (b, v) -> b.setTotalCopies((Integer) v))
                .field("Available copies", "int | 0 to total", intRange(0, 10_000), LibraryBook::getAvailableCopies,
                        (b, v) -> b.setAvailableCopies((Integer) v))
                .creator(v -> new LibraryBook(v.get(0), v.get(1), v.get(2), v.get(3), v.get(4), v.get(5)));
    }

    /** Opened by "Borrow Library Book". Student and book are locked after saving so stock stays consistent. */
    private Form<BookLoan> bookLoanForm() {
        Select<LibraryBook> books = new Select<>("Library book", () -> store.all(LibraryBook.class),
                () -> libraryBook, true);
        return new Form<>(BookLoan.class, "Book loan", store)
                .step("Borrower & book")
                .field("Loan ID", ID, Registrable::id, BookLoan::getLoanId, null)
                .unique(Identifiable.class, Identifiable::getId)
                .select("Student", students(), BookLoan::getStudent, null)
                .select("Book", books, BookLoan::getBook, null)
                .step("Dates")
                .field("Borrowed date", DATE + " | not future", NOT_FUTURE, BookLoan::getBorrowedDate,
                        (l, v) -> l.setBorrowedDate((LocalDate) v))
                .field("Due date", DATE + " | after borrowed", ANY_DATE, BookLoan::getDueDate,
                        (l, v) -> l.setDueDate((LocalDate) v))
                .derived("Returned date", BookLoan::getReturnedDate)
                .creator(v -> new BookLoan(v.get(0), v.get(1), v.get(2), v.get(3), v.get(4), v.get(5)))
                .onCreate(this::takeCopy);
    }

    private void takeCopy(BookLoan loan) throws BookUnavailableException {
        LibraryBook book = loan.getBook();
        if (book.getAvailableCopies() <= 0) {
            throw new BookUnavailableException("No copies of '" + book.getTitle() + "' remain. Choose another book.");
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);
    }
}

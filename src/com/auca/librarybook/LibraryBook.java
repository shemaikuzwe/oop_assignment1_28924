package com.auca.librarybook;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;

public class LibraryBook implements Identifiable, Registrable {
    private final String bookId;
    private String isbn;
    private String title;
    private String author;
    private int totalCopies;
    private int availableCopies;

    public LibraryBook(String bookId, String isbn, String title, String author, int totalCopies,
            int availableCopies) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    @Override
    public String getId() {
        return bookId;
    }

    @Override
    public String getDisplayName() {
        return title + " by " + author;
    }

    @Override
    public boolean isActive() {
        return availableCopies > 0;
    }

    @Override
    public void validate() throws ValidationException {
        Registrable.id(bookId, "Book ID");
        Registrable.isbn(isbn, "ISBN");
        Registrable.text(title, "Title", 1, 120);
        Registrable.name(author, "Author");
        Registrable.range(totalCopies, 0, 10_000, "Total copies");
        Registrable.range(availableCopies, 0, totalCopies, "Available copies");
    }

    @Override
    public void printDetails() {
        System.out.println(this);
    }

    @Override
    public String getRegistrationSummary() {
        return Identifiable.label(this) + " - " + availableCopies + "/" + totalCopies + " available";
    }

    @Override
    public String toString() {
        return String.format("""
                LIBRARY BOOK
                  Book ID            : %s
                  ISBN               : %s
                  Title              : %s
                  Author             : %s
                  Total copies       : %d3
                  Available copies   : %d
                """, bookId, isbn, title, author, totalCopies, availableCopies);
    }

    public String getBookId() {
        return bookId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }
}

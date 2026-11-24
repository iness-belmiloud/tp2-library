package library;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Library {
    private final Map<String, Book> books = new HashMap<>();
    private final Map<String, Copy> copies = new HashMap<>();
    private final Map<String, Member> members = new HashMap<>();
    private final List<Loan> loans = new ArrayList<>();

    public void addBook(Book book) {
        books.put(book.getIsbn(), book);
    }

    public void addCopy(String isbn, String inventoryNumber) {
        Book book = books.get(isbn);
        if (book == null) throw new IllegalArgumentException("Unknown ISBN");
        copies.put(inventoryNumber, new Copy(inventoryNumber, book));
    }

    public void registerMember(Member member) {
        members.put(member.getNumber(), member);
    }

    public Copy findCopy(String inventoryNumber) {
        return copies.get(inventoryNumber);
    }

    public List<Book> searchByTitle(String keyword) {
        String k = keyword.toLowerCase();
        return books.values().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(k))
                .toList();
    }

    public Loan lend(String memberNumber, String inventoryNumber, LocalDate today) {
        Member member = members.get(memberNumber);
        Copy copy = copies.get(inventoryNumber);
        if (member == null || copy == null) {
            throw new IllegalArgumentException("Unknown member or copy");
        }
        if (!copy.isAvailable()) {
            throw new IllegalStateException("Copy not available");
        }
        if (openLoans(member) >= 5) {
            throw new IllegalStateException("Too many loans");
        }
        Loan loan = new Loan(member, copy, today);
        loans.add(loan);
        copy.setAvailable(false);
        return loan;
    }

    public void returnCopy(String inventoryNumber, LocalDate today) {
        Copy copy = copies.get(inventoryNumber);
        if (copy == null) throw new IllegalArgumentException("Unknown copy");
        Loan loan = loans.stream()
                .filter(l -> l.getCopy() == copy && l.isOpen())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Copy not on loan"));
        loan.close(today);
        copy.setAvailable(true);
    }

    private long openLoans(Member member) {
        return loans.stream()
                .filter(l -> l.getMember() == member && l.isOpen())
                .count();
    }
}

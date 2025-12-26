import java.time.LocalDate;

public class Loan {
    private final Book book;
    private final LocalDate issueDate;
    private final LocalDate dueDate; // срок возврата (например, +14 дней)

    public Loan(Book book, LocalDate issueDate) {
        this.book = book;
        this.issueDate = issueDate;
        this.dueDate = issueDate.plusDays(14);
    }

    public Book getBook() { return book; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }

    public boolean isOverdue() {
        return LocalDate.now().isAfter(dueDate);
    }

    @Override
    public String toString() {
        return String.format("%s (выдана %s, вернуть до %s)%s",
                book.getTitle(), issueDate, dueDate,
                isOverdue() ? " — ПРОСРОЧЕНО!" : "");
    }
}
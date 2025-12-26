public class Book {
    private final String isbn;
    private final String title;
    private final String author;
    private final int year;
    private boolean available;

    public Book(String isbn, String title, String author, int year) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.year = year;
        this.available = true;
    }

    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getYear() { return year; }
    public boolean isAvailable() { return available; }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s, %d) [%s]",
                isbn, title, author, year, available ? "доступна" : "выдана");
    }
}
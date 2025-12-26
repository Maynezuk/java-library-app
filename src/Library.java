import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class Library {
    private Connection conn;

    public Library() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:library.db");
            createTables();
        } catch (SQLException e) {
            System.out.println("Ошибка подключения к БД: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        String sqlBooks = "CREATE TABLE IF NOT EXISTS books (" +
                "isbn TEXT PRIMARY KEY, " +
                "title TEXT NOT NULL, " +
                "author TEXT NOT NULL, " +
                "year INTEGER NOT NULL, " +
                "available BOOLEAN NOT NULL DEFAULT TRUE" +
                ");";

        String sqlReaders = "CREATE TABLE IF NOT EXISTS readers (" +
                "id TEXT PRIMARY KEY, " +
                "name TEXT NOT NULL" +
                ");";

        String sqlLoans = "CREATE TABLE IF NOT EXISTS loans (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "book_isbn TEXT NOT NULL, " +
                "reader_id TEXT NOT NULL, " +
                "issue_date TEXT NOT NULL, " +
                "due_date TEXT NOT NULL, " +
                "FOREIGN KEY (book_isbn) REFERENCES books(isbn), " +
                "FOREIGN KEY (reader_id) REFERENCES readers(id)" +
                ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlBooks);
            stmt.execute(sqlReaders);
            stmt.execute(sqlLoans);
        }
    }

    // Добавить книгу
    public void addBook(Book book) throws LibraryException {
        String sql = "INSERT INTO books (isbn, title, author, year, available) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getIsbn());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getAuthor());
            pstmt.setInt(4, book.getYear());
            pstmt.setBoolean(5, true);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new LibraryException("Ошибка добавления книги: " + e.getMessage());
        }
    }

    // Удалить книгу
    public void deleteBook(String isbn) throws LibraryException {
        String sql = "DELETE FROM books WHERE isbn = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            int rows = pstmt.executeUpdate();
            if (rows == 0) throw new LibraryException("Книга не найдена");
        } catch (SQLException e) {
            throw new LibraryException("Ошибка удаления книги: " + e.getMessage());
        }
    }

    // Зарегистрировать читателя
    public void registerReader(Reader reader) throws LibraryException {
        String sql = "INSERT INTO readers (id, name) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, reader.getId());
            pstmt.setString(2, reader.getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new LibraryException("Ошибка регистрации читателя: " + e.getMessage());
        }
    }

    // Удалить читателя
    public void deleteReader(String id) throws LibraryException {
        String sql = "DELETE FROM readers WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            int rows = pstmt.executeUpdate();
            if (rows == 0) throw new LibraryException("Читатель не найден");
        } catch (SQLException e) {
            throw new LibraryException("Ошибка удаления читателя: " + e.getMessage());
        }
    }

    // Выдать книгу
    public void lendBook(String isbn, String readerId) throws LibraryException {
        Book book = findByIsbn(isbn);
        if (book == null) throw new LibraryException("Книга не найдена");
        if (!book.isAvailable()) throw new LibraryException("Книга уже выдана");

        Reader reader = getReader(readerId);
        if (reader == null) throw new LibraryException("Читатель не найден");

        String sqlUpdateBook = "UPDATE books SET available = FALSE WHERE isbn = ?";
        String sqlInsertLoan = "INSERT INTO loans (book_isbn, reader_id, issue_date, due_date) VALUES (?, ?, ?, ?)";

        LocalDate now = LocalDate.now();
        LocalDate due = now.plusDays(14);

        try {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmtBook = conn.prepareStatement(sqlUpdateBook);
                 PreparedStatement pstmtLoan = conn.prepareStatement(sqlInsertLoan)) {
                pstmtBook.setString(1, isbn);
                pstmtBook.executeUpdate();

                pstmtLoan.setString(1, isbn);
                pstmtLoan.setString(2, readerId);
                pstmtLoan.setString(3, now.toString());
                pstmtLoan.setString(4, due.toString());
                pstmtLoan.executeUpdate();

                conn.commit();
            }
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                // ignore
            }
            throw new LibraryException("Ошибка выдачи книги: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    // Вернуть книгу
    public void returnBook(String isbn, String readerId) throws LibraryException {
        Book book = findByIsbn(isbn);
        if (book == null) throw new LibraryException("Книга не найдена");
        if (book.isAvailable()) throw new LibraryException("Книга и так в библиотеке");

        String sqlUpdateBook = "UPDATE books SET available = TRUE WHERE isbn = ?";
        String sqlDeleteLoan = "DELETE FROM loans WHERE book_isbn = ? AND reader_id = ?";

        try {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmtBook = conn.prepareStatement(sqlUpdateBook);
                 PreparedStatement pstmtLoan = conn.prepareStatement(sqlDeleteLoan)) {
                pstmtBook.setString(1, isbn);
                pstmtBook.executeUpdate();

                pstmtLoan.setString(1, isbn);
                pstmtLoan.setString(2, readerId);
                pstmtLoan.executeUpdate();

                conn.commit();
            }
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                // ignore
            }
            throw new LibraryException("Ошибка возврата книги: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    // Получить все книги
    public Map<String, Book> getBooks() {
        Map<String, Book> books = new HashMap<>();
        String sql = "SELECT * FROM books";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Book book = new Book(
                        rs.getString("isbn"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("year")
                );
                book.setAvailable(rs.getBoolean("available"));
                books.put(book.getIsbn(), book);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка загрузки книг: " + e.getMessage());
        }
        return Collections.unmodifiableMap(books);
    }

    // Получить всех читателей с их выдачами
    public Map<String, Reader> getReaders() {
        Map<String, Reader> readers = new HashMap<>();
        String sqlReaders = "SELECT * FROM readers";
        String sqlLoans = "SELECT * FROM loans WHERE reader_id = ?";

        try (Statement stmtReaders = conn.createStatement();
             ResultSet rsReaders = stmtReaders.executeQuery(sqlReaders)) {

            while (rsReaders.next()) {
                String id = rsReaders.getString("id");
                String name = rsReaders.getString("name");
                Reader reader = new Reader(id, name);

                try (PreparedStatement pstmtLoans = conn.prepareStatement(sqlLoans)) {
                    pstmtLoans.setString(1, id);
                    try (ResultSet rsLoans = pstmtLoans.executeQuery()) {
                        while (rsLoans.next()) {
                            Book book = findByIsbn(rsLoans.getString("book_isbn"));
                            if (book != null) {
                                LocalDate issue = LocalDate.parse(rsLoans.getString("issue_date"));
                                Loan loan = new Loan(book, issue);
                                reader.getLoans().add(loan);
                            }
                        }
                    }
                }
                readers.put(id, reader);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка загрузки читателей: " + e.getMessage());
        }
        return Collections.unmodifiableMap(readers);
    }

    // Поиск книги по ISBN
    public Book findByIsbn(String isbn) {
        String sql = "SELECT * FROM books WHERE isbn = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Book book = new Book(
                            rs.getString("isbn"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getInt("year")
                    );
                    book.setAvailable(rs.getBoolean("available"));
                    return book;
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка поиска книги: " + e.getMessage());
        }
        return null;
    }

    // Получить читателя по ID
    private Reader getReader(String id) {
        String sql = "SELECT * FROM readers WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Reader(rs.getString("id"), rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка поиска читателя: " + e.getMessage());
        }
        return null;
    }

    // Поиск по автору или названию
    public List<Book> searchBooks(String query) {
        List<Book> result = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(getBooks().values());
        }
        String lowerQuery = query.toLowerCase();
        for (Book book : getBooks().values()) {
            if (book.getTitle().toLowerCase().contains(lowerQuery) ||
                    book.getAuthor().toLowerCase().contains(lowerQuery)) {
                result.add(book);
            }
        }
        return result;
    }

    // Закрыть соединение при выходе
    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("Ошибка закрытия БД: " + e.getMessage());
            }
        }
    }
}
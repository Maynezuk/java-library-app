import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class LibraryGUI extends JFrame {
    private Library library = new Library();

    // Компоненты интерфейса
    private DefaultTableModel catalogModel;
    private JTable catalogTable;
    private DefaultTableModel readersModel;
    private JTable readersTable;

    private JTextField searchField;
    private JTextField isbnField, readerIdField;

    private JTextField addBookIsbn, addBookTitle, addBookAuthor, addBookYear;

    private JTextField addReaderId, addReaderName;

    private JTextField deleteBookIsbn, deleteReaderId;

    public LibraryGUI() {
        setupUI();
        refreshCatalog();
        refreshReaders();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                library.close();
            }
        });
    }

    private void setupUI() {
        setTitle("Система управления библиотекой");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel catalogPanel = createCatalogPanel();
        tabbedPane.addTab("Каталог книг", catalogPanel);
        JPanel readersPanel = createReadersPanel();
        tabbedPane.addTab("Читатели и выдачи", readersPanel);
        JPanel operationsPanel = createOperationsPanel();
        tabbedPane.addTab("Выдача / Возврат", operationsPanel);
        JPanel managementPanel = createManagementPanel();
        tabbedPane.addTab("Управление", managementPanel);

        add(tabbedPane, BorderLayout.CENTER);

        setLocationRelativeTo(null);
    }

    private JPanel createCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Таблица каталога
        String[] columns = {"ISBN", "Название", "Автор", "Год", "Статус"};
        catalogModel = new DefaultTableModel(columns, 0);
        catalogTable = new JTable(catalogModel);
        JScrollPane scroll = new JScrollPane(catalogTable);

        // Поиск
        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Поиск по автору или названию:"));
        searchField = new JTextField(30);
        JButton searchButton = new JButton("Найти");
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        searchButton.addActionListener(e -> performSearch());

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createReadersPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"ID читателя", "ФИО", "Взятая книга", "Дата выдачи", "Вернуть до", "Статус"};
        readersModel = new DefaultTableModel(columns, 0);
        readersTable = new JTable(readersModel);
        JScrollPane scroll = new JScrollPane(readersTable);

        JButton refreshButton = new JButton("Обновить список");
        refreshButton.addActionListener(e -> refreshReaders());

        panel.add(refreshButton, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createOperationsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("ISBN книги:"), gbc);
        gbc.gridx = 1;
        isbnField = new JTextField(20);
        panel.add(isbnField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("ID читателя:"), gbc);
        gbc.gridx = 1;
        readerIdField = new JTextField(20);
        panel.add(readerIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JButton lendButton = new JButton("Выдать книгу");
        JButton returnButton = new JButton("Вернуть книгу");

        lendButton.addActionListener(e -> lendBook());
        returnButton.addActionListener(e -> returnBook());

        panel.add(lendButton, gbc);
        gbc.gridy = 3;
        panel.add(returnButton, gbc);

        return panel;
    }

    private JPanel createManagementPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Добавление книги
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Добавить книгу:"), gbc);

        gbc.gridy++;
        panel.add(new JLabel("ISBN:"), gbc);
        gbc.gridx = 1;
        addBookIsbn = new JTextField(20);
        panel.add(addBookIsbn, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Название:"), gbc);
        gbc.gridx = 1;
        addBookTitle = new JTextField(20);
        panel.add(addBookTitle, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Автор:"), gbc);
        gbc.gridx = 1;
        addBookAuthor = new JTextField(20);
        panel.add(addBookAuthor, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Год:"), gbc);
        gbc.gridx = 1;
        addBookYear = new JTextField(20);
        panel.add(addBookYear, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton addBookButton = new JButton("Добавить книгу");
        addBookButton.addActionListener(e -> addBook());
        panel.add(addBookButton, gbc);

        // Добавление читателя
        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Добавить читателя:"), gbc);

        gbc.gridy++;
        panel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1;
        addReaderId = new JTextField(20);
        panel.add(addReaderId, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("ФИО:"), gbc);
        gbc.gridx = 1;
        addReaderName = new JTextField(20);
        panel.add(addReaderName, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton addReaderButton = new JButton("Добавить читателя");
        addReaderButton.addActionListener(e -> addReader());
        panel.add(addReaderButton, gbc);

        // Удаление книги
        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Удалить книгу по ISBN:"), gbc);
        gbc.gridx = 1;
        deleteBookIsbn = new JTextField(20);
        panel.add(deleteBookIsbn, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton deleteBookButton = new JButton("Удалить книгу");
        deleteBookButton.addActionListener(e -> deleteBook());
        panel.add(deleteBookButton, gbc);

        // Удаление читателя
        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Удалить читателя по ID:"), gbc);
        gbc.gridx = 1;
        deleteReaderId = new JTextField(20);
        panel.add(deleteReaderId, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton deleteReaderButton = new JButton("Удалить читателя");
        deleteReaderButton.addActionListener(e -> deleteReader());
        panel.add(deleteReaderButton, gbc);

        return panel;
    }

    private void performSearch() {
        String query = searchField.getText().trim();
        catalogModel.setRowCount(0);

        if (query.isEmpty()) {
            refreshCatalog();
            return;
        }

        List<Book> results = library.searchBooks(query);
        for (Book book : results) {
            addBookToTable(book);
        }
    }

    private void refreshCatalog() {
        catalogModel.setRowCount(0);
        for (Book book : library.getBooks().values()) {
            addBookToTable(book);
        }
    }

    private void addBookToTable(Book book) {
        catalogModel.addRow(new Object[]{
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor(),
                book.getYear(),
                book.isAvailable() ? "Доступна" : "Выдана"
        });
    }

    private void refreshReaders() {
        readersModel.setRowCount(0);
        for (Reader reader : library.getReaders().values()) {
            if (reader.getLoans().isEmpty()) {
                readersModel.addRow(new Object[]{
                        reader.getId(), reader.getName(), "Нет книг", "-", "-", "-"
                });
            } else {
                for (Loan loan : reader.getLoans()) {
                    String status = loan.isOverdue() ? "ПРОСРОЧЕНО!" : "В сроке";
                    readersModel.addRow(new Object[]{
                            reader.getId(),
                            reader.getName(),
                            loan.getBook().getTitle(),
                            loan.getIssueDate(),
                            loan.getDueDate(),
                            status
                    });
                }
            }
        }
    }

    private void lendBook() {
        String isbn = isbnField.getText().trim();
        String readerId = readerIdField.getText().trim();
        if (isbn.isEmpty() || readerId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Заполните оба поля!");
            return;
        }
        try {
            library.lendBook(isbn, readerId);
            JOptionPane.showMessageDialog(this, "Книга успешно выдана!");
            refreshCatalog();
            refreshReaders();
        } catch (LibraryException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void returnBook() {
        String isbn = isbnField.getText().trim();
        String readerId = readerIdField.getText().trim();
        if (isbn.isEmpty() || readerId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Заполните оба поля!");
            return;
        }
        try {
            library.returnBook(isbn, readerId);
            JOptionPane.showMessageDialog(this, "Книга успешно возвращена!");
            refreshCatalog();
            refreshReaders();
        } catch (LibraryException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addBook() {
        String isbn = addBookIsbn.getText().trim();
        String title = addBookTitle.getText().trim();
        String author = addBookAuthor.getText().trim();
        String yearStr = addBookYear.getText().trim();

        if (isbn.isEmpty() || title.isEmpty() || author.isEmpty() || yearStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Заполните все поля!");
            return;
        }

        try {
            int year = Integer.parseInt(yearStr);
            Book book = new Book(isbn, title, author, year);
            library.addBook(book);
            JOptionPane.showMessageDialog(this, "Книга добавлена!");
            refreshCatalog();
            clearAddBookFields();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Год должен быть числом!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (LibraryException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearAddBookFields() {
        addBookIsbn.setText("");
        addBookTitle.setText("");
        addBookAuthor.setText("");
        addBookYear.setText("");
    }

    private void addReader() {
        String id = addReaderId.getText().trim();
        String name = addReaderName.getText().trim();

        if (id.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Заполните все поля!");
            return;
        }

        try {
            Reader reader = new Reader(id, name);
            library.registerReader(reader);
            JOptionPane.showMessageDialog(this, "Читатель добавлен!");
            refreshReaders();
            clearAddReaderFields();
        } catch (LibraryException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearAddReaderFields() {
        addReaderId.setText("");
        addReaderName.setText("");
    }

    private void deleteBook() {
        String isbn = deleteBookIsbn.getText().trim();
        if (isbn.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите ISBN!");
            return;
        }

        try {
            library.deleteBook(isbn);
            JOptionPane.showMessageDialog(this, "Книга удалена!");
            refreshCatalog();
            deleteBookIsbn.setText("");
        } catch (LibraryException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteReader() {
        String id = deleteReaderId.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите ID!");
            return;
        }

        try {
            library.deleteReader(id);
            JOptionPane.showMessageDialog(this, "Читатель удалён!");
            refreshReaders();
            deleteReaderId.setText("");
        } catch (LibraryException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LibraryGUI().setVisible(true);
        });
    }
}
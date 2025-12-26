import java.util.ArrayList;
import java.util.List;

public class Reader {
    private final String id;
    private final String name;
    private final List<Loan> loans = new ArrayList<>();

    public Reader(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<Loan> getLoans() { return loans; }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}
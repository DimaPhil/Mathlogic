import java.math.BigInteger;

class Atom implements Ordinal {
    BigInteger number;

    Atom(BigInteger number) {
        this.number = number;
    }

    private Atom(int number) {
        this.number = BigInteger.valueOf(number);
    }

    static final Atom ZERO = new Atom(0);
    static final Atom ONE = new Atom(1);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Atom atom = (Atom) o;
        return number.equals(atom.number);
    }

    @Override
    public String toString() {
        return "\"" + number.toString() + "\"";
    }

    @Override
    public int hashCode() {
        return number.hashCode();
    }
}

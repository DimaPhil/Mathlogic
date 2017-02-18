class CList implements Ordinal {
    Ordinal first;
    Ordinal second;

    static final CList NIL = new CList();

    private CList() {
    }

    CList(Ordinal first, Ordinal second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return "<" + first.toString() + ", " + second.toString() + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CList cList = (CList) o;
        if (first != null ? !first.equals(cList.first) : cList.first != null) {
            return false;
        }
        return second != null ? second.equals(cList.second) : cList.second == null;
    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }
}

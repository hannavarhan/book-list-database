package sample;

import java.util.Objects;

public class BookInfo {

    protected String name;
    protected String authorName;
    protected String authorSurname;
    protected String author;
    protected double rate;

    public BookInfo(String name, double rate, String authorName, String authorSurname) {
        this.name = name;
        this.authorName = authorName;
        this.authorSurname = authorSurname;
        author = this.authorName + " " + this.authorSurname;
        this.rate = rate;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorSurname() {
        return authorSurname;
    }

    public void setAuthorSurname(String authorSurname) {
        this.authorSurname = authorSurname;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookInfo book = (BookInfo) o;
        return Double.compare(book.rate, rate) == 0 &&
                name.equals(book.name) &&
                authorName.equals(book.authorName) &&
                authorSurname.equals(book.authorSurname);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return Objects.hash(name, authorName, authorSurname, rate);
    }


}

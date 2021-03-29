package sample;

import java.util.ArrayList;

public class Book extends BookInfo {


    private ArrayList<String> keywords;

    public Book(String name, String authorName, String authorSurname, double rate, ArrayList<String> keywords) {
        super(name, rate, authorName, authorSurname);
        this.keywords = keywords;
    }

    public ArrayList<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(ArrayList<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Book{");
        sb.append("name='").append(name).append('\'');
        sb.append(", authorName='").append(authorName).append('\'');
        sb.append(", authorSurname='").append(authorSurname).append('\'');
        sb.append(", rate=").append(rate);
        sb.append('}');
        return sb.toString();
    }
}


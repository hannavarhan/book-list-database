package sample;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class DatabaseHandler extends Configs {

    Connection dbConnection;

    public Connection getDbConnection() throws ClassNotFoundException, SQLException {

        Class.forName("com.mysql.jdbc.Driver");

        String connectionString = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useUnicode=true&serverTimezone=UTC";
        dbConnection = DriverManager.getConnection(connectionString, dbUser, dbPass);

        return dbConnection;
    }

    public void insertBook(Book book) throws SQLException, ClassNotFoundException {
        if(isBookNameExists(book.getName())) {
            return;
        }

        int authorId = isAuthorExists(book.getAuthorName(), book.getAuthorSurname());
        if (authorId == -1) {
            authorId = insertAuthor(book.getAuthorName(), book.getAuthorSurname());
        }

        int idBook = insertIntoBooks(book.getName(), authorId, book.getRate());
        insertWords(book.getKeywords());
        insertKeyWordLinks(idBook, book.getKeywords());
    }

    private int insertIntoBooks(String bookName, int authorId, double rate) throws SQLException, ClassNotFoundException {
        String insertBook = "INSERT INTO " + Constant.BOOKS_TABLE + "(" + Constant.BOOK_NAME + "," +
                Constant.BOOK_AUTHOR + "," + Constant.BOOK_RATE + ")" + "VALUES(?,?,?)";
        PreparedStatement preparedStatement = getDbConnection().prepareStatement(insertBook, Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setString(1, bookName);
        preparedStatement.setString(2, String.valueOf(authorId));
        preparedStatement.setString(3, String.valueOf(rate));
        preparedStatement.executeUpdate();

        int idBook = -1;
        ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
        if(generatedKeys.next()) {
            idBook = generatedKeys.getInt(1);
        }
        return idBook;
    }

    private void insertKeyWordLinks(int idBook, ArrayList<String> keywords) throws SQLException, ClassNotFoundException {
        String insertKeyWords = "INSERT INTO " + Constant.KEYWORDS_TABLE + "(" + Constant.KEYWORD_BOOK_ID + "," +
                Constant.KEYWORD_WORD_ID + ")" + "VALUES(?,?)";
        String selectId = "SELECT " + Constant.WORD_ID + " FROM " + Constant.WORDS_TABLE + " WHERE " +
                Constant.WORD_NAME + "=?";
        for(String word : keywords) {
            PreparedStatement wordIDStatement = getDbConnection().prepareStatement(selectId);
            wordIDStatement.setString(1, word);
            ResultSet resultSet = wordIDStatement.executeQuery();
            int wordId = 0;
            if (resultSet.next()) {
                wordId = resultSet.getInt(1);
            }
            PreparedStatement preparedStatement = getDbConnection().prepareStatement(insertKeyWords);
            preparedStatement.setString(1, String.valueOf(idBook));
            preparedStatement.setString(2, String.valueOf(wordId));
            preparedStatement.executeUpdate();
        }
    }

    private boolean isBookNameExists(String bookName) throws SQLException, ClassNotFoundException {
        String checkName = "SELECT " + Constant.BOOK_ID + " FROM " + Constant.BOOKS_TABLE +
                " WHERE " + Constant.BOOK_NAME + "=?";
        ResultSet resultSet;
        PreparedStatement preparedStatement = getDbConnection().prepareStatement(checkName);
        preparedStatement.setString(1, bookName);
        resultSet = preparedStatement.executeQuery();
        return resultSet.next();
    }

    private int isAuthorExists(String authorName, String authorSurname) throws SQLException, ClassNotFoundException {
        String checkAuthor = "SELECT " + Constant.AUTHOR_ID + " FROM " + Constant.AUTHORS_TABLE +
                " WHERE " + Constant.AUTHOR_NAME + "=? AND " + Constant.AUTHOR_SURNAME + "=?";
        ResultSet resultSet;
        PreparedStatement preparedStatement = getDbConnection().prepareStatement(checkAuthor);
        preparedStatement.setString(1, authorName);
        preparedStatement.setString(2, authorSurname);
        resultSet = preparedStatement.executeQuery();
        if(resultSet.next()) {
            return resultSet.getInt(1);
        } else return -1;
    }

    private int insertAuthor(String authorName, String authorSurname) throws SQLException, ClassNotFoundException {
        String insertAuthor = "INSERT INTO " + Constant.AUTHORS_TABLE + "(" + Constant.AUTHOR_NAME + "," +
                Constant.AUTHOR_SURNAME + ")" + "VALUES(?,?)";
        PreparedStatement preparedStatement = getDbConnection().prepareStatement(
                insertAuthor, Statement.RETURN_GENERATED_KEYS);
        try {
            preparedStatement.setString(1, authorName);
            preparedStatement.setString(2, authorSurname);
            preparedStatement.executeUpdate();
        } catch(SQLIntegrityConstraintViolationException exc) {
            System.out.println("Book author already exists");
        }

        ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
        if(generatedKeys.next()) {
            return generatedKeys.getInt(1);
        } else return -1;
    }

    private void insertWords(ArrayList<String> words) throws SQLException, ClassNotFoundException {
        String insert = "INSERT INTO " + Constant.WORDS_TABLE + "(" + Constant.WORD_NAME + ") VALUES(?)";
        for (String word : words) {
            try {
                PreparedStatement preparedStatement = getDbConnection().prepareStatement(insert);
                preparedStatement.setString(1, word);
                preparedStatement.executeUpdate();
                //preparedStatement.addBatch();
                //preparedStatement.executeBatch();
            } catch (SQLIntegrityConstraintViolationException exc) {
                System.out.println("Word " + word + " already exists");
            }
        }

    }

    public ArrayList<BookInfo> getAllBooksInfo() throws SQLException, ClassNotFoundException {
        ArrayList<BookInfo> books = new ArrayList<>();

        String selectBookInfo = "SELECT " + Constant.BOOKS_TABLE + "." + Constant.BOOK_NAME + "," +
                Constant.BOOKS_TABLE + "." + Constant.BOOK_RATE + "," +
                Constant.AUTHORS_TABLE + "." + Constant.AUTHOR_NAME + "," +
                Constant.AUTHORS_TABLE + "." + Constant.AUTHOR_SURNAME +
                " FROM " + Constant.BOOKS_TABLE +
                " INNER JOIN " + Constant.AUTHORS_TABLE +
                " ON " + Constant.AUTHORS_TABLE + "." + Constant.AUTHOR_ID + "=" +
                Constant.BOOKS_TABLE + "." + Constant.BOOK_AUTHOR +
                " ORDER BY " + Constant.BOOKS_TABLE + "." + Constant.BOOK_NAME;

        PreparedStatement preparedStatement = getDbConnection().prepareStatement(selectBookInfo);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            books.add(new BookInfo(resultSet.getString(1), Double.parseDouble(resultSet.getString(2)),
                    resultSet.getString(3),resultSet.getString(4)));
        }
        return books;
    }

    //заменить на функцию, принимающую переменное число параметров????
    public Set<BookInfo> getBooksInfoByKeywords(ArrayList<String> words) throws SQLException, ClassNotFoundException {
        Set<BookInfo> bookSet = new LinkedHashSet<>();

        String selectFromWords = "SELECT " + Constant.BOOKS_TABLE + "." + Constant.BOOK_NAME + "," +
                Constant.BOOKS_TABLE + "." + Constant.BOOK_RATE + "," +
                Constant.AUTHORS_TABLE + "." + Constant.AUTHOR_NAME + "," +
                Constant.AUTHORS_TABLE + "." + Constant.AUTHOR_SURNAME +
                " FROM " + Constant.WORDS_TABLE +
                " INNER JOIN " + Constant.KEYWORDS_TABLE +
                " ON " + Constant.KEYWORDS_TABLE + "." + Constant.KEYWORD_WORD_ID + "=" +
                Constant.WORDS_TABLE + "." + Constant.WORD_ID +
                " INNER JOIN " + Constant.BOOKS_TABLE +
                " ON " + Constant.KEYWORDS_TABLE + "." + Constant.KEYWORD_BOOK_ID + "=" +
                Constant.BOOKS_TABLE + "." + Constant.BOOK_ID +
                " INNER JOIN " + Constant.AUTHORS_TABLE +
                " ON " + Constant.AUTHORS_TABLE + "." + Constant.AUTHOR_ID + "=" +
                Constant.BOOKS_TABLE + "." + Constant.BOOK_AUTHOR +
                " WHERE " + Constant.WORDS_TABLE + "." + Constant.WORD_NAME + "=?";

        for(String word : words) {
            PreparedStatement preparedStatement = getDbConnection().prepareStatement(selectFromWords);
            preparedStatement.setString(1, word);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                bookSet.add(new BookInfo(resultSet.getString(1), Double.parseDouble(resultSet.getString(2)),
                        resultSet.getString(3),resultSet.getString(4)));
                /*System.out.println(resultSet.getString(1) + " " + resultSet.getString(2) +
                        " " + resultSet.getString(3) + " " + resultSet.getString(4));*/
            }
        }
        return bookSet;
    }

    public void updateBookByName(String name, String newAuthorName, String newAuthorSurname)
            throws SQLException, ClassNotFoundException {
        PreparedStatement statement;

        String updateByName = "UPDATE " + Constant.AUTHORS_TABLE +
                " SET " + Constant.AUTHOR_NAME + "=?," +
                Constant.AUTHOR_SURNAME + "=? WHERE " + Constant.AUTHOR_ID + "= (SELECT " +
                Constant.BOOK_AUTHOR + " FROM " + Constant.BOOKS_TABLE +
                " WHERE " + Constant.BOOK_NAME + "=?)";

        statement = getDbConnection().prepareStatement(updateByName);
        statement.setString(1, newAuthorName);
        statement.setString(2, newAuthorSurname);
        statement.setString(3, name);
        statement.executeUpdate();
    }

    public void deleteBookByName(String name) throws SQLException, ClassNotFoundException {
        //boolean isAuthorExists = isAuthorExistsMoreThanOneTime(name);

        PreparedStatement preparedStatement;

        String delete = "DELETE FROM " + Constant.BOOKS_TABLE + ", " + Constant.KEYWORDS_TABLE +
                " USING " + Constant.BOOKS_TABLE +
                " JOIN " + Constant.KEYWORDS_TABLE + " ON " + Constant.BOOKS_TABLE + "." + Constant.BOOK_ID +
                "=" + Constant.KEYWORDS_TABLE + "." + Constant.KEYWORD_BOOK_ID +
                " WHERE " + Constant.BOOKS_TABLE + "." + Constant.BOOK_NAME + "='" + name + "'";
        preparedStatement = getDbConnection().prepareStatement(delete);
        preparedStatement.executeUpdate();

        checkAuthors();
        checkWords();
    }

    private void checkAuthors() throws SQLException, ClassNotFoundException {
        PreparedStatement preparedStatement;

        String selectAuthors = "DELETE FROM " + Constant.AUTHORS_TABLE +
                " WHERE " + " NOT EXISTS (SELECT * FROM " + Constant.BOOKS_TABLE +
                " WHERE " + Constant.BOOKS_TABLE + "." + Constant.BOOK_AUTHOR + "=" + Constant.AUTHORS_TABLE + "." + Constant.AUTHOR_ID + ")";

        preparedStatement = getDbConnection().prepareStatement(selectAuthors);
        preparedStatement.executeUpdate();
    }

    private void checkWords() throws SQLException, ClassNotFoundException {
        PreparedStatement preparedStatement;

        String selectAuthors = "DELETE FROM " + Constant.WORDS_TABLE +
                " WHERE " + " NOT EXISTS (SELECT * FROM " + Constant.KEYWORDS_TABLE +
                " WHERE " + Constant.KEYWORDS_TABLE + "." + Constant.KEYWORD_WORD_ID + "=" +
                Constant.WORDS_TABLE + "." + Constant.WORD_ID + ")";

        preparedStatement = getDbConnection().prepareStatement(selectAuthors);
        preparedStatement.executeUpdate();
    }

    public boolean isAuthorExistsMoreThanOneTime(String name) throws SQLException, ClassNotFoundException {
        PreparedStatement preparedStatement;
        ResultSet authorsResult;
        boolean isAuthorExists = false;
        int counter = 0;

        String selectAuthor = "SELECT " + Constant.BOOKS_TABLE + "." + Constant.BOOK_AUTHOR +
                " FROM " + Constant.BOOKS_TABLE +
                " WHERE " + Constant.BOOK_AUTHOR + "= (SELECT " +
                Constant.BOOK_AUTHOR + " FROM " + Constant.BOOKS_TABLE +
                " WHERE " + Constant.BOOK_NAME + "='" + name + "')";

        preparedStatement = getDbConnection().prepareStatement(selectAuthor);
        authorsResult = preparedStatement.executeQuery();
        if (authorsResult.next()) {
            counter++;
        }

        if(counter > 1) {
            isAuthorExists = true;
        }
        return isAuthorExists;
    }

}


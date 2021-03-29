package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Controller {

    private static final String REGEX = "(^[0-4]((\\.)[0-9])?$)|(^5((\\.)0)?$)";
    public static Pattern pattern = Pattern.compile(REGEX);

    @FXML
    private TableView<BookInfo> tableView;

    @FXML
    private TableColumn<BookInfo, String> bookNameColumn;

    @FXML
    private TableColumn<BookInfo, String> bookAuthorColumn;

    @FXML
    private TableColumn<BookInfo, String> bookRateColumn;

    @FXML
    private TextField insertBookNameField;

    @FXML
    private TextField insertAuthorSurnameField;

    @FXML
    private TextField insertAuthorNameField;

    @FXML
    private TextField insertKeywordsField;

    @FXML
    private TextField insertRateField;

    @FXML
    private Button insertBtn;

    @FXML
    private Text helpText;

    @FXML
    private TextField updateAuthorSurnameField;

    @FXML
    private TextField updateAuthorNameField;

    @FXML
    private Button updateBtn;

    @FXML
    private TextField findByKeywordsField;

    @FXML
    private Button findBtn;

    @FXML
    private Button deleteBtn;

    ObservableList<BookInfo> olist = FXCollections.observableArrayList();
    DatabaseHandler dbh = new DatabaseHandler();

    @FXML
    void initialize() throws SQLException, ClassNotFoundException {

        // устанавливаем тип и значение которое должно хранится в колонке
        bookNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        bookAuthorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        bookRateColumn.setCellValueFactory(new PropertyValueFactory<>("rate"));

        tableView.setItems(olist);

        initializeBooksIntoTable();

        insertBtn.setOnAction(actionEvent -> {
            if (isInsertInputValid()) {
                ArrayList<String> keywords = new ArrayList<String>(Arrays.asList(insertKeywordsField.getText().split(" ")));
                Book book = new Book(insertBookNameField.getText(), insertAuthorNameField.getText(),
                        insertAuthorSurnameField.getText(), Double.parseDouble(insertRateField.getText()), keywords);
                try {
                    dbh.insertBook(book);
                    initializeBooksIntoTable();
                    clearInsertInput();
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        updateBtn.setOnAction(actionEvent -> {
            if (isUpdateValid()) {
                try {
                    dbh.updateBookByName(tableView.getSelectionModel().getSelectedItem().getName(),
                            updateAuthorNameField.getText(), updateAuthorSurnameField.getText());
                    initializeBooksIntoTable();
                    clearUpdateInput();
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            };
        });

        findBtn.setOnAction(actionEvent -> {
            if (findByKeywordsField.getText().length() < 1) {
                helpText.setText("Enter keywords");
            } else {
                ArrayList<String> keywords = new ArrayList<>(Arrays.asList(
                        findByKeywordsField.getText().split(" ")));
                try {
                    showBooksWindow(dbh.getBooksInfoByKeywords(keywords), keywords);
                } catch (SQLException | ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
            }
        });

        deleteBtn.setOnAction(actionEvent -> {
            if (tableView.getSelectionModel().getSelectedItem() == null) {
                helpText.setText("Select book you want to delete");
            } else {
                try {
                    dbh.deleteBookByName(tableView.getSelectionModel().getSelectedItem().getName());
                    initializeBooksIntoTable();
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showBooksWindow(Set<BookInfo> booksInfoByKeywords, ArrayList<String> keywords) throws IOException {
        // Загружаем fxml-файл и создаём новую сцену
        // для всплывающего диалогового окна.
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Controller.class.getResource("info.fxml"));
        AnchorPane page = (AnchorPane) loader.load();

        // Создаём диалоговое окно Stage.
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Info");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(null);
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        System.out.println(booksInfoByKeywords);
        // Передаём адресата в контроллер.
        InfoController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.setOList(new ArrayList<>(booksInfoByKeywords));
        controller.setKeywordsText(keywords);

        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
        dialogStage.showAndWait();
    }

    private boolean isInsertInputValid() {
        if (insertBookNameField.getText().length() < 1) {
            helpText.setText("Enter book name");
            return false;
        } else if (insertAuthorNameField.getText().length() < 1) {
            helpText.setText("Enter author name");
            return false;
        } else if (insertAuthorSurnameField.getText().length() < 1) {
            helpText.setText("Enter author surname");
            return false;
        } else if (insertRateField.getText().length() < 1) {
            helpText.setText("Enter book rate");
            return false;
        } else if (!pattern.matcher(insertRateField.getText().trim()).find()) {
            helpText.setText("Book rate is invalid");
            return false;
        } else if (insertKeywordsField.getText().length() < 1) {
            helpText.setText("Enter keywords");
            return false;
        } else return true;
    }

    private void clearInsertInput() {
        insertBookNameField.setText("");
        insertAuthorNameField.setText("");
        insertAuthorSurnameField.setText("");
        insertRateField.setText("");
        insertKeywordsField.setText("");
    }

    private boolean isUpdateValid() {
        if (updateAuthorNameField.getText().length() < 1) {
            helpText.setText("Enter author name");
            return false;
        } else if (updateAuthorSurnameField.getText().length() < 1) {
            helpText.setText("Enter author surname");
            return false;
        } else if (tableView.getSelectionModel().getSelectedItem() == null) {
            helpText.setText("Select book you want to update");
            return false;
        } else return true;
    }

    private void clearUpdateInput() {
        updateAuthorNameField.setText("");
        updateAuthorSurnameField.setText("");
    }

    private void initializeBooksIntoTable() throws SQLException, ClassNotFoundException {
        ArrayList<BookInfo> databaseBooks = dbh.getAllBooksInfo();
        olist.clear();
        for(BookInfo bookInfo : databaseBooks) {
            olist.add(bookInfo);
        }
        tableView.refresh();
    }


    /*Book selectedTrain = tableView.getSelectionModel().getSelectedItem();
    tableView.refresh();*/


}


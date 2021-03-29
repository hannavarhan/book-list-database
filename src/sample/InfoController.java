package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class InfoController {

    @FXML
    private Button okBtn;

    @FXML
    private TableView<BookInfo> tableView;

    @FXML
    private TableColumn<BookInfo, String> bookNameColumn;

    @FXML
    private TableColumn<BookInfo, String> bookAuthorColumn;

    @FXML
    private TableColumn<BookInfo, String> bookRateColumn;

    @FXML
    private Text keywordsText;

    ObservableList<BookInfo> olist = FXCollections.observableArrayList();
    private Stage dialogStage;

    @FXML
    private void initialize() {
        bookNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        bookAuthorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        bookRateColumn.setCellValueFactory(new PropertyValueFactory<>("rate"));

        tableView.setItems(olist);

        okBtn.setOnAction(actionEvent -> {
            dialogStage.close();
        });

    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setKeywordsText(ArrayList<String> keywords) {
        StringBuilder sBuilder = new StringBuilder();
        for(String word : keywords) {
            sBuilder.append(" ");
            sBuilder.append(word);
        }
        keywordsText.setText(sBuilder.toString());
    }

    public void setOList(ArrayList<BookInfo> list) {
        olist.clear();
        olist.addAll(list);
        //tableView.refresh();
    }


}

package MyChatProject;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ControllerAuthWindow implements Initializable {
    private String loginStr;
    private String passStr;
    @FXML
    TextField loginField;
    @FXML
    TextField passField;
    @FXML
    Button authButton;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.loginStr = "";
        this.passStr = "";
    }

    public String getLoginStr() {
        return loginStr;
    }

    public String getPassStr() {
        return passStr;
    }

    public void txtRegistrLogin(ActionEvent actionEvent) {
        loginStr = loginField.getText();
        passField.requestFocus();
    }

    public void txtRegistrPass(ActionEvent actionEvent) {
        passStr = passField.getText();
        if (loginStr != null) {
            authButton.requestFocus();
        } else {
            loginField.requestFocus();
        }
    }

    public void btnMakeAuth(ActionEvent actionEvent) {
        loginStr = loginField.getText();
        passStr = passField.getText();
        Button authButton = (Button) actionEvent.getSource();
        Stage stage = (Stage) authButton.getScene().getWindow();
        stage.close();
    }

}

package MyChatProject;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    TextField answerField;
    @FXML
    TextArea dialogArea;
    @FXML
    ComboBox<String> clientsBox;
    @FXML
    Button newClientButton;
    @FXML
    Button changeNickButton;

    private static List<String> messageLogs;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String clientToName;

    public void EchoClient() {
        try {
            openConnection();
        } catch (IOException e) {
            System.out.println("Клиент покинул чат");
        }
    }

    private void openConnection() throws IOException {
        socket = new Socket(ChatParams.SERVER_ADDR, ChatParams.SERVER_PORT);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String strFromServer = in.readUTF();
                        if (strFromServer.startsWith(ChatParams.AUTH_OK_FLAG)) {
                            break;
                        }
                        Controller.printMesLogs(strFromServer, messageLogs, dialogArea);
                    }
                    while (true) {
                        String strFromServer = in.readUTF();
                        if (strFromServer.equalsIgnoreCase(ChatParams.BREAK_FLAG)) {
                            break;
                        }
                        if (strFromServer.startsWith(ChatParams.CLIENTS_SET_FLAG)) {
                            clientsBox.getItems().clear();
                            clientsBox.getItems().add(ChatParams.SEND_TO_ALL_WORD);
                            for (String cl : strFromServer.substring(ChatParams.CLIENTS_SET_FLAG.length() + 1).split(ChatParams.CLIENTS_SET_SEP)) {
                                clientsBox.getItems().add(cl);
                            }
                            Platform.runLater(() -> {
                                clientsBox.getSelectionModel().clearAndSelect(0);
                            });
                        } else {
                            Controller.printMesLogs(strFromServer, messageLogs, dialogArea);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Диалог завершен");
                }
            }
        }).start();
    }

    public void closeConnection() {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        messageLogs = new ArrayList<>();
        dialogArea.setFocusTraversable(false);
        this.EchoClient();
    }

    public void btnSend(ActionEvent actionEvent) {
        Button button = (Button) actionEvent.getSource();
        this.sendMessage();
    }

    public void btnExitAction(ActionEvent actionEvent) {
        Platform.exit();
        try {
            out.writeUTF(ChatParams.BREAK_FLAG);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.closeConnection();
        }
    }

    public void txtSendAnswer(ActionEvent actionEvent) {
        this.sendMessage();
    }

    public void selectClientTo(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        clientToName = element.getSelectionModel().getSelectedItem();
        if (clientToName.equals(ChatParams.SEND_TO_ALL_WORD)) {
            clientToName = "";
        }
    }

    public void sendMessage() {
        if (!answerField.getText().trim().isEmpty()) {
            try {
                out.writeUTF(clientToName.equals("") ? answerField.getText() : ChatParams.PRIVAT_FLAG + " " + clientToName + " " + answerField.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        answerField.clear();
    }

    public static void printMesLogs(String answer, List<String> messageLogs, TextArea dialogArea) {
        messageLogs.add(answer);
        String resultStr = "";
        for (String s : messageLogs) {
            resultStr = resultStr.concat(s).concat("\n");
        }
        dialogArea.setText(resultStr);
    }

    public void btnAuthAction(ActionEvent actionEvent) throws IOException {
        if (socket == null || socket.isClosed()) {
            openConnection();
        }
        try {
            Stage authWindowStage = new Stage();
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/authWindow.fxml")));
            Parent authWindowRoot = loader.load();
            authWindowStage.setTitle("Authentication");
            authWindowStage.setScene(new Scene(authWindowRoot, 300, 200));
            ControllerAuthWindow authController = loader.getController();
            authWindowStage.showAndWait();
            out.writeUTF(ChatParams.AUTH_FLAG + " " + authController.getLoginStr() + " " + authController.getPassStr());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void btnNewClient(ActionEvent actionEvent) throws IOException {
        if (socket == null || socket.isClosed()) {
            openConnection();
        }
        try {
            Stage authWindowStage = new Stage();
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/authWindow.fxml")));
            Parent authWindowRoot = loader.load();
            authWindowStage.setTitle("New Client");
            authWindowStage.setScene(new Scene(authWindowRoot, 300, 200));
            ControllerAuthWindow authController = loader.getController();
            authWindowStage.showAndWait();
            out.writeUTF(ChatParams.NEW_CLIENT_FLAG + " " + authController.getLoginStr() + " " + authController.getPassStr());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

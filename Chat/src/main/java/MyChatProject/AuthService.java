package MyChatProject;

import java.sql.SQLException;

public interface AuthService {
    void addNewClient(String clientName, String password, String nick) throws SQLException;

    void changeClientNick(String clientName, String newNick) throws SQLException;

    void start();

    String getNickByLoginPass(String clientName, String password) throws SQLException;

    void stop();

}


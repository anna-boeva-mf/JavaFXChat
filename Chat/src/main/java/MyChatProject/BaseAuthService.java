package MyChatProject;

import java.sql.*;


public class BaseAuthService implements AuthService {
    private static Connection connection;


    @Override
    public void start() {
        System.out.println("Сервис аутентификации запущен");
    }

    @Override
    public void stop() {
        System.out.println("Сервис аутентификации остановлен");
    }

    public void connect()  {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:database.db");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    public void disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public BaseAuthService() {
    }

    @Override
    public String getNickByLoginPass(String clientName, String password) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:database.db");
        PreparedStatement ps = connection.prepareStatement("select NICKNAME from CLIENTS where CLIENT_NAME = ? and PASSWORD = ?;");
        ps.setString(1, clientName);
        ps.setString(2, password);
        String rs = ps.executeQuery().getString(1);
        connection.close();
        return rs ;
    }

    @Override
    public void addNewClient(String clientName, String password, String nick) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:database.db");
        PreparedStatement ps = connection.prepareStatement("INSERT INTO CLIENTS (CLIENT_NAME, PASSWORD, NICKNAME) VALUES (?, ?, ?);");
        ps.setString(1, clientName);
        ps.setString(2, password);
        ps.setString(3, nick);
        ps.executeUpdate();
        connection.close();
    }

    @Override
    public void changeClientNick(String clientName, String newNick) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:database.db");
        PreparedStatement ps = connection.prepareStatement("update CLIENTS set NICKNAME = ? where CLIENT_NAME = ?;");
        ps.setString(1, newNick);
        ps.setString(2, clientName);
        ps.executeUpdate();
        connection.close();
    }
}

package MyChatProject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;

public class ClientHandler {
    private EchoServer echoServer;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String name = "";

    public String getName() {
        return name;
    }

    public ClientHandler(EchoServer echoServer, Socket socket) {
        try {
            this.echoServer = echoServer;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.name = "";
            Thread threadWaitAuth = new Thread(() -> {
                try {
                    Thread.sleep(120 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (name.equals("")) {
                    closeNotAuthConnection();
                }
            });
            threadWaitAuth.setDaemon(true);
            Thread mainThread = new Thread(() -> {
                try {
                    authentication();
                    System.out.println("Клиент авторизовался");
                    readMessages();
                } catch (SocketException e) {
                    System.out.println("Подключение прервано");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (name.equals("")) {
                        closeNotAuthConnection();
                    } else {
                        closeConnection();
                    }
                }
            });
            threadWaitAuth.start();
            mainThread.start();
        } catch (IOException e) {
            throw new RuntimeException("Проблемы при создании обработчика клиента");
        }
    }

    public void authentication() throws IOException {
        while (true) {
            String str = in.readUTF();
            if (str.startsWith(ChatParams.AUTH_FLAG)) {
                String[] parts = str.split("\\s");
                String nick = null;
                try {
                    nick = echoServer.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                } catch (SQLException e) {
                    System.out.println("Неверный пользователь или пароль");
                }
                if (nick != null) {
                    if (!echoServer.isNickBusy(nick)) {
                        sendMsg(ChatParams.AUTH_OK_FLAG + " " + nick);
                        name = nick;
                        echoServer.broadcastMsg(name + " зашел в чат");
                        echoServer.subscribe(this);
                        return;
                    } else {
                        sendMsg("Учетная запись уже используется");
                    }
                }
            } else if (str.startsWith(ChatParams.NEW_CLIENT_FLAG)) {
                String[] parts = str.split("\\s");
                String nick = null;
                try {
                    echoServer.getAuthService().addNewClient(parts[1], parts[2], parts[1]);
                    nick = parts[1];
                } catch (SQLException e) {
                    System.out.println("Учетная запись с таким именем уже используется");
                }
                if (nick != null) {
                    if (!echoServer.isNickBusy(nick)) {
                        sendMsg(ChatParams.AUTH_OK_FLAG + " " + nick);
                        name = nick;
                        echoServer.broadcastMsg(name + " зашел в чат");
                        echoServer.subscribe(this);
                        return;
                    } else {
                        sendMsg("Ник уже используется");
                    }
                }
                // для смены ника отправить запрос: /changenick имя_клиента новый_ник
            } else {
                sendMsg("Неверные логин/пароль");
            }
        }
    }

    public void readMessages() throws IOException {
        while (true) {
            String strFromClient = in.readUTF();
            System.out.println("от " + name + ": " + strFromClient);
            if (strFromClient.equals(ChatParams.BREAK_FLAG)) {
                return;
            }
            if (strFromClient.startsWith(ChatParams.PRIVAT_FLAG)) {
                String[] tokens = strFromClient.split("\\s");
                String nickTo = tokens[1];
                String mesIs = strFromClient.substring(ChatParams.PRIVAT_FLAG.length() + 2 + nickTo.length());
                echoServer.sendPrivatMsg(this, nickTo, nickTo + ": " + mesIs);
            } else if (strFromClient.startsWith(ChatParams.CHANGE_NICK_FLAG)) {
                String[] parts = strFromClient.split("\\s");
                try {
                    echoServer.getAuthService().changeClientNick(parts[1], parts[2]);
                    name = parts[2];
                } catch (SQLException e) {
                    System.out.println("Ник занят");
                }
            } else {
                echoServer.broadcastMsg(name + ": " + strFromClient);
            }
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeNick(String clientName, String newNick) {
        try {
            echoServer.getAuthService().changeClientNick(clientName, newNick);
        } catch (SQLException e) {
            System.out.println("Такой ник уже используется");
        }
    }

    public void addNewClient(String clientName, String password, String nick) {
        try {
            echoServer.getAuthService().addNewClient(clientName, password, nick);
        } catch (SQLException e) {
            System.out.println("Такой пользователь уже существует");
        }
    }

    public void closeConnection() {
        echoServer.unsubscribe(this);
        echoServer.broadcastMsg(name + " вышел из чата");
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

    public void closeNotAuthConnection() {
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
}

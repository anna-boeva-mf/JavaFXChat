package MyChatProject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class EchoServer {
    private List<ClientHandler> clients;
    private AuthService authService;

    public AuthService getAuthService() {
        return authService;
    }

    public EchoServer() {
        try (ServerSocket server = new ServerSocket(ChatParams.SERVER_PORT)) {
            authService = new BaseAuthService();
            authService.start();
            clients = new ArrayList<>();
            while (true) {
                System.out.println("Сервер ожидает подключения");
                Socket socket = server.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            System.out.println("Ошибка в работе сервера");
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }

    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler o : clients) {
            if (o.getName().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public synchronized String makeClientsList() {
        StringJoiner strJ = new StringJoiner(ChatParams.CLIENTS_SET_SEP);
        for (ClientHandler o : clients) {
            strJ.add(o.getName());
        }
        return strJ.toString();
    }

    public synchronized void broadcastClientsList() {
        String clientsList = makeClientsList();
        for (ClientHandler o : clients) {
            o.sendMsg(ChatParams.CLIENTS_SET_FLAG + " " + clientsList);
        }
    }

    public synchronized void broadcastMsg(String msg) {
        for (ClientHandler o : clients) {
            o.sendMsg(msg);
        }
    }

    public synchronized void sendPrivatMsg(ClientHandler clientFrom, String nickTo, String msg) {
        for (ClientHandler o : clients) {
            if (o.getName().equals(nickTo)) {
                o.sendMsg(msg);
                clientFrom.sendMsg(msg);
                return;
            }
        }
        clientFrom.sendMsg("Пользователь " + nickTo + " сейчас не в сети");
    }

    public synchronized void unsubscribe(ClientHandler o) {
        clients.remove(o);
    }

    public synchronized void subscribe(ClientHandler o) {
        clients.add(o);
        broadcastClientsList();
        o.sendMsg("Вы авторизовались как " + o.getName());
        o.sendMsg("Пользователи онлайн: " + makeClientsList());
    }

    public void changeClientNick(String part, String part1) {

    }
}

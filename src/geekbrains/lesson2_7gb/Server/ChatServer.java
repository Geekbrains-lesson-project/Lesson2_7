package geekbrains.lesson2_7gb.Server;
import geekbrains.lesson2_7gb.auth.AuthenticationService;
import geekbrains.lesson2_7gb.auth.BasicAuthenticationService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ChatServer implements Server {
    private Set<ClientHandler> clients;
    private AuthenticationService authenticationService;
    public ChatServer() {
        try {
            System.out.println("Server is starting up...");
            ServerSocket serverSocket = new ServerSocket(8888);
            clients = new HashSet<>();
            authenticationService = new BasicAuthenticationService();
            System.out.println("Server is started up...");
            while (true) {
                System.out.println("Server is listening for clients...");
                Socket socket = serverSocket.accept();
                System.out.println("Client accepted: " + socket);
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            throw new RuntimeException("SWW", e);
        }
    }


    // 2. * Реализовать личные сообщения, если клиент пишет «/w nick3 Привет»,
    // то только клиенту с ником nick3 должно прийти сообщение «Привет»

    @Override
    public synchronized void sendPrivateMessage(String message, String from) {
        String nickname = "[whisper]" + from;
        String[] privateMessageValues = message.split("\\s");
        if (privateMessageValues.length > 2) {
            if (isLoggedIn(privateMessageValues[1])) {
                String content = String.join("\s",
                        Arrays.copyOfRange(privateMessageValues, 2, privateMessageValues.length));
                clients.stream().
                        filter(clientHandler -> clientHandler.getName().equals(privateMessageValues[1])).
                        findFirst().
                        get().
                        sendMessage(doMessage(content, nickname));
            }
        }
    }

    @Override
    public synchronized void broadcastMessage(String message, String from) {
        clients.forEach(client -> client.sendMessage(doMessage(message, from)));
    }

    @Override
    public synchronized boolean isLoggedIn(String nickname) {
        return clients.stream()
                .filter(clientHandler -> clientHandler.getName().equals(nickname))
                .findFirst()
                .isPresent();
    }
    @Override
    public synchronized void subscribe(ClientHandler client) {
        clients.add(client);
    }
    @Override
    public synchronized void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }
    @Override
    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    private String doMessage(String message, String nickname) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss")) +
                " " + nickname + " whispers: " + message;
    }

}
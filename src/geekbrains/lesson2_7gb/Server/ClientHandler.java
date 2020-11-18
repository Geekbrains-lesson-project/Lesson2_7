package geekbrains.lesson2_7gb.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientHandler {

    private final String exitMessage = "-exit";
    private final String privateMessage = "/w";
    private final String authMessage = "-auth";
    private final int timeout = 4000;


    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String name;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            doListen();
        } catch (IOException e) {
            throw new RuntimeException("SWW", e);
        }
    }
    public String getName() {
        return name;
    }
    private void doListen() {
        new Thread(() -> {
            try {
                doAuth();
                receiveMessage();
            } catch (Exception e) {
                throw new RuntimeException("SWW", e);
            } finally {
                server.unsubscribe(this);
            }
        }).start();
    }

    private void doAuth() {
        AtomicBoolean isAuthSucceed = new AtomicBoolean(false);

        try {
            while (!isAuthSucceed.get()) {

                String credentials = in.readUTF();
                /**
                 * Input credentials sample
                 * "-auth n1@mail.com 1"
                 */
                if (credentials.startsWith(authMessage)) {
                    /**
                     * After splitting sample
                     * array of ["-auth", "n1@mail.com", "1"]
                     */
                    String[] credentialValues = credentials.split("\\s");
                    server.getAuthenticationService()
                            .doAuth(credentialValues[1], credentialValues[2])
                            .ifPresentOrElse(
                                    user -> {
                                        if (!server.isLoggedIn(user.getNickname())) {
                                            sendMessage("cmd auth: Status OK");
                                            name = user.getNickname();
                                            server.broadcastMessage(name + " is logged in.", name);
                                            server.subscribe(this);
                                            isAuthSucceed.set(true);
                                        } else {
                                            sendMessage("Current user is already logged in.");
                                        }
                                    },
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            sendMessage("No a such user by email and password.");
                                        }
                                    }
                            );
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("SWW", e);
        }
    }

/**
 * Receives input data from {@link ClientHandler#in} and then broadcast via {@link Server#broadcastMessage(String, String)}
 */



// Варианты для 3го задания:
//Future<?> awaitingAuth = Executors.newSingleThreadExecutor ().submit (() -> {
//    while (true) {
//        if (inMessage.hasNext ()) {
//            String clientMessage = inMessage.nextLine ();
//            if (isAuth (clientMessage)) {
//                return getLogin (clientMessage);
//            }
//        }
//
//    }
//    return null;
//});
//
//            try {
//        String login = awaitingAuth.get (120, TimeUnit.SECONDS);
//    } catch (TimeoutException e) {
//
//   обработка таймаута ...пока на подумать.




private void receiveMessage() {
    try {
        while (true) {
            String message = in.readUTF();
            if (message.equals(exitMessage)) {
                return;
            }
            if (message.startsWith(privateMessage)) {
                server.sendPrivateMessage(message, name);
            } else {
                server.broadcastMessage(message, name);
            }
        }
    } catch (IOException e) {
        throw new RuntimeException("SWW", e);
    }
}
    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            throw new RuntimeException("SWW", e);
        }
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientHandler that = (ClientHandler) o;
        return Objects.equals(server, that.server) &&
                Objects.equals(socket, that.socket) &&
                Objects.equals(in, that.in) &&
                Objects.equals(out, that.out) &&
                Objects.equals(name, that.name);
    }
    @Override
    public int hashCode() {
        return Objects.hash(server, socket, in, out, name);
    }
}
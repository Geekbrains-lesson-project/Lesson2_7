package geekbrains.lesson2_7gb.auth;
import geekbrains.lesson2_7gb.entity.User;
import java.util.Optional;

public interface AuthenticationService {

    Optional<User> doAuth(String login, String password);
}


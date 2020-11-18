package geekbrains.lesson2_7gb.auth;
import geekbrains.lesson2_7gb.entity.User;

import java.util.List;
import java.util.Optional;

public class BasicAuthenticationService implements geekbrains.lesson2_7gb.auth.AuthenticationService {

    /**
     * Fake database with stubbed entities
     */
    private static final List<User> users;

    static {
        users = List.of(
                new User("Roman", "roman@mail.com", "1"),
                new User("Alex", "alex@mail.com", "2"),
                new User("Fill", "fill@mail.com", "3")
        );
    }

    @Override
    public Optional<User> doAuth(String email, String password) {
        for (User user : users) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
}
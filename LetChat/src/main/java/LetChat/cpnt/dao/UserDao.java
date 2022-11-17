package LetChat.cpnt.dao;

import java.util.List;
import LetChat.cpnt.domain.User;

public interface UserDao {

    void add(User user);

    User get(String id);
    void update(User user);

    List<User> getAll();

    void deleteAll();

}

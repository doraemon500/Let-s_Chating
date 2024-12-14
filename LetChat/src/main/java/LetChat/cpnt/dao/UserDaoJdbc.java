package LetChat.cpnt.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class UserDaoJdbc implements UserDao {

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    private JdbcTemplate jdbcTemplate;
    private RowMapper<User> userMapper =
            new RowMapper<User>() {
                public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                    User user = new User();
                    user.setId(rs.getString("id"));
                    user.setPassword(rs.getString("pwd"));
                    user.setIp(rs.getString("ip"));
                    user.setOpensign(rs.getString("opensign"));
                    return user;
                }
            };


    public void add(final User user) {
        this.jdbcTemplate.update("insert into users(id, pwd, ip, opensign) values(?,?,?,?)",
                user.getId(),  user.getPassword(), user.getIp(), user.getOpensign());
    }

    public User get(String id) {
        return this.jdbcTemplate.queryForObject("select * from users where id = ?",
                new Object[] {id}, this.userMapper);
    }

    public void update(final User user) {
        this.jdbcTemplate.update("update users set ip = ? , opensign = ? where id = ? and pwd = ?",
                new Object[] {user.getIp(), user.getOpensign(), user.getId(),user.getPassword() });
    }


    public void deleteAll() {
        this.jdbcTemplate.update("delete from users");
    }


    public List<User> getAll() {
        return this.jdbcTemplate.query("select * from users where opensign = ? order by id", new Object[] {"300"},this.userMapper);
    }


}
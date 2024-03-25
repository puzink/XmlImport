package app;

import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Getter
public class DbConnection {

    private String username;
    private String pass;
    private String url;

    public DbConnection(String username, String pass, String url) {
        this.username = username;
        this.pass = pass;
        this.url = url;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, pass);
    }
}

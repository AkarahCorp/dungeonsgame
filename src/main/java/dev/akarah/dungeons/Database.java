package dev.akarah.dungeons;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class Database {
    static Connection databaseConnection;

    public static void tryConnectDb() {
        try {
            Class.forName("org.postgresql.Driver");

            var props = new Properties();
            props.load(new FileInputStream(Paths.get("./.env").toFile()));

            String url = (String) props.remove("url");

            Database.databaseConnection = DriverManager.getConnection(url, props);
        } catch (IOException | ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sql(String statement) {
        Thread.ofVirtual().start(() -> {
            try {
                Database.conn().prepareStatement(statement).execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<ResultSet> sqlResult(String statement) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var preparedStatement = Database.conn().prepareStatement(statement);
                return preparedStatement.executeQuery();
            } catch (SQLException e) {
                return null;
            }
        });
    }

    public static void tryInitializeDb() {
        Database.sql("""
                    create table if not exists players (
                        uuid text not null,
                        username text not null,
                        inventory text not null
                    )
                    """);

        Database.sql("""
                    alter table players
                        add column if not exists experience
                        int not null default 0;
                    """);

        Database.sql("""
                alter table players
                    add column if not exists essence
                    int not null default 0;
                """);
    }

    public static Connection conn() {
        return databaseConnection;
    }
}

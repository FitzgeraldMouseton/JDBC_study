package connectionpool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionPool {

    /*
    Создание и закрытие соединений требует затрат ресурсов, поэтому лучше иметь пул открытых соединений с БД.
    В этом случае каждый поток, которому нужно подключиться к базе, будет использовать уже имеющееся в пуле соединение.
     */

    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/jdbc_practice?user=root&password=password&serverTimezone=UTC");
        dataSource = new HikariDataSource(config);
        dataSource.setMaximumPoolSize(4);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}

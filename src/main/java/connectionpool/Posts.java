package connectionpool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Posts {

    public void tryConnection() {

        try (Connection connection = ConnectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement("select * from posts")) {

            statement.execute();

            String msg = Thread.currentThread().getName() + " --> " + this.getConnectionId(connection);
            System.out.println(msg);

        } catch (SQLException exception) {
            System.out.println(exception.getErrorCode());
        }
    }

    private String getConnectionId(Connection connection) {
        String conId = connection.toString();
        int lastPos = conId.length() - 2;
        conId = conId.substring(lastPos);
        return conId;
    }
}

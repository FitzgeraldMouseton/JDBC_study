import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/*
Clob - формат для хранения больших(или не очень) объемов текста. Записывается в поля формата TEXT, MEDIUMTEXT, LONGTEXT.
Blob - формат для хранения изображений и т.п.. Бывают TINYBLOB, BLOB, MEDIUMBLOB и LONGBLOB
 */
public class ClobsAndBlobs {
    public static void main(String[] args) {

        String dbUrl = "jdbc:mysql://localhost:3306/jdbc_practice?serverTimezone=UTC";
        String user = "root";
        String password = "password";

        try (Connection connection = DriverManager.getConnection(dbUrl, user, password)) {

            // CLOB
            // Запись и чтение большого текста
            String writeTextQuery = "update posts set full_text = ? where id = 1";
            String readTextQuery = "select full_text from posts where id = 1";
            Path path = Path.of("books/for whom the bell tolls.txt");
            try (PreparedStatement writeStatement = connection.prepareStatement(writeTextQuery);
                 PreparedStatement readStatement = connection.prepareStatement(readTextQuery);
                 BufferedReader reader = Files.newBufferedReader(path);) {

                // write
                writeStatement.setCharacterStream(1, reader);
                writeStatement.executeUpdate();

                // read
                try (final ResultSet resultSet = readStatement.executeQuery();) {
                    while (resultSet.next()) {
                        try (final Reader characterStream = resultSet.getCharacterStream(1);
                        final BufferedReader bufferedReader = new BufferedReader(characterStream);) {
                            String line;
                            while ((line = bufferedReader.readLine()) != null) {
                                System.out.println(line);
                            }
                        }
                    }
                }
            }

            // BLOB
            // Запись и чтение картинок
            String writeImageQuery = "update posts set image = ? where id = 1";
            String readImageQuery = "select image from posts where id = 1";
            File image = new File("images/The_dance_of_rats.jpg");
            try (PreparedStatement writeStatement = connection.prepareStatement(writeImageQuery);
                 PreparedStatement readStatement = connection.prepareStatement(readImageQuery);
                 FileInputStream inputStream = new FileInputStream(image);) {

                // write
                writeStatement.setBinaryStream(1, inputStream);
                writeStatement.executeUpdate();

                // read
                try (final ResultSet resultSet = readStatement.executeQuery();) {
                    File newImage = new File("images/The_dance_of_rats_new.jpg");
                    while (resultSet.next()) {
                        final InputStream binaryStream = resultSet.getBinaryStream(1);
                        final FileOutputStream outputStream = new FileOutputStream(newImage);
                        byte[] buffer = new byte[1024];
                        while (binaryStream.read(buffer) > 0) {
                            outputStream.write(buffer);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws IOException {

        // ========================================Установка соединения=================================================

        /*
        В pom добавляем зависимость для mysql коннектора. Дальше для простоты можно было бы просто три строки создать,
        но просто в учебных целях мы тут еще записываем и читаем свойства из файла при помощи класса Properties.
         */

        String pathToProperties = "src/main/resources/application.properties";
        String dbUrl = "jdbc:mysql://localhost:3306/jdbc_practice?serverTimezone=UTC";

        //Способ записи properties в файл
        Properties writeProperties = new Properties();
        writeProperties.setProperty("DB_URL", dbUrl);
        writeProperties.setProperty("username", "root");
        writeProperties.setProperty("password", "password");
        writeProperties.store(new FileOutputStream(pathToProperties), "");

        //Чтение properties из файла
        Properties readProperties = new Properties();
        String url = null;
        String username = null;
        String password = null;
        try (InputStream is = new FileInputStream(pathToProperties)) {
            readProperties.load(is);
            url = readProperties.getProperty("DB_URL");
            username = readProperties.getProperty("username");
            password = readProperties.getProperty("password");
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        // Раньше нужно было загружать нужный драйвер такой строкой, осталось в legacy коде
        // Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();

        // Наконец дошли до получения соединения
        try (Connection connection = DriverManager.getConnection(url, username, password);
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery("SELECT * FROM POSTS");) {

            /*
            Это основной паттерн обхода resultset'a. Каждая запись в resultset представляет собой одну строчку sql таблицы.
            JDBC, понятное дело, не достает из БД объекты, поэтому программисту самому нужно решать, что конкретно, какие
            поля ему нужны. Для этого существует множество методов типа getString(), getInt(), getTime(),
             getBinaryStream и т.д., где в качестве параметра передается либо номер, либо название столбца
            */
            while (resultSet.next()) {
                String title = resultSet.getString("title");
                System.out.println(title);
            }
            System.out.println("\n====================================\n");

            /*
            Главное отличие PreparedStatement от Statement заключается в следующем. При отправке запроса к БД,
            БД сначала парсит запрос, затем составляет план оптимального выполнения этого запроса и, наконец, выполняет его.
            При использовании PreparedStatement этот план сохраняется для последующего использования, что ускоряет дело,
            если мы часто используем один и тот же запрос. Как правило нет причин использовать Statement.
             */

            printAllWithTitleLengthBetween(connection, 5, 15);
            System.out.println("\n====================================\n");
            printParticularFields(connection);
            System.out.println("\n====================================\n");
            printAllWithDifferentNavigationMethods(connection);

            // ========================================Запросы для изменения таблиц=====================================

            setViewCountToPost(connection, 1, 156);

//            addPost(connection, 1, "ACCEPTED", "Frufru", "2020-04-05 00:00:01",
//                    "Mouse", 6, 3, 4);
            deletePost(connection, 21);

        } catch (Exception ex) {
            handleException(ex);
        }
        // =============================================================================================================
    }

    private static void printAllWithTitleLengthBetween(Connection connection, int minLength, int maxLength) {

        // Передача параметров в запрос. В принципе все очевидно
        try (PreparedStatement statement =
                     connection.prepareStatement("SELECT * FROM POSTS where char_length(title) between ? and ?")){
            statement.setInt(1, minLength);
            statement.setInt(2, maxLength);


            // Здесь мы не можем поместить resultset в первый try-with-resources, т.к. мы устанавливаем параметры statement
            // уже в теле этого блока, а resultset, естественно, доджен создаваться с уже установленными параметрами
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String title = resultSet.getString("title");
                    System.out.println(title + ": " + title.length());
                }
            }
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    private static void printParticularFields(Connection connection) {
        try (PreparedStatement statement =
                     connection.prepareStatement("Select title as book, users.name as author, text from posts" +
                                                    " join users on posts.user_id = users.id where char_length(text) < 200 ");
             ResultSet resultSet = statement.executeQuery()) {


            while (resultSet.next()) {
                String book = resultSet.getString("book");
                String author = resultSet.getString("author");
                String text = resultSet.getString("text");
                System.out.printf("%-35s %-35s %-10s%n", book, author, text);
            }
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    private static void printAllWithDifferentNavigationMethods(Connection connection) {
        try (PreparedStatement statement =
                     connection.prepareStatement("Select title from posts where char_length(text) < 200 ");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String title = resultSet.getString("title");
                System.out.println(title);
            }

            System.out.println("======================================");

             // Первая запись
            if (resultSet.first()) {
                String title = resultSet.getString("title");
                System.out.println("First: " + title);
            }

            // Последняя запись
            if (resultSet.last()) {
                String title = resultSet.getString("title");
                System.out.println("Last: " + title);
            }

            // Запись под определенным номером
            if (resultSet.absolute(2)) {
                String title = resultSet.getString("title");
                System.out.println("Row 2: " + title);
            }

            // Запись под через заданное количество рядов от текущего положения
            if (resultSet.relative(2)) {
                String title = resultSet.getString("title");
                System.out.println("Row 2 after 2: " + title);
            }

            // Установка курсора в конец resultset (сразу за последним элементом) и обход в обратном направлении
            // Противоположный метод - beforeFirst()
            System.out.println("======================================");
            resultSet.afterLast();
            while (resultSet.previous()) {
                String title = resultSet.getString("title");
                System.out.println(title);
            }

        } catch (Exception ex) {
            handleException(ex);
        }
    }

    // Выполнение запроса по внесению изменений в данные таблицы
    private static void setViewCountToPost(Connection connection, int postId, int viewCount) {

        String setViewCountQuery = "Update posts set view_count = ? where id = ?";

        try (final PreparedStatement statement = connection.prepareStatement(setViewCountQuery)) {

            statement.setInt(1, 156);
            statement.setInt(2, postId);
            // Выражение помимо исновной функции возвращает количество измененных строк.
            // В нашем случае это без надобности, но вообще может пригодиться
            final int i = statement.executeUpdate();
            System.out.println(i);
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    // Добавление поста
    private static void addPost(Connection connection, int isActive, String moderationStatus,
                                String text, String time, String title, int view_count, int moderator_id, int user_id) {

        String addPostQuery = "Insert into posts (is_active, moderation_status, text, time, title," +
                                " view_count, moderator_id, user_id) values (?,?,?,?,?,?,?,?)";

        // Для автогенерации id добавляем соответствующий параметр
        try (final PreparedStatement statement = connection.prepareStatement(addPostQuery, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, isActive);
            statement.setString(2,moderationStatus);
            statement.setString(3, text);
            statement.setString(4, time);
            statement.setString(5, title);
            statement.setInt(6, view_count);
            statement.setInt(7, moderator_id);
            statement.setInt(8, user_id);
            statement.executeUpdate();

            try (final ResultSet generatedKeys = statement.getGeneratedKeys()) {
                System.out.println("Generated keys");
                while (generatedKeys.next()) {
                    int key = generatedKeys.getInt(1);
                    System.out.println("Key: " + key);
                }
            }
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    // Удаление поста
    /*
    На самом деле не принято удалять строки из таблиц. Обычно их просто помечают, как удаленные, устанавливая соответствующее
    значение в специальном столбце таблицы. Это связано с тем, что данные могут все равно быть полезны (например для продажи
    цру или уолмарту), а так же могут содержать поля, используемые в качестве foreign key другими таблицами.
    Но мы все равно удалим пост.
     */
    private static void deletePost (Connection connection, int postId) {

        String setViewCountQuery = "delete from posts where id = ?";

        try (final PreparedStatement statement = connection.prepareStatement(setViewCountQuery)) {

            statement.setInt(1, postId);
            statement.executeUpdate();
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    private static void handleException(Exception ex) {
        System.out.println(ex.getMessage());
        if (ex instanceof SQLException) {
            /*
            SQLException несет в себе некоторую дополнительную информацию.
            Error code - вендеро-спецефичный код в формате int. Посмотреть, что означает тот или иной код, можно на
            сайте https://www.briandunning.com/error-codes/ (хоть это и капец как странно)
            */
            SQLException sqlException = (SQLException) ex;
            System.out.println("Error code: " + sqlException.getErrorCode());
            System.out.println("SQL state: " + sqlException.getSQLState());
        }
    }
}

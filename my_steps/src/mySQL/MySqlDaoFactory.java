package mySQL;

import annotations.KeyField;
import annotations.TaggedObject;
import classes.ReflectionHelper;
import dao.DaoFactory;
import dao.GenericDao;
import dao.ReflectionJdbcDao;
import exceptions.NoKeyFieldsException;
import exceptions.NoTableTitleException;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Фабрика для создания Data Access Object'ов
 */
public class MySqlDaoFactory implements DaoFactory<Connection> {

    private String user = "kost";//Логин пользователя
    private String password = "webster";//Пароль пользователя
    private String url = "jdbc:mysql://localhost:3306/";//URL адрес
    private String driver = "com.mysql.jdbc.Driver";//Имя драйвера
    private Map<String, DaoCreator> creators; //"Создаватели" подключений

    /**
     * получает подключение к базе
     * @return
     * @throws SQLException
     */
    @Override
    public Connection getContext() throws SQLException {
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return  connection;
    }

    @Override
    public ReflectionJdbcDao getDao(final Connection connection, final Class<?> daoClass)
            throws SQLException, NoTableTitleException, NoKeyFieldsException{

        TaggedObject tag = daoClass.getAnnotation(TaggedObject.class);
        if (tag == null || tag.name().length()==0) {
            throw new NoTableTitleException("Class " + daoClass.getSimpleName() +
                    " is not tagged");
        }

        if (ReflectionHelper.getKeyFields(daoClass).isEmpty()) {
            throw new NoKeyFieldsException("Class " + daoClass.getSimpleName() +
                    " has no key fields");
        }

        if (creators.containsKey(daoClass)) {
            return creators.get(daoClass).create(connection);
        }
        DaoCreator<Connection> creator = new DaoCreator<Connection>() {
            @Override
            public ReflectionJdbcDao create(Connection context) {
                return new GenericDao(connection, daoClass);
            }
        };

        createTable(connection, daoClass);
        creators.put(tag.name(), creator);

        return creator.create(connection);
    }

    public MySqlDaoFactory() {
        try {
            Class.forName(driver);//Регистрируем драйвер
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String sql = "CREATE SCHEMA IF NOT EXISTS `yandex_dao` DEFAULT CHARACTER SET utf8 ;";
        try {
            getContext().createStatement().executeUpdate(sql);
            //url += "yandex_dao";
        } catch (SQLException e) {
            e.printStackTrace();
        }
        creators = new HashMap<>();
    }

    /**
     * Создаёт таблицу в БД на основе передаваемого класса
     * по валидным полям класса
     * @param connection подключение к бд
     * @param daoClass класс
     * @throws NoKeyFieldsException
     */
    private void createTable (Connection connection, Class<?> daoClass) throws NoKeyFieldsException {
        String createTableQuery = getTableCreationQuery(daoClass);
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(createTableQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Создаёт SQL запрос на создание таблицы
     * @param daoClass класс
     * @return
     * @throws NoKeyFieldsException
     */
    private String getTableCreationQuery (Class<?> daoClass) throws NoKeyFieldsException {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS \n`yandex_dao`.`" +
                daoClass.getAnnotation(TaggedObject.class).name()+"` (\n";

        for (Field field: ReflectionHelper.getAllValidFields(daoClass)) {
            if (ReflectionHelper.typeNames.containsKey(field.getType())) {
                createTableQuery += getFieldRecord(field) + "\n";
            }
        }

        List<Field> keyFields = ReflectionHelper.getKeyFields(daoClass);
        createTableQuery += " PRIMARY KEY  (";
        for (int i = 0; i < keyFields.size(); i++) {
            createTableQuery += "`" + ReflectionHelper.underScorize(keyFields.get(i).getName())+"`";
            if (i < keyFields.size() - 1)
                createTableQuery += ", ";
        }
        createTableQuery += ") );";

        return createTableQuery;
    }

    /**
     * Создаёт запись пол для использования в запросе
     * @param field поле
     * @return
     */
    private String getFieldRecord (Field field) {
        return " `" + ReflectionHelper.underScorize(field.getName()) + "` " +
                ReflectionHelper.typeNames.get(field.getType()) +
                (field.isAnnotationPresent(KeyField.class) ? " NOT NULL, " :
                        " NULL, ");
    }
}

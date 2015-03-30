package dao;

import exceptions.NoKeyFieldsException;
import exceptions.NoTableTitleException;

import java.sql.SQLException;

/**
 * User: kost
 * Date: 01.03.2015
 * Time: 7:37
 */
public interface DaoFactory <Connection> {

    /** Интерфейс для создания dao */
    public interface DaoCreator<Connection> {
        public ReflectionJdbcDao create(Connection context);
    }

    /** Возвращает подключение к базе данных */
    public Connection getContext() throws SQLException;

    /** Возвращает объект для управления персистентным состоянием объекта */
    public ReflectionJdbcDao getDao(Connection connection, Class<?> dtoClass) throws SQLException, NoTableTitleException,
            NoKeyFieldsException;
}

package Tests;

import classes.Employee;
import dao.DaoFactory;
import dao.ReflectionJdbcDao;
import exceptions.NoKeyFieldsException;
import exceptions.NoTableTitleException;
import mySQL.MySqlDaoFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Был вспомогательным классом для тестирования
 */
public class A {
    public static void main(String[] args) {
        DaoFactory<Connection> factory = new MySqlDaoFactory();
        Connection connection = null;
        try {
            connection = factory.getContext();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Class<Employee> cls = Employee.class;
        ReflectionJdbcDao dao = null;
        try {
            dao = factory.getDao(connection, cls);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NoTableTitleException e) {
            e.printStackTrace();
        } catch (NoKeyFieldsException e) {
            e.printStackTrace();
        }

        dao.update(new Employee(1, "Frank", "Austin"));
    }
}

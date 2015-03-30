package Tests;

import classes.City;
import dao.DaoFactory;
import dao.ReflectionJdbcDao;
import exceptions.NoKeyFieldsException;
import exceptions.NoTableTitleException;
import mySQL.MySqlDaoFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * User: kost
 * Date: 02.03.2015
 * Time: 2:23
 */
public class MySqlDaoSimpleTest {
    private Connection connection;
    private ReflectionJdbcDao<City> daoCity;
    private Class<?> cls = City.class;

    private static final DaoFactory<Connection> factory = new MySqlDaoFactory();

    @Before
    public void setUp () throws SQLException, NoKeyFieldsException, NoTableTitleException {
        connection = factory.getContext();
        connection.setAutoCommit(false);
        daoCity = factory.getDao(connection, cls);
    }

    @Test
    public void simpleTestCRUD () {
        daoCity.insert(new City("Moscow", "Russia", 16000000, true));
        daoCity.insert(new City("London", "UK", 10000000, true));
        daoCity.insert(new City("Beloretsk", "Russia", 60000, false));
        daoCity.insert(new City("Beijing", "China", 15000000, true));
        daoCity.insert(new City("Tokyo", "Japan", 14000000, true));

        Assert.assertEquals(daoCity.selectAll().size(), 5);

        City fake = new City("Beloretsk", "Russia");
        Assert.assertEquals((daoCity.selectByKey(fake)).getPopulation(), 60000);

        daoCity.update(new City("Beloretsk", "Russia", 75000, false));
        Assert.assertEquals((daoCity.selectByKey(fake)).getPopulation(), 75000);

        fake = new City("London", "UK");
        daoCity.deleteByKey(fake);
        Assert.assertEquals(daoCity.selectAll().size(), 4);

        Assert.assertEquals(daoCity.selectByKey(fake), null);

        Assert.assertEquals(daoCity.selectByKey(new City("Moscow", "Russia")),
                new City("Moscow", "Russia", 16000000, true));

        Assert.assertNotEquals(daoCity.selectByKey(new City("Moscow", "Russia")),
                new City("Moscow", "Russia", 3000, true));

    }

    @After
    public void tearDown() throws SQLException {
        connection.rollback();
        connection.close();
    }

}

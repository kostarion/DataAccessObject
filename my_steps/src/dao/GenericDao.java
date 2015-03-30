package dao;

import annotations.TaggedObject;
import classes.ReflectionHelper;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Класс, реализующий паттерн Data Access Object.
 * Создаёт промежуточный слой между объектным представлением
 * и реляционным представлением в БД.
 * Класс Т должен быть помечен именем таблицы, иметь ключевые поля.
 * Для записи в базу допускаются лишь определенные типы полей.
 * (См. ReflectionHelper.typeNames).
 *
 * В классе MySqlDaoFactory есть поля user и password, которые надо
 * поменять, если захотите тестировать у себя
 *
 * @author Дмитрий Точилкин
 * @date 02.03.2015
 */
public class GenericDao<T> implements ReflectionJdbcDao<T>{
    private Connection connection;
    private Class<?> cls;
    private String tableName;

    public GenericDao(Connection c, Class<T> cls) {
        connection = c;
        this.cls = cls;
        tableName = "yandex_dao." + cls.getAnnotation(TaggedObject.class).name();
    }

    @Override
    public void insert(T object) {
        List<Field> fields = ReflectionHelper.getAllValidFields(cls);
        String query = getInsertQuery(fields);
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < fields.size(); ++i) {
                fields.get(i).setAccessible(true);
                statement.setObject(i+1, fields.get(i).get(object));
            }
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Получает строку для запроса вставки в таблицу
     * @param fields значения каких полей будут вставлены
     * @return
     */
    private String getInsertQuery(List<Field> fields) {
        String query = "INSERT INTO " + tableName + " \n(" +
                getFieldsRecord(fields, false, false) + ") \nVALUES(";
        for (int i = 0; i < fields.size() - 1; ++i) {
            query += "?, ";
        }
        query += "?);";

        return query;
    }

    @Override
    public void update(T object) {
        List<Field> keys = ReflectionHelper.getKeyFields(cls);
        List<Field> nonKeys = ReflectionHelper.getAllValidFields(cls);
        nonKeys.removeAll(keys);
        String query = getUpdateQuery(nonKeys, keys);
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < keys.size() + nonKeys.size(); ++i) {
                Field f = i < nonKeys.size() ? nonKeys.get(i) : keys.get(i - nonKeys.size());
                f.setAccessible(true);
                statement.setObject(i + 1, f.get(object));
            }
            int count = statement.executeUpdate();
            if (count != 1) {
                throw new SQLException("On update modify more then 1 record: " + count);
            }
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * /**
     * Получает строку для запроса обновления таблицы
     * @param nonKeys неключевые поля для обновления
     * @param keys ключевые поля для обновления
     * @return SQL запрос
     */
    private String getUpdateQuery(List<Field> nonKeys, List<Field> keys) {
        String query = "UPDATE " + tableName + " SET " +
            getFieldsRecord(nonKeys, true, false) +
            " WHERE " + getFieldsRecord(keys, true, true) + ";";

        return query;
    }

    @Override
    public void deleteByKey(T key) {
        List<Field> keys = ReflectionHelper.getKeyFields(cls);
        String query = getDeleteQuery(key, keys);
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < keys.size(); ++i) {
                keys.get(i).setAccessible(true);
                statement.setObject(i + 1, keys.get(i).get(key));
            }
            int count = statement.executeUpdate();
            if (count != 1) {
                throw new SQLException("On delete modify more then 1 record: " + count);
            }
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Получает строку для запроса удаления из таблицы
     * @param key объект, ключевые поля которого будут указывать что удалить
     * @param keys ключевые поля
     * @return SQL запрос на удаление
     */
    private String getDeleteQuery(T key, List<Field> keys) {
        String query = "DELETE FROM " + tableName + " WHERE ";
        query += getFieldsRecord(keys, true, true);
        query += ";";

        return query;
    }

    /**
     * Получает письменное представление поля для использования в запросах
     * @param f поле
     * @param withValues указываются ли после названия колонки значения
     * @return
     */
    private String getFieldRecord (Field f, boolean withValues) {
        f.setAccessible(true);
        return ReflectionHelper.underScorize(f.getName()) +
                (withValues ? "= ?" : "");
    }

    /**
     * Получает письменное представление полей для использования в запросах
     * @param list список полей
     * @param withValues идут ли после названий колонок в запросе их значения
     * @param isAND надо ли ставить между равенствами AND
     * @return
     */
    private String getFieldsRecord(List<Field> list,
                                   boolean withValues, boolean isAND) {
        String record = "";
        for (int i = 0; i < list.size(); ++i) {
            list.get(i).setAccessible(true);
            record += getFieldRecord(list.get(i), withValues);
            record += i < list.size() - 1 ? (isAND? " AND " : ", ") : "";
        }

        return record;
    }

    @Override
    public T selectByKey(T key) {
        List<T> result = null;
        List<Field> keys = ReflectionHelper.getKeyFields(cls);
        String selectQuery = getSelectQuery(keys);
        try (PreparedStatement st = connection.prepareStatement(selectQuery)) {
            for (int i = 0; i < keys.size(); ++i) {
                keys.get(i).setAccessible(true);
                st.setObject(i + 1, keys.get(i).get(key));
            }
            ResultSet rs = st.executeQuery();
            result = parseResultSet(rs);
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if (result == null || result.size() != 1) {
            return null;
        }
        return result.get(0);
    }

    /**
     * Получает список объектов класса T из rs
     * @param rs результаты, полученные после выполнения некоторого запроса
     * @return
     */
    protected List<T> parseResultSet(ResultSet rs) {
        LinkedList<T> result = new LinkedList<T>();
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            String[] columnNames = new String[rsmd.getColumnCount()];
            Field[] fields = new Field[columnNames.length];
            for (int i = 1; i <= rsmd.getColumnCount(); ++i) {
                columnNames[i - 1] = rsmd.getColumnName(i);
                fields[i - 1] = ReflectionHelper.getField(cls,
                        ReflectionHelper.camelize(columnNames[i - 1]));
            }
            while (rs.next()) {
                T t = (T) (cls.newInstance());
                for (int i = 1; i <= fields.length; ++i) {
                    fields[i - 1].setAccessible(true);
                    fields[i - 1].set(t, rs.getObject(i));
                }
                result.add(t);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Получает запрос на получение объекта из таблицы
     * @param keys список ключевых полей класса
     * @return
     */
    private String getSelectQuery(List<Field> keys) {
        String query = "SELECT * FROM " + tableName + "\n WHERE ";
        for (int i=0; i<keys.size(); ++i) {
            query += ReflectionHelper.underScorize(keys.get(i).getName()) + " = ?";
            query += i<keys.size()-1 ? " AND \n" : ";";
        }

        return query;
    }

    @Override
    public List<T> selectAll() {
        List<T> result = new ArrayList<T>();
        String selectAllQuery = getSelectAllQuery();
        try (PreparedStatement st = connection.prepareStatement(selectAllQuery)) {
            ResultSet rs = st.executeQuery();
            result = parseResultSet(rs);
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Запрос на получение списка всех объектов из таблицы
     * @return
     */
    private String getSelectAllQuery () {
        return "SELECT * FROM " + tableName + ";";
    }
}

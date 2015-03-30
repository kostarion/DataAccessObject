package classes;

import annotations.KeyField;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Вспомогательный класс, содержащий статические методы
 * для работы с рефлексией
 */
public class ReflectionHelper {
    //хранит доступные для хранения в таблицах их объектов классы
    //и эквиваленты их типов в SQL
    public static Map<Class, String> typeNames;

    static {
        typeNames = new HashMap<Class, String>();

        typeNames.put(String.class, "VARCHAR(100)");
        typeNames.put(char.class, "CHAR");
        typeNames.put(Character.class, "CHAR");
        typeNames.put(int.class, "INT");
        typeNames.put(Integer.class, "INT");
        typeNames.put(long.class, "BIGINT");
        typeNames.put(Long.class, "BIGINT");
        typeNames.put(byte.class, "TINYINT");
        typeNames.put(Byte.class, "TINYINT");
        typeNames.put(short.class, "SMALLINT");
        typeNames.put(Short.class, "SMALLINT");
        typeNames.put(double.class, "DOUBLE");
        typeNames.put(Double.class, "DOUBLE");
        typeNames.put(float.class, "FLOAT");
        typeNames.put(Float.class, "FLOAT");
        typeNames.put(boolean.class, "BOOL");
        typeNames.put(Boolean.class, "BOOL");
    }

    /**
     * Возвращает все поля класса, в т.ч. родительские
     * @param cls класс
     * @return список полей
     */
    public static List<Field> getKeyFields (Class <?> cls) {
        List<Field> keys = new ArrayList<Field>();
        for (Field f: getAllValidFields(cls)) {
            if (f.isAnnotationPresent(KeyField.class)) {
                keys.add(f);
            }
        }

        return keys;
    }

    /**
     * Получает поле класса по его имени, ближайший в иерархии
     * наследования к данному классу
     * @param cls класс
     * @param f имя поля
     * @return поле
     * @throws NoSuchFieldException
     */
    public static Field getField (Class<?> cls, String f)
            throws NoSuchFieldException{
        for (Field field : getAllValidFields(cls)) {
            if (field.getName().equals(f))
                return field;
        }

        throw new NoSuchFieldException("There are no field " + f + " in class " + cls.getName());
    }

    /**
     * Получает список всех полей класса, включая родительские,
     * которые могут храниться в таблице
     * @param cls
     * @return
     */
    public static List<Field> getAllValidFields(Class<?> cls) {
        List<Field> result = new ArrayList<Field>();
        for (Class<?> c = cls; c != null; c = c.getSuperclass())
        {
            Field[] fields = c.getDeclaredFields();
            for (Field classField : fields)
            {
                if (typeNames.containsKey(classField.getType()))
                    result.add(classField);
            }
        }

        return result;
    }

    /**
     * Преобразует строку в under_score из camelCase
     * для использования как имя колонки
     *
     * @param camelCase строка в camelCase
     * @return строка в under_score
     * @post underScorize(camelize(str)) == str
     */
    public static String underScorize (String camelCase) {
        ArrayList<Character> underScore = new ArrayList<Character>();
        char[] camel = camelCase.toCharArray();
        for (int i = 0; i<camelCase.length(); i++ ) {
            Character c = camel[i];
            if (isUpperCase(c)) {
                underScore.add('_');
            }
            underScore.add(toLowerCase(c));
        }
        char[] under = new char[underScore.size()];
        for (int i = 0; i<under.length; i++) {
            under[i] = underScore.get(i).charValue();
        }

        return new String(under);
    }

    /**
     * Преобразует из under_score в camelCase
     * @param underScore строка в underScore
     * @return строка в camelCase
     * @post camelize(underScorize(str)) == str
     */
    public static String camelize (String underScore) {
        char[] str = underScore.toCharArray();
        for (int i = 0; i<str.length-1; ++i) {
            if (str[i]=='_') {
                str[i+1] = toUpperCase(str[i+1]);
            }
        }

        return new String(str).replaceAll("_", "");
    }

    /**
     * Получает строчную букву из заглавной
     * @param c
     * @return
     */
    public static char toLowerCase (char c) {
        return isUpperCase(c) ? (char)(c - 'A' + 'a') : c;
    }

    /**
     * Получает заглавную букву из прописной
     * @param c
     * @return
     */
    public static char toUpperCase (char c) {
        return isLowerCase(c) ? (char)(c - 'a' + 'A') : c;
    }

    public static boolean isLowerCase (char c) {
        if (c <= 'z' && c >= 'a')
            return true;

        return false;
    }

    public static boolean isUpperCase (char c) {
        if (c <= 'Z' && c >= 'A')
            return true;

        return false;
    }

}

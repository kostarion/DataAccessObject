package exceptions;

/**
 * Исключение на случай, если у класса не будет помеченных
 * специальной аннотацией ключевых полей
 */
public class NoKeyFieldsException extends Exception{

    public NoKeyFieldsException() {

    }

    public NoKeyFieldsException(String message) {

        super(message);
    }

    public NoKeyFieldsException(String message, Throwable cause) {

        super(message, cause);
    }

    public NoKeyFieldsException(Throwable cause) {

        super(cause);
    }

    public NoKeyFieldsException(String message, Throwable cause,
                                boolean enableSuppression, boolean writableStackTrace) {

        super(message, cause, enableSuppression, writableStackTrace);
    }
}

package exceptions;

/**
 * Исключение на случай, если класс не меет специальной аннотации,
 * помечающей имя таблицы
 */
public class NoTableTitleException extends Exception{

    public NoTableTitleException() {

    }

    public NoTableTitleException(String message) {

        super(message);
    }

    public NoTableTitleException(String message, Throwable cause) {

        super(message, cause);
    }

    public NoTableTitleException(Throwable cause) {

        super(cause);
    }

    public NoTableTitleException(String message, Throwable cause,
                                boolean enableSuppression,
                                boolean writableStackTrace) {

        super(message, cause, enableSuppression, writableStackTrace);
    }
}

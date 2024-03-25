package app.utils;

/**
 * Вспомогательный класс, содержащий методы для работы с СУБД.
 */
public class DbUtils {

    /**
     * Закрывает ресурс, если он существует(не null).
     * Если во время закрытия произошла ошибка, она выводиться на консоль.
     * @param resource
     */
    public static void closeQuietly(AutoCloseable resource){
        if(resource == null){
            return;
        }

        try{
            resource.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}

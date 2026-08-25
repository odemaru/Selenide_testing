package toolshop.data;

/**
 * Ограничения формы регистрации.
 *
 * Значения сняты со схемы UserRequest в OpenAPI магазина
 * (https://api.practicesoftwaretesting.com/docs?api-docs.json) и перепроверены
 * запросами к стенду.
 */
public final class Limits {

    public static final int FIRST_NAME_MAX = 40;
    public static final int LAST_NAME_MAX = 20;
    public static final int PHONE_MAX = 24;
    public static final int EMAIL_MAX = 256;

    /** Фактический минимум длины пароля: столько требуют и форма, и API. */
    public static final int PASSWORD_MIN = 8;

    /**
     * Минимум, который называет сообщение об ошибке в форме.
     *
     * Расходится с фактическим и потому вынесен отдельной константой:
     * на нём держится проверка BUG-1 в KnownBugsTest.
     */
    public static final int PASSWORD_MIN_CLAIMED = 6;

    /** Число стран в ISO 3166-1 alpha-2 на момент проверки стенда. */
    public static final int COUNTRIES_TOTAL = 249;

    private Limits() {
    }
}

package toolshop.data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Способы оплаты и поля, которые открывает каждый из них.
 *
 * Это таблица решений из docs/test-design.md, записанная кодом: набор
 * обязательных полей зависит только от выбранного способа. Тест перебирает
 * значения перечисления и потому не может разойтись с таблицей — новый способ
 * оплаты добавляется здесь и сразу попадает в прогон.
 *
 * Форматы значений подобраны опытным путём: магазин их нигде не описывает и
 * не показывает сообщений об ошибке — просто оставляет кнопку подтверждения
 * заблокированной. Номер карты принимается только через дефисы, номер счёта
 * только цифрами, номер сертификата ровно из шестнадцати цифр.
 */
public enum PaymentMethod {

    BANK_TRANSFER("bank-transfer", fields(
            "bank_name", "Swedbank",
            "account_name", "Test Probe",
            "account_number", "1234567890")),

    CASH_ON_DELIVERY("cash-on-delivery", fields()),

    CREDIT_CARD("credit-card", fields(
            "credit_card_number", "4111-1111-1111-1111",
            "expiration_date", "12/2030",
            "cvv", "123",
            "card_holder_name", "Test Probe")),

    BUY_NOW_PAY_LATER("buy-now-pay-later", fields(
            "monthly_installments", "6")),

    GIFT_CARD("gift-card", fields(
            "gift_card_number", "1234567890123456",
            "validation_code", "1234"));

    private final String value;
    private final Map<String, String> details;

    PaymentMethod(String value, Map<String, String> details) {
        this.value = value;
        this.details = details;
    }

    public String value() {
        return value;
    }

    /** Поля способа оплаты вместе с подходящими значениями. */
    public Map<String, String> details() {
        return details;
    }

    /** Способ без дополнительных полей проверять на их незаполненность нечем. */
    public boolean hasDetails() {
        return !details.isEmpty();
    }

    private static Map<String, String> fields(String... nameThenValue) {
        Map<String, String> result = new LinkedHashMap<>();
        for (int i = 0; i < nameThenValue.length; i += 2) {
            result.put(nameThenValue[i], nameThenValue[i + 1]);
        }
        return Map.copyOf(result);
    }
}

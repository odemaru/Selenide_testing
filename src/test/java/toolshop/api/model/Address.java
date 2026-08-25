package toolshop.api.model;

/**
 * Адрес пользователя. Имена полей переводятся в snake_case маппером,
 * поэтому здесь они в обычном для Java виде.
 */
public record Address(
        String street,
        String houseNumber,
        String city,
        String state,
        String country,
        String postalCode) {
}

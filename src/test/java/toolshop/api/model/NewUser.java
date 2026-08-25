package toolshop.api.model;

/**
 * Данные для регистрации.
 *
 * Методы with* дают точечную подмену одного поля валидного набора, чтобы
 * намерение теста читалось прямо в месте вызова:
 * {@code Users.valid().withPassword("Aa1!bc")}.
 */
public record NewUser(
        String firstName,
        String lastName,
        String email,
        String password,
        String phone,
        String dob,
        Address address) {

    public NewUser withFirstName(String value) {
        return new NewUser(value, lastName, email, password, phone, dob, address);
    }

    public NewUser withLastName(String value) {
        return new NewUser(firstName, value, email, password, phone, dob, address);
    }

    public NewUser withEmail(String value) {
        return new NewUser(firstName, lastName, value, password, phone, dob, address);
    }

    public NewUser withPassword(String value) {
        return new NewUser(firstName, lastName, email, value, phone, dob, address);
    }

    public NewUser withPhone(String value) {
        return new NewUser(firstName, lastName, email, password, value, dob, address);
    }

    public NewUser withDob(String value) {
        return new NewUser(firstName, lastName, email, password, phone, value, address);
    }
}

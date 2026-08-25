package toolshop.setup;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Тест описывает поведение, которого требует документация, но которое сейчас
 * нарушено известным дефектом.
 *
 * Пока дефект воспроизводится, тест падает — и считается пройденным.
 * Как только дефект исправят, тест перестанет падать, и вот это уже уронит
 * прогон: исправление нельзя пропустить, а помеченный тест нужно вернуть
 * в обычные.
 *
 * @see KnownBugExtension
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@ExtendWith(KnownBugExtension.class)
public @interface KnownBug {

    /** Идентификатор дефекта из docs/bug-report.md, например BUG-3. */
    String value();
}

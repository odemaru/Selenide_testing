package toolshop.setup;

import com.codeborne.selenide.ex.ElementNotFound;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.opentest4j.TestAbortedException;
import org.openqa.selenium.WebDriverException;

import java.util.Optional;

/**
 * Реализация strict xfail для JUnit 5.
 *
 * В pytest это готовый xfail(strict=True), в JUnit такого нет: @Disabled
 * просто выключает тест и перестаёт следить за дефектом, а @Test(expected=...)
 * проверяет тип исключения, а не сам факт падения проверки.
 *
 * Обработчик переворачивает результат теста, помеченного {@link KnownBug}:
 * упавшая проверка означает, что дефект на месте, и тест засчитывается;
 * успешная — что поведение изменилось, и прогон падает.
 */
public class KnownBugExtension implements TestExecutionExceptionHandler, AfterTestExecutionCallback {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(KnownBugExtension.class);
    private static final String REPRODUCED = "reproduced";

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        // Пропущенный тест не говорит о дефекте ничего: у Assumptions свой смысл,
        // и подменять его отметкой «дефект воспроизвёлся» нельзя.
        if (throwable instanceof TestAbortedException) {
            throw throwable;
        }
        if (isBrokenEnvironment(throwable)) {
            throw throwable;
        }
        context.getStore(NAMESPACE).put(REPRODUCED, throwable);
    }

    /**
     * Отличает «дефект на месте» от «страница не открылась».
     *
     * Без этой проверки механизм зачитывает себе всё подряд. Поймано на живом
     * примере: в CI страницы магазина перехватывал Cloudflare, до приложения
     * тесты не доходили — и все восемь проверок дефектов позеленели, хотя
     * ничего не проверили.
     *
     * Разделение опирается на то, какое исключение бросает Selenide.
     * ElementNotFound значит, что элемента нет вовсе, то есть открылось не то,
     * что ожидалось: каждый тест здесь начинается с открытия страницы, а оно
     * само проверяет видимость якорного элемента. А ElementShould возникает,
     * когда элемент на месте, но ведёт себя не так, — вот это и есть дефект.
     */
    private boolean isBrokenEnvironment(Throwable throwable) {
        return throwable instanceof ElementNotFound
                || throwable instanceof WebDriverException;
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        Throwable reproduced = context.getStore(NAMESPACE).get(REPRODUCED, Throwable.class);
        if (reproduced != null) {
            return;
        }
        throw new AssertionError(
                bugId(context) + " больше не воспроизводится: проверка прошла. "
                        + "Похоже, дефект исправлен — снимите @KnownBug и перенесите тест "
                        + "к обычным, а в docs/bug-report.md отметьте дефект закрытым.");
    }

    private String bugId(ExtensionContext context) {
        return findAnnotation(context)
                .map(KnownBug::value)
                .orElse("Дефект");
    }

    private Optional<KnownBug> findAnnotation(ExtensionContext context) {
        Optional<KnownBug> onMethod = context.getTestMethod()
                .map(method -> method.getAnnotation(KnownBug.class));
        if (onMethod.isPresent()) {
            return onMethod;
        }
        return context.getTestClass().map(type -> type.getAnnotation(KnownBug.class));
    }
}

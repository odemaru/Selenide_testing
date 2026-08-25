package toolshop.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.StaleElementReferenceException;
import io.qameta.allure.Step;
import toolshop.pages.component.Header;

import java.util.List;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;
import static toolshop.pages.DataTest.$test;

/**
 * Витрина: поиск, сортировка, фильтры и сетка товаров.
 *
 * Страница ничего не проверяет — она только выполняет действия и отдаёт
 * значения. Проверки живут в тестах, поэтому упавший тест показывает свою
 * строку с assert, а не строку внутри page object.
 *
 * Категории и бренды выбираются по названию, а не по data-test: атрибут
 * содержит идентификатор товара из базы, а демо-стенд периодически
 * пересоздаёт данные и меняет все идентификаторы разом.
 */
public class HomePage {

    public final Header header = new Header();

    public HomePage openPage() {
        open("/");
        countPendingRequests();
        cards().shouldHave(sizeGreaterThan(0));
        return this;
    }

    /**
     * Ставит на страницу счётчик незавершённых запросов.
     *
     * Angular обращается к серверу через XMLHttpRequest, поэтому считать нужно
     * именно его; fetch добавлен на случай перехода магазина на него. Счётчик
     * переживает фильтрацию, потому что она идёт без перезагрузки страницы,
     * и пропадает сам при переходе на другой адрес.
     */
    private void countPendingRequests() {
        Selenide.executeJavaScript(
                "if (window.__pending === undefined) {"
                        + "  window.__pending = 0;"
                        + "  const send = XMLHttpRequest.prototype.send;"
                        + "  XMLHttpRequest.prototype.send = function () {"
                        + "    window.__pending++;"
                        + "    this.addEventListener('loadend', () => window.__pending--);"
                        + "    return send.apply(this, arguments);"
                        + "  };"
                        + "  const original = window.fetch;"
                        + "  window.fetch = function () {"
                        + "    window.__pending++;"
                        + "    return original.apply(this, arguments)"
                        + "      .finally(() => window.__pending--);"
                        + "  };"
                        + "}");
    }

    @Step("Найти «{query}»")
    public HomePage search(String query) {
        typeQuery(query);
        $test("search-submit").click();
        return waitForGridUpdate();
    }

    /**
     * ChromeDriver отказывается печатать символы за пределами BMP — эмодзи
     * роняют sendKeys с «only supports characters in the BMP». Это ограничение
     * драйвера, а не магазина, поэтому такие строки задаются через JS, а не
     * исключаются из проверок: пользователь их вставить может.
     */
    private void typeQuery(String query) {
        SelenideElement field = $test("search-query");
        if (query.codePoints().allMatch(code -> code <= 0xFFFF)) {
            field.setValue(query);
            return;
        }
        field.click();
        Selenide.executeJavaScript(
                "const f=arguments[0];"
                        + "Object.getOwnPropertyDescriptor(HTMLInputElement.prototype,'value')"
                        + ".set.call(f, arguments[1]);"
                        + "f.dispatchEvent(new Event('input',{bubbles:true}));",
                field, query);
    }

    @Step("Сбросить поиск")
    public HomePage resetSearch() {
        $test("search-reset").click();
        return waitForGridUpdate();
    }

    @Step("Отсортировать: {option}")
    public HomePage sortBy(String option) {
        $test("sort").selectOptionByValue(option);
        return waitForGridUpdate();
    }

    @Step("Фильтр по категории: {name}")
    public HomePage filterByCategory(String name) {
        filterCheckbox("category_id", name).click();
        return waitForGridUpdate();
    }

    @Step("Фильтр по бренду: {name}")
    public HomePage filterByBrand(String name) {
        filterCheckbox("brand_id", name).click();
        return waitForGridUpdate();
    }

    @Step("Только экологичные товары: {enabled}")
    public HomePage filterEcoFriendly(boolean enabled) {
        SelenideElement checkbox = $test("eco-friendly-filter");
        if (checkbox.isSelected() != enabled) {
            checkbox.click();
        }
        return waitForGridUpdate();
    }

    public ProductPage openProduct(String name) {
        cards().findBy(exactText(name)).click();
        return new ProductPage();
    }

    public ElementsCollection cards() {
        return $$("a.card");
    }

    /**
     * Названия отдаются коллекцией, а не готовым списком строк.
     *
     * Снимок в List фиксирует то, что есть на странице прямо сейчас, и на
     * ещё не отрисованной сетке даёт пустоту вместо ожидания. Проверки
     * коллекции у Selenide ждут сами, поэтому ждать умеет тест, а не
     * страница.
     */
    public ElementsCollection productNames() {
        return $$("a.card " + DataTest.css("product-name"));
    }

    public ElementsCollection productPrices() {
        return $$("a.card " + DataTest.css("product-price"));
    }

    /**
     * Ждёт, пока витрина закончит обновляться.
     *
     * Вызывается после каждого изменения фильтра, а не один раз в конце.
     * Клики подряд отправляют несколько запросов сразу, и выдача более
     * раннего из них может прийти последней — тогда на экране остаётся
     * результат предыдущего условия. Пользователь так не делает, и тест
     * не должен.
     *
     * Индикатора загрузки у магазина нет, адрес при фильтрации не меняется,
     * так что признака «готово» на странице не найти. Сравнивать только
     * разметку тоже мало: две одинаковые подряд выборки легко приходятся
     * на паузу между запросами, и тест читает выдачу предыдущего фильтра —
     * именно так падала проверка сортировки.
     *
     * Поэтому признаком служит число незавершённых запросов, которое ставит
     * на страницу {@link #countPendingRequests()}. Сетка готова, когда
     * запросов в полёте не осталось и список цен не изменился дважды подряд.
     *
     * Записи Performance API на эту роль не подошли: у буфера ресурсов есть
     * предел, после которого новые записи молча отбрасываются. Счётчик
     * переставал расти, ожидание завершалось на недорисованной сетке, и
     * проверка сортировки падала раз в несколько прогонов — причём на верных
     * данных, API отдавал их отсортированными.
     */
    public HomePage waitForGridUpdate() {
        List<String> previousPrices = null;

        for (int attempt = 0; attempt < 40; attempt++) {
            List<String> prices = pricesOrNullWhileRedrawing();

            if (prices != null && pendingRequests() == 0 && prices.equals(previousPrices)) {
                return this;
            }
            previousPrices = prices;
            Selenide.sleep(300);
        }
        return this;
    }

    private long pendingRequests() {
        Long pending = Selenide.executeJavaScript("return window.__pending || 0;");
        return pending == null ? 0 : pending;
    }

    /**
     * Чтение цен во время перерисовки роняет StaleElementReferenceException:
     * элементы, найденные мгновение назад, уже выброшены из документа. Само
     * это исключение и означает, что сетка меняется, поэтому оно не ошибка,
     * а ответ «ещё не устоялась».
     */
    private List<String> pricesOrNullWhileRedrawing() {
        try {
            return productPrices().texts();
        } catch (StaleElementReferenceException redrawing) {
            return null;
        }
    }

    public SelenideElement nextPage() {
        return $test("pagination-next");
    }

    public SelenideElement searchCaption() {
        return $("h3");
    }

    /**
     * Чекбокс фильтра ищется по подписи целиком.
     *
     * Совпадение по вхождению здесь не годится: среди категорий есть и Saw,
     * и Hand Saw, и фильтр «Saw» молча выбрал бы не тот чекбокс.
     *
     * Панель ищется как div: атрибут data-test="filters" магазин повесил
     * заодно на ссылку-переключатель, и она идёт в разметке первой.
     */
    private SelenideElement filterCheckbox(String inputName, String label) {
        return $("div" + DataTest.css("filters")).$$("label")
                .findBy(exactText(label))
                .$("input[name='" + inputName + "']");
    }
}

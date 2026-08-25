package toolshop.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * Доступ к элементам по атрибуту data-test.
 *
 * Магазин помечает им все контролы, и это единственный признак, переживающий
 * пересборку вёрстки: имена классов Angular генерирует заново.
 *
 * Оговорка на будущее: имена не выдержаны в одном стиле — рядом живут
 * first-name через дефис и postal_code через подчёркивание, — поэтому
 * значение всегда передаётся строкой как есть, без сборки из частей.
 */
public final class DataTest {

    public static SelenideElement $test(String name) {
        return $(css(name));
    }

    public static ElementsCollection $$test(String name) {
        return $$(css(name));
    }

    public static String css(String name) {
        return "[data-test='" + name + "']";
    }

    private DataTest() {
    }
}

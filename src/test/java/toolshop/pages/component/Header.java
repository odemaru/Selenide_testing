package toolshop.pages.component;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static toolshop.pages.DataTest.$test;

/**
 * Шапка магазина: она одинакова на всех страницах, поэтому вынесена из них.
 */
public class Header {

    public SelenideElement cartLink() {
        return $test("nav-cart");
    }

    public SelenideElement cartQuantity() {
        return $test("cart-quantity");
    }

    public SelenideElement signInLink() {
        return $test("nav-sign-in");
    }

    /** Меню пользователя появляется вместо ссылки «Sign in» после входа. */
    public SelenideElement userMenu() {
        return $test("nav-menu");
    }

    /** Версия из подвала. Заявленная версия магазина стоит в заголовке вкладки. */
    public SelenideElement footerVersion() {
        return $("footer");
    }
}

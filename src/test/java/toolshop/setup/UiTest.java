package toolshop.setup;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterEach;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;

import static com.codeborne.selenide.Selenide.executeJavaScript;
import static toolshop.config.TestConfig.CONFIG;

/**
 * Общая настройка UI-тестов.
 *
 * Браузер не перезапускается между тестами: старт Chrome стоит около двух
 * секунд, и на полусотне тестов это дороже самого прогона. Вместо этого
 * после каждого теста чистится состояние, в котором магазин держит корзину
 * и сессию, — localStorage, sessionStorage и cookies. Драйвер у Selenide
 * привязан к потоку, поэтому параллельным тестам чужое состояние не достаётся.
 */
public abstract class UiTest {

    static {
        Configuration.baseUrl = CONFIG.baseUrl();
        Configuration.browser = CONFIG.browser();
        Configuration.browserSize = CONFIG.windowSize();
        Configuration.timeout = CONFIG.timeoutMs();
        Configuration.reportsFolder = "target/selenide-reports";
        Configuration.browserCapabilities = capabilities();

        SelenideLogger.addListener("allure", new AllureSelenide()
                .screenshots(true)
                .savePageSource(true));
    }

    /**
     * Ключ headless задаётся аргументом браузера, а не общей настройкой
     * Selenide: у Chrome и Firefox он называется по-разному.
     */
    private static org.openqa.selenium.MutableCapabilities capabilities() {
        if ("firefox".equalsIgnoreCase(CONFIG.browser())) {
            FirefoxOptions firefox = new FirefoxOptions();
            if (CONFIG.headless()) {
                firefox.addArguments("-headless");
            }
            return firefox;
        }
        ChromeOptions chrome = new ChromeOptions();
        if (CONFIG.headless()) {
            chrome.addArguments("--headless=new");
        }
        chrome.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu");
        return chrome;
    }

    @AfterEach
    void clearBrowserState() {
        if (!WebDriverRunner.hasWebDriverStarted()) {
            return;
        }
        // На about:blank обращение к хранилищу браузер запрещает, поэтому
        // чистим только когда открыта страница магазина.
        if (WebDriverRunner.url().startsWith("http")) {
            executeJavaScript("localStorage.clear(); sessionStorage.clear();");
        }
        WebDriverRunner.getWebDriver().manage().deleteAllCookies();
    }
}

package toolshop.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;

/**
 * Параметры прогона.
 *
 * Системные свойства перекрывают test.properties, поэтому любой параметр
 * меняется ключом -D без правки файла в репозитории. Это нужно и в CI,
 * и локально: браузер с окном включается одним -Dheadless=false.
 */
@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({"system:properties", "classpath:test.properties"})
public interface TestConfig extends Config {

    TestConfig CONFIG = ConfigFactory.create(TestConfig.class, System.getProperties());

    @Key("base.url")
    String baseUrl();

    @Key("api.url")
    String apiUrl();

    @Key("browser")
    String browser();

    @Key("headless")
    boolean headless();

    @Key("window.size")
    String windowSize();

    @Key("timeout.ms")
    long timeoutMs();
}

package toolshop.setup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

/**
 * Проверка самого механизма пометки дефектов.
 *
 * Расширение, которое переворачивает результат теста, обязано быть проверено:
 * если оно засчитает вообще всё, восемь тестов в KnownBugsTest станут зелёными
 * навсегда и перестанут что-либо означать. Проверить его обычным тестом нельзя
 * — он и сам попал бы под переворот, — поэтому образцы запускаются отдельным
 * движком JUnit через EngineTestKit, а здесь сверяется их исход.
 */
class KnownBugExtensionTest {

    @Test
    @DisplayName("Воспроизводящийся дефект засчитывается как успех")
    void reproducedBugCountsAsSuccess() {
        EngineTestKit.engine("junit-jupiter")
                .selectors(selectClass(StillBroken.class))
                .execute()
                .testEvents()
                .assertStatistics(stats -> stats.succeeded(1).failed(0));
    }

    @Test
    @DisplayName("Исправленный дефект роняет прогон и называет свой номер")
    void fixedBugFailsTheRun() {
        EngineTestKit.engine("junit-jupiter")
                .selectors(selectClass(AlreadyFixed.class))
                .execute()
                .testEvents()
                .assertThatEvents()
                .haveExactly(1, event(test(), finishedWithFailure(
                        message(text -> text.contains("BUG-42") && text.contains("больше не воспроизводится")))));
    }

    /**
     * Образцы для запуска изнутри. Имена классов не оканчиваются на Test,
     * поэтому обычный прогон их не подхватывает.
     */
    static class StillBroken {

        @Test
        @KnownBug("BUG-41")
        void checkFails() {
            throw new AssertionError("дефект на месте");
        }
    }

    static class AlreadyFixed {

        @Test
        @KnownBug("BUG-42")
        void checkPasses() {
        }
    }
}

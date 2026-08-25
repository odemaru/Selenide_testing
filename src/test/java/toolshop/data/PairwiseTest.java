package toolshop.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Проверки самого генератора наборов.
 *
 * Инструмент, который сокращает набор тестов, обязан быть проверен: если он
 * потеряет пару значений, вместе с ней тихо пропадёт и покрытие, а прогон
 * останется зелёным. Браузер этим тестам не нужен, поэтому они работают
 * и там, где Chrome недоступен.
 */
class PairwiseTest {

    private static final List<List<Object>> PARAMETERS = List.of(
            List.of("Hammer", "Pliers", "Drill"),
            List.of("ForgeFlex", "MightyCraft"),
            List.of("name,asc", "price,desc"),
            List.of(true, false));

    @Test
    @DisplayName("Набор покрывает все пары значений параметров")
    void everyPairIsCovered() {
        List<Object[]> rows = Pairwise.combinations(PARAMETERS);

        Set<String> covered = new LinkedHashSet<>();
        for (Object[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                for (int j = i + 1; j < row.length; j++) {
                    covered.add(i + "=" + row[i] + " & " + j + "=" + row[j]);
                }
            }
        }

        assertThat(covered).containsAll(expectedPairs());
    }

    @Test
    @DisplayName("Набор заметно меньше полного перебора")
    void reducesFullCombination() {
        int full = PARAMETERS.stream().mapToInt(List::size).reduce(1, (a, b) -> a * b);

        List<Object[]> rows = Pairwise.combinations(PARAMETERS);

        assertThat(full).isEqualTo(24);
        assertThat(rows).hasSizeLessThan(full / 2);
    }

    @Test
    @DisplayName("Один и тот же вход даёт один и тот же набор")
    void resultIsReproducible() {
        List<Object[]> first = Pairwise.combinations(PARAMETERS);
        List<Object[]> second = Pairwise.combinations(PARAMETERS);

        assertThat(first).usingRecursiveComparison().isEqualTo(second);
    }

    private List<String> expectedPairs() {
        List<String> pairs = new ArrayList<>();
        for (int i = 0; i < PARAMETERS.size(); i++) {
            for (int j = i + 1; j < PARAMETERS.size(); j++) {
                for (Object left : PARAMETERS.get(i)) {
                    for (Object right : PARAMETERS.get(j)) {
                        pairs.add(i + "=" + left + " & " + j + "=" + right);
                    }
                }
            }
        }
        return pairs;
    }
}

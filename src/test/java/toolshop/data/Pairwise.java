package toolshop.data;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Генератор попарных наборов.
 *
 * Полный перебор пяти параметров фильтра каталога даёт сотни комбинаций, и
 * прогонять их через браузер бессмысленно. Попарное покрытие исходит из того,
 * что дефекты фильтрации проявляются на взаимодействии двух параметров, а не
 * пяти сразу, и сокращает набор на порядок, сохраняя все пары значений.
 *
 * Алгоритм жадный: строка набирается вокруг ещё не покрытой пары, остальные
 * параметры добираются значением, закрывающим больше всего оставшихся пар.
 * Результат не минимален теоретически, но воспроизводим: обход идёт по
 * LinkedHashSet, поэтому один и тот же вход всегда даёт один и тот же набор,
 * и упавший тест можно перезапустить на тех же данных.
 */
public final class Pairwise {

    private record Pair(int leftIndex, Object leftValue, int rightIndex, Object rightValue) {

        static Pair of(int i, Object vi, int j, Object vj) {
            return i < j ? new Pair(i, vi, j, vj) : new Pair(j, vj, i, vi);
        }
    }

    public static List<Object[]> combinations(List<List<Object>> parameters) {
        if (parameters.size() < 2) {
            throw new IllegalArgumentException("Попарное покрытие требует минимум двух параметров");
        }

        Set<Pair> uncovered = allPairs(parameters);
        List<Object[]> rows = new ArrayList<>();

        while (!uncovered.isEmpty()) {
            Object[] row = buildRow(parameters, uncovered);
            rows.add(row);
            markCovered(row, uncovered);
        }
        return rows;
    }

    private static Set<Pair> allPairs(List<List<Object>> parameters) {
        Set<Pair> pairs = new LinkedHashSet<>();
        for (int i = 0; i < parameters.size(); i++) {
            for (int j = i + 1; j < parameters.size(); j++) {
                for (Object left : parameters.get(i)) {
                    for (Object right : parameters.get(j)) {
                        pairs.add(Pair.of(i, left, j, right));
                    }
                }
            }
        }
        return pairs;
    }

    private static Object[] buildRow(List<List<Object>> parameters, Set<Pair> uncovered) {
        int size = parameters.size();
        Object[] row = new Object[size];
        boolean[] assigned = new boolean[size];

        Pair seed = uncovered.iterator().next();
        row[seed.leftIndex()] = seed.leftValue();
        row[seed.rightIndex()] = seed.rightValue();
        assigned[seed.leftIndex()] = true;
        assigned[seed.rightIndex()] = true;

        for (int p = 0; p < size; p++) {
            if (assigned[p]) {
                continue;
            }
            Object best = null;
            int bestGain = -1;
            for (Object candidate : parameters.get(p)) {
                int gain = 0;
                for (int q = 0; q < size; q++) {
                    if (assigned[q] && uncovered.contains(Pair.of(p, candidate, q, row[q]))) {
                        gain++;
                    }
                }
                if (gain > bestGain) {
                    bestGain = gain;
                    best = candidate;
                }
            }
            row[p] = best;
            assigned[p] = true;
        }
        return row;
    }

    private static void markCovered(Object[] row, Set<Pair> uncovered) {
        for (int i = 0; i < row.length; i++) {
            for (int j = i + 1; j < row.length; j++) {
                uncovered.remove(Pair.of(i, row[i], j, row[j]));
            }
        }
    }

    private Pairwise() {
    }
}

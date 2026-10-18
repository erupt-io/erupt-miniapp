package xyz.erupt.miniapp.live;

import java.time.LocalDate;
import java.util.List;

/**
 * The date range a statistics page asks for: the searched range, capped, else the trailing default.
 *
 * @author YuePeng
 */
final class MaStats {

    private MaStats() {
    }

    // WeChat reports a day the morning after, so the trailing window ends yesterday
    static LocalDate[] range(Object condition, int maxDays, int defaultDays) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate from = yesterday.minusDays(defaultDays - 1), to = yesterday;
        if (condition instanceof List<?> range && range.size() == 2) {
            from = LocalDate.parse(String.valueOf(range.get(0)).substring(0, 10));
            to = LocalDate.parse(String.valueOf(range.get(1)).substring(0, 10));
        }
        if (to.isAfter(yesterday)) to = yesterday;
        if (from.isBefore(to.minusDays(maxDays - 1))) from = to.minusDays(maxDays - 1);
        return new LocalDate[]{from, to};
    }

}

package xyz.erupt.miniapp.core;

import me.chanjar.weixin.common.error.WxErrorException;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * Small helpers around WxJava: every API call goes through {@link #call} so an errcode arrives in
 * the page as one translated message.
 *
 * @author YuePeng
 */
public final class Ma {

    public static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private Ma() {
    }

    @FunctionalInterface
    public interface Call<T> {
        T call() throws WxErrorException;
    }

    @FunctionalInterface
    public interface Run {
        void run() throws WxErrorException;
    }

    public static <T> T call(Call<T> call) {
        try {
            return call.call();
        } catch (WxErrorException e) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("miniapp.api_failed") + " → " + e.getMessage());
        }
    }

    public static void run(Run run) {
        call(() -> {
            run.run();
            return null;
        });
    }

    public static List<String> lines(String text) {
        return null == text ? List.of() : text.lines().map(String::trim).filter(it -> !it.isEmpty()).toList();
    }

    public static LocalDateTime time(Number epochSeconds) {
        return null == epochSeconds ? null : LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds.longValue()), ZoneId.systemDefault());
    }

    public static Date date(LocalDate day) {
        return Date.from(day.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // "20261018", the form the analysis API reports days in
    public static LocalDate day(String compact) {
        return null == compact ? null : LocalDate.parse(compact, DAY);
    }

}

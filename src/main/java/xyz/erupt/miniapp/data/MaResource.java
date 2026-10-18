package xyz.erupt.miniapp.data;

import me.chanjar.weixin.common.error.WxErrorException;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.miniapp.core.MaContext;

import java.util.List;

/**
 * One WeChat resource read live through the API, for the account of the current workspace: the
 * model's rows come from {@link #list}, and writes are what the API allows. Filtering, sorting and
 * paging happen in memory in {@link MaDataService}.
 *
 * @author YuePeng
 */
public interface MaResource<T> {

    Class<T> type();

    List<T> list(MaContext wx, EruptQuery query) throws WxErrorException;

    default void add(MaContext wx, T row) throws WxErrorException {
        throw readOnly();
    }

    default void edit(MaContext wx, T row) throws WxErrorException {
        throw readOnly();
    }

    default void delete(MaContext wx, T row) throws WxErrorException {
        throw readOnly();
    }

    static EruptWebApiRuntimeException readOnly() {
        return new EruptWebApiRuntimeException(I18nTranslate.$translate("data.read_only"));
    }

}

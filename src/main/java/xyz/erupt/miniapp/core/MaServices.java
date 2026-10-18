package xyz.erupt.miniapp.core;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.miniapp.model.MaApp;
import xyz.erupt.miniapp.prop.EruptMiniappProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One {@link WxMaService} per app, keyed by the app row. The access token lives in memory; a
 * cluster swaps in WxJava's Redis config here.
 *
 * @author YuePeng
 */
@Component
public class MaServices {

    private final Map<Long, WxMaService> services = new ConcurrentHashMap<>();

    private final Map<String, Long> idsByAppId = new ConcurrentHashMap<>();

    private final EruptDao eruptDao;

    private final EruptMiniappProperties properties;

    public MaServices(EruptDao eruptDao, EruptMiniappProperties properties) {
        this.eruptDao = eruptDao;
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        eruptDao.lambdaQuery(MaApp.class).list().forEach(this::register);
    }

    public void register(MaApp app) {
        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
        config.setAppid(app.getAppId());
        config.setSecret(app.getSecret());
        config.setToken(app.getToken());
        config.setAesKey(app.getAesKey());
        config.setMsgDataFormat(app.getMsgDataFormat());
        if (StringUtils.isNotBlank(properties.getApiHost())) config.setApiHostUrl(properties.getApiHost());
        WxMaService service = new WxMaServiceImpl();
        service.setWxMaConfig(config);
        services.put(app.getId(), service);
        idsByAppId.put(app.getAppId(), app.getId());
    }

    public void remove(MaApp app) {
        services.remove(app.getId());
        idsByAppId.remove(app.getAppId());
    }

    public WxMaService get(Long appId) {
        WxMaService service = null == appId ? null : services.get(appId);
        if (null == service) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("miniapp.app_unknown") + " → " + appId);
        return service;
    }

    public Long idOf(String appId) {
        return idsByAppId.get(appId);
    }

}

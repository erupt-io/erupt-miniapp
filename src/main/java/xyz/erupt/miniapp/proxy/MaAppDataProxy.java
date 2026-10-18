package xyz.erupt.miniapp.proxy;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.miniapp.core.MaServices;
import xyz.erupt.miniapp.model.MaApp;
import xyz.erupt.miniapp.portal.MaPortalController;
import xyz.erupt.miniapp.prop.EruptMiniappProperties;

import java.util.Collection;
import java.util.Map;

/**
 * Keeps the service registry in step with the app rows.
 *
 * @author YuePeng
 */
@Component
public class MaAppDataProxy implements DataProxy<MaApp> {

    private final EruptDao eruptDao;

    private final EruptMiniappProperties properties;

    private final MaServices services;

    public MaAppDataProxy(EruptDao eruptDao, EruptMiniappProperties properties, MaServices services) {
        this.eruptDao = eruptDao;
        this.properties = properties;
        this.services = services;
    }

    @Override
    public void beforeAdd(MaApp app) {
        if (eruptDao.lambdaQuery(MaApp.class).eq(MaApp::getAppId, app.getAppId()).count() > 0) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("miniapp.appid_exists"));
        }
    }

    @Override
    public void beforeUpdate(MaApp app) {
        // A password field comes back empty when left alone
        MaApp old = eruptDao.find(MaApp.class, app.getId());
        if (StringUtils.isBlank(app.getSecret())) app.setSecret(old.getSecret());
        if (StringUtils.isBlank(app.getAesKey())) app.setAesKey(old.getAesKey());
    }

    @Override
    public void afterAdd(MaApp app) {
        services.register(app);
    }

    @Override
    public void afterUpdate(MaApp app) {
        services.register(app);
    }

    @Override
    public void afterDelete(MaApp app) {
        services.remove(app);
    }

    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        list.forEach(it -> it.put("serverUrl", properties.getCallbackDomain() + MaPortalController.PATH + "/" + it.get("appId")));
    }

}

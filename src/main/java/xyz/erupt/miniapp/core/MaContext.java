package xyz.erupt.miniapp.core;

import cn.binarywang.wx.miniapp.api.WxMaService;
import org.springframework.stereotype.Component;
import xyz.erupt.core.scope.EruptScopes;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.miniapp.model.MaApp;

/**
 * The app of the current request: the one picked in the workspace, already verified by erupt.
 *
 * @author YuePeng
 */
@Component
public class MaContext {

    private final MaServices services;

    private final EruptDao eruptDao;

    public MaContext(MaServices services, EruptDao eruptDao) {
        this.services = services;
        this.eruptDao = eruptDao;
    }

    public Long appId() {
        return EruptScopes.get(MaScope.class).getApp();
    }

    public MaApp app() {
        return eruptDao.find(MaApp.class, this.appId());
    }

    public WxMaService ma() {
        return services.get(this.appId());
    }

    public WxMaService ma(Long appId) {
        return services.get(appId);
    }

}

package xyz.erupt.miniapp.portal;

import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.view.R;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.miniapp.core.Ma;
import xyz.erupt.miniapp.core.MaServices;
import xyz.erupt.miniapp.model.MaApp;
import xyz.erupt.miniapp.model.MaUser;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * The two calls a Mini Program makes to sign a user in; they need no erupt session.
 * <pre>
 * POST /miniapp/{appid}/login?code=   the code of wx.login → {openid, unionid}
 * POST /miniapp/{appid}/phone?code=   the code of getPhoneNumber, with openid= → the phone, kept on the user
 * </pre>
 * The session key stays on the server and is never returned.
 *
 * @author YuePeng
 */
@RestController
public class MaAuthController {

    private final MaServices services;

    private final EruptDao eruptDao;

    public MaAuthController(MaServices services, EruptDao eruptDao) {
        this.services = services;
        this.eruptDao = eruptDao;
    }

    @PostMapping("/miniapp/{appid}/login")
    @Transactional
    public R<Map<String, String>> login(@PathVariable("appid") String appid, @RequestParam("code") String code) {
        Long appRowId = this.appRowId(appid);
        WxMaJscode2SessionResult session = Ma.call(() -> services.get(appRowId).getUserService().getSessionInfo(code));
        MaUser user = this.user(appRowId, session.getOpenid());
        user.setUnionid(session.getUnionid());
        user.setLoginCount(user.getLoginCount() + 1);
        user.setLastLoginTime(LocalDateTime.now());
        eruptDao.merge(user);
        return R.ok(Map.of("openid", session.getOpenid(), "unionid", null == session.getUnionid() ? "" : session.getUnionid()));
    }

    @PostMapping("/miniapp/{appid}/phone")
    @Transactional
    public R<String> phone(@PathVariable("appid") String appid, @RequestParam("openid") String openid, @RequestParam("code") String code) {
        Long appRowId = this.appRowId(appid);
        WxMaPhoneNumberInfo info = Ma.call(() -> services.get(appRowId).getUserService().getPhoneNumber(code));
        MaUser user = this.user(appRowId, openid);
        user.setPhone(info.getPurePhoneNumber());
        eruptDao.merge(user);
        return R.ok(info.getPurePhoneNumber());
    }

    private Long appRowId(String appid) {
        Long id = services.idOf(appid);
        if (null == id) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("miniapp.app_unknown") + " → " + appid);
        return id;
    }

    private MaUser user(Long appRowId, String openid) {
        MaUser user = eruptDao.lambdaQuery(MaUser.class)
                .with(MaUser::getApp).eq(MaApp::getId, appRowId).with()
                .eq(MaUser::getOpenid, openid).list().stream().findFirst().orElse(null);
        if (null == user) {
            user = new MaUser();
            user.setApp(eruptDao.find(MaApp.class, appRowId));
            user.setOpenid(openid);
            user.setFirstLoginTime(LocalDateTime.now());
        }
        return user;
    }

}

package xyz.erupt.miniapp.proxy;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.scheme.WxMaGenerateSchemeRequest;
import cn.binarywang.wx.miniapp.bean.shortlink.GenerateShortLinkRequest;
import cn.binarywang.wx.miniapp.bean.urllink.GenerateUrlLinkRequest;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.miniapp.core.Ma;
import xyz.erupt.miniapp.core.MaContext;
import xyz.erupt.miniapp.model.MaLink;

/**
 * Saving a link asks WeChat for it.
 *
 * @author YuePeng
 */
@Component
public class MaLinkDataProxy implements DataProxy<MaLink> {

    private final MaContext context;

    public MaLinkDataProxy(MaContext context) {
        this.context = context;
    }

    @Override
    public void beforeAdd(MaLink link) {
        WxMaService ma = context.ma(link.getApp().getId());
        boolean expires = null != link.getExpireDays();
        link.setLink(Ma.call(() -> switch (link.getKind()) {
            case MaLink.SCHEME -> {
                WxMaGenerateSchemeRequest.JumpWxa jump = WxMaGenerateSchemeRequest.JumpWxa.newBuilder()
                        .path(link.getPath()).query(link.getQuery()).envVersion(link.getEnvVersion()).build();
                yield ma.getWxMaSchemeService().generate(WxMaGenerateSchemeRequest.newBuilder().jumpWxa(jump).isExpire(expires)
                        .expireType(expires ? 1 : null).expireInterval(expires ? link.getExpireDays() : null).build());
            }
            case MaLink.SHORT_LINK -> ma.getLinkService().generateShortLink(GenerateShortLinkRequest.builder()
                    .pageUrl(link.getPath()).pageTitle(link.getTitle()).isPermanent(true).build());
            default -> ma.getLinkService().generateUrlLink(GenerateUrlLinkRequest.builder()
                    .path(link.getPath()).query(link.getQuery()).envVersion(link.getEnvVersion()).isExpire(expires)
                    .expireType(expires ? 1 : null).expireInterval(expires ? link.getExpireDays() : null).build());
        }));
    }

}

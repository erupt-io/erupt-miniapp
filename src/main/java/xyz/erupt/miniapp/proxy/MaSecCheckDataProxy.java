package xyz.erupt.miniapp.proxy;

import cn.binarywang.wx.miniapp.bean.security.WxMaMsgSecCheckCheckRequest;
import cn.binarywang.wx.miniapp.bean.security.WxMaMsgSecCheckCheckResponse;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.miniapp.core.Ma;
import xyz.erupt.miniapp.core.MaContext;
import xyz.erupt.miniapp.model.MaSecCheck;

/**
 * Saving a text runs WeChat's content check on it.
 *
 * @author YuePeng
 */
@Component
public class MaSecCheckDataProxy implements DataProxy<MaSecCheck> {

    private final MaContext context;

    public MaSecCheckDataProxy(MaContext context) {
        this.context = context;
    }

    @Override
    public void beforeAdd(MaSecCheck check) {
        WxMaMsgSecCheckCheckRequest request = WxMaMsgSecCheckCheckRequest.builder().version("2").openid(check.getOpenid())
                .scene(Integer.valueOf(check.getScene())).content(check.getContent()).build();
        WxMaMsgSecCheckCheckResponse response = Ma.call(() -> context.ma(check.getApp().getId()).getSecurityService().checkMessage(request));
        check.setTraceId(response.getTraceId());
        if (null != response.getResult()) {
            check.setSuggest(response.getResult().getSuggest());
            check.setLabel(response.getResult().getLabel());
        }
    }

}

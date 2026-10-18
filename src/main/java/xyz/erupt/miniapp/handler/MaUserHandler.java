package xyz.erupt.miniapp.handler;

import cn.binarywang.wx.miniapp.bean.WxMaKefuMessage;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.miniapp.core.Ma;
import xyz.erupt.miniapp.core.MaContext;
import xyz.erupt.miniapp.form.MaKefuForm;
import xyz.erupt.miniapp.model.MaUser;

import java.util.List;

/**
 * @author YuePeng
 */
@Component
public class MaUserHandler implements OperationHandler<MaUser, MaKefuForm> {

    private final MaContext context;

    public MaUserHandler(MaContext context) {
        this.context = context;
    }

    @Override
    public String exec(List<MaUser> data, MaKefuForm form, String[] param) {
        WxMaKefuMessage message = WxMaKefuMessage.newTextBuilder().toUser(data.get(0).getOpenid()).content(form.getText()).build();
        Ma.run(() -> context.ma().getMsgService().sendKefuMsg(message));
        return "msg.success('OK')";
    }

}

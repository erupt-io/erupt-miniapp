package xyz.erupt.miniapp.handler;

import cn.binarywang.wx.miniapp.bean.WxMaSubscribeMessage;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.miniapp.core.Ma;
import xyz.erupt.miniapp.core.MaContext;
import xyz.erupt.miniapp.form.MaSubscribeSendForm;
import xyz.erupt.miniapp.live.MaSubscribeTemplate;
import xyz.erupt.miniapp.model.MaSubscribeLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Sends a subscribe message to each openid and logs every attempt.
 *
 * @author YuePeng
 */
@Component
public class MaSubscribeSendHandler implements OperationHandler<MaSubscribeTemplate, MaSubscribeSendForm> {

    private final EruptDao eruptDao;

    private final MaContext context;

    public MaSubscribeSendHandler(EruptDao eruptDao, MaContext context) {
        this.eruptDao = eruptDao;
        this.context = context;
    }

    @Override
    @Transactional
    public String exec(List<MaSubscribeTemplate> data, MaSubscribeSendForm form, String[] param) {
        MaSubscribeTemplate template = data.get(0);
        int success = 0, fail = 0;
        for (String openid : Ma.lines(form.getOpenids())) {
            WxMaSubscribeMessage message = WxMaSubscribeMessage.builder().toUser(openid).templateId(template.getPriTmplId())
                    .page(form.getPage()).miniprogramState(form.getState()).build();
            if (StringUtils.isNotBlank(form.getData())) {
                JsonObject json = GsonFactory.getGson().fromJson(form.getData(), JsonObject.class);
                for (String key : json.keySet()) message.addData(new WxMaSubscribeMessage.MsgData(key, json.get(key).getAsString()));
            }
            MaSubscribeLog log = new MaSubscribeLog();
            log.setApp(context.app());
            log.setTemplateId(template.getPriTmplId());
            log.setTitle(template.getTitle());
            log.setOpenid(openid);
            log.setData(form.getData());
            log.setPage(form.getPage());
            log.setSendTime(LocalDateTime.now());
            try {
                context.ma().getSubscribeService().sendSubscribeMsg(message);
                log.setStatus(MaSubscribeLog.SENT);
                success++;
            } catch (Exception e) {
                log.setStatus(MaSubscribeLog.FAILED);
                log.setErrorMsg(e.getMessage());
                fail++;
            }
            eruptDao.persist(log);
        }
        return "msg.success('" + success + " / " + fail + "')";
    }

}

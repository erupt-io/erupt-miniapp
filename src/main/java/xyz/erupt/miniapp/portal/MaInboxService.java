package xyz.erupt.miniapp.portal;

import cn.binarywang.wx.miniapp.bean.WxMaMessage;
import cn.binarywang.wx.miniapp.bean.WxMaSubscribeMsgEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.miniapp.core.Ma;
import xyz.erupt.miniapp.model.MaApp;
import xyz.erupt.miniapp.model.MaInbox;
import xyz.erupt.miniapp.model.MaSubscription;

import java.time.LocalDateTime;

/**
 * What happens to a message or event WeChat pushes: it is logged, and a subscribe prompt's answer
 * is kept per user and template.
 *
 * @author YuePeng
 */
@Service
public class MaInboxService {

    private final EruptDao eruptDao;

    public MaInboxService(EruptDao eruptDao) {
        this.eruptDao = eruptDao;
    }

    @Transactional
    public void receive(Long appRowId, WxMaMessage in) {
        MaApp app = eruptDao.find(MaApp.class, appRowId);
        MaInbox inbox = new MaInbox();
        inbox.setApp(app);
        inbox.setOpenid(in.getFromUser());
        inbox.setMsgType(in.getMsgType());
        inbox.setEvent(in.getEvent());
        inbox.setContent(StringUtils.abbreviate(StringUtils.firstNonBlank(in.getContent(), in.getTitle(), in.getPicUrl(), in.getPagePath(), in.getMediaId()), 4000));
        inbox.setCreateTime(null == in.getCreateTime() ? LocalDateTime.now() : Ma.time(in.getCreateTime()));
        eruptDao.persist(inbox);
        if (null != in.getSubscribeMsgPopupEvent() && null != in.getSubscribeMsgPopupEvent().getList()) {
            for (WxMaSubscribeMsgEvent.PopupEvent event : in.getSubscribeMsgPopupEvent().getList()) {
                this.subscription(app, in.getFromUser(), event.getTemplateId(), event.getSubscribeStatusString());
            }
        }
        if (null != in.getSubscribeMsgChangeEvent() && null != in.getSubscribeMsgChangeEvent().getList()) {
            for (WxMaSubscribeMsgEvent.ChangeEvent event : in.getSubscribeMsgChangeEvent().getList()) {
                this.subscription(app, in.getFromUser(), event.getTemplateId(), event.getSubscribeStatusString());
            }
        }
    }

    private void subscription(MaApp app, String openid, String templateId, String status) {
        MaSubscription subscription = eruptDao.lambdaQuery(MaSubscription.class)
                .with(MaSubscription::getApp).eq(MaApp::getId, app.getId()).with()
                .eq(MaSubscription::getOpenid, openid).eq(MaSubscription::getTemplateId, templateId).list().stream().findFirst().orElse(null);
        if (null == subscription) {
            subscription = new MaSubscription();
            subscription.setApp(app);
            subscription.setOpenid(openid);
            subscription.setTemplateId(templateId);
        }
        subscription.setStatus(status);
        subscription.setUpdateTime(LocalDateTime.now());
        eruptDao.merge(subscription);
    }

}

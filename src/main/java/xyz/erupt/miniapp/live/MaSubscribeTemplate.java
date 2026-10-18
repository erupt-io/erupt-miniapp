package xyz.erupt.miniapp.live;

import lombok.Getter;
import lombok.Setter;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptScope;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.miniapp.core.Ma;
import xyz.erupt.miniapp.core.MaContext;
import xyz.erupt.miniapp.core.MaScope;
import xyz.erupt.miniapp.data.MaDataService;
import xyz.erupt.miniapp.data.MaResource;
import xyz.erupt.miniapp.form.MaSubscribeSendForm;
import xyz.erupt.miniapp.handler.MaSubscribeSendHandler;
import xyz.erupt.miniapp.model.MaSubscribeLog;

import java.util.List;

/**
 * The app's subscribe message templates. Add one from the Template library page, or here by its
 * library id and keywords.
 *
 * @author YuePeng
 */
@Getter
@Setter
@EruptScope(value = MaScope.class, bind = false)
@EruptDataProcessor(MaDataService.DATA_PROCESSOR)
@Erupt(name = "Subscribe templates", primaryKeyCol = "priTmplId", power = @Power(edit = false),
        rowOperation = @RowOperation(code = "send", title = "Send", icon = "fa fa-bell", mode = RowOperation.Mode.SINGLE,
                eruptClass = MaSubscribeSendForm.class, operationHandler = MaSubscribeSendHandler.class),
        drills = @Drill(title = "Log", icon = "fa fa-history", link = @Link(linkErupt = MaSubscribeLog.class, column = "priTmplId", joinColumn = "templateId")))
public class MaSubscribeTemplate {

    @EruptField(views = @View(title = "Template ID"))
    private String priTmplId;

    @EruptField(views = @View(title = "Title"), edit = @Edit(title = "Title", search = @Search(operator = QueryExpression.LIKE)))
    private String title;

    @EruptField(views = @View(title = "Content"))
    private String content;

    @EruptField(views = @View(title = "Example"))
    private String example;

    @EruptField(views = @View(title = "Kind"))
    private String kind;

    @EruptField(edit = @Edit(title = "Library template ID", notNull = true, desc = "The tid of a public template"))
    private String tid;

    @EruptField(edit = @Edit(title = "Keyword IDs", notNull = true, desc = "The kids to use, comma separated, in display order"))
    private String kids;

    @EruptField(edit = @Edit(title = "Scene", desc = "Where the message is used, up to 15 characters"))
    private String scene;

    @Component
    public static class Resource implements MaResource<MaSubscribeTemplate> {

        @Override
        public Class<MaSubscribeTemplate> type() {
            return MaSubscribeTemplate.class;
        }

        @Override
        public List<MaSubscribeTemplate> list(MaContext ma, EruptQuery query) throws WxErrorException {
            return ma.ma().getSubscribeService().getTemplateList().stream().map(it -> {
                MaSubscribeTemplate row = new MaSubscribeTemplate();
                row.setPriTmplId(it.getPriTmplId());
                row.setTitle(it.getTitle());
                row.setContent(it.getContent());
                row.setExample(it.getExample());
                row.setKind(2 == it.getType() ? "One-time" : "Long-term");
                return row;
            }).toList();
        }

        @Override
        public void add(MaContext ma, MaSubscribeTemplate row) throws WxErrorException {
            List<Integer> kids = Ma.lines(row.getKids().replace(',', '\n')).stream().map(Integer::valueOf).toList();
            row.setPriTmplId(ma.ma().getSubscribeService().addTemplate(row.getTid(), kids, row.getScene()));
        }

        @Override
        public void delete(MaContext ma, MaSubscribeTemplate row) throws WxErrorException {
            ma.ma().getSubscribeService().delTemplate(row.getPriTmplId());
        }
    }

}

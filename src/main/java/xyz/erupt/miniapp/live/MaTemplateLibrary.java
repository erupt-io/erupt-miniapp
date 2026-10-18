package xyz.erupt.miniapp.live;

import lombok.Getter;
import lombok.Setter;
import me.chanjar.weixin.common.bean.subscribemsg.PubTemplateTitleListResult;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptScope;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.miniapp.core.Ma;
import xyz.erupt.miniapp.core.MaContext;
import xyz.erupt.miniapp.core.MaScope;
import xyz.erupt.miniapp.data.MaDataService;
import xyz.erupt.miniapp.data.MaResource;
import xyz.erupt.miniapp.form.MaLibraryAddForm;
import xyz.erupt.miniapp.handler.MaLibraryHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * WeChat's public template library, browsed by the categories the app belongs to.
 *
 * @author YuePeng
 */
@Getter
@Setter
@EruptScope(value = MaScope.class, bind = false)
@EruptDataProcessor(MaDataService.DATA_PROCESSOR)
@Erupt(name = "Template library", primaryKeyCol = "tid", power = @Power(add = false, edit = false, delete = false), rowOperation = {
        @RowOperation(code = "keywords", title = "Keywords", icon = "fa fa-list", mode = RowOperation.Mode.SINGLE, callHint = "",
                operationHandler = MaLibraryHandler.class, operationParam = MaLibraryHandler.KEYWORDS),
        @RowOperation(code = "add", title = "Add to my templates", icon = "fa fa-plus", mode = RowOperation.Mode.SINGLE,
                eruptClass = MaLibraryAddForm.class, operationHandler = MaLibraryHandler.class, operationParam = MaLibraryHandler.ADD)
})
public class MaTemplateLibrary {

    @EruptField(views = @View(title = "Category"), edit = @Edit(title = "Category", type = EditType.CHOICE, search = @Search(notNull = true),
            choiceType = @ChoiceType(fetchHandler = Categories.class)))
    private String category;

    @EruptField(views = @View(title = "Library ID"))
    private Integer tid;

    @EruptField(views = @View(title = "Title"), edit = @Edit(title = "Title", search = @Search))
    private String title;

    @EruptField(views = @View(title = "Kind"))
    private String kind;

    @Component
    public static class Categories implements ChoiceFetchHandler<Object> {

        private final MaContext context;

        public Categories(MaContext context) {
            this.context = context;
        }

        @Override
        public List<VLModel> fetch(String[] params) {
            return Ma.call(() -> context.ma().getSubscribeService().getCategory()).stream().map(it -> new VLModel(String.valueOf(it.getId()), it.getName())).toList();
        }
    }

    @Component
    public static class Resource implements MaResource<MaTemplateLibrary> {

        private static final int PAGE = 30;

        private static final int MAX = 600;

        @Override
        public Class<MaTemplateLibrary> type() {
            return MaTemplateLibrary.class;
        }

        @Override
        public List<MaTemplateLibrary> list(MaContext ma, EruptQuery query) throws WxErrorException {
            Object category = MaDataService.condition(query, "category");
            List<MaTemplateLibrary> rows = new ArrayList<>();
            if (null == category) return rows;
            int start = 0;
            PubTemplateTitleListResult page;
            do {
                page = ma.ma().getSubscribeService().getPubTemplateTitleList(new String[]{category.toString()}, start, PAGE);
                if (null != page.getData()) {
                    for (PubTemplateTitleListResult.TemplateItem item : page.getData()) {
                        MaTemplateLibrary row = new MaTemplateLibrary();
                        row.setCategory(category.toString());
                        row.setTid(item.getTid());
                        row.setTitle(item.getTitle());
                        row.setKind(Integer.valueOf(2).equals(item.getType()) ? "One-time" : "Long-term");
                        rows.add(row);
                    }
                }
                start += PAGE;
            } while (start < page.getCount() && start < MAX);
            return rows;
        }
    }

}

package xyz.erupt.miniapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptScope;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.jpa.model.MetaModel;
import xyz.erupt.miniapp.core.MaScope;
import xyz.erupt.miniapp.proxy.MaLinkDataProxy;

/**
 * Links that open the Mini Program from outside WeChat (URL link, URL scheme) or shorten a page
 * (short link). Saving one asks WeChat for it.
 *
 * @author YuePeng
 */
@Getter
@Setter
@Entity
@Table(name = "e_ma_link")
@EruptScope(MaScope.class)
@Erupt(name = "Links", dataProxy = MaLinkDataProxy.class, power = @Power(edit = false), orderBy = "createTime desc")
public class MaLink extends MetaModel {

    public static final String URL_LINK = "URL_LINK";

    public static final String SCHEME = "SCHEME";

    public static final String SHORT_LINK = "SHORT_LINK";

    @ManyToOne
    @EruptField(views = @View(title = "Mini Program", column = "name"), edit = @Edit(title = "Mini Program", notNull = true, type = EditType.REFERENCE_TABLE,
            referenceTableType = @ReferenceTableType(label = "name")))
    private MaApp app;

    @EruptField(views = @View(title = "Name"), edit = @Edit(title = "Name", notNull = true, search = @Search(operator = QueryExpression.LIKE)))
    private String name;

    @EruptField(views = @View(title = "Kind"), edit = @Edit(title = "Kind", notNull = true, search = @Search, type = EditType.CHOICE, choiceType = @ChoiceType(vl = {
            @VL(value = URL_LINK, label = "URL link (https)"),
            @VL(value = SCHEME, label = "URL scheme (weixin://)"),
            @VL(value = SHORT_LINK, label = "Short link (inside WeChat)")
    })))
    private String kind = URL_LINK;

    @EruptField(views = @View(title = "Page"), edit = @Edit(title = "Page", notNull = true, desc = "pages/index/index; a short link takes the page with its query"))
    private String path;

    @EruptField(edit = @Edit(title = "Query", desc = "a=1&b=2", dynamic = @Dynamic(dependField = "kind", condition = "value !== 'SHORT_LINK'")))
    private String query;

    @EruptField(edit = @Edit(title = "Version", type = EditType.CHOICE, choiceType = @ChoiceType(vl = {
            @VL(value = "release", label = "Release"), @VL(value = "trial", label = "Trial"), @VL(value = "develop", label = "Develop")
    }), dynamic = @Dynamic(dependField = "kind", condition = "value !== 'SHORT_LINK'")))
    private String envVersion = "release";

    @EruptField(edit = @Edit(title = "Valid for (days)", type = EditType.NUMBER, desc = "1 to 30; empty means permanent",
            dynamic = @Dynamic(dependField = "kind", condition = "value !== 'SHORT_LINK'")))
    private Integer expireDays;

    @EruptField(edit = @Edit(title = "Title", dynamic = @Dynamic(dependField = "kind", condition = "value === 'SHORT_LINK'")))
    private String title;

    @EruptField(views = @View(title = "Link"))
    private String link;

}

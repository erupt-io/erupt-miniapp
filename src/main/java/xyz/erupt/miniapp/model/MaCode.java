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
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.jpa.model.MetaModel;
import xyz.erupt.miniapp.core.MaScope;
import xyz.erupt.miniapp.proxy.MaCodeDataProxy;

/**
 * Mini Program codes. Saving one asks WeChat for the image and keeps it under the upload path.
 *
 * @author YuePeng
 */
@Getter
@Setter
@Entity
@Table(name = "e_ma_code")
@EruptScope(MaScope.class)
@Erupt(name = "Codes", dataProxy = MaCodeDataProxy.class, power = @Power(edit = false), orderBy = "createTime desc")
public class MaCode extends MetaModel {

    public static final String UNLIMITED = "UNLIMITED";

    public static final String PATH = "PATH";

    @ManyToOne
    @EruptField(views = @View(title = "Mini Program", column = "name"), edit = @Edit(title = "Mini Program", notNull = true, type = EditType.REFERENCE_TABLE,
            referenceTableType = @ReferenceTableType(label = "name")))
    private MaApp app;

    @EruptField(views = @View(title = "Name"), edit = @Edit(title = "Name", notNull = true, search = @Search(operator = QueryExpression.LIKE)))
    private String name;

    @EruptField(views = @View(title = "Kind"), edit = @Edit(title = "Kind", notNull = true, search = @Search, type = EditType.CHOICE, choiceType = @ChoiceType(vl = {
            @VL(value = UNLIMITED, label = "Scene code (unlimited)"),
            @VL(value = PATH, label = "Path code (100,000 at most)")
    })))
    private String kind = UNLIMITED;

    @EruptField(views = @View(title = "Page"), edit = @Edit(title = "Page", notNull = true, desc = "pages/index/index, a published page without parameters; a path code may carry ?a=1"))
    private String page;

    @EruptField(views = @View(title = "Scene"), edit = @Edit(title = "Scene", desc = "Up to 32 characters, read by the page as options.scene",
            dynamic = @Dynamic(dependField = "kind", condition = "value === 'UNLIMITED'", match = Dynamic.Ctrl.NOTNULL)))
    private String scene;

    @EruptField(edit = @Edit(title = "Width (px)", type = EditType.NUMBER, desc = "280 to 1280"))
    private Integer width = 430;

    @EruptField(views = @View(title = "Version"), edit = @Edit(title = "Version", notNull = true, type = EditType.CHOICE, choiceType = @ChoiceType(vl = {
            @VL(value = "release", label = "Release"), @VL(value = "trial", label = "Trial"), @VL(value = "develop", label = "Develop")
    })))
    private String envVersion = "release";

    @EruptField(views = @View(title = "Code", type = ViewType.IMAGE))
    private String image;

}

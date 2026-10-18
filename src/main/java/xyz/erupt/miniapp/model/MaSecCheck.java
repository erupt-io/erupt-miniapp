package xyz.erupt.miniapp.model;

import jakarta.persistence.Column;
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
import xyz.erupt.miniapp.proxy.MaSecCheckDataProxy;

/**
 * Content security checks: saving a text runs it through WeChat's check and keeps the verdict.
 *
 * @author YuePeng
 */
@Getter
@Setter
@Entity
@Table(name = "e_ma_sec_check")
@EruptScope(MaScope.class)
@Erupt(name = "Content checks", dataProxy = MaSecCheckDataProxy.class, power = @Power(edit = false), orderBy = "createTime desc")
public class MaSecCheck extends MetaModel {

    @ManyToOne
    @EruptField(views = @View(title = "Mini Program", column = "name"), edit = @Edit(title = "Mini Program", notNull = true, type = EditType.REFERENCE_TABLE,
            referenceTableType = @ReferenceTableType(label = "name")))
    private MaApp app;

    @Column(length = 4000)
    @EruptField(views = @View(title = "Text"), edit = @Edit(title = "Text", notNull = true, type = EditType.TEXTAREA, search = @Search(operator = QueryExpression.LIKE)))
    private String content;

    @EruptField(views = @View(title = "Scene"), edit = @Edit(title = "Scene", notNull = true, type = EditType.CHOICE, choiceType = @ChoiceType(vl = {
            @VL(value = "1", label = "Profile"), @VL(value = "2", label = "Comment"), @VL(value = "3", label = "Forum"), @VL(value = "4", label = "Social log")
    })))
    private String scene = "2";

    @EruptField(views = @View(title = "OpenID"), edit = @Edit(title = "OpenID", notNull = true, desc = "The user who wrote it; they must have used the app in the last two hours"))
    private String openid;

    @EruptField(views = @View(title = "Verdict"), edit = @Edit(title = "Verdict", search = @Search))
    private String suggest;

    @EruptField(views = @View(title = "Label"))
    private String label;

    @EruptField(views = @View(title = "Trace ID"))
    private String traceId;

}

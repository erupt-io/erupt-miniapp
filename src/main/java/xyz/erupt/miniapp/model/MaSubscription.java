package xyz.erupt.miniapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
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
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTableType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.miniapp.core.MaScope;

import java.time.LocalDateTime;

/**
 * What users answered to subscribe prompts, as WeChat pushes it to the portal: who may receive
 * which template.
 *
 * @author YuePeng
 */
@Getter
@Setter
@Entity
@Table(name = "e_ma_subscription", indexes = @Index(columnList = "openid"))
@EruptScope(MaScope.class)
@Erupt(name = "Subscriptions", power = @Power(add = false, edit = false), orderBy = "updateTime desc")
public class MaSubscription extends BaseModel {

    @ManyToOne
    @EruptField(views = @View(title = "Mini Program", column = "name"), edit = @Edit(title = "Mini Program", type = EditType.REFERENCE_TABLE,
            referenceTableType = @ReferenceTableType(label = "name")))
    private MaApp app;

    @EruptField(views = @View(title = "OpenID"), edit = @Edit(title = "OpenID", search = @Search(operator = QueryExpression.LIKE)))
    private String openid;

    @EruptField(views = @View(title = "Template"), edit = @Edit(title = "Template", search = @Search(operator = QueryExpression.LIKE)))
    private String templateId;

    @EruptField(views = @View(title = "Answer"), edit = @Edit(title = "Answer", search = @Search))
    private String status;

    @EruptField(views = @View(title = "Answered at", sortable = true))
    private LocalDateTime updateTime;

}

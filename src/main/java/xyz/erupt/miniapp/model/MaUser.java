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
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTableType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.miniapp.core.MaScope;
import xyz.erupt.miniapp.form.MaKefuForm;
import xyz.erupt.miniapp.handler.MaUserHandler;

import java.time.LocalDateTime;

/**
 * Users who signed in through {@code POST /miniapp/{appid}/login}, with the phone they shared.
 *
 * @author YuePeng
 */
@Getter
@Setter
@Entity
@Table(name = "e_ma_user", indexes = @Index(columnList = "openid"))
@EruptScope(MaScope.class)
@Erupt(name = "Users", power = @Power(add = false), orderBy = "lastLoginTime desc",
        rowOperation = @RowOperation(code = "message", title = "Message", icon = "fa fa-comment", mode = RowOperation.Mode.SINGLE,
                tip = "A customer service message: WeChat delivers it within 48 hours of the user's last message",
                eruptClass = MaKefuForm.class, operationHandler = MaUserHandler.class))
public class MaUser extends BaseModel {

    @ManyToOne
    @EruptField(views = @View(title = "Mini Program", column = "name"), edit = @Edit(title = "Mini Program", type = EditType.REFERENCE_TABLE,
            referenceTableType = @ReferenceTableType(label = "name")))
    private MaApp app;

    @EruptField(views = @View(title = "OpenID"), edit = @Edit(title = "OpenID", search = @Search(operator = QueryExpression.LIKE), readonly = @Readonly))
    private String openid;

    @EruptField(views = @View(title = "UnionID"), edit = @Edit(title = "UnionID", readonly = @Readonly))
    private String unionid;

    @EruptField(views = @View(title = "Phone"), edit = @Edit(title = "Phone", search = @Search(operator = QueryExpression.LIKE), readonly = @Readonly))
    private String phone;

    @EruptField(views = @View(title = "Remark"), edit = @Edit(title = "Remark", search = @Search(operator = QueryExpression.LIKE)))
    private String remark;

    @EruptField(views = @View(title = "Logins"))
    private Integer loginCount = 0;

    @EruptField(views = @View(title = "First login"))
    private LocalDateTime firstLoginTime;

    @EruptField(views = @View(title = "Last login", sortable = true))
    private LocalDateTime lastLoginTime;

}

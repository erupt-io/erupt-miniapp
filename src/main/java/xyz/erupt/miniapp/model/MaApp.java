package xyz.erupt.miniapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.jpa.model.MetaModel;
import xyz.erupt.miniapp.proxy.MaAppDataProxy;

/**
 * A Mini Program's credentials. The only page outside the workspace: it is what the workspace picks from.
 *
 * @author YuePeng
 */
@Getter
@Setter
@Entity
@Table(name = "e_ma_app", uniqueConstraints = @UniqueConstraint(columnNames = "app_id"))
@Erupt(name = "Mini Programs", dataProxy = MaAppDataProxy.class)
public class MaApp extends MetaModel {

    @EruptField(views = @View(title = "Name"), edit = @Edit(title = "Name", notNull = true, search = @Search(operator = QueryExpression.LIKE)))
    private String name;

    @EruptField(views = @View(title = "AppID"), edit = @Edit(title = "AppID", notNull = true, search = @Search(operator = QueryExpression.LIKE), readonly = @Readonly(add = false)))
    private String appId;

    @EruptField(edit = @Edit(title = "AppSecret", notNull = true, type = EditType.PASSWORD))
    private String secret;

    @EruptField(edit = @Edit(title = "Token", desc = "The token of the WeChat console's message push configuration"))
    private String token;

    @EruptField(edit = @Edit(title = "EncodingAESKey", type = EditType.PASSWORD, desc = "Required in safe mode, empty in plain mode"))
    private String aesKey;

    @EruptField(views = @View(title = "Push format"), edit = @Edit(title = "Push format", notNull = true, type = EditType.CHOICE,
            choiceType = @ChoiceType(vl = {@VL(value = "JSON", label = "JSON"), @VL(value = "XML", label = "XML")})))
    private String msgDataFormat = "JSON";

    // The URL to paste into the WeChat console, built from the callback domain on fetch
    @Transient
    @EruptField(views = @View(title = "Server URL"))
    private String serverUrl;

}

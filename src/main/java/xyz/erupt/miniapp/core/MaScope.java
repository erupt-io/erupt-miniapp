package xyz.erupt.miniapp.core;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;

/**
 * The workspace every Mini Program page follows: one app, picked once in the page header. The
 * app id binds the JPA models by their {@code app} field.
 *
 * @author YuePeng
 */
@Getter
@Setter
@Erupt(name = "Workspace", primaryKeyCol = "app")
public class MaScope {

    @EruptField(edit = @Edit(title = "Mini Program", type = EditType.CHOICE, notNull = true, choiceType = @ChoiceType(fetchHandler = MaAppFetchHandler.class)))
    private Long app;

}

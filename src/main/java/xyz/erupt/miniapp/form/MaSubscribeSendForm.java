package xyz.erupt.miniapp.form;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.VL;

/**
 * @author YuePeng
 */
@Getter
@Setter
@Erupt(name = "Send subscribe message", primaryKeyCol = "openids")
public class MaSubscribeSendForm {

    @EruptField(edit = @Edit(title = "To", notNull = true, type = EditType.TEXTAREA, desc = "OpenIDs, one per line; each must have accepted this template"))
    private String openids;

    @EruptField(edit = @Edit(title = "Page", desc = "Opened when the user taps the message; optional"))
    private String page;

    @EruptField(edit = @Edit(title = "Data", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "json"),
            desc = "{\"thing1\":\"Hello\",\"time2\":\"2026-01-01\"}"))
    private String data;

    @EruptField(edit = @Edit(title = "Version", type = EditType.CHOICE, choiceType = @ChoiceType(vl = {
            @VL(value = "formal", label = "Release"), @VL(value = "trial", label = "Trial"), @VL(value = "developer", label = "Develop")
    })))
    private String state = "formal";

}

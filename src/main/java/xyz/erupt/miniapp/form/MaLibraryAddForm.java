package xyz.erupt.miniapp.form;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;

/**
 * @author YuePeng
 */
@Getter
@Setter
@Erupt(name = "Add to my templates", primaryKeyCol = "kids")
public class MaLibraryAddForm {

    @EruptField(edit = @Edit(title = "Keyword IDs", notNull = true, desc = "The kids to use, comma separated, in display order; see Keywords for the ids"))
    private String kids;

    @EruptField(edit = @Edit(title = "Scene", desc = "Where the message is used, up to 15 characters"))
    private String scene;

}

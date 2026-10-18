package xyz.erupt.miniapp.handler;

import me.chanjar.weixin.common.bean.subscribemsg.PubTemplateKeyword;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.miniapp.core.Ma;
import xyz.erupt.miniapp.core.MaContext;
import xyz.erupt.miniapp.form.MaLibraryAddForm;
import xyz.erupt.miniapp.live.MaTemplateLibrary;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 */
@Component
public class MaLibraryHandler implements OperationHandler<MaTemplateLibrary, MaLibraryAddForm> {

    public static final String KEYWORDS = "keywords";

    public static final String ADD = "add";

    private final MaContext context;

    public MaLibraryHandler(MaContext context) {
        this.context = context;
    }

    @Override
    public String exec(List<MaTemplateLibrary> data, MaLibraryAddForm form, String[] param) {
        String tid = String.valueOf(data.get(0).getTid());
        if (KEYWORDS.equals(param[0])) {
            List<PubTemplateKeyword> keywords = Ma.call(() -> context.ma().getSubscribeService().getPubTemplateKeyWordsById(tid));
            return "alert('" + keywords.stream().map(it -> it.getKid() + " " + it.getName() + " (" + it.getExample() + ")")
                    .collect(Collectors.joining("\\n")).replace("'", "\\'") + "')";
        }
        List<Integer> kids = Ma.lines(form.getKids().replace(',', '\n')).stream().map(Integer::valueOf).toList();
        String priTmplId = Ma.call(() -> context.ma().getSubscribeService().addTemplate(tid, kids, form.getScene()));
        return "msg.success('" + priTmplId + "')";
    }

}

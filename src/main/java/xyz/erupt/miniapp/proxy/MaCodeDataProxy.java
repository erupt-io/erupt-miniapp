package xyz.erupt.miniapp.proxy;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.miniapp.core.Ma;
import xyz.erupt.miniapp.core.MaContext;
import xyz.erupt.miniapp.model.MaCode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Saving a code asks WeChat for the image, kept as an attachment under the upload path.
 *
 * @author YuePeng
 */
@Component
public class MaCodeDataProxy implements DataProxy<MaCode> {

    private static final String DIR = "/miniapp/codes/";

    private final MaContext context;

    private final EruptProp eruptProp;

    public MaCodeDataProxy(MaContext context, EruptProp eruptProp) {
        this.context = context;
        this.eruptProp = eruptProp;
    }

    @Override
    public void beforeAdd(MaCode code) {
        byte[] image = Ma.call(() -> MaCode.UNLIMITED.equals(code.getKind())
                ? context.ma(code.getApp().getId()).getQrcodeService().createWxaCodeUnlimitBytes(code.getScene(), code.getPage(), false, code.getEnvVersion(), code.getWidth(), true, null, false)
                : context.ma(code.getApp().getId()).getQrcodeService().createWxaCodeBytes(code.getPage(), code.getEnvVersion(), code.getWidth(), true, null, false));
        String path = DIR + Erupts.generateCode(16) + ".png";
        File file = new File(eruptProp.getUploadPath(), path);
        try {
            Files.createDirectories(file.getParentFile().toPath());
            Files.write(file.toPath(), image);
        } catch (IOException e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
        code.setImage(path);
    }

    @Override
    public void afterDelete(MaCode code) {
        if (null != code.getImage()) new File(eruptProp.getUploadPath(), code.getImage()).delete();
    }

}

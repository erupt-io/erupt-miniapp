package xyz.erupt.miniapp;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.module.EruptModule;
import xyz.erupt.core.module.EruptModuleInvoke;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.ModuleInfo;
import xyz.erupt.miniapp.live.*;
import xyz.erupt.miniapp.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * WeChat Mini Program admin on erupt. The apps go in the Mini Programs page, the workspace in the
 * header picks one, and every other page follows it.
 *
 * @author YuePeng
 */
@Configuration
// The standalone launcher lives in this package too; a host application must not pick it up
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = SpringBootApplication.class))
@EruptScan
@EntityScan
public class EruptMiniappAutoConfiguration implements EruptModule {

    static {
        EruptModuleInvoke.addEruptModule(EruptMiniappAutoConfiguration.class);
    }

    @Override
    public ModuleInfo info() {
        return ModuleInfo.builder().name("erupt-miniapp").description("WeChat Mini Program admin").build();
    }

    @Override
    public List<MetaMenu> initMenus() {
        List<MetaMenu> menus = new ArrayList<>();
        MetaMenu root = MetaMenu.createRootMenu("$miniapp", "Mini Program", "fa fa-mobile", 57);
        menus.add(root);
        int sort = 0;
        for (Class<?> model : List.of(MaApp.class, MaUser.class, MaSubscribeTemplate.class, MaTemplateLibrary.class, MaSubscription.class,
                MaCode.class, MaLink.class, MaSecCheck.class, MaLiveRoom.class, MaInbox.class, MaDailyStat.class, MaRetainStat.class)) {
            menus.add(MetaMenu.createEruptClassMenu(model, root, sort += 10));
        }
        // Reached by drilling from a subscribe template
        menus.add(MetaMenu.createEruptClassMenu(MaSubscribeLog.class, root, sort += 10, MenuStatus.HIDE));
        return menus;
    }

}

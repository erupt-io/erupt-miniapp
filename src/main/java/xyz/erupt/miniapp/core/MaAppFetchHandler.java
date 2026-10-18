package xyz.erupt.miniapp.core;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.miniapp.model.MaApp;

import java.util.List;

/**
 * @author YuePeng
 */
@Component
public class MaAppFetchHandler implements ChoiceFetchHandler<MaScope> {

    private final EruptDao eruptDao;

    public MaAppFetchHandler(EruptDao eruptDao) {
        this.eruptDao = eruptDao;
    }

    @Override
    public List<VLModel> fetch(String[] params) {
        return eruptDao.lambdaQuery(MaApp.class).orderBy(MaApp::getName).list().stream()
                .map(it -> new VLModel(String.valueOf(it.getId()), it.getName())).toList();
    }

}

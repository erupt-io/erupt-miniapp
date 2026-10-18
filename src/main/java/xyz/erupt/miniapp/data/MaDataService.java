package xyz.erupt.miniapp.data;

import org.springframework.stereotype.Service;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptBeanDataService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.miniapp.core.Ma;
import xyz.erupt.miniapp.core.MaContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data source of the live models: dispatches to the {@link MaResource} of the model's class.
 * <pre>
 * &#64;EruptDataProcessor(MaDataService.DATA_PROCESSOR)
 * public class MaTag { ... }
 * </pre>
 *
 * @author YuePeng
 */
@Service
public class MaDataService extends EruptBeanDataService<Object> {

    public static final String DATA_PROCESSOR = "MINIAPP";

    static {
        DataProcessorManager.register(DATA_PROCESSOR, MaDataService.class);
    }

    private final Map<Class<?>, MaResource<Object>> resources = new HashMap<>();

    private final MaContext context;

    @SuppressWarnings("unchecked")
    public MaDataService(List<MaResource<?>> resources, MaContext context) {
        this.context = context;
        for (MaResource<?> resource : resources) this.resources.put(resource.type(), (MaResource<Object>) resource);
    }

    private MaResource<Object> resource(EruptModel eruptModel) {
        MaResource<Object> resource = resources.get(eruptModel.getClazz());
        if (null == resource) throw new EruptWebApiRuntimeException("No MaResource for " + eruptModel.getEruptName());
        return resource;
    }

    @Override
    protected List<Object> data(EruptModel eruptModel, EruptQuery eruptQuery) {
        return Ma.call(() -> this.resource(eruptModel).list(context, eruptQuery));
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {
        Ma.run(() -> this.resource(eruptModel).add(context, object));
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        Ma.run(() -> this.resource(eruptModel).edit(context, object));
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        Ma.run(() -> this.resource(eruptModel).delete(context, object));
    }

    /**
     * The value a query asks for on one field, from a search or a drill: a resource whose API
     * takes it as input (a comment thread, a date range) reads it here before listing.
     */
    public static Object condition(EruptQuery query, String key) {
        if (null != query.getConditions()) {
            for (Condition condition : query.getConditions()) {
                if (key.equals(condition.getKey()) && condition.getExpression() != QueryExpression.NULL) return condition.getValue();
            }
        }
        if (null != query.getConditionStrings()) {
            // "Model.key = 'value'", the fragment a drill produces
            for (String fragment : query.getConditionStrings()) {
                int eq = fragment.indexOf('=');
                if (eq < 0) continue;
                String left = fragment.substring(0, eq).trim();
                if (!left.equals(key) && !left.endsWith("." + key)) continue;
                String right = fragment.substring(eq + 1).trim();
                return right.startsWith("'") && right.endsWith("'") && right.length() >= 2 ? right.substring(1, right.length() - 1) : right;
            }
        }
        return null;
    }

}

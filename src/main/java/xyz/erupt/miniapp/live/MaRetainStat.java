package xyz.erupt.miniapp.live;

import cn.binarywang.wx.miniapp.bean.analysis.WxMaRetainInfo;
import lombok.Getter;
import lombok.Setter;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptScope;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.miniapp.core.Ma;
import xyz.erupt.miniapp.core.MaContext;
import xyz.erupt.miniapp.core.MaScope;
import xyz.erupt.miniapp.data.MaDataService;
import xyz.erupt.miniapp.data.MaResource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Retention: of the users first seen on a day, how many came back 1, 2, 3 ... days later.
 *
 * @author YuePeng
 */
@Getter
@Setter
@EruptScope(value = MaScope.class, bind = false)
@EruptDataProcessor(MaDataService.DATA_PROCESSOR)
@Erupt(name = "Retention", power = @Power(add = false, edit = false, delete = false), orderBy = "refDate desc")
public class MaRetainStat {

    @EruptField(views = @View(title = "ID", show = false))
    private String id;

    @EruptField(views = @View(title = "Date", sortable = true), edit = @Edit(title = "Date", type = EditType.DATE, search = @Search))
    private LocalDate refDate;

    @EruptField(views = @View(title = "Days later", sortable = true))
    private Integer daysLater;

    @EruptField(views = @View(title = "New users retained"))
    private Integer newUsers;

    @EruptField(views = @View(title = "Active users retained"))
    private Integer activeUsers;

    @Component
    public static class Resource implements MaResource<MaRetainStat> {

        @Override
        public Class<MaRetainStat> type() {
            return MaRetainStat.class;
        }

        @Override
        public List<MaRetainStat> list(MaContext ma, EruptQuery query) throws WxErrorException {
            LocalDate[] range = MaStats.range(MaDataService.condition(query, "refDate"), 30, 7);
            List<MaRetainStat> rows = new ArrayList<>();
            for (LocalDate day = range[0]; !day.isAfter(range[1]); day = day.plusDays(1)) {
                WxMaRetainInfo info = ma.ma().getAnalysisService().getDailyRetainInfo(Ma.date(day), Ma.date(day));
                if (null == info || null == info.getVisitUvNew()) continue;
                for (Map.Entry<Integer, Integer> entry : info.getVisitUvNew().entrySet()) {
                    MaRetainStat row = new MaRetainStat();
                    row.setRefDate(day);
                    row.setDaysLater(entry.getKey());
                    row.setId(day + "@" + entry.getKey());
                    row.setNewUsers(entry.getValue());
                    if (null != info.getVisitUv()) row.setActiveUsers(info.getVisitUv().get(entry.getKey()));
                    rows.add(row);
                }
            }
            return rows;
        }
    }

}

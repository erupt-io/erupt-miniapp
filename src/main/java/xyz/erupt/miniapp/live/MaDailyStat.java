package xyz.erupt.miniapp.live;

import cn.binarywang.wx.miniapp.bean.analysis.WxMaSummaryTrend;
import cn.binarywang.wx.miniapp.bean.analysis.WxMaVisitTrend;
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

/**
 * Visits per day: sessions, page views, visitors, new visitors, stay time and shares. The last 7
 * days unless a range is searched; WeChat keeps 30.
 *
 * @author YuePeng
 */
@Getter
@Setter
@EruptScope(value = MaScope.class, bind = false)
@EruptDataProcessor(MaDataService.DATA_PROCESSOR)
@Erupt(name = "Daily statistics", primaryKeyCol = "refDate", power = @Power(add = false, edit = false, delete = false), orderBy = "refDate desc")
public class MaDailyStat {

    @EruptField(views = @View(title = "Date", sortable = true), edit = @Edit(title = "Date", type = EditType.DATE, search = @Search))
    private LocalDate refDate;

    @EruptField(views = @View(title = "Sessions"))
    private Long sessionCnt;

    @EruptField(views = @View(title = "Page views"))
    private Long visitPv;

    @EruptField(views = @View(title = "Visitors"))
    private Long visitUv;

    @EruptField(views = @View(title = "New visitors"))
    private Long visitUvNew;

    @EruptField(views = @View(title = "Stay per visitor (s)"))
    private Float stayTimeUv;

    @EruptField(views = @View(title = "Pages per session"))
    private Float visitDepth;

    @EruptField(views = @View(title = "Total users"))
    private Long visitTotal;

    @EruptField(views = @View(title = "Shares"))
    private Long sharePv;

    @EruptField(views = @View(title = "Sharers"))
    private Long shareUv;

    @Component
    public static class Resource implements MaResource<MaDailyStat> {

        @Override
        public Class<MaDailyStat> type() {
            return MaDailyStat.class;
        }

        @Override
        public List<MaDailyStat> list(MaContext ma, EruptQuery query) throws WxErrorException {
            LocalDate[] range = MaStats.range(MaDataService.condition(query, "refDate"), 30, 7);
            List<MaDailyStat> rows = new ArrayList<>();
            // both calls span a single day
            for (LocalDate day = range[0]; !day.isAfter(range[1]); day = day.plusDays(1)) {
                MaDailyStat row = new MaDailyStat();
                row.setRefDate(day);
                for (WxMaVisitTrend trend : ma.ma().getAnalysisService().getDailyVisitTrend(Ma.date(day), Ma.date(day))) {
                    row.setSessionCnt(trend.getSessionCnt());
                    row.setVisitPv(trend.getVisitPv());
                    row.setVisitUv(trend.getVisitUv());
                    row.setVisitUvNew(trend.getVisitUvNew());
                    row.setStayTimeUv(trend.getStayTimeUv());
                    row.setVisitDepth(trend.getVisitDepth());
                }
                for (WxMaSummaryTrend summary : ma.ma().getAnalysisService().getDailySummaryTrend(Ma.date(day), Ma.date(day))) {
                    row.setVisitTotal(summary.getVisitTotal());
                    row.setSharePv(summary.getSharePv());
                    row.setShareUv(summary.getShareUv());
                }
                rows.add(row);
            }
            return rows;
        }
    }

}

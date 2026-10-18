package xyz.erupt.miniapp.live;

import lombok.Getter;
import lombok.Setter;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptScope;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.annotation.EruptDataProcessor;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.miniapp.core.Ma;
import xyz.erupt.miniapp.core.MaContext;
import xyz.erupt.miniapp.core.MaScope;
import xyz.erupt.miniapp.data.MaDataService;
import xyz.erupt.miniapp.data.MaResource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Live rooms of the app, as the live streaming component reports them.
 *
 * @author YuePeng
 */
@Getter
@Setter
@EruptScope(value = MaScope.class, bind = false)
@EruptDataProcessor(MaDataService.DATA_PROCESSOR)
@Erupt(name = "Live rooms", primaryKeyCol = "roomId", power = @Power(add = false, edit = false), orderBy = "startTime desc")
public class MaLiveRoom {

    private static final Map<Integer, String> STATUS = Map.of(101, "Live", 102, "Not started", 103, "Ended", 104, "Forbidden",
            105, "Paused", 106, "Error", 107, "Expired");

    @EruptField(views = @View(title = "Room ID"))
    private Integer roomId;

    @EruptField(views = @View(title = "Name"), edit = @Edit(title = "Name", search = @Search(operator = QueryExpression.LIKE)))
    private String name;

    @EruptField(views = @View(title = "Cover", type = ViewType.IMAGE))
    private String coverImg;

    @EruptField(views = @View(title = "Anchor"))
    private String anchorName;

    @EruptField(views = @View(title = "Status"), edit = @Edit(title = "Status", search = @Search))
    private String status;

    @EruptField(views = @View(title = "Starts at", sortable = true))
    private LocalDateTime startTime;

    @EruptField(views = @View(title = "Ends at"))
    private LocalDateTime endTime;

    @Component
    public static class Resource implements MaResource<MaLiveRoom> {

        @Override
        public Class<MaLiveRoom> type() {
            return MaLiveRoom.class;
        }

        @Override
        public List<MaLiveRoom> list(MaContext ma, EruptQuery query) throws WxErrorException {
            return ma.ma().getLiveService().getLiveInfos().stream().map(it -> {
                MaLiveRoom row = new MaLiveRoom();
                row.setRoomId(it.getRoomId());
                row.setName(it.getName());
                row.setCoverImg(it.getCoverImg());
                row.setAnchorName(it.getAnchorName());
                row.setStatus(STATUS.getOrDefault(it.getLiveStatus(), String.valueOf(it.getLiveStatus())));
                row.setStartTime(Ma.time(it.getStartTime()));
                row.setEndTime(Ma.time(it.getEndTime()));
                return row;
            }).toList();
        }

        @Override
        public void delete(MaContext ma, MaLiveRoom row) throws WxErrorException {
            ma.ma().getLiveService().deleteRoom(row.getRoomId());
        }
    }

}

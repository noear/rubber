package rubberadmin.dso.db;

import org.noear.solon.Utils;
import org.noear.water.dso.GatewayUtils;
import org.noear.weed.DbContext;
import org.noear.weed.DbTableQuery;
import rubberadmin.Config;
import rubberadmin.models.TagCountsModel;
import rubberadmin.models.water_cfg.GatewayModel;

import java.sql.SQLException;
import java.util.List;

/**
 * @author noear
 */
public class DbWaterCfgGatewayApi {
    private static DbContext db() {
        return Config.water;
    }


    public static int saveGateway(int gatewayId, String tag, String name, String agent, String policy, int is_enabled) throws SQLException {
        DbTableQuery tb = db().table("water_cfg_gateway")
                .set("tag", tag.trim())
                .set("name", name.trim())
                .set("agent", agent.trim())
                .set("policy", policy.trim())
                .set("is_enabled", is_enabled)
                .set("gmt_modified", System.currentTimeMillis());

        if (gatewayId > 0) {
            tb.whereEq("gateway_id", gatewayId).update();
            GatewayUtils.notice(tag, name);
            return gatewayId;
        } else {
            return (int) tb.insert();
        }
    }

    public static List<TagCountsModel> getGatewayTagList() throws SQLException {
        return db().table("water_cfg_gateway")
                .groupBy("tag")
                .orderByAsc("tag")
                .selectList("tag, count(*) counts", TagCountsModel.class);
    }

    public static List<TagCountsModel> getGatewayTagListByEnabled() throws SQLException {
        return db().table("water_cfg_gateway")
                .whereEq("is_enabled", 1)
                .groupBy("tag")
                .orderByAsc("tag")
                .selectList("tag, count(*) counts", TagCountsModel.class);
    }


    public static List<GatewayModel> getGatewayList(String tag, int is_enabled) throws SQLException {
        return db().table("water_cfg_gateway")
                .whereEq("is_enabled", is_enabled)
                .andIf(Utils.isNotEmpty(tag), "tag=?", tag)
                .orderByAsc("name")
                .selectList("*", GatewayModel.class);
    }

    public static GatewayModel getGateway(int gatewayId) throws SQLException {
        return db().table("water_cfg_gateway")
                .whereEq("gateway_id", gatewayId)
                .selectItem("*", GatewayModel.class);
    }

    //设置启用状态
    public static void setGatewayEnabled(int gatewayId, int is_enabled) throws SQLException {
        db().table("water_cfg_gateway")
                .whereEq("gateway_id", gatewayId)
                .set("is_enabled", is_enabled)
                .set("gmt_modified", System.currentTimeMillis())
                .update();
    }

    public static void delGateway(int gatewayId) throws SQLException {
        db().table("water_cfg_gateway")
                .whereEq("gateway_id", gatewayId)
                .delete();
    }
}

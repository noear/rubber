package rubberadmin.dso.db;

import org.noear.water.WW;
import org.noear.weed.DbContext;
import rubberadmin.Config;
import rubberadmin.dso.CacheUtil;

import java.sql.SQLException;

/**
 * @author noear 2021/11/24 created
 */
public class DbWaterCfgSafeApi {
    private static DbContext db() {
        return Config.water;
    }


    public static String getServerTokenOne() throws SQLException {
        return db().table("water_cfg_whitelist")
                .whereEq("tag", WW.whitelist_tag_server)
                .andEq("type", WW.whitelist_type_token)
                .andEq("is_enabled", 1)
                .caching(CacheUtil.data)
                .cacheTag("whitelist:server")
                .usingCache(60)
                .selectValue("value", "");

    }

}

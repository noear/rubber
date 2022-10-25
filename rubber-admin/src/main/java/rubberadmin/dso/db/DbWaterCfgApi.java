package rubberadmin.dso.db;

import org.noear.wood.DbContext;
import rubberadmin.Config;
import rubberadmin.dso.CacheUtil;
import rubberadmin.dso.ConfigType;
import rubberadmin.models.water_cfg.ConfigModel;

import java.sql.SQLException;
import java.util.List;

public class DbWaterCfgApi {

    private static DbContext db() {
        return Config.water;
    }



    public static ConfigModel getConfigByTagName(String tag, String name) throws SQLException {
        return getConfigByTagName(tag,name,false);
    }

    public static ConfigModel getConfigByTagName(String tag, String name, boolean cache) throws SQLException {
        return db().table("water_cfg_properties")
                .whereEq("tag", tag)
                .andEq("key", name)
                .limit(1)
                .select("*")
                .caching(CacheUtil.data).usingCache(cache)
                .getItem(ConfigModel.class);
    }

    //====================================================

    //获取type=10的配置（结构化数据库）
    public static List<ConfigModel> getDbConfigs() throws SQLException {
        return db().table("water_cfg_properties")
                .whereEq("type", ConfigType.db)
                .orderBy("`tag`,`key`")
                .select("*")
                .getList(ConfigModel.class);
    }

    //获取type=10,11,12的配置（结构化数据库 + 非结构化数据库）
    public static List<ConfigModel> getDbConfigsEx() throws SQLException {
        return db().table("water_cfg_properties")
                .where("type >=10 AND type<20")
                .select("*")
                .getList(ConfigModel.class);
    }
}

package org.noear.rubber;

import org.noear.water.WW;
import org.noear.water.WaterClient;
import org.noear.water.model.ConfigM;
import org.noear.water.utils.DsCacheUtils;
import org.noear.wood.DbContext;
import org.noear.wood.cache.ICacheServiceEx;
import org.noear.wood.cache.LocalCache;
import rubberapi.Config;
import rubberapi.utils.SnowflakeUtils;

final class RcConfig {
    public static ICacheServiceEx inner_cache = new LocalCache("rubber", 60 * 60 * 24);

    protected static ICacheServiceEx _data_cache;

    public static ICacheServiceEx data_cache() {
        if (_data_cache == null)
            return RcConfig.inner_cache;
        else
            return _data_cache;
    }


    public static final DbContext water_paas;
    public static final DbContext water_paas_request;

    static {
        water_paas = Config.water_paas;
        water_paas_request = DsCacheUtils.getDb(cfg(WW.water_paas_request).value, true, water_paas);
    }



    public static boolean is_debug = false;


    public static long newLogID() {
        return SnowflakeUtils.genId(); //IDUtils.newID("rubber", "log_id", 60 * 60 * 24 * 365) + 10000;
    }

    public static ConfigM cfg(String key) {
        if (key.indexOf("/") < 0) {
            return WaterClient.Config.get(WW.water, key);
        } else {
            return WaterClient.Config.getByTagKey(key);
        }
    }
}

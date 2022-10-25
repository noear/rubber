package rubberadmin.dso.db;

import lombok.extern.slf4j.Slf4j;
import org.noear.snack.ONode;
import org.noear.solon.core.handle.Context;
import org.noear.water.utils.Datetime;
import org.noear.water.utils.EncryptUtils;
import org.noear.water.utils.TextUtils;
import org.noear.wood.DbContext;
import rubberadmin.Config;
import rubberadmin.dso.CacheUtil;
import rubberadmin.dso.Session;
import rubberadmin.models.water.VersionModel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class DbWaterVerApi {
    private static DbContext db() {
        return Config.water;
    }

    /**
     * 备份表的数据版本
     */
    public static void logVersion(DbContext db, String table, String keyName, Object keyValue0) {
        logVersion(db, table, keyName, keyValue0, null);
    }

    public static void logVersion(DbContext db, String table, String keyName, Object keyValue0, Consumer<Map<String, Object>> customize) {

        if (TextUtils.isEmpty(keyName) || keyValue0 == null) {
            return;
        }

        String keyValue = keyValue0.toString();


        try {
            Map<String, Object> data = db.table(table).whereEq(keyName, keyValue0).limit(1).selectMap("*");

            if (data == null || data.size() == 0) {
                return;
            }

            if (customize != null) {
                customize.accept(data);
            }

            Datetime now_time = Datetime.Now();
            String data_json = ONode.stringify(data);
            String data_md5 = EncryptUtils.md5(data_json);

            String old_data_md5 = getLastVersionMd5(table, keyName, keyValue);

            //如果md5一样，说明没什么变化
            if (data_md5.equals(old_data_md5)) {
                return;
            }

            String log_ip = Context.current().realIp();

            db().table("water_tool_versions")
                    .set("table", table)
                    .set("key_name", keyName)
                    .set("key_value", keyValue)
                    .set("data", data_json)
                    .set("data_md5", data_md5)
                    .set("log_user", Session.current().getDisplayName())
                    .set("log_ip", log_ip)
                    .set("log_date", now_time.getDate())
                    .set("log_fulltime", now_time.getTicks())
                    .insert();

        } catch (Exception ex) {
            log.error("{}", ex);
        }
    }

    /**
     * 获取历史版本最近10个
     */
    public static List<VersionModel> getVersions(String table, String keyName, String keyValue) throws SQLException {

        if (TextUtils.isEmpty(keyValue)) {
            return new ArrayList<>();
        }


        return db().table("water_tool_versions")
                .where("`table` = ?", table)
                .and("`key_name`=?", keyName)
                .and("`key_value`=?", keyValue)
                .orderBy("commit_id DESC")
                .limit(10)
                .selectList("*", VersionModel.class);
    }

    public static VersionModel getVersionByCommit(int commit_id) throws SQLException {

        return db().table("water_tool_versions")
                .where("`commit_id` = ?", commit_id)
                .limit(1)
                .caching(CacheUtil.data)
                .selectItem("*", VersionModel.class);
    }

    /**
     * 最后一个历史版本的MD5
     */
    public static String getLastVersionMd5(String table, String keyName, String keyValue) throws SQLException {

        return db().table("water_tool_versions")
                .where("`table` = ?", table)
                .and("`key_name`=?", keyName)
                .and("`key_value`=?", keyValue)
                .orderBy("commit_id DESC")
                .limit(1)
                .selectValue("data_md5", "");
    }
}

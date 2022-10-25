package rubberadmin.dso.db;

import org.noear.water.dso.NoticeUtils;
import org.noear.water.utils.TextUtils;
import org.noear.wood.DataItem;
import org.noear.wood.DbContext;
import org.noear.wood.DbTableQuery;
import rubberadmin.Config;
import rubberadmin.models.water_paas.LuffyFileModel;
import rubberadmin.models.water_paas.LuffyFileType;
import rubberadmin.models.TagCountsModel;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DbLuffyApi {
    private static DbContext db() {
        return Config.water_paas;
    }

    //获取logger表tag
    public static List<TagCountsModel> getFileTags(LuffyFileType type) throws SQLException {
        return db().table("luffy_file").where("file_type=?", type.code)
                .groupBy("tag")
                .orderByAsc("tag")
                .select("tag,count(*) counts")
                .getList(TagCountsModel.class);
    }

    private static String list_sels = "file_id,tag,label,path,`rank`,note,is_staticize,is_editable,is_disabled,is_exclude,link_to,edit_mode,content_type,update_fulltime,plan_state,plan_begin_time,plan_last_time,plan_last_timespan,plan_interval,plan_max,plan_count,use_whitelist";

    public static List<LuffyFileModel> getFileList(String tag, LuffyFileType type) throws SQLException {
        return getFileList(tag, type, false, null, 0);
    }

    public static List<LuffyFileModel> getFileList(String tag, LuffyFileType type, boolean disabled, String key, int keyType) throws SQLException {
        DbTableQuery qr = db().table("luffy_file").where("1=1");

        if (type.code < LuffyFileType.all.code) {
            qr.andEq("is_disabled", (disabled ? 1 : 0));
            qr.andEq("file_type", type.code);
        }

        if (TextUtils.isNotEmpty(key)) {
            if (key.startsWith("@")) {
                key = key.substring(1);
            }

            if (keyType == 12) {
                qr.andLk("path", "%" + key + "%");
            } else if (keyType == 21) {
                if (tag != null) {
                    qr.andEq("tag", tag);
                }
                qr.andLk("content", "%" + key + "%");
            } else if (keyType == 22) {
                qr.andLk("content", "%" + key + "%");
            } else {
                if (tag != null) {
                    qr.andEq("tag", tag);
                }
                qr.andLk("path", "%" + key + "%");
            }
        } else {
            if (tag != null) {
                qr.andEq("tag", tag);
            }
        }

        if (type == LuffyFileType.tml) {
            qr.orderByAsc("rank");
        }

        return qr.orderByAsc("path")
                .select(list_sels)
                .getList(LuffyFileModel.class);
    }

    public static LuffyFileModel getFile(int file_id) throws SQLException {
        return db().table("luffy_file")
                .where("file_id=?", file_id)
                .select("*")
                .getItem(LuffyFileModel.class);
    }

    public static int delFile(int file_id) throws SQLException {
        return db().table("luffy_file")
                .where("file_id=?", file_id)
                .delete();
    }

    public static int resetFilePlan(String ids) throws SQLException {
        List<Object> list = Arrays.asList(ids.split(","))
                .stream()
                .map(s -> Integer.parseInt(s))
                .collect(Collectors.toList());

        return db().table("luffy_file").usingExpr(true)
                .set("plan_last_time","$NULL")
                .set("plan_last_timespan",0)
                .set("plan_state",1)
                .whereIn("file_id", list)
                .update();
    }

    public static void setFile(long file_id, DataItem data, LuffyFileType type) throws SQLException {

        data.set("file_type", type.code);

        if (file_id > 0) {
            db().table("luffy_file").whereEq("file_id", file_id).update(data);

            //更知:缓存更新
            NoticeUtils.updateCache("paas:" + file_id);
        } else {
            file_id = db().table("luffy_file").insert(data);
        }
    }

    public static void setFileContent(int file_id, String content) throws SQLException {
        if (file_id > 0) {
            db().table("luffy_file")
                    .set("content", content)
                    .set("update_fulltime", System.currentTimeMillis())
                    .whereEq("file_id", file_id)
                    .update();

            //更知:缓存更新
            NoticeUtils.updateCache("paas:" + file_id);

            /** 记录历史版本 */
            DbWaterVerApi.logVersion(db(),"luffy_file", "file_id", file_id);
        }
    }

    public static List<LuffyFileModel> getFilesByIds(LuffyFileType type, String ids) throws SQLException {
        List<Object> list = Arrays.asList(ids.split(","))
                .stream()
                .map(s -> Integer.parseInt(s))
                .collect(Collectors.toList());

        return db().table("luffy_file")
                .whereIn("file_id", list)
                .andEq("file_type", type.code)
                .select("*")
                .getList(LuffyFileModel.class);
    }

    //批量导入
    public static void impFile(LuffyFileType type, String tag, LuffyFileModel wm) throws SQLException {
        if (TextUtils.isEmpty(tag) == false) {
            wm.tag = tag;
        }

        if (TextUtils.isEmpty(wm.tag) || TextUtils.isEmpty(wm.path) || TextUtils.isEmpty(wm.content)) {
            return;
        }

        if (wm.file_type != type.code) {
            return;
        }

        DataItem item = new DataItem();
        item.setEntity(wm);
        item.remove("file_id");

        //只支持新增导入
        DbTableQuery qr = db().table("luffy_file")
                .set("file_type", wm.file_type)
                .set("tag", wm.tag)
                .set("label", wm.label)
                .set("note", wm.note)
                .set("path", wm.path)
                .set("rank", wm.rank)
                .set("is_staticize", wm.is_staticize)
                .set("is_editable", wm.is_editable)
                .set("is_disabled", wm.is_disabled)
                .set("link_to", wm.link_to)
                .set("edit_mode", wm.edit_mode)
                .set("content_type", wm.content_type)
                .set("content", wm.content)
                .set("plan_last_timespan", wm.plan_last_timespan)
                .set("plan_interval", wm.plan_interval)
                .set("plan_max", wm.plan_max)
                .set("update_fulltime", wm.update_fulltime.getTime())
                .set("use_whitelist", wm.use_whitelist);

        if (wm.plan_begin_time != null) {
            qr.set("plan_begin_time", wm.plan_begin_time.getTime());
        }

        if (qr.whereEq("path", wm.path).selectExists()) {
            qr.update();
        } else {
            qr.set("create_fulltime", wm.create_fulltime.getTime());
            qr.insert();
        }
    }

    //批量删除
    public static void delFileByIds(LuffyFileType type, int act, String ids) throws SQLException {
        List<Object> list = Arrays.asList(ids.split(",")).stream().map(s -> Integer.parseInt(s)).collect(Collectors.toList());

        if (act == 9) {
            db().table("luffy_file")
                    .whereIn("file_id", list)
                    .andEq("file_type", type.code)
                    .delete();
        } else {
            db().table("luffy_file")
                    .set("is_disabled", (act == 1 ? 1 : 0))
                    .whereIn("file_id", list)
                    .andEq("file_type", type.code)
                    .update();
        }

        //更知:缓存更新
        for (Object file_id : list) {
            NoticeUtils.updateCache("paas:" + file_id);
        }
    }
}
package rubberadmin.models.water;


import lombok.Getter;
import lombok.Setter;
import org.noear.weed.GetHandlerEx;
import org.noear.weed.IBinder;

import java.util.Date;

/// <summary>
/// 生成:2018/12/10 10:13:47
///
/// </summary>
@Setter
@Getter
public class VersionModel implements IBinder {
    public int commit_id;
    public String table;
    public String key_name;
    public String key_value;
    public boolean is_hand;
    public String data;
    public String log_user;
    public String log_ip;
    public int log_date;
    public Date log_fulltime;

    public void bind(GetHandlerEx s) {
        //1.source:数据源
        //
        commit_id = s.get("commit_id").value(0);
        table = s.get("table").value(null);
        key_name = s.get("key_name").value(null);
        key_value = s.get("key_value").value(null);
        is_hand = s.get("is_hand").value(false);
        data = s.get("data").value(null);
        log_user = s.get("log_user").value(null);
        log_ip = s.get("log_ip").value(null);
        log_date = s.get("log_date").value(0);
        log_fulltime = s.get("log_fulltime").dateValue(null);

        if (log_fulltime == null) {
            log_fulltime = new Date();
        }
    }

    public IBinder clone() {
        return new VersionModel();
    }
}
package rubberadmin.models.water_reg;

import lombok.Getter;
import org.noear.weed.*;
import java.util.*;

/// <summary>
/// 生成:2018/04/26 09:46:37
/// 
/// </summary>
@Getter
public class ServiceSpeedModel implements IBinder {
    public String service;
    public String tag;
    public String name;
    public String name_md5;
    public long average;
    public long fastest;
    public long slowest;
    public long total_num;
    public long total_num_slow1;
    public long total_num_slow2;
    public long total_num_slow5;
    public Date last_updatetime;
    public long counts;

    public Date gmt_modified;

    public void bind(GetHandlerEx s) {
        //1.source:数据源
        //
        service = s.get("service").value(null);
        tag = s.get("tag").value(null);
        name = s.get("name").value(null);
        name_md5 = s.get("name_md5").value(null);
        average = s.get("average").value(0L);
        fastest = s.get("fastest").value(0L);
        slowest = s.get("slowest").value(0L);
        total_num = s.get("total_num").value(0L);
        total_num_slow1 = s.get("total_num_slow1").value(0L);
        total_num_slow2 = s.get("total_num_slow2").value(0L);
        total_num_slow5 = s.get("total_num_slow5").value(0L);
        last_updatetime = s.get("last_updatetime").value(null);
        counts = s.get("counts").value(0L);

        gmt_modified = s.get("gmt_modified").dateValue(null);
        if (gmt_modified == null) {
            gmt_modified = new Date();
        }
    }

    public IBinder clone() {
        return new ServiceSpeedModel();
    }

    public boolean isHighlight() {
        return average > 1000;
    }
}
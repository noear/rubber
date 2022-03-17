package rubberadmin.models.water_msg;

import lombok.Getter;
import org.noear.water.utils.EncryptUtils;
import org.noear.weed.*;

/// <summary>
/// 生成:2017/12/21 04:30:35
/// 
/// </summary>
@Getter
public class TopicModel implements IBinder
{
    public int topic_id;
    public String topic_name;
    public String tag;
    public int max_msg_num;
    public int max_distribution_num;
    public int max_concurrency_num;
    public int stat_msg_day_num;
    public int alarm_model;

	public void bind(GetHandlerEx s)
	{
		//1.source:数据源
		//
        topic_id = s.get("topic_id").value(0);
        topic_name = s.get("topic_name").value(null);
        tag = s.get("tag").value(null);
        max_msg_num = s.get("max_msg_num").value(0);
        max_distribution_num = s.get("max_distribution_num").value(0);
        max_concurrency_num = s.get("max_concurrency_num").value(0);
        stat_msg_day_num = s.get("stat_msg_day_num").value(0);
        alarm_model = s.get("alarm_model").value(0);
	}
	
	public IBinder clone()
	{
		return new TopicModel();
	}

	public String topic_md5(){
        return "%7Bmd5%7D" + EncryptUtils.md5(topic_name);
    }
}
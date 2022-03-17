package rubberadmin.models.water_mot;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ServiceRuntimeModel {

    /**  */
    public int row_id;
    /**  */
    public String key;
    /**  */
    public String name;
    /**  */
    public String address;
    /**  */
    public int memory_max;
    /**  */
    public int memory_total;
    /**  */
    public int memory_used;
    /**  */
    public int thread_peak_count;
    /**  */
    public int thread_count;
    /**  */
    public int thread_daemon_count;
    /**  */
    public int log_date;
    /**  */
    public int log_hour;
    /**  */
    public int log_minute;


    public long val;

}
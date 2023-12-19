package rubberadmin;

import org.noear.solon.Solon;
import org.noear.solon.cloud.utils.http.PreheatUtils;
import org.noear.water.WW;
import rubberadmin.dso.InitPlugin;

public class RubberadminApp {
    public static void main(String[] args) throws Exception {
        Solon.start(RubberadminApp.class, args, x -> {
            Config.tryInit(x);

            x.pluginAdd(0, new InitPlugin());
        });

        PreheatUtils.preheat(WW.path_run_check);
        PreheatUtils.preheat("/login");
    }
}
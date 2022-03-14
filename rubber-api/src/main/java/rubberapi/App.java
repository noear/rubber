package rubberapi;

import org.noear.rubber.Rubber;
import org.noear.solon.Solon;
import org.noear.luffy.dso.*;
import luffy.JtRun;
import org.noear.solon.cloud.utils.http.PreheatUtils;
import org.noear.water.WW;
import rubberapi.controller.DebugController;
import rubberapi.controller.PreviewController;
import rubberapi.controller.ReleaseController;
import rubberapi.controller.debug.BlockController;
import rubberapi.controller.debug.QueryController;
import rubberapi.controller.release.ModelController;
import rubberapi.controller.release.SchemeController;
import rubberapi.dso.CacheUtil;


public class App {

    public static void main(String[] args) throws Exception{

        //开始
        JtRun.init();

        Solon.start(App.class, args, (x) -> {
            Config.tryInit();

            x.enableErrorAutoprint(false);

            x.sharedAdd("cache", Config.cache_data);
            x.sharedAdd("XFun", JtFun.g);
            x.sharedAdd("XMsg", JtMsg.g);
            x.sharedAdd("XUtil", JtUtil.g);
            x.sharedAdd("XLock", JtLock.g);


            x.all("/debug", new DebugController());
            x.all("/release", new ReleaseController());
            x.get("/preview(.js)?", new PreviewController());

            x.all("/s/*/*", new SchemeController());
            x.all("/m/*/*", new ModelController());
            x.all("/q/*/*", new QueryController());
            x.all("/d/*/*", new BlockController());
        });

        JtRun.xfunInit();

        try {
            Rubber.tryInit(CacheUtil.data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        PreheatUtils.preheat(WW.path_run_check);
    }
}

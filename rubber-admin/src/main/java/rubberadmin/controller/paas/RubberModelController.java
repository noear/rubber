package rubberadmin.controller.paas;

import com.alibaba.fastjson.JSONObject;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.auth.annotation.AuthPermissions;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.core.handle.UploadedFile;
import org.noear.water.utils.*;
import rubberadmin.controller.BaseController;
import rubberadmin.dso.SessionPerms;
import rubberadmin.dso.TagChecker;
import rubberadmin.dso.db.DbRubberApi;
import rubberadmin.models.TagCountsModel;
import rubberadmin.models.water_paas.ModelSerializeModel;
import rubberadmin.models.water_paas.RebberModelFieldModel;
import rubberadmin.models.water_paas.RebberModelModel;
import rubberadmin.viewModels.ViewModel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Controller
@Mapping("/rubber/")
public class RubberModelController extends BaseController {

    //计算模型
    @Mapping("model")
    public ModelAndView model(Integer model_id, Integer field_id, String tag_name, String name, String f) throws SQLException {
        List<TagCountsModel> tags = DbRubberApi.getModelTags();

        TagChecker.filter(tags, m -> m.tag);

        viewModel.put("tags", tags);
        if (TextUtils.isEmpty(tag_name) == false) {
            viewModel.put("tag_name", tag_name);
        } else {
            if (tags.isEmpty() == false) {
                viewModel.put("tag_name", tags.get(0).tag);
            } else {
                viewModel.put("tag_name", null);
            }
        }
        viewModel.put("name", name);
        viewModel.put("model_id", model_id);
        viewModel.put("field_id", field_id);
        viewModel.put("f", f);

        return view("rubber/model");
    }

    //数据模型右侧列表
    @Mapping("model/inner")
    public ModelAndView inner(Integer model_id, Integer field_id, String tag_name, String name, String f) throws SQLException {

        if (field_id != null && field_id > 0) {
            return fieldEdit(model_id, field_id, f);
        }

        if (model_id != null && model_id > 0) {
            return edit(model_id, f);
        }

        List<RebberModelModel> models = DbRubberApi.getModelList(tag_name, name);
        viewModel.put("models", models);
        viewModel.put("tag_name", tag_name);
        viewModel.put("name", name);
        viewModel.put("f", f);


        return view("rubber/model_inner");
    }


    //修改数据模型
    @Mapping("model/edit")
    public ModelAndView edit(Integer model_id, String f) throws SQLException {
        if (model_id == null) {
            model_id = 0;
        }


        List<String> option_sources = new ArrayList<>();
        //todo: del
//        List<ConfigModel> configs = DbWaterCfgApi.getDbConfigs();
//        for (ConfigModel config : configs) {
//            option_sources.add(config.tag + "/" + config.key);
//        }
        viewModel.put("option_sources", option_sources);


        RebberModelModel model = DbRubberApi.getModelById(model_id);
        viewModel.put("model", model);

        if ("sponge".equals(f)) {
            //viewModel.put("backUrl",Config.sponge_url);
        }
        viewModel.put("f", f);

        return view("rubber/model_edit");
    }

    //数据模型保存编辑
    @AuthPermissions({SessionPerms.operator, SessionPerms.admin})
    @Mapping("model/edit/ajax/save")
    public JSONObject editSave(Integer model_id, String tag, String name, String name_display, String init_expr, String debug_args, String related_db) throws SQLException {
        JSONObject resp = new JSONObject();
        boolean result = DbRubberApi.setModel(model_id, tag, name, name_display, init_expr, debug_args, related_db) > 0;

        if (result) {
            resp.put("code", 1);
            resp.put("msg", "编辑成功");
        } else {
            resp.put("code", 0);
            resp.put("msg", "编辑失败");
        }

        return resp;
    }

    //数据模型删除
    @AuthPermissions({SessionPerms.operator, SessionPerms.admin})
    @Mapping("model/edit/ajax/del")
    public ViewModel modelDel(Integer model_id) throws SQLException {
        boolean result = DbRubberApi.delModel(model_id);
        if (result) {
            viewModel.code(1, "删除成功！");
        } else {
            viewModel.code(0, "删除失败!");
        }

        return viewModel;
    }


    //数据模型字段列表
    @Mapping("model/field")
    public ModelAndView field(Integer model_id, String name, String f) throws SQLException {
        RebberModelModel model = DbRubberApi.getModelById(model_id);
        List<RebberModelFieldModel> fields = DbRubberApi.getFieldList(model_id, name);
        viewModel.put("model", model);
        viewModel.put("fields", fields);
        viewModel.put("name", name);

        viewModel.put("f", f);

        return view("rubber/model_field");
    }


    //数据模型字段编辑页面
    @Mapping("model/field/edit")
    public ModelAndView fieldEdit(Integer model_id, Integer field_id, String f) throws SQLException {
        if (field_id == null) {
            field_id = 0;
        }

        RebberModelFieldModel field = DbRubberApi.getFieldById(field_id);
        RebberModelModel model = DbRubberApi.getModelById(model_id);

        viewModel.put("field", field);
        viewModel.put("model_id", model_id);
        viewModel.put("model_name", model.name_display);

        viewModel.put("model", model);

        viewModel.put("f", f);
        if ("sponge".equals(f)) {
            //viewModel.put("backUrl",Config.sponge_url);
        }

        return view("rubber/model_field_edit");
    }


    //数据模型字段保存编辑
    @AuthPermissions({SessionPerms.operator, SessionPerms.admin})
    @Mapping("model/field/edit/ajax/save")
    public JSONObject fieldEditSave(Integer model_id, Integer field_id, String name, String name_display,
                                    String expr, String note, Integer is_pk) throws SQLException {
        JSONObject resp = new JSONObject();
        boolean result = DbRubberApi.setModelField(model_id, field_id, name, name_display, expr, note, is_pk);

        if (result) {
            resp.put("code", 1);
            resp.put("msg", "编辑成功");
        } else {
            resp.put("code", 0);
            resp.put("msg", "编辑失败");
        }

        return resp;
    }

    //删除模型字段
    @AuthPermissions({SessionPerms.operator, SessionPerms.admin})
    @Mapping("model/field/del/ajax/save")
    public JSONObject fieldDelSave(Integer field_id, Integer model_id) throws SQLException {
        JSONObject resp = new JSONObject();
        boolean result = DbRubberApi.delModelField(field_id, model_id);

        if (result) {
            resp.put("code", 1);
            resp.put("msg", "删除成功");
        } else {
            resp.put("code", 0);
            resp.put("msg", "删除失败");
        }

        return resp;
    }

    //数据模型字段另存为
    @AuthPermissions({SessionPerms.operator, SessionPerms.admin})
    @Mapping("model/edit/ajax/saveAs")
    public ViewModel modelEditSaveAs(String tag, Integer model_id, String name, String name_display, String debug_args, String init_expr, String related_db) throws SQLException {
        boolean result = DbRubberApi.saveAsModel(tag, model_id, name, name_display, debug_args, init_expr, related_db);

        if (result) {
            viewModel.code(1, "操作成功！");
        } else {
            viewModel.code(0, "操作失败!");
        }

        return viewModel;
    }


    //批量导出
    @AuthPermissions({SessionPerms.operator, SessionPerms.admin})
    @Mapping("model/ajax/export")
    public void exportDo(Context ctx, String tag, String ids) throws Exception {
        List<RebberModelModel> tmpList = DbRubberApi.getModelByIds(ids);

        List<ModelSerializeModel> list = new ArrayList<>(tmpList.size());
        for (RebberModelModel m1 : tmpList) {
            ModelSerializeModel vm = new ModelSerializeModel();
            vm.model = m1;
            vm.fields = DbRubberApi.getModelFieldListByModelId(m1.model_id);

            list.add(vm);
        }

        String jsonD = JsondUtils.encode("rubber_model", list);

        String filename2 = "water_raasfile_model_" + tag + "_" + Datetime.Now().getDate() + ".jsond";

        ctx.headerSet("Content-Disposition", "attachment; filename=\"" + filename2 + "\"");

        ctx.output(jsonD);
    }


    //批量导入
    @AuthPermissions(SessionPerms.admin)
    @Mapping("model/ajax/import")
    public ViewModel importDo(Context ctx, String tag, UploadedFile file) throws Exception {
        String jsonD = IOUtils.toString(file.content);
        JsondEntity entity = JsondUtils.decode(jsonD);

        if (entity == null || "rubber_model".equals(entity.table) == false) {
            return viewModel.code(0, "数据不对！");
        }

        List<ModelSerializeModel> list = entity.data.toObjectList(ModelSerializeModel.class);


        for (ModelSerializeModel vm : list) {
            DbRubberApi.modelImp(tag, vm);
        }

        return viewModel.code(1, "ok");
    }
}

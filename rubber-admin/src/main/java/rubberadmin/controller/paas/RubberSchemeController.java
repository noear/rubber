package rubberadmin.controller.paas;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.noear.solon.Utils;
import org.noear.solon.auth.annotation.AuthPermissions;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.UploadedFile;
import org.noear.water.utils.*;

import org.noear.solon.annotation.Mapping;

import org.noear.solon.annotation.Controller;
import org.noear.solon.core.handle.ModelAndView;
import rubberadmin.controller.BaseController;
import rubberadmin.dso.TagChecker;
import rubberadmin.dso.SessionPerms;
import rubberadmin.dso.db.DbRubberApi;
import rubberadmin.models.TagCountsModel;
import rubberadmin.models.water_paas.*;
import rubberadmin.viewModels.ViewModel;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@Controller
@Mapping("/rubber/")
public class RubberSchemeController extends BaseController {

    //获取计算方案分组列表
    @Mapping("scheme")
    public ModelAndView scheme(Integer scheme_id,String tag_name, String name) throws SQLException {
        List<TagCountsModel> tags = DbRubberApi.getSchemeTags();

        TagChecker.filter(tags, m -> m.tag);

        viewModel.put("tags",tags);
        if (TextUtils.isEmpty(tag_name) == false) {
            viewModel.put("tag_name", tag_name);
        } else {
            if (tags.isEmpty() == false) {
                viewModel.put("tag_name", tags.get(0).tag);
            } else {
                viewModel.put("tag_name", null);
            }
        }
        viewModel.put("name",name);
        viewModel.put("scheme_id",scheme_id);
        return view("rubber/scheme");
    }

    //获取计算方案列表
    @Mapping("scheme/inner")
    public ModelAndView inner(Integer scheme_id, String tag_name, String name,String f) throws SQLException {
        if(scheme_id!=null && scheme_id>0){
            return eventEdit(scheme_id,f);
        }


        List<RebberSchemeModel> schemes = DbRubberApi.getSchemeList(tag_name, name);
        viewModel.put("schemes",schemes);
        viewModel.put("tag_name", tag_name);
        viewModel.put("name",name);
        viewModel.put("f",f);

        return view("rubber/scheme_inner");
    }

    //跳转计算方案添加、修改
    @Mapping("scheme/edit")
    public ModelAndView toAddScheme(Integer scheme_id,String f) throws SQLException {
        List<RebberModelModel> modelList = DbRubberApi.getModelList();
        viewModel.put("models",modelList);
        RebberSchemeModel schemeModel = null;
        if(scheme_id != null){
             schemeModel = DbRubberApi.getSchemeById(scheme_id);
        }else{
            schemeModel = new RebberSchemeModel();
        }

        viewModel.put("scheme", schemeModel);

        viewModel.put("f",f);
        if ("sponge".equals(f)) {
            //viewModel.put("backUrl",Config.sponge_url);
        }

        return view("rubber/scheme_edit");
    }

    //ajax保存计算方案
    @Mapping("scheme/edit/ajax/save")
    public ViewModel saveScheme(Integer id, String tag, String name, String name_display, String related_model, String related_model_display, String related_block,String debug_args) {
        try {
            long schemeId = DbRubberApi.setScheme(id, tag, name, name_display, related_model, related_model_display, related_block,debug_args);
            if (schemeId>0) {
                viewModel.code(1, "保存成功！");
            } else {
                viewModel.code(0, "保存失败!");
            }
        }catch (SQLException e){
            viewModel.code(0, "保存错误!");
        }
        return viewModel;
    }

    //跳转计算方案事件编辑页面
    @Mapping("scheme/event/edit")
    public ModelAndView eventEdit(Integer scheme_id,String f) throws SQLException{
        RebberSchemeModel scheme = DbRubberApi.getSchemeById(scheme_id);
        viewModel.put("scheme",scheme);

        viewModel.put("f",f);
        if ("sponge".equals(f)) {
            //viewModel.put("backUrl",Config.sponge_url);
        }

        return view("rubber/scheme_event_edit");
    }

    //计算方案事件编辑保存
    @Mapping("scheme/event/edit/ajax/save")
    public  ViewModel saveEdit(Integer scheme_id,String event) throws SQLException{
        boolean result = DbRubberApi.updateSchemeEvent(scheme_id, event);
        if (result) {
            viewModel.code(1,"保存成功");
        } else {
            viewModel.code(0, "保存失败!");
        }
        return viewModel;
    }

    //ajax另存为计算方案
    @Mapping("scheme/edit/ajax/saveAs")
    public ViewModel saveScheme(String tag,Integer scheme_id, String name,String name_display,String debug_args) throws SQLException{
        boolean result = DbRubberApi.saveAsScheme(tag,scheme_id,name,name_display,debug_args);
        if (result) {
            viewModel.code(1, "保存成功！");
        } else {
            viewModel.code(0, "保存失败!");
        }
        return viewModel;
    }

    //ajax删除计算方案
    @Mapping("scheme/edit/ajax/del")
    public ViewModel delScheme(Integer scheme_id) throws SQLException{

        boolean result = DbRubberApi.delScheme(scheme_id);
        if (result) {
            viewModel.code(1, "删除成功！");
        } else {
            viewModel.code(0, "删除失败!");
        }

        return viewModel;
    }


    //跳转计算方案规则列表
    @Mapping("scheme/rule/design")
    public ModelAndView rule_inner(Integer scheme_id,String tag_name,String name_display,String name,String f) throws SQLException {
        List<RebberSchemeRuleModel> rules = DbRubberApi.getSchemeRuleListBySchemeId(scheme_id,name);
        RebberSchemeModel scheme = DbRubberApi.getSchemeById(scheme_id);

        //控制，是否显示查询
        RebberModelModel model = null;
        if (!Utils.isEmpty(scheme.related_model)){
            model = DbRubberApi.getModelFieldListByModelTagAndName(scheme.related_model);
            viewModel.put("related_db",model.related_db);
        }else{
            viewModel.put("related_db","");
        }

        viewModel.put("scheme",scheme);
        viewModel.put("scheme_id",scheme_id);
        viewModel.put("tag_name",tag_name);
        viewModel.put("name_display",name_display);
        viewModel.put("name",name);
        viewModel.put("rules",rules);

        viewModel.put("f",f);

        return view("rubber/scheme_rule_design");
    }

    //跳转规则设计
    @Mapping("scheme/rule/edit")
    public ModelAndView toRuleAdd(Integer rule_id, Integer scheme_id,String debug_args,String f) throws SQLException {
        if(rule_id==null){
            rule_id = 0;
        }

        RebberSchemeRuleModel rule = DbRubberApi.getSchemeRuleByRuleId(rule_id);

        if(rule.rule_id>0){
            scheme_id = rule.scheme_id;
        }

        String expr = "[{}]";
        String leftList = "[]";
        RebberSchemeModel scheme = null;
        RebberModelModel model = null;
        List<RebberBlockModel> functions = null;

        if (scheme_id != null && scheme_id != 0){
            scheme = DbRubberApi.getSchemeById(scheme_id);

            if (!Utils.isEmpty(scheme.related_model)){
                model = DbRubberApi.getModelFieldListByModelTagAndName(scheme.related_model);
            }

            if (scheme != null){
                if(!Utils.isEmpty(scheme.related_block)){
                    functions = DbRubberApi.getBlocksByTagOrNameArray(scheme.related_block);
                }
            }

            if (model != null){
                if (model.model_id != 0){
                    List<RebberModelFieldModel> fieldList = DbRubberApi.getFieldList(model.model_id, null);
                    List<JSONObject> list = new ArrayList<>();

                    for (RebberModelFieldModel mode:
                            fieldList) {
                        JSONObject item = new JSONObject();
                        item.put("name",mode.name);
                        item.put("name_display",mode.name_display);
                        list.add(item);
                    }
                    leftList = JSON.toJSONString(list);
                }
            }
        }
        RebberSchemeRuleModel schemeRule;
        if (rule_id > 0){
             schemeRule = DbRubberApi.getSchemeRuleByRuleId(rule_id);
            JSONObject json = (JSONObject) JSON.parse(schemeRule.expr);
            List<JSONObject> list = new ArrayList<>();
            for (String key: json.keySet()) {
                JSONObject jsonObject = json.getJSONObject(key);
                list.add(jsonObject);
            }
            expr = JSON.toJSONString(list);

        }else{
            schemeRule = new RebberSchemeRuleModel();
        }

        if(TextUtils.isEmpty(debug_args)){
            debug_args = scheme.debug_args;
        }

        if(functions == null){
            functions = new ArrayList<>();
        }

        viewModel.put("blocks",functions);
        viewModel.put("schemeRule",schemeRule);
        viewModel.put("rule_id",rule_id);
        viewModel.put("scheme_id", scheme_id);
        viewModel.put("scheme",scheme);
        viewModel.put("debug_args",debug_args);
        viewModel.put("tag_name",scheme.tag);
        viewModel.put("name_display",scheme.name_display);
        viewModel.put("leftList", leftList);
        viewModel.put("expr",expr);

        viewModel.put("f",f);
        if ("sponge".equals(f)) {
            //viewModel.put("backUrl",Config.sponge_url);
        }

        return view("rubber/scheme_rule_edit");
    }


    //ajax编辑保存功能
    @Mapping("scheme/rule/edit/ajax/save")
    public ViewModel ruleSave(Integer rule_id ,Integer scheme_id ,String name_display,Integer advice,Integer score,Integer sort, String expr) throws SQLException {
        boolean result = false;
        System.out.println(expr);
        List<JSONObject> list = new ArrayList<>();
        if (!Utils.isEmpty(expr)){
            list = (List<JSONObject>) JSON.parse(expr);
        }
        String exprDisplay = "";
        int index = 1;
        JSONObject exprValue = new JSONObject();
        for (JSONObject json : list){
            String leftValue = json.getString("left");
            String leftDisplay = json.getString("leftValue");
            String center = json.getString("center");
            String right = json.getString("right");                     //key
            String rightVal = json.getString("rightValue");            //显示名
            String ct = json.getString("ct");

            JSONObject exprValueItem = new JSONObject();
            exprValueItem.put("l", leftValue);
            exprValueItem.put("op", center);

            if (!Utils.isEmpty(right)){
                exprValueItem.put("r", right);
            }else{
                exprValueItem.put("r", rightVal);
            }

            exprValueItem.put("ct", ct);
            exprValue.put("_" + index, exprValueItem);

            String rightValue = "";
            if (Utils.isEmpty(right)){
                rightValue = "{_"+ index +":"+rightVal+"}";
            }else{
                rightValue = rightVal;
            }
            String ctDisplay = "";
            if ("&&".equals(ct)){
                ctDisplay = "并且";
            }else if("||".equals(ct)){
                ctDisplay = "或者";
            }
            exprDisplay += MessageFormat.format("{0} {1} {2} {3} ",leftDisplay,center,rightValue,ctDisplay);
            index++;
        }

        result = DbRubberApi.setSchemeRule(rule_id, scheme_id, name_display, advice, score, sort, JSON.toJSONString(exprValue), exprDisplay,1);

        DbRubberApi.updSchemeRuleCount(scheme_id);

        if (result) {
            viewModel.code(1, "保存成功！");
        } else {
            viewModel.code(0, "保存失败!");
        }
        return viewModel;
    }

    //删除计算方案-规则
    @Mapping("scheme/rule/del/ajax/save")
    public ViewModel ruleSave(Integer rule_id,Integer scheme_id) throws SQLException{
        boolean result = DbRubberApi.delSchemeRule(rule_id, scheme_id);
        if (result) {
            viewModel.code(1,"删除成功");
        } else {
            viewModel.code(0,"删除失败");
        }
        return viewModel;
    }

    //ajax编辑保存功能
    @Mapping("scheme/rule/expr/edit/ajax/save")
    public ViewModel schemeInserSave(String exprs,Integer rule_relation,Integer scheme_id) throws SQLException {
        boolean result = false;
        JSONObject exprResult = (JSONObject )JSON.parse(exprs);

        List<Integer> ruleids = new ArrayList<>();
        for (String rule_key: exprResult.keySet()) {
            ruleids.add(Integer.parseInt(rule_key));
        }
        List<RebberSchemeRuleModel> schemeRuleModelList = DbRubberApi.getSchemeRuleByRuleIds(ruleids);

        for (String rule_key:
                exprResult.keySet()) {
            JSONObject jsonObject = exprResult.getJSONObject(rule_key);
            RebberSchemeRuleModel rule = null;
            for (RebberSchemeRuleModel ruleModel:
                    schemeRuleModelList) {
                if (String.valueOf(ruleModel.rule_id).equals(rule_key)){
                    rule = ruleModel;
                    break;
                }
            }
            if (rule != null){
                String  expr_display = rule.expr_display;
                JSONObject exprSource = (JSONObject)JSON.parse(rule.expr);
                for (String key :
                        jsonObject.keySet()) {
                    if (!"is_enabled".equals(key)){
                        Object value = jsonObject.get(key);
                        JSONObject target = exprSource.getJSONObject(key);

                        expr_display = expr_display.replace("{"+key+":"+target.getString("r")+"}","{"+key+":"+value+"}");

                        target.put("r",value);
                        exprSource.put(key, target);
                    }
                }
                Integer is_enabled = jsonObject.getInteger("is_enabled");
                result = DbRubberApi.setSchemeRule(Integer.parseInt(rule_key), rule.scheme_id, rule.name_display,rule.advice, rule.score, rule.sort, JSON.toJSONString(exprSource), expr_display,is_enabled);

                DbRubberApi.updSchemeRuleCount(rule.scheme_id);
            }
        }

        if (result) {
            DbRubberApi.updataSchemeRuleRelation(scheme_id,rule_relation);
            viewModel.code(1, "保存成功！");
        } else {
            viewModel.code(0, "保存失败!");
        }
        return viewModel;
    }

    //计算方案-流程设计
    @Mapping("scheme/flow")
    public ModelAndView process(Integer scheme_id) throws SQLException{
        RebberSchemeModel scheme = DbRubberApi.getSchemeById(scheme_id);
        RebberSchemeNodeDesignModel design = DbRubberApi.getSchemeNodeDesign(scheme_id);
        viewModel.put("scheme",scheme);
        viewModel.put("design",design);

        return view("rubber/scheme_node");
    }

    //保存计算方案流程设计
    @Mapping("scheme/flow/ajax/savedesign")
    public JSONObject saveDesign(Integer scheme_id,String details) throws SQLException{
        return DbRubberApi.setSchemeNodeAll(scheme_id,details);
    }

    //计算方案-流程设计 执行节点弹出窗
    @Mapping("scheme/flow/excute")
    public ModelAndView excuteLayer(String node_id,Integer scheme_id) throws SQLException{

        RebberSchemeNodeModel node = DbRubberApi.getSchemeNodeByNodeKey(node_id,scheme_id);
        List<RebberSchemeNodeRespModel> schemeNode = DbRubberApi.getSchemeNodeTask(node);
        List<RebberActorModel> actors = DbRubberApi.getActorList(node);

        viewModel.put("schemeNode",schemeNode);
        viewModel.put("actors",actors);
        viewModel.put("node",node);

        return view("rubber/scheme_node_excute");
    }

    //获取执行任务默认选项
    @Mapping("scheme/flow/ajax/getTask")
    public JSONArray getTask(String taskType,Integer scheme_id) throws SQLException{
        //taskType: F->动态函数  R->计算方案
        JSONArray resp = DbRubberApi.getTaskDefultByType(taskType,scheme_id);
        return resp;
    }

    //执行节点编辑保存
    @Mapping("scheme/flow/ajax/saveExcuteNode")
    public Boolean saveExcuteNode(Integer scheme_id,String node_id,String name,String actor,String tasks) throws SQLException{
        return DbRubberApi.setSchemeNodeExcute(scheme_id,node_id, name, actor, tasks);
    }

    //计算方案-流程设计 分支节点弹出窗
    @Mapping("scheme/flow/branch")
    public ModelAndView branchLayer(Integer scheme_id,String node_key) throws SQLException{

        RebberSchemeNodeModel node = DbRubberApi.getSchemeNodeByNodeKey(node_key,scheme_id);

        if (scheme_id != null && scheme_id != 0){
            RebberSchemeModel schemeInfo = DbRubberApi.getSchemeById(scheme_id);
            RebberModelModel model = DbRubberApi.getModelFieldListByModelTagAndName(schemeInfo.related_model);
            List<RebberBlockModel> functions = new ArrayList<>();
            if (schemeInfo != null){
                if(!Utils.isEmpty(schemeInfo.related_block)){
                    functions = DbRubberApi.getBlocksByTagOrNameArray(schemeInfo.related_block);
                }
            }
            viewModel.put("functions",functions);

            if (model != null){
                if (model.model_id != 0){
                    List<RebberModelFieldModel> fieldList = DbRubberApi.getFieldList(model.model_id, null);
                    List<JSONObject> list = new ArrayList<>();

                    for (RebberModelFieldModel mode:
                            fieldList) {
                        JSONObject item = new JSONObject();
                        item.put("name",mode.name);
                        item.put("name_display",mode.name_display);
                        list.add(item);
                    }
                    viewModel.put("leftList", JSON.toJSONString(list));
                }
            }
        }
        if (node.node_id > 0){
            JSONObject json = (JSONObject) JSON.parse(node.condition);
            List<JSONObject> list = new ArrayList<>();
            if (json != null) {
                for (String key:json.keySet()) {
                    JSONObject jsonObject = json.getJSONObject(key);
                    list.add(jsonObject);
                }
            }
            viewModel.put("expr",JSON.toJSONString(list));
        }

        viewModel.put("scheme_id", scheme_id);
        if (TextUtils.isEmpty(node.condition)) {
            viewModel.put("hasCondition",0);
        } else {
            viewModel.put("hasCondition",1);
        }
        viewModel.put("schemeNode", node);

        return view("rubber/scheme_node_branch");
    }

    //分支节点编辑保存
    @Mapping("scheme/flow/ajax/saveBranchNode")
    public Boolean saveBranchNode(Integer scheme_id,String node_id,String name,String condition) throws SQLException{
        return DbRubberApi.setSchemeNodeBranch(scheme_id,node_id, name, condition);
    }


    //批量导出
    @Mapping("scheme/ajax/export")
    public void exportDo(Context ctx, String tag, String ids) throws Exception {
        List<RebberSchemeModel> tmpList = DbRubberApi.getSchemeByIds(ids);

        List<RebberSchemeSerializeModel> list = new ArrayList<>(tmpList.size());
        for (RebberSchemeModel m1 : tmpList) {
            RebberSchemeSerializeModel vm = new RebberSchemeSerializeModel();
            vm.model = m1;
            vm.node_design = DbRubberApi.getSchemeNodeDesign(m1.scheme_id);
            vm.nodes = DbRubberApi.getSchemeNodeBySchemeId(m1.scheme_id);
            vm.rules = DbRubberApi.getSchemeRulesSchemeId(m1.scheme_id);

            list.add(vm);
        }

        String jsonD = JsondUtils.encode("rubber_scheme", list);

        String filename2 = "water_raasfile_scheme_" + tag + "_" + Datetime.Now().getDate() + ".jsond";

        ctx.headerSet("Content-Disposition", "attachment; filename=\"" + filename2 + "\"");
        ctx.output(jsonD);
    }


    //批量导入
    @AuthPermissions(SessionPerms.admin)
    @Mapping("scheme/ajax/import")
    public ViewModel importDo(Context ctx, String tag, UploadedFile file) throws Exception {
        String jsonD = IOUtils.toString(file.content);
        JsondEntity entity = JsondUtils.decode(jsonD);

        if(entity == null || "rubber_scheme".equals(entity.table) == false){
            return viewModel.code(0, "数据不对！");
        }

        List<RebberSchemeSerializeModel> list = entity.data.toObjectList(RebberSchemeSerializeModel.class);


        for (RebberSchemeSerializeModel vm : list) {
            DbRubberApi.schemeImp(tag, vm);
        }

        return viewModel.code(1,"ok");
    }
}

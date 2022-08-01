package top.mooyea.api.controller.api;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import top.mooyea.api.enums.RedisConfigEnum;
import top.mooyea.api.enums.URIEnums;
import top.mooyea.api.utils.*;
import top.mooyea.api.vo.result.Result;

import javax.annotation.Resource;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * <h1>ApiController<h1>
 * <p>Copyright (C), 星期三,27,7月,2022</p>
 * <br/>
 * <hr>
 * <h3>File Info:</h3>
 * <p>FileName: ApiController</p>
 * <p>Author:   mooye</p>
 * <p>Work_Email： lidy@skyvis.com.cn</p>
 * <p>E-mail： curtainldy@163.com</p>
 * <p>Date:     2022/7/27</p>
 * <p>Description: api 对外访问接口</p>
 * <hr>
 * <h3>History:</h3>
 * <hr>
 * <table>
 *  <thead>
 *  <tr><td style='width:100px;' center>Author</td><td style='width:200px;' center>Time</td><td style='width:100px;' center>Version_Number</td><td style='width:100px;' center>Description</td></tr>
 *  </thead>
 *  <tbody>
 *    <tr><td style='width:100px;' center>mooye</td><td style='width:200px;' center>11:05 2022/7/27</td><td style='width:100px;' center>v_1.0.0</td><td style='width:100px;' center>创建</td></tr>
 *  </tbody>
 * </table>
 * <hr>
 * <br/>
 *
 * @author mooye
 */

@RestController
@RequestMapping("/api")
@Slf4j
public class ApiController {
    
    @Resource
    RedisTemplateUtil redisUtil;
    
    private final HtmlToImageUtil.Param0 param = new HtmlToImageUtil.Param0();
    
    private static final String ALL_NUMBER = "[0-9]*";
    
    /**
     * 校验状态
     * @param groupCode 群号码
     * @param qqCode 个人号码
     * @return 0:正常执行, 1:群冷却中, 2:个人黑名单
     */
    @GetMapping("/check")
    @ResponseBody
    public JSONObject checkPermissions(String groupCode,String qqCode,String cmd){
        JSONObject result = new JSONObject();
        if (redisUtil.hasKey(RedisConfigEnum.COOLING_TIME.getKey()+groupCode+"_"+cmd, RedisConfigEnum.COOLING_TIME.getSelect())) {
            long cooling = redisUtil.getExpire(RedisConfigEnum.COOLING_TIME.getKey()+groupCode+"_"+cmd, RedisConfigEnum.COOLING_TIME.getSelect());
            result.put("success",false);
            result.put("msg","爷累了!等 "+ cooling +" 秒后再来找我喵~");
        }else {
            if (redisUtil.sHasKey(RedisConfigEnum.REJECT_LIST.getKey(), qqCode, RedisConfigEnum.REJECT_LIST.getSelect())) {
                result.put("success",false);
                result.put("msg","你瞅着不像好人,主人不让我和你玩喵~");
            }else {
                result.put("success",true);
            }
        }
        return result;
    }
    
    /**
     * 添加黑名单
     * @param qqNumber QQ号
     * @return 添加结果
     */
    @GetMapping("/addReject")
    @ResponseBody
    public Result rejectList(String qqNumber){
        Result result = new Result();
        if (redisUtil.sHasKey(RedisConfigEnum.REJECT_LIST.getKey(),qqNumber, RedisConfigEnum.REJECT_LIST.getSelect())) {
            result.setCode(201);
            result.setSuccess(true);
            result.setMessage("再黑名单中存在!");
            return result;
        }
        if (redisUtil.sSet(RedisConfigEnum.REJECT_LIST.getKey(), RedisConfigEnum.REJECT_LIST.getSelect(),qqNumber) == 1) {
            result.setCode(200);
            result.setSuccess(true);
            result.setMessage("已添加黑名单!");
        }else {
            result.setCode(500);
            result.setSuccess(false);
            result.setMessage("拒绝添加黑名单!");
        }
        return result;
    }
    
    @GetMapping("/moveReject")
    @ResponseBody
    public Result moveReject(String qqNumber){
        Result result = new Result();
        if (!redisUtil.sHasKey(RedisConfigEnum.REJECT_LIST.getKey(),qqNumber, RedisConfigEnum.REJECT_LIST.getSelect())) {
            result.setCode(201);
            result.setSuccess(true);
            result.setMessage("不在小黑屋里呢喵~");
            return result;
        }
        if (redisUtil.setRemove(RedisConfigEnum.REJECT_LIST.getKey(), RedisConfigEnum.REJECT_LIST.getSelect(), qqNumber) ==1) {
            result.setMessage("以后不要在来了哦,这次就把你放出来了喵~");
            result.setSuccess(true);
            result.setCode(200);
        }else {
            result.setMessage("报告不知道为啥他不想出来,我也没办法了喵~");
            result.setSuccess(false);
            result.setCode(500);
        }
        return result;
    }
    
    
    @GetMapping(value = "/queryDanmaku")
    @ResponseBody
    public JSONObject queryDanmaku(String uid,String groupCode,String cmd) {
        JSONObject result = new JSONObject();
        JSONObject userInfo = queryUserInfo(uid);
        if (!userInfo.getBooleanValue("success")){
            return userInfo;
        }
        userInfo = userInfo.getJSONObject("userInfo");
        uid = userInfo.getString("uid");
        if (redisUtil.hasKey(uid, 7)){
            String url =  (String) redisUtil.get(uid,7);
            if (url!=null) {
                result.put("success",true);
                result.put("uri",url);
                result.put("msg","查到了喵~");
                setCooling(groupCode,cmd);
                return result;
            }
        }
        String sendUrl = URIEnums.SEARCH_DANMAKU.getUri().replace(URIEnums.SEARCH_DANMAKU.getPlaceholder(),userInfo.getString("uid"));
        log.info("sendUrl:{}",sendUrl);
        String html = FetchThirdPartyDataUtil.analysisDanmaku(sendUrl);
        String savePath = "/opt/bilibili/html/danmu_" + uid + "_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()) + ".html";
        String tempPath = "/opt/bilibili/template/danmuTemp.html";
        if (Objects.equals(OsInfoUtil.getOsName(),"Mac OS X".toUpperCase(Locale.ROOT))){
            savePath = "/Users/mooye/Desktop/bot/danmu-" + uid + "_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()) + ".html";
            tempPath = "/Users/mooye/Desktop/bot/template/danmuTemp.html";
        }
        try {
            String content = FileUtils.readFileToString(new File(tempPath), StandardCharsets.UTF_8);
            String htmlPath = FetchThirdPartyDataUtil.generateHtml(content, FetchThirdPartyDataUtil.handlerHtml(html), savePath, userInfo);
            if (htmlPath != null){
                param.setJavascriptDelay(1000);
                param.setUrl(htmlPath);
                String url = getString(htmlPath);
                redisUtil.set(uid,url,7);
                redisUtil.expire(uid,7,cacheTime());
                result.put("success",true);
                result.put("uri",url);
                result.put("msg","查到了喵~");
                setCooling(groupCode,cmd);
            }else {
                result.put("success",false);
                result.put("uri","");
                result.put("msg","啊嘞!出错惹~");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            result.put("success",false);
            result.put("uri","");
            result.put("msg","靠!!!! 你D了多少?累死爷了,爷罢工了喵!");
        }
        return result;
    }
    
    @GetMapping(value = "/queryGift")
    @ResponseBody
    public JSONObject queryGift(String uid, String groupCode,String cmd) {
        JSONObject result = new JSONObject();
        JSONObject userInfo = queryUserInfo(uid);
        if (!userInfo.getBooleanValue("success")){
            return userInfo;
        }
        userInfo = userInfo.getJSONObject("userInfo");
        uid = userInfo.getString("uid");
        if (redisUtil.hasKey(uid, 6)){
            String url =  (String) redisUtil.get(uid,6);
            if (url!=null) {
                result.put("success",true);
                result.put("uri",url);
                result.put("msg","查到了喵~");
                setCooling(groupCode,cmd);
                return result;
            }
        }
        String url;
        String sendUrl = URIEnums.SEARCH_GIFT.getUri().replace(URIEnums.SEARCH_GIFT.getPlaceholder(),userInfo.getString("uid"));
        log.info("sendUrl:{}",sendUrl);
        String html = FetchThirdPartyDataUtil.analysisDanmaku(sendUrl);
        String savePath = "/opt/bilibili/html/gift_" + uid + "_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()) + ".html";
        String tempPath = "/opt/bilibili/template/giftTemp.html";
        if (Objects.equals(OsInfoUtil.getOsName(),"Mac OS X".toUpperCase(Locale.ROOT))){
            savePath = "/Users/mooye/Desktop/bot/gift-" + uid + "_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()) + ".html";
            tempPath = "/Users/mooye/Desktop/bot/template/giftTemp.html";
        }
        log.info("tempPath:{}",tempPath);
        try {
            String content = FileUtils.readFileToString(new File(tempPath), StandardCharsets.UTF_8);
            String htmlPath = FetchThirdPartyDataUtil.generateHtml(content, html, savePath, userInfo);
            if (htmlPath != null) {
                param.setDpi("2");
                param.setJavascriptDelay(1000);
                param.setUrl(htmlPath);
                url = getString(htmlPath);
                redisUtil.set(uid,url,6);
                redisUtil.expire(uid,6,cacheTime());
                result.put("success",true);
                result.put("uri",url);
                result.put("msg","查到了喵~");
                setCooling(groupCode,cmd);
            }else {
                result.put("success",false);
                result.put("uri","");
                result.put("msg","啊嘞!出错惹~");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            result.put("success",false);
            result.put("uri","");
            result.put("msg","靠!!!! 你D了多少?累死爷了,爷罢工了喵!");
        }
        return result;
    }
    
    @GetMapping(value = "/queryEnter")
    @ResponseBody
    public JSONObject queryEnter(String uid, String groupCode,String cmd) {
        JSONObject result = new JSONObject();
        JSONObject userInfo = queryUserInfo(uid);
        if (!userInfo.getBooleanValue("success")){
            return userInfo;
        }
        userInfo = userInfo.getJSONObject("userInfo");
        uid = userInfo.getString("uid");
        if (redisUtil.hasKey(uid, 8)){
            String url =  (String) redisUtil.get(uid,8);
            if (url!=null) {
                result.put("success",true);
                result.put("uri",url);
                result.put("msg","查到了喵~");
                setCooling(groupCode,cmd);
                return result;
            }
        }
        String url;
        String sendUrl = URIEnums.SEARCH_ENTER.getUri().replace(URIEnums.SEARCH_ENTER.getPlaceholder(),userInfo.getString("uid"));
        log.info("sendUrl:{}",sendUrl);
        String html = FetchThirdPartyDataUtil.getHtml(sendUrl);
        String savePath = "/opt/bilibili/html/enter_" + uid + "_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()) + ".html";
        String tempPath = "/opt/bilibili/template/enterTemp.html";
        if (Objects.equals(OsInfoUtil.getOsName(),"Mac OS X".toUpperCase(Locale.ROOT))){
            savePath = "/Users/mooye/Desktop/bot/enter-" + uid + "_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()) + ".html";
            tempPath = "/Users/mooye/Desktop/bot/template/enterTemp.html";
        }
        try {
            String content = FileUtils.readFileToString(new File(tempPath), StandardCharsets.UTF_8);
            String htmlPath = FetchThirdPartyDataUtil.generateHtml(content, html, savePath, userInfo);
            if (htmlPath != null) {
                param.setDpi("2");
                param.setJavascriptDelay(1000);
                param.setUrl(htmlPath);
                url = getString(htmlPath);
                redisUtil.set(uid, url, 8);
                redisUtil.expire(uid, 8, cacheTime());
                result.put("success", true);
                result.put("uri", url);
                result.put("msg", "查到了喵~");
                setCooling(groupCode, cmd);
            }else {
                result.put("success",false);
                result.put("uri","");
                result.put("msg","啊嘞!出错惹~");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            result.put("success",false);
            result.put("uri","");
            result.put("msg","靠!!!! 你D了多少?累死爷了,爷罢工了喵!");
        }
        return result;
    }
    
    private void setCooling(String groupCode,String cmd){
        redisUtil.set(RedisConfigEnum.COOLING_TIME.getKey()+groupCode+"_"+cmd, cmd, RedisConfigEnum.COOLING_TIME.getSelect(), 3*60);
    }
    
    private String getString(String htmlPath) throws IOException, InterruptedException {
        File pdf;
        String url;
        pdf = HtmlToImageUtil.htmlToPdf(param);
        File image = HtmlToImageUtil.pdfToImage(pdf, param,htmlPath.substring(htmlPath.lastIndexOf("/")+1,htmlPath.lastIndexOf(".")));
        url = OssBootUtil.upload(Files.newInputStream(image.toPath()), image.getName());
        image.delete();
        pdf.delete();
        new File(htmlPath).delete();
        return url;
    }
    
    private JSONObject queryUserInfo(String uid){
        JSONObject result = new JSONObject();
        JSONObject userInfo = HttpClientUtil.getBilibiliUserInfo(URIEnums.SEARCH_USER_INFO.getUri(), uid, URIEnums.SEARCH_USER_INFO.getPlaceholder());
        if (userInfo.isEmpty()){
            result.put("success",false);
            result.put("uri","");
            result.put("msg","你在查谁?本尊查遍三界也没找到喵~");
            return result;
        }
        result.put("success",true);
        result.put("userInfo",userInfo);
        return result;
    }
    
    private long cacheTime(){
        long time = 0;
        long now = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date())+" 23:59:59";
        try {
            long failureTime = format.parse(date).getTime();
            time = (failureTime - now)/1000L ;
        } catch (ParseException e) {
            time = 2*60*60L;
        }
        return time;
    }
}

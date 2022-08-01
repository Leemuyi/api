package top.mooyea.api.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import top.mooyea.api.enums.URIEnums;

import java.io.IOException;

/**
 * <h1>HttpClientUtil<h1>
 * <p>Copyright (C), 星期四,28,7月,2022</p>
 * <br/>
 * <hr>
 * <h3>File Info:</h3>
 * <p>FileName: HttpClientUtil</p>
 * <p>Author:   mooye</p>
 * <p>Work_Email： lidy@skyvis.com.cn</p>
 * <p>E-mail： curtainldy@163.com</p>
 * <p>Date:     2022/7/28</p>
 * <p>Description: HTTP 请求接口类</p>
 * <hr>
 * <h3>History:</h3>
 * <hr>
 * <table>
 *  <thead>
 *  <tr><td style='width:100px;' center>Author</td><td style='width:200px;' center>Time</td><td style='width:100px;' center>Version_Number</td><td style='width:100px;' center>Description</td></tr>
 *  </thead>
 *  <tbody>
 *    <tr><td style='width:100px;' center>mooye</td><td style='width:200px;' center>14:47 2022/7/28</td><td style='width:100px;' center>v_1.0.0</td><td style='width:100px;' center>创建</td></tr>
 *  </tbody>
 * </table>
 * <hr>
 * <br/>
 *
 * @author mooye
 */

@Slf4j
public class HttpClientUtil {
    
    
    public static JSONObject httpGet(String url){
        JSONObject result = new JSONObject();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            RequestConfig config = RequestConfig.custom().setConnectTimeout(2000)
                    .setConnectionRequestTimeout(1000)
                    .setSocketTimeout(10 * 1000)
                    .build();
            get.setConfig(config);
            CloseableHttpResponse response = client.execute(get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result = JSON.parseObject(EntityUtils.toString(response.getEntity()));
//                log.info("HttpClientUtil.httpGet().result:{}",result);
            } else {
                log.info("掉用失败时的返回:{}", EntityUtils.toString(response.getEntity()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    
    
    public static String getUid(String url){
        String uid = null;
        JSONObject searchData = httpGet(url);
        if (!searchData.isEmpty()) {
            JSONArray jsonArray = searchData.getJSONObject("data").getJSONArray("result");
            if (jsonArray != null &&!jsonArray.isEmpty()) {
                uid = JSON.parseObject(JSON.toJSONString(jsonArray.get(0))).getString("mid");
            }
        }
        return uid;
    }
    
    public static JSONObject getBilibiliUserInfo(String url, String param, String occupancy){
        JSONObject result = new JSONObject();
        if (StringUtils.isNumeric(param)){
            url = url.replace(occupancy,param);
        }else {
            String uid = getUid(URIEnums.SEARCH_USER.getUri().replace(URIEnums.SEARCH_USER.getPlaceholder(),param));
            if (uid == null){
                return result;
            }
            url = url.replace(occupancy,uid);
        }
        JSONObject bilibiliUser = httpGet(url);
        if (!bilibiliUser.isEmpty()) {
            JSONObject data = bilibiliUser.getJSONObject("data");
            result.put("uid",data.getString("mid"));
            result.put("name",data.getString("name"));
            result.put("face",data.getString("face"));
            JSONObject fansMedal = data.getJSONObject("fans_medal");
            if (fansMedal.getBooleanValue("show")) {
                JSONObject medal = fansMedal.getJSONObject("medal");
                String fansName = medal.getString("medal_name");
                String level = medal.getString("level");
                String guardLevel = guardLevel(medal.getInteger("guard_level"));
                result.put("fans_medal", fansName + "- Lv." + level + "(" + guardLevel + ")");
            }else {
                result.put("fans_medal","暂无粉丝勋章");
            }
        }
        return result;
    }
    
    private static String guardLevel(Integer guardLevel) {
        if (guardLevel == null){
            return "其他";
        }
        switch (guardLevel){
            case 1:
                return "总督";
            case 2:
                return "提督";
            case 3:
                return "舰长";
            default:
                return "其他";
        }
    }
    
    
    public static void main(String[] args) {
        String url = "https://api.bilibili.com/x/web-interface/search/type?keyword=小鹿的Daddy&search_type=bili_user";
        
//        String uid = getUid(url);
//        log.info("uid:{}",uid);
        url = "https://api.bilibili.com/x/space/acc/info?mid=670598709&jsonp=jsonp";
        JSONObject info  = getBilibiliUserInfo(URIEnums.SEARCH_USER_INFO.getUri(),"小鹿草的Daddy",URIEnums.SEARCH_USER_INFO.getPlaceholder());
        
        log.info("用户信息:{}",info);
    }
}

package top.mooyea.api.utils;


import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <h1>FetchThirdPartyDataUtil<h1>
 * <p>Copyright (C), 星期二,14,6月,2022</p>
 * <br/>
 * <hr>
 * <h3>File Info:</h3>
 * <p>FileName: FetchThirdPartyDataUtil</p>
 * <p>Author:   mooye</p>
 * <p>Work_Email： lidy@skyvis.com.cn</p>
 * <p>E-mail： curtainldy@163.com</p>
 * <p>Date:     2022/6/14</p>
 * <p>Description: 获取 B站数据工具类</p>
 * <hr>
 * <h3>History:</h3>
 * <hr>
 * <table>
 *  <thead>
 *  <tr><td style='width:100px;' center>Author</td><td style='width:200px;' center>Time</td><td style='width:100px;' center>Version_Number</td><td style='width:100px;' center>Description</td></tr>
 *  </thead>
 *  <tbody>
 *    <tr><td style='width:100px;' center>mooye</td><td style='width:200px;' center>16:32 2022/6/14</td><td style='width:100px;' center>v_0.0.1</td><td style='width:100px;' center>创建</td></tr>
 *  </tbody>
 * </table>
 * <hr>
 * <br/>
 *
 * @author mooye
 */

@Slf4j
public class FetchThirdPartyDataUtil {
    /**
     * 获取网页(B站搜索)返回的页面
     *
     * @param url 搜索链接
     *
     * @return HTML 页面
     */
    public static String getHtml(String url) {
        String result;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            RequestConfig config = RequestConfig.custom().setConnectTimeout(2000)
                    .setConnectionRequestTimeout(1000)
                    .setSocketTimeout(3 * 1000)
                    .build();
            get.setConfig(config);
            CloseableHttpResponse response = client.execute(get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result = EntityUtils.toString(response.getEntity());
            } else {
                result = null;
                log.info("掉用失败时的返回:{}", EntityUtils.toString(response.getEntity()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    
    /**
     * 获取返回数据的总页数
     *
     * @param html 搜索接口的 HTML 页面
     *
     * @return 页数
     */
    public static Integer getPageCount(String html) {
        int count;
        Document doc = Jsoup.parse(html);
        Elements elements = doc.getElementsByClass("page-item last");
        log.info("elements:{}", elements);
        AtomicReference<Element> element = new AtomicReference<>();
        if (elements.size() == 1) {
            element.set(elements.get(0));
        } else {
            elements.forEach(ele -> {
                if (ele.children().hasClass("pagination-btn")) {
                    ele.children().forEach(cl -> {
                        if (cl.hasClass("pagination-btn")) {
                            element.set(cl);
                        }
                    });
                }
            });
        }
        String ht = element.get().text().trim();
        log.info(ht);
        count = Integer.parseInt(ht);
        return count;
    }
    
    /**
     * 处理 HTML 页面 提取数据
     *
     * @param html    HTML 页面
     * @param keyWord 关键字
     *
     * @return bvid 集合
     */
    public static List<String> analysisVideoList(String html, String keyWord) {
        List<String> list = new ArrayList<>();
        Document document = Jsoup.parse(html);
        Elements elements = document.getElementsByClass("video-list clearfix");
        elements.forEach(element -> element.getElementsByTag("li").forEach(li -> li.select("div.tags").forEach(tag -> {
            Elements aTag = tag.select("a.up-name");
            if (Objects.equals(aTag.text().trim(), keyWord)) {
                Elements videoInfos = li.select("a.img-anchor");
                String bvId = Objects.requireNonNull(videoInfos.first()).attr("href").split("\\?")[0];
                bvId = bvId.substring(bvId.lastIndexOf("/") + 1);
                list.add(bvId);
            }
        })));
        
        return list;
    }
    
    public static JSONObject analysisGuard(String api) {
        JSONObject result;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(api);
            RequestConfig config = RequestConfig.custom().setConnectTimeout(2000)
                    .setConnectionRequestTimeout(1000)
                    .setSocketTimeout(3 * 1000)
                    .build();
            get.setConfig(config);
            CloseableHttpResponse response = client.execute(get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result = JSONObject.parseObject(EntityUtils.toString(response.getEntity()));
            } else {
                result = null;
                log.info("掉用失败时的返回:{}", EntityUtils.toString(response.getEntity()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    
    public static JSONObject analysisTime(String api) {
        JSONObject result;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost get = new HttpPost(api);
            RequestConfig config = RequestConfig.custom().setConnectTimeout(2000)
                    .setConnectionRequestTimeout(1000)
                    .setSocketTimeout(3 * 1000)
                    .build();
            get.setConfig(config);
            CloseableHttpResponse response = client.execute(get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result = JSONObject.parseObject(EntityUtils.toString(response.getEntity()));
            } else {
                result = null;
                log.info("掉用失败时的返回:{}", EntityUtils.toString(response.getEntity()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    
    
    public static String analysisDanmaku(String api) {
        String result;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost get = new HttpPost(api);
            RequestConfig config = RequestConfig.custom().setConnectTimeout(2000)
                    .setConnectionRequestTimeout(1000)
                    .setSocketTimeout(3 * 1000)
                    .build();
            get.setConfig(config);
            CloseableHttpResponse response = client.execute(get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result = EntityUtils.toString(response.getEntity());
            } else {
                result = null;
                log.info("掉用失败时的返回:{}", EntityUtils.toString(response.getEntity()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    
    public static List<JSONObject> handlerHtml(String html) {
        List<JSONObject> list = new ArrayList<>();
        Document document = Jsoup.parse(html);
        Elements elements = document.getElementsByClass("card danmaku_card");
        elements.forEach(element -> {
            String sendTime;
            String upName;
            int count;
            Elements cardHeader = element.getElementsByClass("card-header");
            if (!cardHeader.isEmpty()) {
                String text = cardHeader.text();
                sendTime = text.split(" ")[0].replaceAll("_", "-");
                upName = text.split(" ")[1];
                count = Integer.parseInt(text.split(" ")[2]);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", upName);
                jsonObject.put("time", sendTime);
                jsonObject.put("count", count);
                list.add(jsonObject);
            }
        });
        list.sort(Comparator.comparing(t -> t.getDate("time")));
        return list;
    }
    
    public static String generateHtml(String temp, List<JSONObject> list, String path, JSONObject json) {
        String queryTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        temp = temp.replace("{{queryTime}}", queryTime).replace("{{UID}}", json.getString("uid"))
                .replace("{{name}}",json.getString("name")).replace("{{fans_medal}}",json.getString("fans_medal"))
                .replace("{{face}}",json.getString("face"));
        StringBuffer buffer = new StringBuffer();
        list.forEach(liJson -> buffer.append("<tr><td>").append(liJson.getString("time"))
                .append("</td><td>").append(liJson.getString("name"))
                .append("</td><td>").append(liJson.getInteger("count"))
                .append("</td><tr>"));
        temp = temp.replace("{{data}}", buffer.toString());
        
        try {
            File file = new File(path);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    return null;
                }
            }
            FileUtils.writeStringToFile(file, temp, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            log.info("写文件时出现 IO 异常");
            path = null;
        }
        return path;
    }
    
    public static String generateHtml(String temp, String html, String path, JSONObject json) {
        String queryTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        temp = temp.replace("{{queryTime}}", queryTime).replace("{{UID}}", json.getString("uid"))
                .replace("{{name}}",json.getString("name")).replace("{{fans_medal}}",json.getString("fans_medal"))
                .replace("{{face}}",json.getString("face"));
        temp = temp.replace("{{tableData}}", html);
        
        try {
            FileUtils.writeStringToFile(new File(path), temp, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            log.info("写文件时出现 IO 异常");
            path = null;
        }
        return path;
    }
    
    public static void main(String[] args) throws IOException {
        String uid = "670598709";
        String savePath = "/Users/mooye/Desktop/" + uid + "_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()) + ".html";
        String tempPath = "/Users/mooye/Desktop/danmuTemp.html";
        String content = FileUtils.readFileToString(new File(tempPath), StandardCharsets.UTF_8);
        log.info(content);
//        log.info(generateHtml(content,handlerHtml(analysisDanmaku("https://biligank.com/blive/danmaku/"+uid)),savePath,uid));
    }
}

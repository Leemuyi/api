package top.mooyea.api.enums;

import org.springframework.web.bind.annotation.RequestMethod;

/**
 * <h1>URIEnums<h1>
 * <p>Copyright (C), 星期二,26,7月,2022</p>
 * <br/>
 * <hr>
 * <h3>File Info:</h3>
 * <p>FileName: URIEnums</p>
 * <p>Author:   mooye</p>
 * <p>Work_Email： lidy@skyvis.com.cn</p>
 * <p>E-mail： curtainldy@163.com</p>
 * <p>Date:     2022/7/26</p>
 * <p>Description: URI 枚举类</p>
 * <hr>
 * <h3>History:</h3>
 * <hr>
 * <table>
 *  <thead>
 *  <tr><td style='width:100px;' center>Author</td><td style='width:200px;' center>Time</td><td style='width:100px;' center>Version_Number</td><td style='width:100px;' center>Description</td></tr>
 *  </thead>
 *  <tbody>
 *    <tr><td style='width:100px;' center>mooye</td><td style='width:200px;' center>16:51 2022/7/26</td><td style='width:100px;' center>v_0.0.1</td><td style='width:100px;' center>创建</td></tr>
 *  </tbody>
 * </table>
 * <hr>
 * <br/>
 *
 * @author mooye
 */


public enum URIEnums {
    /**
     * B站用户搜索接口
     */
    SEARCH_USER("https://api.bilibili.com/x/web-interface/search/type?keyword=#{keyWord}&search_type=bili_user","#{keyWord}", RequestMethod.GET),
    /**
     * 第三方接口查询弹幕
     */
    SEARCH_DANMAKU("https://biligank.com/blive/danmaku/#{uid}?rt=false","#{uid}", RequestMethod.GET),
    /**
     * 第三方接口查询礼物
     */
    SEARCH_GIFT("https://biligank.com/blive/gift/#{uid}?rt=false","#{uid}", RequestMethod.GET),
    SEARCH_ENTER("https://biligank.com/blive/entry/#{uid}?rt=false","#{uid}", RequestMethod.GET),
    /**
     * 获取用户详细信息
     */
    SEARCH_USER_INFO("https://api.bilibili.com/x/space/acc/info?mid=#{mid}&jsonp=jsonp","#{mid}", RequestMethod.GET)
    
    ;
    private final String uri;
    private final String placeholder;
    private final RequestMethod method;
    
    
    URIEnums(String uri, String placeholder, RequestMethod method) {
        this.uri = uri;
        this.placeholder = placeholder;
        this.method = method;
    }
    
    public String getUri() {
        return uri;
    }
    
    public String getPlaceholder() {
        return placeholder;
    }
    
    public RequestMethod getMethod() {
        return method;
    }
}

package top.mooyea.api.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Locale;

/**
 * <h1>OsInfoUtil<h1>
 * <p>Copyright (C), 星期四,28,7月,2022</p>
 * <br/>
 * <hr>
 * <h3>File Info:</h3>
 * <p>FileName: OsInfoUtil</p>
 * <p>Author:   mooye</p>
 * <p>Work_Email： lidy@skyvis.com.cn</p>
 * <p>E-mail： curtainldy@163.com</p>
 * <p>Date:     2022/7/28</p>
 * <p>Description: 获取系统信息</p>
 * <hr>
 * <h3>History:</h3>
 * <hr>
 * <table>
 *  <thead>
 *  <tr><td style='width:100px;' center>Author</td><td style='width:200px;' center>Time</td><td style='width:100px;' center>Version_Number</td><td style='width:100px;' center>Description</td></tr>
 *  </thead>
 *  <tbody>
 *    <tr><td style='width:100px;' center>mooye</td><td style='width:200px;' center>11:35 2022/7/28</td><td style='width:100px;' center>v_1.0.0</td><td style='width:100px;' center>创建</td></tr>
 *  </tbody>
 * </table>
 * <hr>
 * <br/>
 *
 * @author mooye
 */

@Slf4j
public class OsInfoUtil {
    private static final String OS_NAME = System.getProperty("os.name");
    
    public static String getOsName(){
        return OS_NAME.toUpperCase(Locale.ROOT);
    }
    
    
    public static void main(String[] args) {
        log.info("OS Name:{}",OS_NAME);
    }
}

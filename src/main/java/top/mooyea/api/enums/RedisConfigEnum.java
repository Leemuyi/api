package top.mooyea.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>RedisConfigEnum<h1>
 * <p>Copyright (C), 星期三,27,7月,2022</p>
 * <br/>
 * <hr>
 * <h3>File Info:</h3>
 * <p>FileName: RedisConfigEnum</p>
 * <p>Author:   mooye</p>
 * <p>Work_Email： lidy@skyvis.com.cn</p>
 * <p>E-mail： curtainldy@163.com</p>
 * <p>Date:     2022/7/27</p>
 * <p>Description: redis 的一些配置</p>
 * <hr>
 * <h3>History:</h3>
 * <hr>
 * <table>
 *  <thead>
 *  <tr><td style='width:100px;' center>Author</td><td style='width:200px;' center>Time</td><td style='width:100px;' center>Version_Number</td><td style='width:100px;' center>Description</td></tr>
 *  </thead>
 *  <tbody>
 *    <tr><td style='width:100px;' center>mooye</td><td style='width:200px;' center>12:03 2022/7/27</td><td style='width:100px;' center>v_1.0.0</td><td style='width:100px;' center>创建</td></tr>
 *  </tbody>
 * </table>
 * <hr>
 * <br/>
 *
 * @author mooye
 */

@AllArgsConstructor
@Getter
public enum RedisConfigEnum {
    /**
     * 黑名单
     */
    REJECT_LIST("REJECT_LIST",0),
    COOLING_TIME("COOLING_TIME_",1)
    ;
    private final String key;
    private final int select;
}

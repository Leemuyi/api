package top.mooyea.api.thread;

import org.apache.pdfbox.rendering.PDFRenderer;
import top.mooyea.api.utils.HtmlToImageUtil;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;



/**
 * <h1>PDFToImageThread<h1>
 * <p>Copyright (C), 星期三,06,7月,2022</p>
 * <br/>
 * <hr>
 * <h3>File Info:</h3>
 * <p>FileName: PDFToImageThread</p>
 * <p>Author:   mooye</p>
 * <p>Work_Email： lidy@skyvis.com.cn</p>
 * <p>E-mail： curtainldy@163.com</p>
 * <p>Date:     2022/7/6</p>
 * <p>Description: pdf 转图片线程</p>
 * <hr>
 * <h3>History:</h3>
 * <hr>
 * <table>
 *  <thead>
 *  <tr><td style='width:100px;' center>Author</td><td style='width:200px;' center>Time</td><td style='width:100px;' center>Version_Number</td><td style='width:100px;' center>Description</td></tr>
 *  </thead>
 *  <tbody>
 *    <tr><td style='width:100px;' center>mooye</td><td style='width:200px;' center>12:36 2022/7/6</td><td style='width:100px;' center>v_0.0.1</td><td style='width:100px;' center>创建</td></tr>
 *  </tbody>
 * </table>
 * <hr>
 * <br/>
 *
 * @author mooye
 */


public class PDFToImageThread implements Runnable{
    private String[] url;
    private final int index;
    private final HtmlToImageUtil.Param0 param;
    private final PDFRenderer render;
    private final String path;
    
    public PDFToImageThread(String[] url, int index, HtmlToImageUtil.Param0 param, PDFRenderer render, String path) {
        this.url = url;
        this.index = index;
        this.param = param;
        this.render = render;
        this.path = path;
    }
    
    @Override
    public void run() {
        BufferedImage image = null;
        try {
            image = render.renderImage(index, Float.parseFloat(param.getDpi()));
            BufferedImage newImage = HtmlToImageUtil.cutOffInvliadPart(image);
            OutputStream out = Files.newOutputStream(Paths.get(path));
            if (ImageIO.write(newImage, "png", out)) {
                out.close();
                url[index] = path;
            }
        } catch (Exception e) {
        
        }
    }
}

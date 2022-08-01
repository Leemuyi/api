package top.mooyea.api.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import top.mooyea.api.thread.PDFToImageThread;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * <h1>HtmlToImageUtil<h1>
 * <p>Copyright (C), 星期六,02,7月,2022</p>
 * <br/>
 * <hr>
 * <h3>File Info:</h3>
 * <p>FileName: HtmlToImageUtil</p>
 * <p>Author:   mooye</p>
 * <p>Work_Email： lidy@skyvis.com.cn</p>
 * <p>E-mail： curtainldy@163.com</p>
 * <p>Date:     2022/7/2</p>
 * <p>Description: HTML转图片工具类</p>
 * <hr>
 * <h3>History:</h3>
 * <hr>
 * <table>
 *  <thead>
 *  <tr><td style='width:100px;' center>Author</td><td style='width:200px;' center>Time</td><td style='width:100px;' center>Version_Number</td><td style='width:100px;' center>Description</td></tr>
 *  </thead>
 *  <tbody>
 *    <tr><td style='width:100px;' center>mooye</td><td style='width:200px;' center>21:33 2022/7/2</td><td style='width:100px;' center>v_0.0.1</td><td style='width:100px;' center>创建</td></tr>
 *  </tbody>
 * </table>
 * <hr>
 * <br/>
 *
 * @author mooye
 */

@Slf4j
public class HtmlToImageUtil {
    
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder()
            .setNameFormat("pdf 转图片线程 -%d").build();
    
    public static void main(String[] args) throws Exception {
        Param0 param = new Param0();
        param.setDpi("2");
        param.setJavascriptDelay(1000);
        param.setUrl("/Users/mooye/Desktop/小鹿草之森的工具_2022_07_03_01_54_43.html");
        File pdf = htmlToPdf(param);
        File image = pdfToImage(pdf, param,"小鹿草之森的工具_2022_07_03_01_54_43.html");
    }
    
    /**
     * 去掉无用的部分(pdf转成的图片有很大一部分是无用的白色背景)
     * 找出图片的有效部分，把有效部分复制到另一张新图上。
     */
    public static BufferedImage cutOffInvliadPart(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        //记录原始图片每个像素的color
        int[][] rgbs = new int[width][height];
        //以下是图片有效部分的边界
        int top = height;
        int bottom = 0;
        int left = -1;
        int right = -1;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = image.getRGB(i, j);
                rgbs[i][j] = rgb;
                int tempRgb = rgb & 0x00ffffff;
                boolean isWhite = (tempRgb == 0x00ffffff);
                if (!isWhite) {
                    if (j < top) {
                        top = j;
                    }
                    if (j > bottom) {
                        bottom = j;
                    }
                    if (left == -1) {
                        left = i;
                    }
                    if (i > right) {
                        right = i;
                    }
                }
            }
        }
        //留白
        bottom = Math.min((bottom + 10), (height - 1));
        left = (left > 1) ? (left - 1) : 0;
        
        log.info("left:{} right:{} top:{} bottom:{}", left, right, top, bottom);
        //像素的坐标是从0算起的，所以宽高要+1.
        BufferedImage newImage = new BufferedImage(right - left + 1, bottom - top + 1, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if ((j >= top) && (j <= bottom) && (i >= left) && (i <= right)) {
                    newImage.setRGB(i - left, j - top, rgbs[i][j]);
                }
            }
        }
        return newImage;
    }
    
    public static File pdfToImage(File pdf, Param0 param,String fileName){
        String filePath = "/opt/bilibili/image/danmaku/" + fileName + ".png";
//        String filePath = "/Users/mooye/Desktop/" + fileName + ".png";
        if (Objects.equals(OsInfoUtil.getOsName(),"Mac OS X".toUpperCase(Locale.ROOT))){
            filePath = "/Users/mooye/Desktop/" + fileName + ".png";
        }
        log.info("filePath:{}",filePath);
        File file = null;
        String[] urls = null;
        try(PDDocument pdDoc = PDDocument.load(pdf)) {
            long time = System.currentTimeMillis();
            log.info("耗时:{}",(System.currentTimeMillis()-time)/1000d);
            log.info("页数:{}",pdDoc.getNumberOfPages());
            PDFRenderer render = new PDFRenderer(pdDoc);
            int page = pdDoc.getNumberOfPages();
            urls = new String[page];
            ExecutorService service = new ThreadPoolExecutor(2, 40, 40L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(5),THREAD_FACTORY);
            for (int i = 0; i < page; i++) {
                String zeroPath = filePath.substring(0,filePath.lastIndexOf("."))+"-"+i+filePath.substring(filePath.lastIndexOf("."));
                log.info("标志:{},路径:{}",i,zeroPath);
                service.submit(new PDFToImageThread(urls,i,param,render,zeroPath));
            }
            service.shutdown();
            while (true){
                if (service.isTerminated()){
                    log.info("url 集合:{}", Arrays.asList(urls));
                    break;
                }
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        } finally {
            if (pdf != null && pdf.exists()) {
                pdf.delete();
            }
            long time = System.currentTimeMillis();
            if (urls != null && ImageUtil.merge(urls,"png",filePath)) {
                log.info("耗时:{}",(System.currentTimeMillis()-time)/1000d);
                file =  new File(filePath);
            }
        }
        return file;
    }
    
    public static File htmlToPdf(Param0 param) throws IOException, InterruptedException {
        
        StringBuilder commandBuilder = new StringBuilder();
        if (System.getProperty("os.name").contains("Mac") || System.getProperty("os.name").contains("mac")) {
            //我的本地配置
            commandBuilder.append("/usr/local/bin/wkhtmltopdf");
        } else {
            //服务器配置
            commandBuilder.append("/usr/local/bin/wkhtmltopdf");
        }
        //wkhtmltopdf --javascript-delay 1000 -B 1mm -L 1mm -R 1mm -T 1mm  url qptest.pdf
        commandBuilder.append(" --javascript-delay ").append(param.getJavascriptDelay() + " ")
                .append(" -B 0mm -L 0mm -R 0mm -T 0mm ");
        commandBuilder.append(param.getUrl());
        String pdfPath = System.getProperty("java.io.tmpdir") + "/" + "test" + ".pdf";
        File pdf = new File(pdfPath);
        commandBuilder.append(" " + pdfPath);
        String command = commandBuilder.toString();
        System.out.println(command);
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
        return pdf;
    }
    
    /**
     * 外部传入的参数
     */
    public static class Param0 {
        private String url;
        private String dpi = "2";
        private int javascriptDelay = 200;
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        
        public String getDpi() {
            return dpi;
        }
        
        public void setDpi(String dpi) {
            this.dpi = dpi;
        }
        
        public int getJavascriptDelay() {
            return javascriptDelay;
        }
        
        public void setJavascriptDelay(int javascriptDelay) {
            this.javascriptDelay = javascriptDelay;
        }
    }
    
    
}

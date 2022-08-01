package top.mooyea.api.utils;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class ImageUtil {
    public static boolean merge(String[] imgs, String type, String mergePic) {
        int dstHeight = 0;
        int dstWidth = 0;
        // 获取需要拼接的图片长度
        int len = imgs.length;
        // 判断长度是否大于0
        if (len < 1) {
            return false;
        }
        File[] file = new File[len];
        BufferedImage[] images = new BufferedImage[len];
        int[][] ImageArrays = new int[len][];
        for (int i = 0; i < len; i++) {
            try {
                file[i] = new File(imgs[i]);
                images[i] = ImageIO.read(file[i]);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            
            int width = images[i].getWidth();
            int height = images[i].getHeight();
            
            // 从图片中读取RGB 像素
            ImageArrays[i] = new int[width * height];
            ImageArrays[i] = images[i].getRGB(0, 0, width, height, ImageArrays[i], 0, width);
            
            // 计算合并的宽度和高度
            dstWidth = Math.max(dstWidth, width);
            dstHeight += height;
        }
        
        // 合成图片像素
        System.out.println("宽度:" + dstWidth);
        System.out.println("高度:" + dstHeight);
        
        if (dstHeight < 1) {
            System.out.println("dstHeight < 1");
            return false;
        }
        // 生成新图片
        try {
            BufferedImage imageNew = new BufferedImage(dstWidth, dstHeight, BufferedImage.TYPE_INT_RGB);
            int width_i = 0;
            int height_i = 0;
            for (int i = 0; i < images.length; i++) {
                int width = images[i].getWidth();
                int height = images[i].getHeight();
                imageNew.setRGB(0, height_i, width, height, ImageArrays[i], 0, width);
                height_i += height;
            }
            
            File outFile = new File(mergePic);
            // 写图片，输出到硬盘
            ImageIO.write(imageNew, type, Files.newOutputStream(Paths.get(mergePic)));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public static void mergeVertical(List<File> list, String path,String formatName){
        try {
            //计算画布总宽
            int allWidth = 0;
            //计算画布总高
            int allHeight = 0;
            List<BufferedImage> imgs = new ArrayList<>();
            for(int i=0; i< list.size(); i++){
                imgs.add(ImageIO.read(list.get(i)));
                //因为是竖向合成，拿图片里最大的一个宽度就行
                allWidth = Math.max(allWidth,imgs.get(i).getWidth());
                allHeight += imgs.get(i).getHeight();
            }
            BufferedImage combined = new BufferedImage(allWidth, allHeight,BufferedImage.TYPE_INT_RGB);
            Graphics g = combined.getGraphics();
            //设置画布背景颜色 ，默认黑色
            g.setColor(Color.white);
            g.fillRect(0, 0, allWidth, allHeight);
            int height = 0;
            for (BufferedImage img : imgs) {
                g.drawImage(img, 0, height, null);
                //+10为了设置上下两个图片间距
                height += img.getHeight() + 10;
            }
            ImageOutputStream imageOutputStream = new FileImageOutputStream(new File(path));
            if (ImageIO.write(combined, formatName, imageOutputStream)) {
                imageOutputStream.close();
                System.out.println("===合成成功====");
            }else {
                System.out.println("===合成失败====");
            }
        } catch (Exception e) {
            System.out.println("===合成失败====");
            e.printStackTrace();
        }
    }
    
    /**
     * 压缩图片（通过降低图片质量）
     * @explain 压缩图片,通过压缩图片质量，保持原图大小
     * @param quality
     *       图片质量（0-1）
     * @return byte[]
     *      压缩后的图片（jpg）
     * @throws
     */
    public static byte[] compressPicByQuality(byte[] imgByte, float quality) {
        byte[] imgBytes = null;
        try {
            ByteArrayInputStream byteInput = new ByteArrayInputStream(imgByte);
            BufferedImage image = ImageIO.read(byteInput);
            
            // 如果图片空，返回空
            if (image == null) {
                return null;
            }
            // 得到指定Format图片的writer（迭代器）
            Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
            // 得到writer
            ImageWriter writer = (ImageWriter) iter.next();
            // 得到指定writer的输出参数设置(ImageWriteParam )
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            // 设置可否压缩
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            // 设置压缩质量参数
            iwp.setCompressionQuality(quality);
            
            iwp.setProgressiveMode(ImageWriteParam.MODE_DISABLED);
            
            ColorModel colorModel = ColorModel.getRGBdefault();
            // 指定压缩时使用的色彩模式
            iwp.setDestinationType(
                    new javax.imageio.ImageTypeSpecifier(colorModel, colorModel.createCompatibleSampleModel(16, 16)));
            
            // 开始打包图片，写入byte[]
            // 取得内存输出流
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            IIOImage iIamge = new IIOImage(image, null, null);
            
            // 此处因为ImageWriter中用来接收write信息的output要求必须是ImageOutput
            // 通过ImageIo中的静态方法，得到byteArrayOutputStream的ImageOutput
            writer.setOutput(ImageIO.createImageOutputStream(byteArrayOutputStream));
            writer.write(null, iIamge, iwp);
            imgBytes = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            System.out.println("write errro");
            e.printStackTrace();
        }
        return imgBytes;
    }
    public static void compressImage(String path, String outPath) {
        byte[] smallImage = null;
        
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            Thumbnails.of(path).size(ppi, ppi).outputFormat("png").toOutputStream(out);
            Thumbnails.of(path).scale(0.5).outputQuality(0.4).toFile(outPath);
//            smallImage = out.toByteArray();
//            return smallImage;
        } catch (IOException e) {
            log.error(e.getMessage());
//            throw new RuntimeException();
        }
    }
    
    public static void main(String[] args) throws Exception {
//        String saveFile = "/Users/mooye/Desktop/gift-670598709_2022_07_03_23_31_02.png";
//        String images[] = {"/Users/mooye/Desktop/gift-670598709_2022_07_03_23_31_02-0.png", "/Users/mooye/Desktop/gift-670598709_2022_07_03_23_31_02-1.png",
//                "/Users/mooye/Desktop/gift-670598709_2022_07_03_23_31_02-2.png"};
//        ImageUtil.merge(images, "png", saveFile);
//        File file = new File("/Users/mooye/Desktop/ceshi.jpeg");
//        byte[] fileByte = Files.readAllBytes(file.toPath());
//        fileByte = compressPicByQuality(fileByte,0.3f);
//        File out = new File("/Users/mooye/Desktop/ceshi-zip.jpeg");
//        assert fileByte != null;
//        Files.write(out.toPath(),fileByte);
        
        compressImage("/Users/mooye/Desktop/ceshi.jpeg","/Users/mooye/Desktop/ceshi-Thumbnails-1.jpeg");
    }
}

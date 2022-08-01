package top.mooyea.api.utils;


import org.springframework.util.FileCopyUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

public class CommonUtils {
    
    public static String getFileExtendName(byte[] photoByte) {
        String strFileExtendName = "JPG";
        if (photoByte[0] == 71 && photoByte[1] == 73 && photoByte[2] == 70 && photoByte[3] == 56 && (photoByte[4] == 55 || photoByte[4] == 57) && photoByte[5] == 97) {
            strFileExtendName = "GIF";
        } else if (photoByte[6] == 74 && photoByte[7] == 70 && photoByte[8] == 73 && photoByte[9] == 70) {
            strFileExtendName = "JPG";
        } else if (photoByte[0] == 66 && photoByte[1] == 77) {
            strFileExtendName = "BMP";
        } else if (photoByte[1] == 80 && photoByte[2] == 78 && photoByte[3] == 71) {
            strFileExtendName = "PNG";
        }
        
        return strFileExtendName;
    }
    
    public static String uploadOnlineImage(byte[] data, String basePath, String bizPath, String uploadType) {
        String dbPath = null;
        String fileName = "image" + Math.round(Math.random() * 100000000000L);
        fileName += "." + getFileExtendName(data);
        try {
            File file = new File(basePath + File.separator + bizPath + File.separator);
            if (!file.exists()) {
                file.mkdirs();// 创建文件根目录
            }
            String savePath = file.getPath() + File.separator + fileName;
            File saveFile = new File(savePath);
            FileCopyUtils.copy(data, saveFile);
            dbPath = bizPath + File.separator + fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dbPath;
    }
    
    /**
     * 判断文件名是否带盘符，重新处理
     *
     * @param fileName
     *
     * @return
     */
    public static String getFileName(String fileName) {
        //判断是否带有盘符信息
        // Check for Unix-style path
        int unixSep = fileName.lastIndexOf('/');
        // Check for Windows-style path
        int winSep = fileName.lastIndexOf('\\');
        // Cut off at latest possible point
        int pos = (winSep > unixSep ? winSep : unixSep);
        if (pos != -1) {
            // Any sort of path separator found...
            fileName = fileName.substring(pos + 1);
        }
        //替换上传文件名字的特殊字符
        fileName = fileName.replace("=", "").replace(",", "").replace("&", "");
        return fileName;
    }
}

package com.kennyzhu.micro.framework.util;

import java.io.File;

/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     FileUtil   
 *  * @package    com.kennyzhu.micro.framework.util  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/8 11:02  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
public class FileUtil {
    public static String stripPath(String fileName) {
        if (fileName == null) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(File.separatorChar)+1, fileName.length());
    }

    public static String stripExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        if (!fileName.contains(".")) {
            return fileName;
        }
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }
}

package com.ltm.encrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtility {

    public static void zipFolder(String sourceFolderPath, String zipFilePath) throws IOException {
        FileOutputStream fos = new FileOutputStream(zipFilePath);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(sourceFolderPath);
        System.out.println("fileToZip:" + fileToZip);
        zipFile(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
        fos.close();
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                System.out.println("filename has and /:" + fileName);
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                System.out.println("fileNamenot end /:" + fileName);
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                System.out.println("childFile: " + childFile.getName());
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
//        System.out.println("111");
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }
}
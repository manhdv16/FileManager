package com.ltm.data;

import java.io.File;
import java.util.ArrayList;

public class FileWorker {

    public static String URL_FOLDER = "C:\\Users\\LAPTOP24H\\Documents";

    public String[] getAllFileName() {
        File file = new File(URL_FOLDER);
        String[] files = file.list();
        return files;
    }

    public String[] searchFile(String keyword) {
        File folder = new File(URL_FOLDER);
        File[] files = folder.listFiles();
        ArrayList<String> fileSearches = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().contains(keyword)) {
                    fileSearches.add(file.getName());
                }
            }
        }
        String[] result = new String[fileSearches.size()];
        result = fileSearches.toArray(result);
        return result;
    }
    
    public boolean checkFile(String fileNameReceived) {
        // TODO Auto-generated method stub
        File file = new File(URL_FOLDER);
        String[] files = file.list();
        for (String file1 : files) {
            if (file1.equals(fileNameReceived)) {
                return false;
            }
        }
        return true;
    }
}

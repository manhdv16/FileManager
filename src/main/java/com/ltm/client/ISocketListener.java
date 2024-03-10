package com.ltm.client;

import com.ltm.data.DataFile;

public interface ISocketListener {

    void updateListFile(String[] listFile);

    void setProgress(int n);

    void showDialog(String str, String type);

    String chooserFileToSave(DataFile dataFile,String fileName);

}

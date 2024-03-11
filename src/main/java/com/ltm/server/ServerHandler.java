package com.ltm.server;

import com.ltm.client.ISocketListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.ltm.data.DataFile;
import com.ltm.data.FileWorker;
import com.ltm.data.SEND_TYPE;
import com.ltm.encrypt.EncryptionUtil;
import com.ltm.encrypt.ZipUtility;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.Timer;

public class ServerHandler extends Thread {

    private Socket socket;
    private boolean isStop = false;
    private Timer progressResetTimer;
    ISocketListener iSocketListener;
    InputStream is;
    OutputStream os;
    SEND_TYPE sendType = SEND_TYPE.DO_NOT_SEND;
    String message;
    String fileName;

    FileWorker fileWorker;
    private String fileNameReceived;
    private String folderNameReceived;
    DataFile m_dtf;
    Integer cnt =1;
    Map<Integer, DataFile> dataMap = new TreeMap<>();
    
    public ServerHandler(Socket socket) throws Exception {
        this.socket = socket;
        os = socket.getOutputStream();
        is = socket.getInputStream();

        fileWorker = new FileWorker();
        SendDataThread sendDataThread = new SendDataThread();
        sendDataThread.start();

        m_dtf = new DataFile();
        progressResetTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iSocketListener.setProgress(0);
                progressResetTimer.stop();
            }
        });
    }

    @Override
    public void run() {
        System.out.println("Processing: " + socket);
        // TODO Auto-generated method stub
        while (!isStop) {
            try {
                readData();
            } catch (Exception e) {
                connectClientFail();
                break;
            }
        }
        System.out.println("Complete processing: " + socket);
        closeSocket();
    }

    void readData() throws Exception {
        try {

            System.out.println("Recieving...");
            ObjectInputStream ois = new ObjectInputStream(is);
            Object obj = ois.readObject();
            if (obj instanceof String) {
                readString(obj);
            } else if (obj instanceof DataFile) {
                readFile(obj);
            }

        } catch (Exception e) {
            connectClientFail();
            closeSocket();
        }
    }

    public String readString(Object obj) throws IOException {
        String str = obj.toString();

        if (str.equals("STOP")) {
            isStop = true;
        } else if (str.equals("VIEW_ALL_FILE")) {
            String[] files = fileWorker.getAllFileName();
            String data = "ALL_FILE";
            for (String file : files) {
                data += "--" + file;
            }
            this.sendString(data);
        } else if (str.contains("SEARCH_FILE")) {
            String[] searches = str.split("--");

            String[] files = fileWorker.searchFile(searches[1]);
            String data = "ALL_FILE";
            for (String file : files) {
                data += "--" + file;
            }
            this.sendString(data);
        } else if (str.contains("DOWNLOAD_FILE")) {
            String[] array = str.split("--");
            sendFile(array[1]);
        } else if (str.contains("SEND_FOLDER")) {
            String[] folderInfor = str.split("--");
            folderNameReceived = folderInfor[1]; //foler.zip
            folderNameReceived = folderNameReceived.substring(0, folderNameReceived.length()-4);
            System.out.println("folderNameReceived: "+ folderNameReceived);
            File outputFolder = new File("C:\\Users\\LAPTOP24H\\Documents\\" + folderNameReceived);
            if (!outputFolder.exists()) {
                outputFolder.mkdirs(); // Tạo thư mục đầu ra nếu nó không tồn tại
            } else {
                this.sendString("ERROR--Folder đã tồn tại trên server");
            }
            dataMap = new HashMap<>();
            m_dtf.clear();
        } else if (str.contains("END_FILE_IN_FOLDER")) {
            if (!fileNameReceived.endsWith("\\")) {
                System.out.println("fileNameReceived(serverhandle): " + fileNameReceived);
                String[] listStr = fileNameReceived.split("/");
                fileNameReceived = listStr[listStr.length - 1];
                m_dtf.saveFile(FileWorker.URL_FOLDER + "\\" + folderNameReceived + "\\" + fileNameReceived);
                m_dtf.clear();
            }
        } else if (str.contains("END_FOLDER")) {
            ZipUtility.zipFolder(FileWorker.URL_FOLDER + "\\" + folderNameReceived, FileWorker.URL_FOLDER + "\\" + folderNameReceived + ".zip");
        } else if (str.contains("SEND_FILE")) {
            String[] fileInfor = str.split("--");
            fileNameReceived = fileInfor[1];
            m_dtf.clear();
            dataMap.clear();
            cnt=1;
            if (!fileWorker.checkFile(fileNameReceived)) {
                this.sendString("ERROR--File đã tồn tại trên server");
            }
        } else if (str.contains("END_FILE")) {
            m_dtf.appendDataMap(dataMap);
            m_dtf.saveFile(FileWorker.URL_FOLDER + "\\" + fileNameReceived);
            dataMap.clear();

        } else if (str.contains("END_ALL_FILE")) {
            this.sendString("UPLOAD_SUCCESSFULLY");
        }
        return str;
    }

    void readFile(Object obj) throws Exception {
        DataFile dtf = (DataFile) obj;
        byte[] decryptedData = EncryptionUtil.decrypt(dtf.data);
//        m_dtf.appendByte(decryptedData);
        int index = dtf.index;
        if(dataMap.containsKey(index)){
            DataFile tmp = dataMap.get(index);
            tmp.appendByte(decryptedData);
            dataMap.put(index, tmp);
        }
        else {
            DataFile tmp = new DataFile();
            tmp.appendByte(decryptedData);
            dataMap.put(index,tmp);
        }
        for (Map.Entry<Integer, DataFile> entry : dataMap.entrySet()) {
            Integer key = entry.getKey();
            System.out.println("key:" + key + " || " + "value:...");
        }
    }
    
    class SendDataThread extends Thread {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (!isStop) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                if (sendType != SEND_TYPE.DO_NOT_SEND) {
                    sendData();
                }
            }
        }
    }

    private void sendData() {
        // TODO Auto-generated method stub
        if (sendType == SEND_TYPE.SEND_STRING) {
            sendMessage(message);
        } else if (sendType == SEND_TYPE.SEND_FILE) {
            File source = new File(FileWorker.URL_FOLDER + "\\" + fileName);
            InputStream fin;
            try {
                fin = new FileInputStream(source);
                long lenghtOfFile = source.length();
                sendMessage("SEND_FILE" + "--" + fileName + "--" + lenghtOfFile);

                byte[] buf = new byte[1024];
                int byteRead;
                while ((byteRead = fin.read(buf)) != -1) {
                    byte[] buf1 = Arrays.copyOf(buf, byteRead);
                    DataFile dtf = new DataFile();
                    dtf.data = EncryptionUtil.encrypt(buf1);
                    sendMessage(dtf);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            sendMessage("END_FILE--" + fileName);
        }
        sendType = SEND_TYPE.DO_NOT_SEND;
    }

    void sendString(String str) {
        System.out.println("SENDING STRING	");
        sendType = SEND_TYPE.SEND_STRING;
        message = str;
    }

    void sendFile(String fileName) {
        System.out.println("SENDING FILE	");
        sendType = SEND_TYPE.SEND_FILE;
        this.fileName = fileName;

    }

    // void send Message
    public synchronized void sendMessage(Object obj) {  
        try {
            ObjectOutputStream oos = new ObjectOutputStream(os);
            // only send text
            if (obj instanceof String) {
                String message = obj.toString();
                oos.writeObject(message);
                oos.flush();
            } // send attach file
            else if (obj instanceof DataFile) {
                oos.writeObject(obj);
                oos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connectClientFail() {
        isStop = true;
        closeSocket();
    }

    private void closeSocket() {
        isStop = true;
        try {
            this.sendString("STOP");
            if (os != null) {
                os.close();
            }
            if (is != null) {
                is.close();
            }
            if (socket != null) {
                socket.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

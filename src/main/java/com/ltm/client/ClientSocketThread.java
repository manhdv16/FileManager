package com.ltm.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import com.ltm.data.DataFile;
import com.ltm.data.SEND_TYPE;
import com.ltm.encrypt.EncryptionUtil;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.Timer;

public class ClientSocketThread extends Thread {

    private Socket socket;
    private boolean isStop = false;
    private Timer progressResetTimer;
    private boolean sendFileSuccess = true;
    // Receive
    InputStream is;
    ISocketListener iSocketListener;
    long lengFileReceive;
    long currentFileReceive;
    // Send
    OutputStream os;
    SEND_TYPE sendType = SEND_TYPE.DO_NOT_SEND;
    String message;
    List<String> listFileNames;

    private String fileNameReceived;

    DataFile m_dtf;

    ExecutorService executor = Executors.newFixedThreadPool(5);

    public ClientSocketThread(ISocketListener iSocketListener) throws Exception {
        this.iSocketListener = iSocketListener;
        m_dtf = new DataFile();
        progressResetTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iSocketListener.setProgress(0);
                progressResetTimer.stop();
            }
        });
    }

    public void setSocket(String serverIp, int port) {
        try {

            socket = new Socket(serverIp, port);
            // Connect to server
            System.out.println("Connected: " + socket);

            os = socket.getOutputStream();
            is = socket.getInputStream();

            iSocketListener.showDialog("CONNECTED TO SERVER", "INFOR");
            SendDataThread sendDataThread = new SendDataThread();
            sendDataThread.start();
        } catch (Exception e) {
            iSocketListener.showDialog("Can't connect to Server", "ERROR");
        }
    }

    @Override
    public void run() {
        while (!isStop) {
            try {
                readData();
            } catch (Exception e) {
                connectServerFail();
                e.printStackTrace();
                break;
            }
        }
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
            e.printStackTrace();
            connectServerFail();
            closeSocket();
        }
    }

    void readString(Object obj) throws Exception {
        String str = obj.toString();
        if (str.equals("STOP")) {
            isStop = true;
        } else if (str.contains("SEND_FILE")) {
            String[] fileInfor = str.split("--");
            fileNameReceived = fileInfor[1];
            lengFileReceive = Long.parseLong(fileInfor[2]);
            currentFileReceive = 0;
            m_dtf.clear();
        } else if (str.contains("END_FILE")) {
            String[] liststrEndFile = str.split("--");
            iSocketListener.chooserFileToSave(m_dtf, liststrEndFile[1]);
        } else if (str.contains("ALL_FILE")) {
            String[] listFile = str.split("--");
            String[] yourArray = Arrays.copyOfRange(listFile, 1, listFile.length);
            iSocketListener.updateListFile(yourArray);
        } else if (str.contains("ERROR")) {
            String[] list = str.split("--");
            sendFileSuccess = false;
            iSocketListener.showDialog(list[1], "ERROR");
        } else if (str.equals("UPLOAD_SUCCESSFULLY")) {
            iSocketListener.showDialog("Uploaded successfully", "INFOR");

        }
    }

    void readFile(Object obj) throws Exception {
        DataFile dtf = (DataFile) obj;
        byte[] decryptedData = EncryptionUtil.decrypt(dtf.data);
        m_dtf.appendByte(decryptedData);
        currentFileReceive += 1024;
        if (currentFileReceive >= lengFileReceive) {
            iSocketListener.setProgress(100);
            progressResetTimer.start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            iSocketListener.setProgress((int) (currentFileReceive * 100 / lengFileReceive));
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
                    // TODO Auto-generated catch block
                    e.printStackTrace();
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
            int i = 0;
            for (String pathFile : listFileNames) {
                int index = ++i;
//                executor.submit(() -> {
                    fileToSend(pathFile);
//                });
            }
            executor.shutdown();
            
            if (sendFileSuccess == true) {
                sendMessage("END_ALL_FILE");
            }
        }
        sendType = SEND_TYPE.DO_NOT_SEND;
    }

    void sendString(String str) {
        System.out.println("SENDING STRING	" + str);
        sendType = SEND_TYPE.SEND_STRING;
        message = str;
    }

    void sendFile(List<String> listFilePaths) {
        System.out.println("SENDING FILE	");
        sendType = SEND_TYPE.SEND_FILE;
        System.out.println("filename:" + listFilePaths);
        this.listFileNames = listFilePaths;
    }
    
    public static long totals = 0;
    public static long lengthFile=0;
    
    private void fileToSend(String pathFile) {
        sendFileSuccess = true;
        long lenghtOfFile;
        byte[] buf = new byte[1024];
        long total = 0;
        int len;
        File source;
        if (pathFile.contains(".zip--")) {
            String[] fileInfor = pathFile.split("--");
            pathFile = fileInfor[0];
            lenghtOfFile = Integer.parseInt(fileInfor[1]);
            source = new File(pathFile);
        } else {
            source = new File(pathFile);
            lenghtOfFile = source.length();
            lengthFile = lenghtOfFile;
        }

        try {
            if (pathFile.contains(".zip")) {
                sendMessage("SEND_FOLDER" + "--" + source.getName());
                if (sendFileSuccess == true) {
                    ZipInputStream zis = new ZipInputStream(new FileInputStream(source));
                    ZipEntry zipEntry;
                    while ((zipEntry = zis.getNextEntry()) != null) {
                        // Lấy tên tệp tin trong ZIP
                        String entryName = zipEntry.getName();
                        sendMessage("SEND_FILE" + "--" + entryName);
                        // Đọc dữ liệu từ zipEntry và gửi lên máy chủ
                        while ((len = zis.read(buf)) != -1) {
                            total += len;
                            DataFile dtf = new DataFile();
                            byte[] buf1 = Arrays.copyOf(buf, len);
                            dtf.data = EncryptionUtil.encrypt(buf1);
                            sendMessage(dtf);
                            iSocketListener.setProgress((int) (total * 100 / lenghtOfFile));
                        }
                        zis.closeEntry();// đóng entry hiện tại
                        sendMessage("END_FILE_IN_FOLDER");
                    }
                    sendMessage("END_FOLDER");
                    if (total >= lenghtOfFile) {
                        progressResetTimer.start();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
            else {
                
                InputStream fin = new FileInputStream(source);
                sendMessage("SEND_FILE" + "--" + source.getName());
                long chuckSize = lenghtOfFile/5;
                CountDownLatch latch = new CountDownLatch(5);
                for(int i =0;i<5;i++) {
                    long start = i*chuckSize;
                    long end = (i == 4) ? lenghtOfFile-1 : start+chuckSize-1;
                    int index = i;
                    try {
                        sendFileChuck(source, start, end, index);
                    } finally {
                        latch.countDown(); // Giảm số lần đếm sau mỗi lần luồng hoàn thành
                    }
                }
                try {
                    latch.await(); // Chờ cho tới khi latch đếm về 0
                    sendMessage("END_FILE");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (sendFileSuccess == true) {
//                    while ((len = fin.read(buf)) != -1) {
//                        total += len;
//                        DataFile dtf = new DataFile();
//                        byte[] buf1 = Arrays.copyOf(buf, len);
//                        dtf.data = EncryptionUtil.encrypt(buf1);
//                        sendMessage(dtf);
//                        iSocketListener.setProgress((int) (total * 100 / lenghtOfFile));
//                    }
                    fin.close();
//                    if (total >= lenghtOfFile) {
//                        progressResetTimer.start();
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
//                    }
//                    sendMessage("END_FILE");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendFileChuck(File source,long start,long end,int index) {
       
        try (InputStream inputStream = new FileInputStream(source)) {
            inputStream.skip(start);

            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer, 0, bufferSize)) != -1 && start <= end) {
                DataFile dataFile = new DataFile();
                dataFile.index = index;
                dataFile.data = EncryptionUtil.encrypt(Arrays.copyOf(buffer, bytesRead));
                sendMessage(dataFile);
                start += bytesRead;
                totals+= bytesRead;
                iSocketListener.setProgress((int) (totals * 100 / lengthFile));
            }
            if (totals >= lengthFile) {
                progressResetTimer.start();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                totals = 0;lengthFile=0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void connectServerFail() {
        iSocketListener.showDialog("Can't connect to Server", "ERROR");
        isStop = true;
        closeSocket();
    }

    public void closeSocket() {
        isStop = true;
        try {
            this.sendString("STOP");

            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

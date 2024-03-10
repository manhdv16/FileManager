package com.ltm.data;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
//Tắt cảnh báo của trình biên dịch do không có trường serialVersionUID trong Serializable.
@SuppressWarnings("serial")
public class DataFile implements Serializable {

	public byte[] data;
	public int size;
        public int index;

	public void clear() {
		data = new byte[0];
		size = 0;
	}

	public DataFile() {
		data = new byte[0];
		size = 0;
	}

	public DataFile(String fileName) throws IOException {
		File file = new File(fileName);
		data = Files.readAllBytes(Paths.get(fileName));
		System.out.println(data);
	}
        
	@Override
	public String toString() {
		return size + " : " + data.toString();
	}

	public void appendByte(byte[] array) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			baos.write(data);
			baos.write(array);
			data = baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
        public void appendDataMap(Map<Integer, DataFile> dataMap){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for(Map.Entry<Integer, DataFile> entry: dataMap.entrySet()){
                try {
                    DataFile tmp = entry.getValue();
                    baos.write(tmp.data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            data = baos.toByteArray();
        }

	public void saveFile(String fileToReceived) {
		Path path = Paths.get(fileToReceived);
		try {
                    System.out.println("fileToReceived(datafile): "+ fileToReceived);
			Files.write(path, data);
                        System.out.println("success");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
        
}
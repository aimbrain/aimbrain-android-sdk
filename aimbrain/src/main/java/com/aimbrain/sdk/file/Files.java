package com.aimbrain.sdk.file;

import java.io.FileInputStream;
import java.io.IOException;

public class Files {

    public static final String TMP_VIDEO_FILE_NAME = "AMBN_FACE_CAPTURE_VIDEO.MOV";

    public static byte[] readAllBytes(FileInputStream fileInputStream) throws IOException {
        int size = (int) fileInputStream.getChannel().size();
        byte bytes[] = new byte[size];
        byte tmpBuff[] = new byte[size];
        try {
            int read = fileInputStream.read(bytes, 0, size);
            if (read < size) {
                int remain = size - read;
                while (remain > 0) {
                    read = fileInputStream.read(tmpBuff, 0, remain);
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
                    remain -= read;
                }
            }
        }  catch (IOException e){
            throw e;
        } finally {
            fileInputStream.close();
        }

        return bytes;
    }

}

package com.aimbrain.sdk.voiceCapture.helpers;

import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 */
public class WaveFileHelper {

    public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    public static final int AUDIO_FREQUENCY = 22050;
    public static final int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    public static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int RECORDER_BPP = 16;
    public static final int CHANNELS_MONO = 1;
    public static final int CHANNELS_STEREO = 2;

    public static byte[] getAudioBytesWithWaveHeaders(byte[] source) throws IOException {

        ByteArrayOutputStream outByte = new ByteArrayOutputStream();
        writeHeader(outByte, source.length);
        outByte.write(source);
        return outByte.toByteArray();

    }

    private static void writeHeader(
            ByteArrayOutputStream out, long totalAudioLen) throws IOException
    {
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = AUDIO_FREQUENCY;
        int channels = CHANNELS_MONO; //mono
        long byteRate = RECORDER_BPP * AUDIO_FREQUENCY * channels/8;

        Log.d("MainActivity", "File size: " + totalDataLen);
        Log.d("MainActivity", "totalAudioLenght" + totalAudioLen);

        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1 what is the audio format? 1 for PCM = Pulse Code Modulation
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (channels * 16 / 8);  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }
}

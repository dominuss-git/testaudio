package com.example.myapplication;

import android.media.*;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.*;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup
    static {
        System.loadLibrary("native-lib");
    }
    public native short[] helloWorld(short[] buffer);
    static {
        System.loadLibrary("ndkhello");
    }

    private final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private final int audioChannel = AudioFormat.CHANNEL_IN_MONO;
    private final String fileName = "recorder.pcm";
    private final int[] rates = { 96000 , 48000, 44100, 32000, 22050, 16000, 11025, 8000, 4800 };
    private int sampleRateInHz = rates[0];
    private int minBufferSize;

    TextView textView;
    AudioRecord audioRecord;
    AudioTrack audioTrack;
    boolean isReading = false;
    boolean isRecording = false;
    boolean isTest = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.minBufferSize = AudioRecord.getMinBufferSize(
                this.sampleRateInHz, this.audioChannel, this.audioFormat) * 4;

        this.audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, this.sampleRateInHz,
               this.audioChannel , this.audioFormat, minBufferSize);
//        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, this.sampleRateInHz, AudioFormat.CHANNEL_OUT_MONO, this.audioFormat, )
        textView = (TextView) findViewById(R.id.text);

        ImageButton startRecord = (ImageButton) findViewById(R.id.btnRecord);
        ImageButton stopPlay = (ImageButton) findViewById(R.id.btnStop);
        ImageButton startPlay = (ImageButton) findViewById(R.id.btnPlay);

        startRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isTest == false) {
                    Testing();
                } else {
                    Thread recordThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            StartRecord();
                        }
                    });
                    recordThread.start();
                }

            }
        });


        stopPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isReading == true) {
                    isReading = false;
                } else {
                    isRecording = false;
                }
            }
        });

        startPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playRecord();
            }
        });


    }

    private void StartRecord() {
        if( !PermissionAdding.isAudioRecordPermissionGranted(this)) {
            return;
        }

        try {
            File fullPath = getFileStreamPath(fileName);
            OutputStream outputStream = openFileOutput(fileName, MODE_PRIVATE);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);

            if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED){
                audioRecord.startRecording();
                isRecording = true;
                short[] audioData = new short[minBufferSize];
                while(isRecording) {
                    int countOfOutput = audioRecord.read(audioData, 0, minBufferSize);
                    for (int i = 0; i < countOfOutput; i++) {
                        dataOutputStream.writeShort(audioData[i]);
                    }
                }
            } else {
                textView.setText("Audio recording is not supported");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            isRecording = false;
        }
    }

    void playRecord(){
        final File file = getFileStreamPath(fileName);
        final long filelen = file.length();
        if( filelen == 0 ){
            textView.setText("audio file is empty");
            return;
        }

        int shortSizeInBytes = Short.SIZE/Byte.SIZE;
        final int bufferSizeInBytes = (int)(filelen / shortSizeInBytes);

//        final Thread recordThread = new Thread(new Runnable(){
            short[] audioData = new short[bufferSizeInBytes];
//
//            @Override
//            public void run() {

                try {
                    InputStream inputStream = new FileInputStream(file);
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                    DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);
                    int i = 0;
                    while(dataInputStream.available() > 0){
                        audioData[i] = dataInputStream.readShort();
                        i++;
                    }
                    dataInputStream.close();
                    audioData = helloWorld(audioData);

                    AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                            sampleRateInHz, AudioFormat.CHANNEL_OUT_MONO, audioFormat,
                            bufferSizeInBytes, AudioTrack.MODE_STREAM);
//                    isReading = true;
                    audioTrack.play();
                    audioTrack.write(audioData, 0, bufferSizeInBytes);
//                    while(true) {
//                        Thread.sleep(100);
//                        if(isReading == false) {
//                            audioTrack.flush();
//                            audioTrack.stop();
//                            audioTrack.release();
//                            break;
//                        }
//                    }
                } catch (IOException /*| InterruptedException*/ e) {
                    e.printStackTrace();
                }
//            }
//        });
//        recordThread.start();
    }

    private void Testing() {
        if( !PermissionAdding.isAudioRecordPermissionGranted(this)) {
            textView.setText(textView.getText() + "\nMic permission is not granted !!!");
            return;
        } else {
            textView.setText("Mic permission is granted !!!");
//            textView.setText("Testing ...");
//            return;
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {

            this.audioRecord.startRecording();
            isRecording = true;
            short[] audioData = new short[minBufferSize];
            int i = 1;
            while(isRecording) {
                int countOfOutput = audioRecord.read(audioData, 0, minBufferSize);
                if (countOfOutput == 0) {
                    this.sampleRateInHz = rates[i];
                    this.minBufferSize = AudioRecord.getMinBufferSize(
                            this.sampleRateInHz, this.audioChannel, this.audioFormat) * 4;
                    i++;
                } else if (i > 8) {
                    isRecording = false;
                    textView.setText(("Audio recording is not supported"));
                }
                else {
                    isRecording = false;
                    isTest = true;
                    textView.setText("Test Complite");
                }
            }
        } else {
            this.audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, this.sampleRateInHz,
                    this.audioChannel , this.audioFormat, minBufferSize);
            textView.setText("Audio recording is not supported");
            return;
        }
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();



}

package com.example.batterytest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button startButton;
    private Button stopButton;
    private TextView textView;

    private Handler handler;
    private Runnable recordBatteryDataRunnable;

    private boolean isRecording = false;
    private String csvFilePath;
    private PrintWriter csvWriter;
    long startTimeMillis = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        textView = findViewById(R.id.textView);

        handler = new Handler();
        recordBatteryDataRunnable = new Runnable() {
            @Override
            public void run() {
                recordBatteryData();
                handler.postDelayed(this, 1000); // 每秒记录一次
            }
        };

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });
    }






    private void startRecording() {
        if (isRecording) {
            Toast.makeText(this, "已经在记录中", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建CSV文件
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        csvFilePath = getExternalFilesDir(null) + "/battery_data_" + timeStamp + ".csv";
        try {
            csvWriter = new PrintWriter(new FileWriter(csvFilePath));
            csvWriter.println("Time,Current,Voltage,Level,diffTime");
            csvWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "创建CSV文件失败", Toast.LENGTH_SHORT).show();
            return;
        }

        isRecording = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        handler.post(recordBatteryDataRunnable);
    }

    private void stopRecording() {
        if (!isRecording) {
            Toast.makeText(this, "尚未开始记录", Toast.LENGTH_SHORT).show();
            return;
        }

        isRecording = false;
        startButton.setEnabled(true);
        stopButton.setEnabled(false);


        handler.removeCallbacks(recordBatteryDataRunnable);
        csvWriter.close();

        Toast.makeText(this, "数据记录完成，保存路径：" + csvFilePath, Toast.LENGTH_LONG).show();
    }



    private void recordBatteryData() {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatusIntent = registerReceiver(null, intentFilter);
        BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);

        // 获取当前时间戳
        long currentTimeMillis = System.currentTimeMillis();

        // 计算与起始时间差
        long endTime = currentTimeMillis - startTimeMillis;

        // 定义时间格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        // 将时间戳转换为日期对象
        Date date = new Date(currentTimeMillis);

        // 格式化日期
        String formattedTime = sdf.format(date);

        // 格式化时间差并显示在界面上
        String diffTime = formatElapsedTime(endTime);
        textView.setText(diffTime);

        if (batteryStatusIntent != null) {
            long current = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
            int voltage = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
            int level = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            csvWriter.println(formattedTime + "," + current + "," + voltage + "," + level + "," + diffTime);
            csvWriter.flush();
        }

    }

    @SuppressLint("DefaultLocale")
    private String formatElapsedTime(long elapsedTimeMillis) {
        long seconds = elapsedTimeMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        // 格式化时间差并返回
        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }

}
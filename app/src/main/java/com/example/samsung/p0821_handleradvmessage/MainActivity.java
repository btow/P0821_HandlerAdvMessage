package com.example.samsung.p0821_handleradvmessage;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    final String LOG_TAG = "myLogs";

    final int STATUS_NONE           = 0,    //Отсутвие подключения
              STATUS_CONNECTING     = 1,    //Производится подключение
              STATUS_CONNECTED      = 2,    //Подключение установлено
              STATUS_DOWNLOAD_START = 3,    //Загрузка началась
              STATUS_DOWNLOAD_FILE  = 4,    //Файл загружен
              STATUS_DOWNLOAD_END   = 5,    //Загрузка закончена
              STATUS_DOWNLOAD_NONE  = 6;    //Загрузка остановлена

    Handler handler;

    TextView tvStatus;
    ProgressBar pbDownload;
    Button btnConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = (TextView) findViewById(R.id.tvStatus);
        pbDownload = (ProgressBar) findViewById(R.id.pbDownload);
        btnConnect = (Button) findViewById(R.id.btnConnect);

        handler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {

                switch (msg.what) {

                    case STATUS_NONE :
                        btnConnect.setEnabled(true);
                        tvStatus.setText("Not connected");
                        pbDownload.setVisibility(View.GONE);
                        break;
                    case STATUS_CONNECTING :
                        btnConnect.setEnabled(false);
                        tvStatus.setText("Connecting");
                        break;
                    case STATUS_CONNECTED :
                        tvStatus.setText("Connected");
                        break;
                    case STATUS_DOWNLOAD_START :
                        tvStatus.setText("Start the download " + msg.arg1 + " files");
                        pbDownload.setMax(msg.arg1);
                        pbDownload.setProgress(0);
                        pbDownload.setVisibility(View.VISIBLE);
                        break;
                    case STATUS_DOWNLOAD_FILE :
                        tvStatus.setText("Downloading. Left " + msg.arg2 + " files.");
                        pbDownload.setProgress(msg.arg1);
                        saveFile((byte[]) msg.obj);
                        break;
                    case STATUS_DOWNLOAD_END :
                        tvStatus.setText("Download complete!");
                        break;
                    case STATUS_DOWNLOAD_NONE :
                        tvStatus.setText("No files for download.");
                        break;

                }

            }

        };
        handler.sendEmptyMessage(STATUS_NONE);
    }

    private void saveFile(byte[] file) {

    }

    public void onClickButton(View view) {

        Thread thread = new Thread(new Runnable() {

            Message msg;
            byte[] file;
            Random random = new Random();

            @Override
            public void run() {

                try {
                    //Эмуляция процесса установки подключения
                    handler.sendEmptyMessage(STATUS_CONNECTING);
                    TimeUnit.SECONDS.sleep(1);
                    //Эмуляция выполненного подключения
                    handler.sendEmptyMessage(STATUS_CONNECTED);
                    //Эмуляция определения количества файлов для загрузки
                    TimeUnit.SECONDS.sleep(1);
                    int filesCount = random.nextInt(5);

                    if (filesCount == 0) {
                        //Сообщение об отсутсвии фалов для загрузки
                        handler.sendEmptyMessage(STATUS_DOWNLOAD_NONE);
                        //Эмуляция разрыва подключения
                        TimeUnit.MILLISECONDS.sleep(1500);
                        handler.sendEmptyMessage(STATUS_NONE);
                        return;

                    } else {
                        /**Эмуляция загрузки файлов.
                         * Создание сообщения с информацией о количестве файлов
                         */
                        msg = handler.obtainMessage(STATUS_DOWNLOAD_START, filesCount, 0);
                        //Отправка созданного сообщения
                        handler.sendMessage(msg);

                        for (int i = 1; i <= filesCount; i++) {
                            //Эмуляция загрузки файла
                            file = downloadFile();
                            /**Создание сообщения с инфрмацией
                             * - о порядковом номере файла;
                             * - о количестве ещё оставшихся файлов;
                             * - о самом загруженном файле.
                             */
                            msg = handler.obtainMessage(STATUS_DOWNLOAD_FILE, i, filesCount - i, file);
                            //Отправка созданного сообщения
                            handler.sendMessage(msg);

                        }
                        //Сообщение о завершении загрузки
                        handler.sendEmptyMessage(STATUS_DOWNLOAD_END);
                        //Эмуляция разрыва подключения
                        TimeUnit.MILLISECONDS.sleep(1500);
                        handler.sendEmptyMessage(STATUS_NONE);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();
    }

    private byte[] downloadFile() throws InterruptedException {
        TimeUnit.SECONDS.sleep(2);
        return new byte[1024];
    }
}

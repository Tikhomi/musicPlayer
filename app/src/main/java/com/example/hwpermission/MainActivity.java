package com.example.hwpermission;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // поля
    private ActivityResultLauncher<String[]> storagePermissionLauncher; // поле результата активности параметризованного массивом разрешений
    private final String[] PERMISSION = new String[]{
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private final String DATA_SD = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + "/Н.А.Римский-Корсаков - Полёт шмеля.mp3"; // путь к аудио-файлу
    private FloatingActionButton fabPlayPause, fabBack, fabForward; // кнопки управления воспроизведением
    private MediaPlayer mediaPlayer; // поле медиа-плеера

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // привязка полей к разметке
        fabPlayPause = findViewById(R.id.fab_play_pause);
        fabBack = findViewById(R.id.fab_back);
        fabForward = findViewById(R.id.fab_forward);

        // метод регистрации разрешения
        registerPermission();
        // метод проверки наличия разрешения
        checkPermission();
        // считывание аудио-файла с SD-карты
        readTrackExternalStorage();

        // обработка нажатия кнопок
        fabPlayPause.setOnClickListener(listener);
        fabBack.setOnClickListener(listener);
        fabForward.setOnClickListener(listener);
    }

    // регистрация разрешения
    private void registerPermission() {
        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                result -> result.forEach(
                        (permission, granted) -> {
                            // вывод информации о состоянии регистрации
                            if (Objects.equals(permission, Manifest.permission.READ_EXTERNAL_STORAGE)) { // для разрешения чтения SD карты
                                if (granted) {
                                    Toast.makeText(this, "Разрешение доступа к аудиофайлам дано", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Доступ к аудиофайлам запрещён", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }));
    }

    private void checkPermission() {
        // Проверка разрешений
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Дано разрешение на чтение и запись аудио-файлов на SD-карту", Toast.LENGTH_SHORT).show();
        } else { // Запрос массива разрешений
            storagePermissionLauncher.launch(PERMISSION);
        }
    }

    // метод считывания аудио-файла из SD карты
    private void readTrackExternalStorage() {
        mediaPlayer = new MediaPlayer(); // создание объекта медиа-плеера
        try {
            mediaPlayer.setDataSource(DATA_SD); // указание источника аудио
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); // подключение аудио-менеджера
            mediaPlayer.prepare(); // ассинхронная подготовка плеера к проигрыванию
        } catch (IOException exception) {
            Toast.makeText(this, "Запрашиваемого аудио-файла на SD-карте не нашлось", Toast.LENGTH_SHORT).show();
        }
    }

    // создадим один слушатель на все кнопки
    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.fab_play_pause) {
                // код старта и паузы
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    // назначение кнопке картинки паузы
                    fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_pause));
                    mediaPlayer.start();
                } else if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    // назначение кнопке картинки воспроизведения
                    fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
                    mediaPlayer.pause();
                }
            } else if (view.getId() == R.id.fab_back) {
                // код перемотки назад
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000); // перемотка назад на 5 секунд
                }
            } else if (view.getId() == R.id.fab_forward) {
                // код перемотки вперёд
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 5000); // перемотка вперёд на 5 секунд
                }
            }
        }
    };

    // метод очистки занятой аудио-плеером памяти
    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release(); // очистка памяти
            mediaPlayer = null; // обнуление объекта аудио-плеера
        }
    }

    @Override
    protected void onDestroy() {
        releasePlayer(); // очистка памяти прошлого воспроизведения
        super.onDestroy();
    }
}
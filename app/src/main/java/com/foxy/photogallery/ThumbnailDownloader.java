package com.foxy.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;  // для идентификации сообщений как запросов на загрузку

    private boolean hasQuit = false;
    private Handler requestHandler;  // постановка в очередь запросов на загрузку в фоновом потоке
    private ConcurrentMap<T, String> requestMap = new ConcurrentHashMap<>();
    private Handler responseHandler;  // для хранения handler переданного из главного потока
    private ThumbnailDownloadListener<T> thumbnailDownloadListener;

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        this.responseHandler = responseHandler;
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
        thumbnailDownloadListener = listener;
    }

    @Override
    protected void onLooperPrepared() {
        requestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {  // метод вызывается, когда сообщение извлечено из очереди и готово к обработке
                if (msg.what == MESSAGE_DOWNLOAD) {   // проверяем тип сообщения
                    T target = (T) msg.obj;           // читаем значение obj и передаем его handlerRequest()
                    Log.i(TAG, "Got a request for URL: " + requestMap.get(target));
                    handlerRequest(target);
                }
            }
        };
    }

    @Override
    public boolean quit() {
        hasQuit = true;
        return super.quit();
    }

    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "Got a URL: " + url);

        if (url == null) {
            requestMap.remove(target);
        } else {
            requestMap.put(target, url);
            requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)    // запрос на загрузку
                    .sendToTarget();
        }
    }

    // удаление всех запросов из очереди
    public void clearQueue() {
        requestHandler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();
    }

    private void handlerRequest(final T target) {
        try {
            final String url = requestMap.get(target);

            if (url == null) {  // проверяем существование адреса
                return;
            }

            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);         // получаем массив байтов
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);  // из полученного массива строим растровое изображение
            Log.i(TAG, "Bitmap created");

            responseHandler.post(new Runnable() {
                @Override
                public void run() {
                    // проверка гарантирует, что каждый PhotoHolder получит правильное изображение
                    if (requestMap.get(target) != url || hasQuit) {
                        return;
                    }
                    // удаляем связь PhotoHolder-URL и назначаем картинку для PhotoHolder
                    requestMap.remove(target);
                    thumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
                }
            });

        } catch (IOException e) {
            Log.e(TAG, "Error downloading image", e);
        }
    }

    public interface ThumbnailDownloadListener<T> {
        // вызывается, когда картинка полностью загружена и готова к добавлению
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }


}

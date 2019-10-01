package com.foxy.photogallery;

import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FlickrFletchr {

    private static final String TAG = "FlickrFletchr";
    private static final String API_KEY = "3d94bfd04d25d8f1e269fbf13e2a04b3";

    public byte[] getUrlBytes(String urlSspec) throws IOException {
        // Создает объект url на базе строки
        URL url = new URL(urlSspec);

        // Создает подключение к заданному url. UrlConnection преобразуем в HttpURLConnection, что
        // открывает доступ к Http-интерфейсам.
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // Устанавливаем связь с конечной точкой
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSspec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];

            // Вызываем reed пока в подключении не кончатся данные. Когда чтение будет завершено,
            // возвращается массив из байтов
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    // Преобразует полученные байты в String
    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public void fetchItems() {
        try {

            // Используем Uri.Builder для построения полного URL-адреса
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .build().toString();

            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
        } catch (IOException e) {
            Log.e(TAG, "Failed to fetch items ", e);
        }

    }
}

package com.foxy.photogallery;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FlickrFletchr {

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
}

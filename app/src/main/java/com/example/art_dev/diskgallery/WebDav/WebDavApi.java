package com.example.art_dev.diskgallery.WebDav;

import android.graphics.drawable.Drawable;
import android.util.Pair;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
//класс для работы с WebDav api
public class WebDavApi {
    public static final String DEFAULT_URL = "https://webdav.yandex.ru";

    public static final String GET = "GET";


    public static final Pair<String, String> OAUTH = new Pair("Authorization", "OAuth ");
    public static final Pair<String, String> USER_AGENT = new Pair("User-Agent", "gallery/0.0.1");
    public static final Pair<String, String> HOST = new Pair("Host", "webdav.yandex.ru");


    public static final String PREVIEW = "?preview&size=";
    public static final String PREVIEW_XXXS = "?preview&size=XXXS";
    public static final String PREVIEW_XXS = "?preview&size=XXS";
    public static final String PREVIEW_XS = "?preview&size=XS";
    public static final String PREVIEW_S = "?preview&size=S";
    public static final String PREVIEW_M = "?preview&size=M";
    public static final String PREVIEW_L = "?preview&size=L";
    public static final String PREVIEW_XL = "?preview&size=XL";
    public static final String PREVIEW_XXL = "?preview&size=XXL";


    private HttpURLConnection connection = null;
    private String urlResponse = null;
    URL url;

//получить превью
    public Drawable getImagePreview(String token, String path) throws IOException {
        urlResponse = DEFAULT_URL + path + PREVIEW_S;
        url = new URL(urlResponse);
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(GET);
        connection.addRequestProperty(USER_AGENT.first, USER_AGENT.second);
        connection.addRequestProperty(HOST.first, HOST.second);
        connection.addRequestProperty(OAUTH.first, OAUTH.second + token);
        connection.connect();
        String str = connection.getResponseMessage();
        if (!str.equals("OK")) {
            //ошибка запроса
            return null;
        }
        Drawable drawable = Drawable.createFromStream(connection.getInputStream(), "someImage");
        connection.disconnect();
        return drawable;
    }
}

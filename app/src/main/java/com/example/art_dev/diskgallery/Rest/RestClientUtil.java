package com.example.art_dev.diskgallery.Rest;


import com.squareup.okhttp.OkHttpClient;
import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.OkHttpClientFactory;
import com.yandex.disk.rest.RestClient;

//класс для работы с RestApi
public class RestClientUtil {

    public static RestClient getInstance(final Credentials credentials) {
        OkHttpClient client = OkHttpClientFactory.makeClient();
        return new RestClient(credentials, client);
    }
}

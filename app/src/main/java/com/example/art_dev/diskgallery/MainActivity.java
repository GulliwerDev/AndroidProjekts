package com.example.art_dev.diskgallery;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.example.art_dev.diskgallery.Rest.RestClientUtil;
import com.example.art_dev.diskgallery.WebDav.WebDavApi;
import com.yandex.authsdk.YandexAuthException;
import com.yandex.authsdk.YandexAuthOptions;
import com.yandex.authsdk.YandexAuthSdk;
import com.yandex.authsdk.YandexAuthToken;
import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.ResourcesArgs;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.json.Resource;
import com.yandex.disk.rest.json.ResourceList;

import java.io.IOException;
//стартовая активность
public class MainActivity extends AppCompatActivity {

    public static final String TOKEN = "token";
    public static final String USERNAME = "username";
    public static final String MEDIA_TYPE = "image";

    public static final int REQUEST_CODE_YA_LOGIN = 1;

    private YandexAuthSdk sdk;
    private String mToken = null;
    private DiskInfo mDiskInfo;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDiskInfo = DiskInfo.getContext();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        //проверяем нет ли уже токена
        mToken = sp.getString(TOKEN, null);
        updateContent();
        if (mToken == null) {
            //токена нет, логинимся на яндексе
            startLogin();
        } else if (!mDiskInfo.isImagesLoaded()) {
            //токен есть, грузим картинки с диска
            loadImagesFromDisk();
        }


    }

    private void updateContent() {
        //функция, вызывающая заполнение RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.images_recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ImageRecyclerViewAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void startLogin() {
        sdk = new YandexAuthSdk(this, new YandexAuthOptions(this, true));
        startActivityForResult(sdk.createLoginIntent(this, null), REQUEST_CODE_YA_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_YA_LOGIN) {
            try {
                final YandexAuthToken yandexAuthToken = sdk.extractToken(resultCode, data);
                if (yandexAuthToken != null) {
                    saveToken(yandexAuthToken.getValue());
                    loadImagesFromDisk();
                }
            } catch (YandexAuthException e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.AuthError)
                        .setMessage(e.getMessage())
                        .setCancelable(false)
                        .setNegativeButton("ОК",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void saveToken(String token) {
        //сохраняем токен в SP
        mToken = token;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(USERNAME, "myApp");
        editor.putString(TOKEN, token);
        editor.apply();
    }

    private void loadImagesFromDisk() {
        new AsyncReadContent().execute();
    }

    private class AsyncReadContent extends AsyncTask<Void, String, Void> {
        //класс в для чтения картинок с yandex в отдельном потоке
        private RestClient mClient;
        private Credentials mCredentials;
        private WebDavApi api;

        @Override
        protected Void doInBackground(Void... voids) {
            synchronized (mToken) {
                mCredentials = new Credentials(USERNAME, mToken);
            }
            mClient = RestClientUtil.getInstance(mCredentials);
            //получаем картинки, в пределах 1000 штук
            ResourcesArgs resourcesArgs = new ResourcesArgs.Builder().setLimit(10000).setMediaType(MEDIA_TYPE).build();

            ResourceList resourceList;
            try {
                //список всех изображение на диске
                resourceList = mClient.getFlatResourceList(resourcesArgs);

                getImagesList(resourceList);
            } catch (IOException e) {
                publishProgress(e.getMessage());
            } catch (com.yandex.disk.rest.exceptions.ServerException e) {
                publishProgress(e.getMessage());
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(String... values) {
            //уведомляем пользователя об ошибке
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.error)
                    .setMessage(values[0])
                    .setCancelable(false)
                    .setNegativeButton("ОК",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }

        private void getImagesList(ResourceList resourceList) throws IOException {
            //получаем список картинок
            api = new WebDavApi();
            for (Resource resouce : resourceList.getItems()) {
                synchronized (mDiskInfo) {
                    mDiskInfo.setItemCnt(resourceList.getItems().size());
                    Drawable drawable = getImagePreview(resouce.getPath().getPath().replaceAll(" ", "%20"));
                    if (drawable != null) {
                        Image image = new Image(resouce.getName(), resouce.getPath().getPath(), drawable);
                        mDiskInfo.addImage(image);
                        boolean notify = false;
                        while (!notify) {
                            try {
                                mAdapter.notifyItemInserted(mAdapter.getItemCount());
                                notify = true;
                            }
                            catch (IllegalStateException e){
                                //ничего тут не делаем, это дщля того, чтобы вставлять новые view в RecyclerView
                            }

                        }
                    }
                }
            }
        }

        private Drawable getImagePreview(String path) throws IOException {
            //получаем preview картинки, с использование WebDav api
            Drawable image;
            synchronized (mToken) {
                image = api.getImagePreview(mToken, path);
            }
            return image;
        }
    }
}

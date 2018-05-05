package com.example.art_dev.diskgallery;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideExtension;
import com.bumptech.glide.annotation.GlideOption;
import com.bumptech.glide.annotation.GlideType;
import com.example.art_dev.diskgallery.ImageView.TouchImageView;
import com.example.art_dev.diskgallery.Rest.RestClientUtil;
import com.squareup.picasso.Picasso;
import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerException;

import java.io.File;
import java.io.IOException;
//Фрагмент с большой картинокой, отображающийся во ViewPager
public class PageBigImage extends Fragment {
    public static final String PAGE_NUMBER = "page_number";

    private int mPageNumber;
    private AsyncWaitForBigImage mAsyncTask;
    private ProgressBar progressBar;
    private TouchImageView imageView;

    private DiskInfo mInfo;
    private String mToken;
    private String mUserName;
    private File mRezult;

    static PageBigImage newInstance(int page) {
        //создаем новый instance
        PageBigImage pageBigImage = new PageBigImage();
        Bundle arguments = new Bundle();
        arguments.putInt(PAGE_NUMBER, page);
        pageBigImage.setArguments(arguments);
        return pageBigImage;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_big_image_activity, menu);

    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(PAGE_NUMBER);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.big_image_base_item, null);

        mInfo = DiskInfo.getContext();
        FragmentActivity activity = (FragmentActivity) getActivity();
        final ActionBar appBar = activity.getActionBar();

        imageView = (TouchImageView) view.findViewById(R.id.imgDisplay);
        imageView.setOnClickListener(new View.OnClickListener() {
            //по клику на картинку показываем или скрываем ActionBar
            @Override
            public void onClick(View v) {
                if (appBar.isShowing()) {
                    appBar.hide();
                } else {
                    appBar.show();
                }
            }
        });
        progressBar = (ProgressBar) view.findViewById(R.id.bigImage_load_pb);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        mToken = sp.getString(MainActivity.TOKEN, "");
        mUserName = sp.getString(MainActivity.USERNAME, "");
        //проверяем не загружена ли уже нужная картинка
        if (mInfo.isImageLoaded(mPageNumber)) {
            //картинка уже есть
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            imageView.setImageDrawable(Drawable.createFromPath(mInfo.getImage(mPageNumber).getBigImgPath()));
        } else {
            //загружаем картинку в фоне
            mAsyncTask = new AsyncWaitForBigImage();
            mRezult = new File(getContext().getFilesDir(), new File(mInfo.getImage(mPageNumber).getPathOnDisk()).getName());
            mAsyncTask.execute();
        }


        return view;
    }

    @Override
    public void onDestroyView() {
        //тут помогаем сборщику мусора управиться по-быстрее
        mRezult = null;
        if(imageView.getDrawable() !=null) {
            //((BitmapDrawable) imageView.getDrawable()).getBitmap().recycle();
            imageView = null;

        }

        if (mAsyncTask != null) {
            //отменяем загрузку в фоне
            mAsyncTask.cancel(false);
            mAsyncTask = null;
        }
        super.onDestroyView();
    }

    //поток для ожидания загрузки большой картинки
    private class AsyncWaitForBigImage extends AsyncTask<Void, String, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Credentials credentials = new Credentials(mUserName, mToken);
            RestClient client = RestClientUtil.getInstance(credentials);
            try {
                //если вдруг файл уже есть, сначала его удаляем
                mRezult.delete();
                //выкачиваем картинку зерез RestApi
                client.downloadFile(mInfo.getImage(mPageNumber).getPathOnDisk(), mRezult, null);

            } catch (IOException e) {
                publishProgress(e.getMessage());
                return null;
            } catch (ServerException e) {
                publishProgress(e.getMessage());
                return null;
            }
            synchronized (mInfo) {
                mInfo.loadNewImage(mRezult.getPath(), mPageNumber);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            //сообщаем об ошибке
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

        @Override
        protected void onPostExecute(Void aVoid) {
            //если картинка загружена, показываем ее и убираем ProgressBar
            if (mInfo.isImageLoaded(mPageNumber) && getActivity() != null) {
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                imageView.setImageDrawable(Drawable.createFromPath(mRezult.getPath()));
            }
        }

    }
}

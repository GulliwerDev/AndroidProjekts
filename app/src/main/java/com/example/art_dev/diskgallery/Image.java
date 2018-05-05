package com.example.art_dev.diskgallery;

import android.graphics.drawable.Drawable;

import java.io.File;

//класс описывающий одну картинку на диске
public class Image {
    private String name;
    private String pathOnDisk;
    private Drawable imagePreview;
    private String bigImgPath;

    public Image(String name, String pathOnDisk, Drawable imagePreview) {
        this.name = name;
        this.pathOnDisk = pathOnDisk;
        this.imagePreview = imagePreview;
        bigImgPath = "";
    }

    public String getName() {
        return name;
    }

    public String getPathOnDisk() {
        return pathOnDisk;
    }

    public Drawable getImagePreview() {
        return imagePreview;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getBigImgPath() {
        return bigImgPath;
    }

    public void setBigImgPath(String bigImgPath) {
        this.bigImgPath = bigImgPath;
    }
    //удаление файла с большой картинкой(кешируем только 10 последних загруженных)
    public boolean deleteBigImageFromMemory() {
        File f = new File(bigImgPath);
        return f.delete();
    }

}

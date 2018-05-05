package com.example.art_dev.diskgallery;


import java.util.ArrayList;
import java.util.List;
//синглтон для хранения картинок с диска
public class DiskInfo {
    private static DiskInfo mContext;
    private int itemCnt;
    private List<Image> mImages;
    private boolean mImagesLoaded;
    private List<Integer> mLoadedImages;



    public static DiskInfo getContext() {
        if (mContext == null) {
            mContext = new DiskInfo();
        }
        return mContext;
    }

    private DiskInfo() {
        mImages = new ArrayList<>();
        mImagesLoaded = false;
        mLoadedImages = new ArrayList<>();
    }

    public boolean isImagesLoaded() {
        return mImagesLoaded;
    }

    public void addImage(Image image) {
        mImagesLoaded = true;
        mImages.add(image);
    }
    public int getItemCnt() {
        return itemCnt;
    }
    public List<Image> getmImages() {
        return mImages;
    }

    public Image getImage(int position) {
        return mImages.get(position);
    }



    public void setItemCnt(int itemCnt) {
        this.itemCnt = itemCnt;
    }
    //загрузка новой картинки, если нужно, удаляем старую
    public void loadNewImage(String path, int position){
        if(mLoadedImages.size()<10){
            mLoadedImages.add(position);
            mImages.get(position).setBigImgPath(path);
        }
        else {
            mImages.get(mLoadedImages.get(0)).deleteBigImageFromMemory();
            mLoadedImages.remove(0);
            mLoadedImages.add(position);
            mImages.get(position).setBigImgPath(path);
        }
    }
    public boolean isImageLoaded(int position){
        return mLoadedImages.contains(position);
    }

}

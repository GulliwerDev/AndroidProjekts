package com.example.art_dev.diskgallery;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
//адаптер для RecyclerView
public class ImageRecyclerViewAdapter extends RecyclerView.Adapter<ImageRecyclerViewAdapter.ViewHolder>{

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.preview_image_base_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(mContext).load(mInfo.getImage(position).getImagePreview()).into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mInfo.getmImages().size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView mImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.preview_ImageView);
            mImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int nPos = getAdapterPosition();
            if(nPos != RecyclerView.NO_POSITION){
                Intent intent = new Intent(mContext,BigImageActivity.class);
                intent.putExtra(BigImageActivity.EXTRA_POS,nPos);
                mContext.startActivity(intent);
            }
        }
    }

    public ImageRecyclerViewAdapter(Context context) {
        mInfo = DiskInfo.getContext();
        mContext = context;
    }


    private DiskInfo mInfo;
    private Context mContext;

}

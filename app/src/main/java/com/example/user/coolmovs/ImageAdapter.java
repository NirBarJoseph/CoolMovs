package com.example.user.coolmovs;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> mThumbUris;
    private ArrayList<String> mov_id_arr;
    private ArrayList<String> mov_tit_arr;

    private final String LOG_TAG = ImageAdapter.class.getSimpleName();

    public ImageAdapter(Context c) {
        mContext = c;
        mov_id_arr =new ArrayList<>();
        mov_tit_arr=new ArrayList<>();
        mThumbUris=new ArrayList<>();
    }

    public int getCount() {
        return mThumbUris.size();
    }

    public Object getItem(int position) {
        return mThumbUris.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new ImageView(mContext);
        }
        ImageView imageView = (ImageView) convertView;

        String url = mThumbUris.get(position);

        Picasso.with(mContext).load(url).into(imageView);

        return convertView;
    }

    public ArrayList<String> getUriList(){
        return mThumbUris;
    }
    public ArrayList<String> getidlist(){
        return mov_id_arr;
    }
    public ArrayList<String> getTiteList(){ return mov_tit_arr;  }

    public void clear() {
        if(mThumbUris != null) mThumbUris.clear();
        if(mov_id_arr != null) mov_id_arr.clear();
        if(mov_tit_arr != null) mov_tit_arr.clear();
    }

    public void refresh(ArrayList<ArrayList<String>> result) {

        if(mThumbUris == null) mThumbUris = new ArrayList<>();
        mThumbUris.addAll(result.get(0));
        if(mov_id_arr == null) mov_id_arr = new ArrayList<>();
        mov_id_arr.addAll(result.get(1));
        if(mov_tit_arr == null) mov_tit_arr = new ArrayList<>();
        mov_tit_arr.addAll(result.get(2));
        notifyDataSetChanged();

    }
}

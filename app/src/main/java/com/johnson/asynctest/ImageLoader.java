package com.johnson.asynctest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2016/6/20.
 */
public class ImageLoader {
    private ImageView mImageView;
    private String mUrl;
    //创建Cache
    private LruCache<String,Bitmap> mCaches;
    private ListView mListView;
    private Set<NewsAsycnTask> mTask;

    public ImageLoader (ListView listView){
        mListView=listView;
        mTask=new HashSet<>();
        int maxMemory= (int) Runtime.getRuntime().maxMemory();
        int cacheSize=maxMemory/4;
        mCaches=new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //在每次存入缓存的时候调用
                return value.getByteCount();
            }
        };
    }

    //增加到缓存
    public void addBitmapToCache(String url, Bitmap bitmap){
        if(getBitmapFromCache(url)==null){
            mCaches.put(url,bitmap);
        }
    }

    //从缓存中获取数据
    public Bitmap getBitmapFromCache(String url){
        return mCaches.get(url);
    }

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mImageView.getTag().equals(mUrl)){
                mImageView.setImageBitmap((Bitmap) msg.obj);
            }
        }
    };

    public void showImageByThread(ImageView imageView, final String url){
        mImageView=imageView;
        mUrl=url;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap=getBitmap(url);
                Message message=new Message();
                message.obj=bitmap;
                mHandler.sendMessage(message);
            }
        }).start();
    }

    public Bitmap getBitmap(String urlString){
        Bitmap bitmap;
        InputStream in = null;
        try {
            URL url=new URL(urlString);
            HttpURLConnection connection= (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            in = new BufferedInputStream(connection.getInputStream());
            bitmap= BitmapFactory.decodeStream(in);
            connection.disconnect();
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void showImageByAsyncTask(ImageView imageView,String url){
        //从缓存中取出对应的图片
        Bitmap bitmap=getBitmapFromCache(url);
        //如果缓存中没有，从网络中下载
        if(bitmap==null){
            imageView.setImageResource(R.mipmap.ic_launcher);
        }else{
            imageView.setImageBitmap(bitmap);
        }
    }

    //用来加载从start到end的所有图片
    public void loadImages(int start,int end){
        for (int i=start;i<end;i++){
            String url=NewsAdapter.URLS[i];
            //从缓存中取出对应的图片
            Bitmap bitmap=getBitmapFromCache(url);
            //如果缓存中没有，从网络中下载
            if(bitmap==null){
                NewsAsycnTask task=new NewsAsycnTask(url);
                task.execute(url);
                mTask.add(task);
            }else{
                ImageView imageView= (ImageView) mListView.findViewWithTag(url);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    public void cancelAllTasks() {
        if(mTask!=null){
            for (NewsAsycnTask task:mTask){
                task.cancel(false);
            }
        }
    }

    private class NewsAsycnTask extends AsyncTask<String,Void,Bitmap>{

//        private ImageView mImageView;
        private String mUrl;

        public NewsAsycnTask(String url){
//            mImageView=imageView;
            mUrl=url;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            String url=strings[0];
            //从网络上获取图片
            Bitmap bitmap=getBitmap(url);
            if(bitmap!=null){
                //将不在缓存中的图片加入缓存
                addBitmapToCache(url,bitmap);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ImageView imageView= (ImageView) mListView.findViewWithTag(mUrl);
            if(imageView!=null&&bitmap!=null){
               imageView.setImageBitmap(bitmap);
            }
            mTask.remove(this);
        }
    }
}

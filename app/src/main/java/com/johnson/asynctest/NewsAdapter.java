package com.johnson.asynctest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Administrator on 2016/6/20.
 */
public class NewsAdapter extends BaseAdapter implements AbsListView.OnScrollListener{
    private List<NewsBean> mList;

    private LayoutInflater mInflater;

    private ImageLoader mImageLoader;

    private int mStart,mEnd;

    public static String[]URLS;

    private boolean mFirstIn;

    public NewsAdapter(Context context, List<NewsBean> data, ListView listView){
        mList=data;
        mInflater=LayoutInflater.from(context);
        mImageLoader=new ImageLoader(listView);
        URLS=new String[data.size()];
        for (int i=0;i<data.size();i++){
            URLS[i]=data.get(i).getNewsIconUrl();
        }
        mFirstIn=true;
        //注册滚动事件
        listView.setOnScrollListener(this);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if(view==null) {
            viewHolder = new ViewHolder();
            view = mInflater.inflate(R.layout.item, null);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.image_view);
            viewHolder.title = (TextView) view.findViewById(R.id.tv_title);
            viewHolder.content = (TextView) view.findViewById(R.id.tv_content);
            view.setTag(viewHolder);
        }else{
            viewHolder= (ViewHolder) view.getTag();
        }
        viewHolder.imageView.setImageResource(R.mipmap.ic_launcher);
        String url=mList.get(i).getNewsIconUrl();
        viewHolder.imageView.setTag(url);
//        new ImageLoader().showImageByThread(viewHolder.imageView,url);
        mImageLoader.showImageByAsyncTask(viewHolder.imageView,url);
        viewHolder.title.setText(mList.get(i).getNewsTitle());
        viewHolder.content.setText(mList.get(i).getNewsContent());
        return view;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        if(i==SCROLL_STATE_IDLE){
            //加载可见项
            mImageLoader.loadImages(mStart,mEnd);
        }else{
         //停止任务
            mImageLoader.cancelAllTasks();
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {
        mStart=i;
        mEnd=i+i1;
        //第一次加载显示的时候调用
        if(mFirstIn&&i1>0){
            mImageLoader.loadImages(mStart,mEnd);
            mFirstIn=false;
        }
    }

    class ViewHolder{
        ImageView imageView;

        TextView title;

        TextView content;
    }
}

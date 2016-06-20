package com.johnson.asynctest;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Adapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView mListView;

    private static String URL="http://www.imooc.com/api/teacher?type=4&num=30";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView= (ListView) findViewById(R.id.list_view);
        new NewsAsyncTask().execute(URL);
    }

    /**
     * 将URL所对应的json数据转化为所封装的NewsBea对象
     * @param url
     * @return
     */
    private List<NewsBean> getJsonData(String url){
        List<NewsBean> newsBeanList =new ArrayList<NewsBean>();
        try {
            String jsonString=readStream(new URL(url).openStream());
            NewsBean newsBean;
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(jsonString);
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    newsBean = new NewsBean();
                    newsBean.setNewsIconUrl(jsonObject.getString("picSmall"));
                    newsBean.setNewsTitle(jsonObject.getString("name"));
                    newsBean.setNewsContent(jsonObject.getString("description"));
                    newsBeanList.add(newsBean);
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newsBeanList;
    }

    /**
     * 解析网页返回的数据
     * @param in
     * @return
     */
    private String readStream(InputStream in){
        InputStreamReader isr;
        StringBuffer response=new StringBuffer();
        try{
            String line="";
            isr=new InputStreamReader(in,"utf-8");
            BufferedReader reader=new BufferedReader(isr);
            while((line=reader.readLine())!=null){
                response.append(line);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return response.toString();
    }

    /**
     * 实现网络的异步访问
     */
    class NewsAsyncTask extends AsyncTask<String,Void,List<NewsBean>>{

        @Override
        protected List<NewsBean> doInBackground(String... strings) {
            return getJsonData(strings[0]);
        }

        @Override
        protected void onPostExecute(List<NewsBean> newsBeans) {
            super.onPostExecute(newsBeans);
            NewsAdapter adapter=new NewsAdapter(MainActivity.this,newsBeans,mListView);
            mListView.setAdapter(adapter);
        }
    }
}

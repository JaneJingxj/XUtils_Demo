package com.example.aaa.xutils_demo;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ListView;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
@ContentView(R.layout.activity_main)
public class MainActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener {
    @ViewInject(R.id.refresh)
    private SwipeRefreshLayout refresh;
    @ViewInject(R.id.list)
    private ListView listView;
    private BeanAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        //初始化BitmapUtils
        BitmapHelper.init(this);
        //初始化DbHelper
        DbHelper.init(this);
        List<Bean> list = null;
        try {
            list=DbHelper.getUtils().findAll(Bean.class);
        } catch (DbException e) {
            e.printStackTrace();
        }
//        list = new ArrayList<Bean>();
        adapter = new BeanAdapter(this,list);
        listView.setAdapter(adapter);
        refresh.setOnRefreshListener(this);


    }


    @Override
    public void onRefresh() {
        adapter.clear();
        HttpUtils utils = new HttpUtils();
        RequestParams params = new RequestParams();
        params.addQueryStringParameter("pageindex", "2");
        params.addQueryStringParameter("pagesize", "20");
        params.addQueryStringParameter("cityId", "226");
        utils.send(HttpRequest.HttpMethod.POST, "http://a1.greentree.cn:8029/Api/index.php/Other/getActivityList", params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> objectResponseInfo) {
                try {
                    JSONObject object = new JSONObject(objectResponseInfo.result);
                    JSONArray array = object.getJSONObject("responseData").getJSONArray("items");
                    Log.i("syso","++++++++++++++++"+objectResponseInfo.result);
                    List<Bean> beans=new ArrayList<>();
                    for (int i = 0; i <array.length() ; i++) {
                        JSONObject item=array.getJSONObject(i);
                        Bean bean=new Bean();
                        //设置
                        bean.setId(item.getInt("id"));
                        bean.setTitle(item.getString("title"));
                        bean.setImage(item.getString("imageUrl"));
                        //添加到beans里
                        beans.add(bean);
                        Log.i("syso","-----------"+bean.toString());
                    }
                    DbHelper.getUtils().saveOrUpdateAll(beans);
                    adapter.addALL(beans);

                } catch (JSONException e) {


                } catch (DbException e) {
                    e.printStackTrace();
                }
                refresh.setRefreshing(false);
            }

            @Override
            public void onFailure(HttpException e, String s) {
                refresh.setRefreshing(false);
            }
        });
    }
}

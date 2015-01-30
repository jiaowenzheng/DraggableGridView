package com.android.drag.gridview;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.drag.gridview.framework.DragGridView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends ActionBarActivity implements DragAdapter.DeleteOnClickListener,
        DragGridView.OnLongClickListener,ViewTreeObserver.OnGlobalLayoutListener {

    private DragGridView mDragGridView;
    private GridView mGridView;
    private LinearLayout mBottomLayout;

    private DragAdapter showDragAdapter;
    private DragAdapter hideDragAdapter;

    private List<HashMap<String, Object>> showDataList = new ArrayList<HashMap<String, Object>>();
    private List<HashMap<String, Object>> hideDataList = new ArrayList<HashMap<String, Object>>();

    private View mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mView = LayoutInflater.from(this).inflate(R.layout.activity_main,null);
        setContentView(mView);

        mBottomLayout = (LinearLayout) findViewById(R.id.bottom_layout);
        mDragGridView = (DragGridView) findViewById(R.id.dragGridView);
        mGridView = (GridView) findViewById(R.id.hid_grid_view);
        mDragGridView.setOnLongClickListener(this);
        ViewTreeObserver vto = mView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(this);


        if (!parseData()) {
            for (int i = 0; i < 50; i++) {
                HashMap<String, Object> itemHashMap = new HashMap<String, Object>();
                itemHashMap.put("item_text", "测试" + Integer.toString(i));

                if(i < 10){
                    showDataList.add(itemHashMap);
                }else{
                    hideDataList.add(itemHashMap);
                }
            }
        }


        //show adapter
        showDragAdapter = new DragAdapter(this, showDataList);
        mDragGridView.setAdapter(showDragAdapter);
        showDragAdapter.setDeleteListener(this);


        mDragGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                mDragGridView.removeItemAnimation(position);
            }
        });

        //hide adapter
        hideDragAdapter = new DragAdapter(this,hideDataList);
        mGridView.setAdapter(hideDragAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, Object> itemHashMap = (HashMap<String, Object>) mGridView.getItemAtPosition(position);
                showDataList.add(itemHashMap);
                hideDataList.remove(position);
                showDragAdapter.notifyDataSetChanged();
                hideDragAdapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        saveDate();
        // mDragAdapte

    }

    public void saveDate() {

        int size = showDataList.size();
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < size; i++) {
            HashMap<String, Object> map = showDataList.get(i);
            JSONObject object = new JSONObject(map);
            jsonArray.add(object);
        }
        Log.i("jiao", " json " + jsonArray.toJSONString());


        int hideSize = hideDataList.size();
        JSONArray hideJsonArray = new JSONArray();
        for (int i = 0; i < hideSize; i++) {
            HashMap<String, Object> map = hideDataList.get(i);
            JSONObject object = new JSONObject(map);
            hideJsonArray.add(object);
        }
        Log.i("jiao", " json " + hideJsonArray.toJSONString());

        SharedPreferences sharedPreferences = getSharedPreferences("json", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString("show_data", jsonArray.toJSONString());
        edit.putString("hide_data",hideJsonArray.toJSONString());
        edit.commit();

    }

    public boolean parseData() {

        SharedPreferences sharedPreferences = getSharedPreferences("json", Context.MODE_PRIVATE);
        String json1 = sharedPreferences.getString("show_data", "");
        String json2 = sharedPreferences.getString("hide_data","");
        if (TextUtils.isEmpty(json1)) {
            return false;
        }

        JSONArray array_1 = JSONArray.parseArray(json1);
        showDataList.clear();

        int size = array_1.size();
        for (int i = 0; i < size; i++) {
            JSONObject objct = array_1.getJSONObject(i);
            HashMap<String, Object> itemHashMap = new HashMap<String, Object>();
            itemHashMap.put("item_text", objct.getString("item_text"));
            showDataList.add(itemHashMap);
        }

        JSONArray array_2 = JSONArray.parseArray(json2);
        hideDataList.clear();

        int size2 = array_2.size();
        for (int i = 0; i < size2; i++) {
            JSONObject objct = array_2.getJSONObject(i);
            HashMap<String, Object> itemHashMap = new HashMap<String, Object>();
            itemHashMap.put("item_text", objct.getString("item_text"));
            hideDataList.add(itemHashMap);
        }

        return true;
    }

    @Override
    public void onDelete(HashMap<String, Object> item) {
        hideDataList.add(item);
        hideDragAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLongClick() {
        mBottomLayout.setVisibility(View.GONE);
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        Log.i("jiao"," event "+event.getAction()+" cross "+showDragAdapter.getVisibleCross());
        if(showDragAdapter.getVisibleCross()){
            if(KeyEvent.KEYCODE_BACK == keyCode){
                showDragAdapter.setVisibleCross(false);
                mBottomLayout.setVisibility(View.VISIBLE);
                return false;
            }
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onGlobalLayout() {
        setListViewHeightBasedOnChildren(mGridView,hideDataList.size());
        setListViewHeightBasedOnChildren(mDragGridView,showDataList.size());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(Build.VERSION.SDK_INT > 15){
            mView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }else{
            mView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }
    }

    public static void setListViewHeightBasedOnChildren(GridView gridView,int size) {
        ListAdapter listAdapter = gridView.getAdapter();

        if (listAdapter == null) {
            return;
        }

        int i = 0;
        int maxHeight = 0;
        int minHeight = 0;
        int itemHeight = 0;
        int totalHeight = 0;
        View listItem = null;
        int numColumns = gridView.getNumColumns();

        try {
            listItem = listAdapter.getView(0, null, gridView);
            listItem.measure(0, 0);
            minHeight = listItem.getMeasuredHeight();
            maxHeight = minHeight;
        } catch (Exception e) {
            minHeight = 0;
            maxHeight = 0;
        }

        int z = size % numColumns;
        int y = size / numColumns;
        if(z != 0){
            y += 1;
        }

        int vertticSpac = 0;
        int bottomPadding = gridView.getPaddingBottom();
        int topPadding = gridView.getPaddingTop();

        if(Build.VERSION.SDK_INT > 15){
            vertticSpac = gridView.getVerticalSpacing();
            Log.i("jiao","vertticSpac "+vertticSpac);
            vertticSpac = vertticSpac * (y - 1);
        }

        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = minHeight * y + bottomPadding + topPadding + vertticSpac;
        gridView.setLayoutParams(params);

        Log.i("jiao","minHeight  "+minHeight+" numColumns "+numColumns+" y "+y);
    }
}

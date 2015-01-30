package com.android.drag.gridview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.drag.gridview.framework.DragGridBaseAdapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;


/**
 * @blog http://blog.csdn.net/xiaanming
 *
 * @author xiaanming
 *
 */
public class DragAdapter extends BaseAdapter implements DragGridBaseAdapter {
    private List<HashMap<String, Object>> list;
    private LayoutInflater mInflater;
    private int mHidePosition = -1;

    private boolean isCrossVisable = false;

    private DeleteOnClickListener mDeleteListener;

    public DragAdapter(Context context, List<HashMap<String, Object>> list) {
        this.list = list;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }



    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(R.layout.grid_item, null);
        ImageView cross = (ImageView) convertView.findViewById(R.id.delete_img);
        TextView mTextView = (TextView) convertView.findViewById(R.id.item_text);

        mTextView.setText((CharSequence) list.get(position).get("item_text"));

        if (position == mHidePosition) {
            convertView.setVisibility(View.INVISIBLE);
        }

        if (isCrossVisable) {
            cross.setVisibility(View.VISIBLE);
            cross.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDeleteListener.onDelete(list.get(position));
                    list.remove(position);
                    notifyDataSetChanged();
                }
            });
        } else {
            cross.setVisibility(View.GONE);
        }

        return convertView;
    }


    @Override
    public void reorderItems(int oldPosition, int newPosition) {
        HashMap<String, Object> temp = list.get(oldPosition);
        if (oldPosition < newPosition) {
            for (int i = oldPosition; i < newPosition; i++) {
                Collections.swap(list, i, i + 1);
            }
        } else if (oldPosition > newPosition) {
            for (int i = oldPosition; i > newPosition; i--) {
                Collections.swap(list, i, i - 1);
            }
        }

        list.set(newPosition, temp);
    }

    @Override
    public void setHideItem(int hidePosition) {
        this.mHidePosition = hidePosition;
        notifyDataSetChanged();
    }

    @Override
    public void removeItem(int removePosition) {
        list.remove(removePosition);
        notifyDataSetChanged();

    }

    @Override
    public void setVisibleCross(boolean visable) {
        isCrossVisable = visable;
        notifyDataSetChanged();
    }

    public boolean getVisibleCross(){
        return isCrossVisable;
    }

    public void setDeleteListener(DeleteOnClickListener listener){
        this.mDeleteListener = listener;
    }

    public interface DeleteOnClickListener{
        public void onDelete(HashMap<String,Object> item);
    }

}

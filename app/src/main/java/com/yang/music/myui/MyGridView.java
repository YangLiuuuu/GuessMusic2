package com.yang.music.myui;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.yang.music.R;
import com.yang.music.model.IWordButtonClickListener;
import com.yang.music.model.WordButton;
import com.yang.music.util.Util;

import java.util.ArrayList;

/**
 * Created by yang on 2017/6/5.
 */

public class MyGridView extends GridView {
    public final static int COUNTS_WORD = 24;

    private ArrayList<WordButton>mArrayList = new ArrayList<>();
    private MyGridAdapter mAdapter;
    private Context mContext;

    private Animation mScaleAnimation;

    private IWordButtonClickListener mWordButtonListenr;
    public MyGridView(Context context) {
        super(context);
    }

    public MyGridView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mAdapter = new MyGridAdapter();
        this.setAdapter(mAdapter);
        mContext = context;
    }

    public MyGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MyGridView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void updateData(ArrayList<WordButton>list){
        mArrayList = list;

        //重新设置数据源
        setAdapter(mAdapter);
    }

    class MyGridAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return mArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final WordButton holder;
            if (convertView == null){
                convertView = Util.getView(mContext, R.layout.self_ui_gridview_item);

                holder = mArrayList.get(position);

                //加载动画
                mScaleAnimation = AnimationUtils.loadAnimation(mContext,R.anim.scale);
                //设置动画的延迟时间
                mScaleAnimation.setStartOffset(position*100);

                holder.mIndex = position;
                if (holder.mViewButton == null){
                    holder.mViewButton = (Button) convertView.findViewById(R.id.item_btn);
                    holder.mViewButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mWordButtonListenr.onWordButtonClick(holder);
                        }
                    });
                }

                convertView.setTag(holder);
            }else {
                holder = (WordButton) convertView.getTag();
            }
            holder.mViewButton.setText(holder.mWordString);

            //播放动画
            convertView.startAnimation(mScaleAnimation);

            return convertView;
        }
    }

    /**
     * 注册监听接口
     * @param listener
     */
    public void registerOnWordButtonClick(IWordButtonClickListener listener){
        mWordButtonListenr = listener;
    }
}

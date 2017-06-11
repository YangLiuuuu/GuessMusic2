package com.yang.music.model;

import android.widget.Button;

/**文字按钮
 * Created by yang on 2017/6/5.
 */

public class WordButton {
    public int mIndex;
    public boolean mIsVisible;
    public String mWordString;

    public Button mViewButton;

    public WordButton(){
        mIsVisible = true;
        mWordString = "";
    }
}

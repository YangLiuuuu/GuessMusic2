package com.yang.music.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yang.music.R;
import com.yang.music.data.Const;
import com.yang.music.model.IAlertDialogButtonListener;
import com.yang.music.model.IWordButtonClickListener;
import com.yang.music.model.Song;
import com.yang.music.model.WordButton;
import com.yang.music.myui.MyGridView;
import com.yang.music.util.MyLog;
import com.yang.music.util.MyPlayer;
import com.yang.music.util.Util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements IWordButtonClickListener {

    public static final String TAG = "MainActivity";

    public static final int ID_DIALOG_DELETE_WORD = 4;

    public static final int ID_DIALOG_TIP_WORD = 5;

    public static final int ID_DIALOG_LACK_COINS_WORD = 6;

    /**
     * 答案状态的三种状态  正确、错误、不完整
     */
    public static final int STATUS_ANSWER_RIGHT = 1;
    public static final int STATUS_ANSWER_WRONG = 2;
    public static final int STATUS_ANSWER_LACK = 3;

    //闪烁次数
    public static final int SPASH_TINES = 6;

    //唱片相关动画
    private Animation mPanAnim;
    private LinearInterpolator mPanLin;

    private Animation mBarInAnim;
    private LinearInterpolator mBarInLin;

    private Animation mBarOutAnim;
    private LinearInterpolator mBarOutLin;

    //图片
    private ImageView mViewPan;
    private ImageView mViewPanBar;

    //按钮
    private ImageButton mBtnPlayStart;

    //当前关索引
    private TextView mCurrentStagePassView;

    //当前关歌曲名称
    private TextView mCurrentSongNameView;

    //判断动画是否正在播放
    private boolean mIsRunning;

    //过关界面
    private View mPassView;

    //文字框容器
    private ArrayList<WordButton> mAllWords;

    private ArrayList<WordButton> mBtnSelectWords;

    //已选择文字框UI容器
    private LinearLayout mViewWordsContainer;

    //当前的歌曲
    private Song mCurrentSong;

    //当前关的索引
    private int mCurrentStageIndex = -1;

    //当前金币数量
    private int mCurrentCoins = Const.TOTAL_COINS;

    //金币view
    private TextView mViewCurrentCoins;

    private TextView mCurrentStageTextView;

    private MyGridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            int[]data = Util.loadData(this);
            mCurrentStageIndex = data[0];
            mCurrentCoins = data[1];
        } catch (IOException e) {
            e.printStackTrace();
        }

        mViewPan = (ImageView) findViewById(R.id.imageView1);
        mViewPanBar = (ImageView) findViewById(R.id.imageView2);

        mGridView = (MyGridView) findViewById(R.id.gridView);

        mViewCurrentCoins = (TextView) findViewById(R.id.text_bar_coins);
        mViewCurrentCoins.setText(mCurrentCoins + "");

        //注册监听
        mGridView.registerOnWordButtonClick(this);

        mViewWordsContainer = (LinearLayout) findViewById(R.id.word_select_container);

        //初始化动画
        mPanAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        mPanLin = new LinearInterpolator();
        mPanAnim.setInterpolator(mPanLin);
        mPanAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewPanBar.startAnimation(mBarOutAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mBarInAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_45);
        mBarInLin = new LinearInterpolator();
        mBarInAnim.setFillAfter(true);
        mBarInAnim.setInterpolator(mBarInLin);
        mBarInAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewPan.startAnimation(mPanAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mBarOutAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_d_45);
        mBarOutLin = new LinearInterpolator();
        mBarOutAnim.setFillAfter(true);
        mBarInAnim.setInterpolator(mBarOutLin);
        mBarOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIsRunning = false;
                mBtnPlayStart.setVisibility(View.VISIBLE);
                MyPlayer.stopTheSong(MainActivity.this);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mBtnPlayStart = (ImageButton) findViewById(R.id.btn_play_start);
        mBtnPlayStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePlayButton();
            }
        });

        initCurrentStageData();

        //处理删除按键事件
        handleDeleteWord();
        //处理提示按键事件
        handleTipAnswer();
        //一开始就播放音乐
        handlePlayButton();
    }

    /**
     * 处理圆盘中间的播放按钮，就是开始播放音乐
     */
    private void handlePlayButton() {
        if (mViewPanBar != null) {
            if (!mIsRunning) {
                mIsRunning = true;
                //进入拨杆动画
                mViewPanBar.startAnimation(mBarInAnim);
                mBtnPlayStart.setVisibility(View.GONE);
                //播放音乐
                MyPlayer.playSong(MainActivity.this,mCurrentSong.getSongFileName());
            }
        }
    }

    @Override
    public void onWordButtonClick(WordButton wordButton) {
//        Toast.makeText(this,"you click button:"+wordButton.mIndex,Toast.LENGTH_SHORT).show();
        setSelectWord(wordButton);
        //获得答案状态
        int checkResult = checkAnswer();

        //检查答案
        if (checkResult == STATUS_ANSWER_RIGHT) {
            //答案正确，过关并获得奖励
            handlePassEvent();
        } else if (checkResult == STATUS_ANSWER_WRONG) {
            //答案错误，闪烁文字提示答案错误
            sparkTheWords();
        } else if (checkResult == STATUS_ANSWER_LACK) {
            //答案缺失,设置文字颜色为原来的颜色(绿色)
            for (int i = 0; i < mBtnSelectWords.size(); i++) {
                mBtnSelectWords.get(i).mViewButton.setTextColor(Color.GREEN);
            }
        }
    }

    /**
     * 处理过关界面及事件
     */
    private void handlePassEvent() {
        //显示过关
        mPassView = this.findViewById(R.id.pass_view);
        mPassView.setVisibility(View.VISIBLE);

        //停止未完成的动画
        mViewPan.clearAnimation();

        //停止正在播放的音乐
        MyPlayer.stopTheSong(MainActivity.this);

        //播放音效
        MyPlayer.playTone(MainActivity.this,MyPlayer.INDEX_STONE_COIN);

        //当前关的索引
        mCurrentStagePassView = (TextView) findViewById(R.id.text_current_stage_pass);
        if (mCurrentStagePassView != null){
            mCurrentStagePassView.setText((mCurrentStageIndex+1)+"");
        }

        //显示歌曲名称
        mCurrentSongNameView = (TextView) findViewById(R.id.text_current_song_name_pass);
        if (mCurrentSongNameView != null){
            mCurrentSongNameView.setText(mCurrentSong.getSongName());
        }

        ImageButton btnPass = (ImageButton) findViewById(R.id.btn_next);
        btnPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (judgeAppPassed()){
                    //进入通关界面
                    Util.startActivity(MainActivity.this,AllPassView.class);
                }else {
                    //开始新一关
                    mPassView.setVisibility(View.GONE);

                    mCurrentCoins +=3;

                    //加载关卡数据
                    initCurrentStageData();
                }
            }
        });

    }

    /**
     * 判断是否通关
     * @return
     */
    private boolean judgeAppPassed(){
        return mCurrentStageIndex == Const.SONG_INFO.length-1;
    }

    private void clearTheAnswer(WordButton wordButton) {
        wordButton.mViewButton.setText("");
        wordButton.mWordString = "";
        wordButton.mIsVisible = false;

        //设置待选框
        setButtonVisible(mAllWords.get(wordButton.mIndex), View.VISIBLE);
        reCheckAnswer();
    }

    private void reCheckAnswer() {
        for (int i = 0; i < mBtnSelectWords.size(); i++) {
            mBtnSelectWords.get(i).mViewButton.setTextColor(Color.GREEN);
        }
    }

    /**
     * 设置答案
     *
     * @param wordButton
     */
    private void setSelectWord(WordButton wordButton) {
        for (int i = 0; i < mBtnSelectWords.size(); i++) {
            if (mBtnSelectWords.get(i).mWordString.length() == 0) {
                //设置答案文字框内容可见性
                mBtnSelectWords.get(i).mViewButton.setText(wordButton.mWordString);
                mBtnSelectWords.get(i).mIsVisible = true;
                mBtnSelectWords.get(i).mWordString = wordButton.mWordString;
                //记录索引
                mBtnSelectWords.get(i).mIndex = wordButton.mIndex;

                //Log....
                MyLog.d(TAG, mBtnSelectWords.get(i).mIndex + "");

                //设置待选框可见性
                setButtonVisible(wordButton, View.INVISIBLE);
                break;
            }
        }
    }

    /**
     * 设置待选文字框是否可见
     *
     * @param button
     * @param visibility
     */
    private void setButtonVisible(WordButton button, int visibility) {
        button.mViewButton.setVisibility(visibility);
        button.mIsVisible = (visibility == View.VISIBLE);

        MyLog.d(TAG, button.mIsVisible + "");

    }

    @Override
    protected void onPause() {
        //保存游戏数据
        Util.saveData(MainActivity.this,mCurrentStageIndex-1,mCurrentCoins);

        mViewPan.clearAnimation();

        //暂停音乐
        MyPlayer.stopTheSong(MainActivity.this);

        super.onPause();
    }

    private Song loadStageSongInfo(int stageIndex) {
        Song song = new Song();
        String[] stage = Const.SONG_INFO[stageIndex];
        song.setSongFileName(stage[0]);
        song.setSongName(stage[1]);
        return song;
    }

    /**
     * 加载当前关的数据
     */
    private void initCurrentStageData() {
        //读取当前关的歌曲信息
        mCurrentSong = loadStageSongInfo(++mCurrentStageIndex);
        //初始化已选择框
        mBtnSelectWords = initWordSelected();

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(140, 140);

        //清空上一关的答案
        mViewWordsContainer.removeAllViews();
        //增加新的答案框
        for (int i = 0; i < mBtnSelectWords.size(); i++) {
            mViewWordsContainer.addView(mBtnSelectWords.get(i).mViewButton, params);
        }
        //显示当前关的索引
        mCurrentStageTextView = (TextView) findViewById(R.id.txt_current_stage_index);
        if (mCurrentStageTextView != null){
            mCurrentStageTextView.setText((mCurrentStageIndex+1)+"");
        }

        //获得数据
        mAllWords = initAllWord();
        //更新数据 -MyGridView
        mGridView.updateData(mAllWords);

        //一开始就播放音乐
        handlePlayButton();
    }

    //初始化待选文字框
    private ArrayList<WordButton> initAllWord() {
        ArrayList<WordButton> data = new ArrayList<>();
        //获得所有待选文字
        String[] words = generateWords();
        for (int i = 0; i < MyGridView.COUNTS_WORD; i++) {
            WordButton button = new WordButton();

            button.mWordString = words[i];
            data.add(button);
        }
        return data;
    }

    //初始化已选文字框
    private ArrayList<WordButton> initWordSelected() {
        ArrayList<WordButton> data = new ArrayList<>();
        for (int i = 0; i < mCurrentSong.getNameLength(); i++) {
            View view = Util.getView(MainActivity.this, R.layout.self_ui_gridview_item);

            final WordButton holder = new WordButton();
            holder.mViewButton = (Button) view.findViewById(R.id.item_btn);
            holder.mViewButton.setTextColor(Color.GREEN);
            holder.mViewButton.setText("");
            holder.mIsVisible = false;
            holder.mViewButton.setBackgroundResource(R.mipmap.game_wordblank);
            holder.mViewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearTheAnswer(holder);
                }
            });
            data.add(holder);
        }
        return data;
    }

    /**
     * 生成所有待选文字
     *
     * @return
     */
    private String[] generateWords() {
        Random random = new Random();

        String[] words = new String[MyGridView.COUNTS_WORD];
        //存入歌名
        for (int i = 0; i < mCurrentSong.getNameLength(); i++) {
            words[i] = mCurrentSong.getNameCharacters()[i] + "";
        }
        //获取随机文字并存入数组
        for (int i = mCurrentSong.getNameLength(); i < MyGridView.COUNTS_WORD; i++) {
            words[i] = getRandomChar() + "";
        }

        //打乱文字顺序:
        //首先从所有元素中随机选取一个与第一个元素进行交换
        //然后在第二个之后选择一个与第二个交换，直到最后一个元素
        //这样能够保证每个元素在每个位置的概率都是1/n
        for (int i = MyGridView.COUNTS_WORD - 1; i >= 0; i--) {
            int index = random.nextInt(i + 1);

            String buf = words[index];
            words[index] = words[i];
            words[i] = buf;
        }
        return words;
    }

    /**
     * 生成随机汉字
     *
     * @return
     */
    private char getRandomChar() {
        String str = "";
        int highPos;
        int lowPos;

        Random random = new Random();

        highPos = (176 + Math.abs(random.nextInt(39)));
        lowPos = (161 + Math.abs(random.nextInt(93)));

        byte[] b = new byte[2];
        b[0] = (Integer.valueOf(highPos)).byteValue();
        b[1] = (Integer.valueOf(lowPos)).byteValue();

        try {
            str = new String(b, "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str.charAt(0);
    }

    /**
     * 检查答案
     *
     * @return
     */
    private int checkAnswer() {
        //先检查长度
        for (int i = 0; i < mBtnSelectWords.size(); i++) {
            //如果有空的，说明答案不完整
            if (mBtnSelectWords.get(i).mWordString.length() == 0) {
                return STATUS_ANSWER_LACK;
            }
        }
        //答案完整，继续检查正确性
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mBtnSelectWords.size(); i++) {
            sb.append(mBtnSelectWords.get(i).mWordString);
        }
        return (sb.toString().equals(mCurrentSong.getSongName())) ? STATUS_ANSWER_RIGHT : STATUS_ANSWER_WRONG;
    }

    /**
     * 闪烁文字
     */
    private void sparkTheWords() {
        //定时器相关
        TimerTask task = new TimerTask() {
            boolean mChange = false;
            int mSparTimes = 0;

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (++mSparTimes > SPASH_TINES) {
                            return;
                        }
                        //执行闪烁逻辑，交替显示红色和白色文字
                        for (int i = 0; i < mBtnSelectWords.size(); i++) {
                            mBtnSelectWords.get(i).mViewButton.setTextColor(
                                    mChange ? Color.RED : Color.BLUE
                            );
                        }
                        mChange = !mChange;
                    }
                });
            }
        };
//        Timer timer = new Timer();
//        timer.schedule(task,1,150);
        new Timer().schedule(task, 1, 150);
    }

    /**
     * 实现提示：自动选择一个答案
     */
    private void tipAnswer() {
        boolean tipWord = false;
        for (int i = 0; i < mBtnSelectWords.size(); i++) {
            if (mBtnSelectWords.get(i).mWordString.length() == 0) {
                //减少金币数量
                if (!handleCoins(-getTipCoins())) {
                    //金币不够，显示对话框,
                    showConfirmDialog(ID_DIALOG_LACK_COINS_WORD);
                    return;
                }
                //根据当前的答案框条件选择对应的文字并填入
                onWordButtonClick(findIsAnswerWord(i));
                checkRepeatAnswer();
                tipWord = true;
                break;
            }
        }
        //没有找到可以填充的地方
        if (!tipWord) {
            //闪烁文字来提示用户
            sparkTheWords();
        }
    }

    private void checkRepeatAnswer(){
        for (int i = 0;i < mBtnSelectWords.size() - 1;i++){
            for (int j = i+1;j < mBtnSelectWords.size();j++){
                if (mBtnSelectWords.get(i).mWordString.equals(mBtnSelectWords.get(j).mWordString)){
                    clearTheAnswer(mBtnSelectWords.get(j));
                }
            }
        }
    }

    /**
     * 删除文字
     */
    private void deleteOneWord() {
        if (notAnswerSize() > 0){
            //减少金币
            if (!handleCoins(-getDeleteWordCoins())) {
                //金币不够显示提示对话框
                showConfirmDialog(ID_DIALOG_LACK_COINS_WORD);
                return;
            }
            //将这个索引对应的WordButton设置为不可见
            setButtonVisible(findNotAnswerWord(), View.GONE);
        }else {
            Toast.makeText(MainActivity.this,"已经只剩正确答案了哟",Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 获取非正确答案个数
     * @return
     */
    private int notAnswerSize(){
        WordButton buf = null;
        ArrayList<WordButton>list = new ArrayList<>();
        for (int i = 0;i < MyGridView.COUNTS_WORD;i++){
            if (mAllWords.get(i)!=null){
                buf = mAllWords.get(i);
                if (buf.mIsVisible && !isTheAnswerWord(buf)){
                    list.add(buf);
                }
            }
        }
        return list.size();
    }

    /**
     * 找到一个不是答案的文字，并且是可见的
     *
     * @return
     */
    private WordButton findNotAnswerWord() {
        Random random = new Random();
        WordButton buf = null;
        while (true) {
            int index = random.nextInt(24);
            buf = mAllWords.get(index);
            if (buf.mIsVisible && !isTheAnswerWord(buf)) {
                return buf;
            }
        }
    }

    /**
     * 找到一个答案文字
     *
     * @param index 当前需要填入答案框的索引
     * @return
     */
    private WordButton findIsAnswerWord(int index) {
        WordButton buf = null;
        for (int i = 0; i < MyGridView.COUNTS_WORD; i++) {
            buf = mAllWords.get(i);
            if (buf.mWordString.equals(("" + mCurrentSong.getNameCharacters()[index]))) {
                return buf;
            }
        }
        return null;
    }

    /**
     * 判断某个文字是否为答案
     *
     * @param wordButton
     * @return
     */
    private boolean isTheAnswerWord(WordButton wordButton) {
        boolean result = false;
        for (int i = 0; i < mCurrentSong.getNameLength(); i++) {
            if (wordButton.mWordString.equals((mCurrentSong.getNameCharacters()[i]) + "")) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * 增加或者减少指定数量的金币，
     *
     * @param data 数量
     * @return true 增加或者减少成功  false 失败
     */
    private boolean handleCoins(int data) {
        //判断当前总的金币数量是否可被减少
        if (mCurrentCoins + data >= 0) {
            mCurrentCoins += data;

            mViewCurrentCoins.setText(mCurrentCoins + "");
            return true;
        } else {
            //金币不够
            return false;
        }
    }

    /**
     * 从配置文件读取删除操作所要用的金币
     *
     * @return
     */
    private int getDeleteWordCoins() {
        return this.getResources().getInteger(R.integer.pay_delete_word);
    }

    /**
     * 从配置文件里读取提示操作金币所要用的金币
     *
     * @return
     */
    private int getTipCoins() {
        return this.getResources().getInteger(R.integer.pay_tip_answer);
    }

    /**
     * 处理删除待选文字事件
     */
    private void handleDeleteWord() {
        ImageButton button = (ImageButton) findViewById(R.id.btn_delete_word);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                deleteOneWord();
                showConfirmDialog(ID_DIALOG_DELETE_WORD);
            }
        });
    }

    /**
     * 处理提示按键事件
     */
    private void handleTipAnswer() {
        ImageButton button = (ImageButton) findViewById(R.id.btn_tip_answer);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                tipAnswer();
                showConfirmDialog(ID_DIALOG_TIP_WORD);
            }
        });
    }

    //自定义AlertDialog事件相应
    //删除错误答案
    private IAlertDialogButtonListener mBtnOkDeleteWordListener = new IAlertDialogButtonListener() {
        @Override
        public void onClick() {
            //执行事件
            deleteOneWord();
        }
    };
    //答案提示
    private IAlertDialogButtonListener mBtnTipWordListener = new IAlertDialogButtonListener() {
        @Override
        public void onClick() {
            //执行事件
            tipAnswer();
        }
    };
    //金币不足
    private IAlertDialogButtonListener mBtnLackCoinsListener = new IAlertDialogButtonListener() {
        @Override
        public void onClick() {
            //执行事件
            Toast.makeText(MainActivity.this,"金币不足，到商城购买",Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 显示对话框
     * @param id
     */
    private void showConfirmDialog(int id){
        switch (id){
            case ID_DIALOG_DELETE_WORD:
                Util.showDialog(MainActivity.this,"确认花费"+getDeleteWordCoins()+"个金币去掉一个错误答案?",
                        mBtnOkDeleteWordListener);
                break;
            case ID_DIALOG_TIP_WORD:
                Util.showDialog(MainActivity.this,"确认花费"+getDeleteWordCoins()+"个金币获得一个文字提示?",
                        mBtnTipWordListener);
                break;
            case ID_DIALOG_LACK_COINS_WORD:
                Util.showDialog(MainActivity.this,"金币不足，去商店补充？",
                        mBtnLackCoinsListener);
                break;
        }
    }
}

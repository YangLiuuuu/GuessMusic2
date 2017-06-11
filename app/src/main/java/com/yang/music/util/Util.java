package com.yang.music.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.yang.music.R;
import com.yang.music.data.Const;
import com.yang.music.model.IAlertDialogButtonListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by yang on 2017/6/5.
 */

public class Util {

    private static AlertDialog mAlertDialog;

    public static View getView(Context context,int layoutId){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(layoutId,null);
    }

    /**
     * 界面跳转
     * @param context
     * @param destination
     */
    public static void startActivity(Context context,Class destination){
        Intent intent = new Intent();
        intent.setClass(context,destination);
        context.startActivity(intent);
        //关闭当前的Activity
        ((Activity)context).finish();
    }

    /**
     * 显示自定义对话框
     * @param context
     * @param message
     * @param listener
     */
    public static void showDialog(final Context context, String message, final IAlertDialogButtonListener listener){
        View dialogView = null;

        AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.Theme_Transparent);
        dialogView = getView(context, R.layout.dialog_layout);

        ImageButton btnOk = (ImageButton) dialogView.findViewById(R.id.btn_dialog_ok);
        ImageButton btnCancel = (ImageButton) dialogView.findViewById(R.id.btn_dialog_cancel);
        TextView textMessage = (TextView) dialogView.findViewById(R.id.text_dialog_message);

        textMessage.setText(message);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //关闭对话框
                if (mAlertDialog!=null){
                    mAlertDialog.dismiss();
                }
                //事件回调
                if (listener!=null){
                    listener.onClick();
                }
                //播放音效
                MyPlayer.playTone(context,MyPlayer.INDEX_STONE_ENTER);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //关闭对话框
                if (mAlertDialog!=null){
                    mAlertDialog.dismiss();
                }

                //播放音效
                MyPlayer.playTone(context,MyPlayer.INDEX_STONE_CANCEL);
            }
        });
        //为dialog设置view
        builder.setView(dialogView);
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    /**
     * 保存游戏数据
     * @param context
     * @param stageIndex
     * @param coins
     */
    public static void saveData(Context context,int stageIndex,int coins){
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(Const.FILE_NAME_SAVE_DATA,Context.MODE_PRIVATE);
            DataOutputStream dos = new DataOutputStream(fos);

            dos.writeInt(stageIndex);
            dos.writeInt(coins);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (fos!=null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取游戏数据
     * @param context
     * @return
     * @throws IOException
     */
    public static int[] loadData(Context context) throws IOException {
        FileInputStream fis = null;
        int[] data = {-1,Const.TOTAL_COINS};
        try {
            fis = context.openFileInput(Const.FILE_NAME_SAVE_DATA);
            DataInputStream dis = new DataInputStream(fis);
            data[0] = dis.readInt();
            data[1] = dis.readInt();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (fis!=null){
                fis.close();
            }
        }
        return data;
    }


}

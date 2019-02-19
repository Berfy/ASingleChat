package cn.berfy.sdk.mvpbase.view.audio.record;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.berfy.sdk.mvpbase.R;

public class DialogManager {

    /**
     * 以下为dialog的初始化控件，包括其中的布局文件
     */

    private Dialog mDialog;

    private TextView tipsTxt;
    private TextView tipsTxt2;


    private TextView text_time;
    private Context mContext;

    private RelativeLayout voiceRlt;
    private RelativeLayout voiceRlt2;

    public ImageView getVoice_iv() {
        return voice_iv;
    }

    public void setVoice_iv(ImageView voice_iv) {
        this.voice_iv = voice_iv;
    }

    private ImageView voice_iv;
    private ImageView cancel_iv;

    public DialogManager(Context context) {
        mContext = context;
    }

    public void showRecordingDialog() {
        mDialog = new Dialog(mContext, R.style.Theme_audioDialog);
        // 用layoutinflater来引用布局
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.voice_loading_popup, null);
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(false);
        voiceRlt = (RelativeLayout) view.findViewById(R.id.voice_rlt);
        voiceRlt2 = (RelativeLayout) view.findViewById(R.id.voice_rlt2);
        tipsTxt = (TextView) view.findViewById(R.id.dm_tv_txt);
        tipsTxt2 = (TextView) view.findViewById(R.id.dm_tv_txt2);
        text_time = (TextView) view.findViewById(R.id.text_time);
        //声音条
        voice_iv = (ImageView) view.findViewById(R.id.voice_iv);
        cancel_iv = (ImageView) view.findViewById(R.id.cancel_iv);

        mDialog.show();

    }

    /**
     * 设置正在录音时的dialog界面
     */
    public void recording() {
        if (mDialog != null && mDialog.isShowing()) {

            voiceRlt.setVisibility(View.VISIBLE);
            tipsTxt.setVisibility(View.VISIBLE);
            voiceRlt2.setVisibility(View.GONE);
            tipsTxt2.setVisibility(View.GONE);
            voice_iv.setImageResource(R.drawable.voice_1);
            tipsTxt.setText(R.string.up_for_cancel);


        }
    }

    /**
     * 取消界面
     */
    public void wantToCancel() {
        if (mDialog != null && mDialog.isShowing()) {
            voiceRlt.setVisibility(View.GONE);
            tipsTxt.setVisibility(View.GONE);
            voiceRlt2.setVisibility(View.VISIBLE);
            tipsTxt2.setVisibility(View.VISIBLE);
            cancel_iv.setImageResource(R.drawable.voice_cancel);
            tipsTxt2.setText(R.string.want_to_cancle);
//            tipsTxt2.setBackgroundResource(R.drawable.voice_text_background);
        }

    }

    // 时间过短
    public void tooShort() {
        if (mDialog != null && mDialog.isShowing()) {

            voiceRlt2.setVisibility(View.VISIBLE);
            tipsTxt2.setVisibility(View.VISIBLE);
            voiceRlt.setVisibility(View.GONE);
            tipsTxt.setVisibility(View.GONE);
            cancel_iv.setImageResource(R.drawable.voice_short);
            tipsTxt2.setText(R.string.time_too_short);

        }

    }

    // 隐藏dialog
    public void dimissDialog() {

        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }

    }

    public void updateVoiceLevel(int level) {
        if (level > 0 && level < 6) {

        } else {
            level = 5;
        }
        if (mDialog != null && mDialog.isShowing()) {

            switch (level) {
                case 1:
                    voice_iv.setBackgroundResource(R.drawable.voice_1);
                    break;
                case 2:
                    voice_iv.setBackgroundResource(R.drawable.voice_2);
                    break;
                case 3:
                    voice_iv.setBackgroundResource(R.drawable.voice_3);
                    break;
                case 4:
                    voice_iv.setBackgroundResource(R.drawable.voice_4);
                    break;
                case 5:
                    voice_iv.setBackgroundResource(R.drawable.voice_5);
                    break;
                case 6:
                    voice_iv.setBackgroundResource(R.drawable.voice_6);
                    break;
                case 7:
                    voice_iv.setBackgroundResource(R.drawable.voice_7);
                    break;
            }
        }

    }

    public TextView getTipsTxt() {
        return tipsTxt;
    }

    public void setTipsTxt(TextView tipsTxt) {
        this.tipsTxt = tipsTxt;
    }

    public TextView getText_time() {
        return text_time;
    }

    public void setText_time(TextView text_time) {
        this.text_time = text_time;
    }
}

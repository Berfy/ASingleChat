package cn.berfy.sdk.mvpbase.view.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;

import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.config.Constant;
import cn.berfy.sdk.mvpbase.util.LogF;

/**
 * 通用确认提示框
 * 主要封装了一下show和dismiss方法
 *
 * @author Berfy
 * 2018.5.22
 */
public class CommonDialog {

    private final String TAG = "CommonDialog";
    private Activity mContext;
    private AlertDialog mDialog;//APP版本升级提示专用弹框

    public CommonDialog(Activity context) {
        this.mContext = context;
        initDialog();
    }

    private void initDialog() {
        if (null == mDialog) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setCancelable(false);
            mDialog = builder.create();
        }
    }

    public void showTipDialog(String msg, String okBtnText, DialogInterface.OnClickListener onClickListener) {
        showTipDialog("温馨提示", msg, okBtnText, onClickListener);
    }

    public void showTipDialog(String title, String msg, String okBtnText, DialogInterface.OnClickListener onClickListener) {
        initDialog();
        mDialog.setTitle(title);
        mDialog.setMessage(msg);
        mDialog.setCancelable(false);
        mDialog.setButton(AlertDialog.BUTTON_POSITIVE, okBtnText, onClickListener);
        show();
    }

    public void showDialog(String msg, String leftBtnText, String rightBtnText, DialogInterface.OnClickListener leftOnClickListener, DialogInterface.OnClickListener rightOnClickListener) {
        showDialog("温馨提示", msg, leftBtnText, rightBtnText, leftOnClickListener, rightOnClickListener);
    }

    private void showDialog(String title, String msg, String leftBtnText, String rightBtnText, DialogInterface.OnClickListener leftOnClickListener, DialogInterface.OnClickListener rightOnClickListener) {
        initDialog();
        mDialog.setTitle(title);
        mDialog.setMessage(msg);
        mDialog.setCancelable(false);
        mDialog.setButton(AlertDialog.BUTTON_NEGATIVE, leftBtnText, leftOnClickListener);//取消
        mDialog.setButton(AlertDialog.BUTTON_POSITIVE, rightBtnText, rightOnClickListener);//确定
        show();
    }

    public CommonDialog setContentView(int layoutId) {
        initDialog();
        mDialog.setView(View.inflate(mContext, layoutId, null));
        return this;
    }

    public AlertDialog getDialog() {
        initDialog();
        return mDialog;
    }

    public CommonDialog setContentView(View v) {
        initDialog();
        mDialog.setView(v);
        return this;
    }

    public CommonDialog setTitle(String title) {
        initDialog();
        mDialog.setTitle(title);
        return this;
    }

    public CommonDialog setMessage(String msg) {
        initDialog();
        mDialog.setMessage(msg);
        return this;
    }

    public CommonDialog setButton(int whichButton, CharSequence text, DialogInterface.OnClickListener listener) {
        initDialog();
        mDialog.setButton(whichButton, text, listener);
        return this;
    }

    public CommonDialog setButton(int iconResId) {
        initDialog();
        mDialog.setIcon(iconResId);
        return this;
    }

    public CommonDialog setCancelable(boolean cancelable) {
        initDialog();
        mDialog.setCancelable(cancelable);
        return this;
    }

    public CommonDialog setCanceledOnTouchOutside(boolean cancelable) {
        initDialog();
        mDialog.setCanceledOnTouchOutside(cancelable);
        return this;
    }

    public void show() {
        if (!mContext.isFinishing() && null != mDialog && !mDialog.isShowing()) {
            if (Constant.DEBUG) {
                LogF.d(TAG, "显示");
            }
            mDialog.show();
        } else {
            if (Constant.DEBUG) {
                LogF.d(TAG, "无法显示 activity isFinish: " + mContext.isFinishing() + " isShowing:" + mDialog.isShowing());
            }
        }
    }

    public void dismiss() {
        if (!mContext.isFinishing() && null != mDialog && mDialog.isShowing()) {
            if (Constant.DEBUG) {
                LogF.d(TAG, "消失");
            }
            mDialog.dismiss();
            mDialog = null;
        } else {
            if (Constant.DEBUG) {
                LogF.d(TAG, "无法消失 activity isFinish: " + mContext.isFinishing() + " isShowing:" + mDialog.isShowing());
            }
        }
    }
}

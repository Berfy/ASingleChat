package cn.polaris.mqttuikit.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.berfy.sdk.mvpbase.util.DeviceUtils;
import cn.berfy.sdk.mvpbase.util.LogF;
import cn.berfy.sdk.mvpbase.util.ToastUtil;
import cn.berfy.sdk.mvpbase.view.audio.AudioRecordButton;
import cn.polaris.mqttuikit.R;
import cn.polaris.mqttuikit.iview.IChatView;

/**
 * 聊天界面输入控件
 */
public class ChatInput extends RelativeLayout implements TextWatcher, View.OnClickListener {

    private static final String TAG = "IMLogTag_IM底部输入栏";

    private TextView mTvSend;
    private ImageButton mBtnAdd, mBtnVoice, mBtnKeyboard, mBtnEmotion;
    private EditText mEditText;
    private boolean mIsShowIme;//是否显示键盘
    private boolean mIsSendVisible, mIsHoldVoiceBtn, mIsEmoticonReady;
    private InputMode mInputMode = InputMode.NONE;
    private String mIdentify;
    private int mType;
    private IChatView mIChatView;
    private LinearLayout mLlMorePanel, mLlTextPanel;
    //    private TextView mTvVoicePanel;
    private AudioRecordButton mAudioRecordButton;
    private LinearLayout mLlEmoticonPanel;
    private final int REQUEST_CODE_ASK_PERMISSIONS = 100;


    public ChatInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.im_chat_input, this);
        initView();
    }

    private void initView() {
        mLlTextPanel = (LinearLayout) findViewById(R.id.im_text_panel);
        mBtnAdd = (ImageButton) findViewById(R.id.im_btn_add);
        mBtnAdd.setOnClickListener(this);
        mTvSend = findViewById(R.id.im_btn_send);
        mTvSend.setOnClickListener(this);
        mBtnVoice = (ImageButton) findViewById(R.id.im_btn_voice);
        mBtnVoice.setOnClickListener(this);
        mBtnEmotion = (ImageButton) findViewById(R.id.im_btnEmoticon);
        mBtnEmotion.setOnClickListener(this);
        mLlMorePanel = (LinearLayout) findViewById(R.id.im_morePanel);
        findViewById(R.id.im_tv_choose_image).setOnClickListener(this);
        findViewById(R.id.im_tv_send_gift).setOnClickListener(this);
        findViewById(R.id.im_tv_send_file).setOnClickListener(this);
        LinearLayout BtnImage = (LinearLayout) findViewById(R.id.im_btn_photo);
        BtnImage.setOnClickListener(this);
        LinearLayout BtnPhoto = (LinearLayout) findViewById(R.id.im_btn_image);
        BtnPhoto.setOnClickListener(this);
        LinearLayout btnVideo = (LinearLayout) findViewById(R.id.im_btn_video);
        btnVideo.setOnClickListener(this);
        LinearLayout btnFile = (LinearLayout) findViewById(R.id.im_btn_file);
        btnFile.setOnClickListener(this);
        setSendBtn();
        mBtnKeyboard = (ImageButton) findViewById(R.id.im_btn_keyboard);
        mBtnKeyboard.setOnClickListener(this);
        //        mTvVoicePanel = (TextView) findViewById(R.id.im_voice_panel);
        mAudioRecordButton = findViewById(R.id.audioRecord);
        mAudioRecordButton.setAudioFinishRecorderListener(new AudioRecordButton.AudioFinishRecorderListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinished(float seconds, String filePath) {
                LogF.d(TAG, "发送语音" + filePath);
                mIChatView.sendVoice((long) seconds, filePath);
            }
        });

        mEditText = (EditText) findViewById(R.id.im_input);
        mEditText.addTextChangedListener(this);
        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                LogF.d(TAG, "输入框焦点变化" + hasFocus);
                if (hasFocus) {
                    updateView(InputMode.TEXT);
                }
            }
        });
        mIsSendVisible = mEditText.getText().length() != 0;
        mLlEmoticonPanel = (LinearLayout) findViewById(R.id.im_emoticonPanel);

    }

    private void updateView(InputMode mode) {
        if (mode == mInputMode) return;
        mIChatView.onInputModeChanged(mode);
        leavingCurrentState();
        switch (mInputMode = mode) {
            case MORE:
                mLlMorePanel.setVisibility(VISIBLE);
                break;
            case TEXT:
                if (mEditText.requestFocus()) {
                    showKeyboard();
                }
                break;
            case VOICE:
                mAudioRecordButton.setVisibility(VISIBLE);
                //                mTvVoicePanel.setVisibility(VISIBLE);
                mLlTextPanel.setVisibility(GONE);
                mBtnVoice.setVisibility(GONE);
                mBtnKeyboard.setVisibility(VISIBLE);
                break;
            case EMOTICON:
                if (!mIsEmoticonReady) {
                    prepareEmoticon();
                }
                mLlEmoticonPanel.setVisibility(VISIBLE);
                break;
        }
    }

    public boolean isShowIme() {
        return mIsShowIme;
    }

    private void showKeyboard() {
        LogF.d(TAG, "打开键盘");
        mIsShowIme = true;
        DeviceUtils.openKeyboard(getContext());
    }

    private void closeKeyboard() {
        LogF.d(TAG, "关闭键盘");
        DeviceUtils.closeKeyboard(getContext(), mEditText.getWindowToken());
        mEditText.clearFocus();
        mIsShowIme = false;
    }

    private void leavingCurrentState() {
        switch (mInputMode) {
            case TEXT:
                closeKeyboard();
                break;
            case MORE:
                mLlMorePanel.setVisibility(GONE);
                break;
            case VOICE:
                mAudioRecordButton.setVisibility(GONE);
                //                mTvVoicePanel.setVisibility(GONE);
                mLlTextPanel.setVisibility(VISIBLE);
                mBtnVoice.setVisibility(VISIBLE);
                mBtnKeyboard.setVisibility(GONE);
                break;
            case EMOTICON:
                mLlEmoticonPanel.setVisibility(GONE);
        }
    }

    //    private void updateVoiceView() {
    //        if (mIsHoldVoiceBtn) {
    //            mTvVoicePanel.setText(getResources().getString(R.string.im_chat_release_send));
    //            mTvVoicePanel.setBackground(getResources().getDrawable(R.drawable.im_btn_voice_pressed));
    //            mChatView.startSendVoice();
    //        } else {
    //            mTvVoicePanel.setText(getResources().getString(R.string.im_chat_press_talk));
    //            mTvVoicePanel.setBackground(getResources().getDrawable(R.drawable.im_btn_voice_normal));
    //            mChatView.sendVoice();
    //        }
    //    }

    /**
     * 关联聊天界面逻辑
     */
    public void setChatData(int type, IChatView IChatView) {
        mType = type;
        mIChatView = IChatView;
        switch (mType) {
            case 0://Group:
                findViewById(R.id.im_tv_send_gift).setVisibility(View.GONE);
                break;
            case 1: //C2C:
                findViewById(R.id.im_tv_send_gift).setVisibility(View.GONE);
                break;
        }
    }

    /**
     * This method is called to notify you that, within <code>s</code>,
     * the <code>count</code> characters beginning at <code>start</code>
     * are about to be replaced by new text with length <code>after</code>.
     * It is an error to attempt to make changes to <code>s</code> from
     * this callback.
     *
     * @param s
     * @param start
     * @param count
     * @param after
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    /**
     * This method is called to notify you that, within <code>s</code>,
     * the <code>count</code> characters beginning at <code>start</code>
     * have just replaced old text that had length <code>before</code>.
     * It is an error to attempt to make changes to <code>s</code> from
     * this callback.
     *
     * @param s
     * @param start
     * @param before
     * @param count
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mIsSendVisible = s != null && s.length() > 0;
        setSendBtn();
        //显示对方正在输入的逻辑，去掉
        //        if (mIsSendVisible) {
        //            mChatView.sending();
        //        }
    }

    /**
     * This method is called to notify you that, somewhere within
     * <code>s</code>, the text has been changed.
     * It is legitimate to make further changes to <code>s</code> from
     * this callback, but be careful not to get yourself into an infinite
     * loop, because any changes you make will cause this method to be
     * called again recursively.
     * (You are not told where the change took place because other
     * afterTextChanged() methods may already have made other changes
     * and invalidated the offsets.  But if you need to know here,
     * you can use {@link Spannable#setSpan} in {@link #onTextChanged}
     * to mark your place and then look up from here where the span
     * ended up.
     *
     * @param s
     */
    @Override
    public void afterTextChanged(Editable s) {

    }

    private void setSendBtn() {
        if (mIsSendVisible) {
            //            mBtnAdd.setVisibility(GONE);
            mTvSend.setVisibility(VISIBLE);
        } else {
            //            mBtnAdd.setVisibility(VISIBLE);
            mTvSend.setVisibility(GONE);
        }
    }

    private void prepareEmoticon() {
        if (mLlEmoticonPanel == null) return;
        for (int i = 0; i < 5; ++i) {
            LinearLayout linearLayout = new LinearLayout(getContext());
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
            for (int j = 0; j < 7; ++j) {

                try {
                    AssetManager am = getContext().getAssets();
                    final int index = 7 * i + j;
                    InputStream is = am.open(String.format("emoticon/%d.gif", index));
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    Matrix matrix = new Matrix();
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    matrix.postScale(3.5f, 3.5f);
                    final Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                            width, height, matrix, true);
                    ImageView image = new ImageView(getContext());
                    image.setImageBitmap(resizedBitmap);
                    image.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
                    linearLayout.addView(image);
                    image.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String content = String.valueOf(index);
                            SpannableString str = new SpannableString(String.valueOf(index));
                            ImageSpan span = new ImageSpan(getContext(), resizedBitmap, ImageSpan.ALIGN_BASELINE);
                            str.setSpan(span, 0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            mEditText.append(str);
                        }
                    });
                    is.close();
                } catch (IOException e) {

                }

            }
            mLlEmoticonPanel.addView(linearLayout);
        }
        mIsEmoticonReady = true;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        Activity activity = (Activity) getContext();
        int id = v.getId();
        if (id == R.id.im_btn_send) {
            String text = getSendText();
            if (TextUtils.isEmpty(text)) {
                ToastUtil.getInstances().showShort(R.string.im_chat_content_null);
            } else {
                mIChatView.sendText();
            }
        }
        if (id == R.id.im_btn_add) {
            updateView(mInputMode == InputMode.MORE ? InputMode.TEXT : InputMode.MORE);
        }
        if (id == R.id.im_tv_choose_image) {
            if (activity != null && requestCamera(activity)) {
                mIChatView.sendPhoto();
            }
        }
        if (id == R.id.im_btn_photo) {
            if (activity != null && requestCamera(activity)) {
                mIChatView.sendPhoto();
            }
        }
        if (id == R.id.im_btn_image) {
            if (activity != null && requestStorage(activity)) {
                mIChatView.sendPhoto();
            }
        }
        if (id == R.id.im_btn_voice) {
            if (activity != null && requestAudio(activity)) {
                updateView(InputMode.VOICE);
            }
        }
        if (id == R.id.im_btn_keyboard) {
            updateView(InputMode.TEXT);
        }
        if (id == R.id.im_btn_video) {
            if (getContext() instanceof FragmentActivity) {
                FragmentActivity fragmentActivity = (FragmentActivity) getContext();
                if (requestVideo(fragmentActivity)) {
                    //                    VideoInputDialog.show(fragmentActivity.getSupportFragmentManager());
                    if (requestRtmp()) {
                        mIChatView.videoAction();
                    } else {
                        ToastUtil.getInstances().showShort("系统版本太低");
                    }

                }
            }
        }
        if (id == R.id.im_btnEmoticon) {
            updateView(mInputMode == InputMode.EMOTICON ? InputMode.TEXT : InputMode.EMOTICON);
        }
        if (id == R.id.im_btn_file || id == R.id.im_tv_send_file) {
            mIChatView.sendFile();
        }
    }


    /**
     * 获取输入框文字
     */
    public Editable getText() {
        return mEditText.getText();
    }


    /**
     * 获取输入框文字
     */
    public String getSendText() {
        return mEditText.getText().toString().trim().replace(" ", "");
    }

    /**
     * 设置输入框文字
     */
    public void setText(String text) {
        mEditText.setText(text);
    }


    /**
     * 设置输入模式
     */
    public void setInputMode(InputMode mode) {
        updateView(mode);
    }


    public enum InputMode {
        TEXT,
        VOICE,
        EMOTICON,
        MORE,
        VIDEO,
        NONE,
    }

    private boolean requestRtmp() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    private boolean requestVideo(Activity activity) {
        if (afterM()) {
            final List<String> permissionsList = new ArrayList<>();
            if ((activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED))
                permissionsList.add(Manifest.permission.CAMERA);
            if ((activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED))
                permissionsList.add(Manifest.permission.RECORD_AUDIO);
            if (permissionsList.size() != 0) {
                activity.requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_CODE_ASK_PERMISSIONS);
                return false;
            }
            int hasPermission = activity.checkSelfPermission(Manifest.permission.CAMERA);
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(new String[]{Manifest.permission.CAMERA},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return false;
            }
        }
        return true;
    }

    private boolean requestCamera(Activity activity) {
        if (afterM()) {
            int hasPermission = activity.checkSelfPermission(Manifest.permission.CAMERA);
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(new String[]{Manifest.permission.CAMERA},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return false;
            }
        }
        return true;
    }

    private boolean requestAudio(Activity activity) {
        if (afterM()) {
            int hasPermission = activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO);
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return false;
            }
        }
        return true;
    }

    private boolean requestStorage(Activity activity) {
        if (afterM()) {
            int hasPermission = activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return false;
            }
        }
        return true;
    }

    private boolean afterM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

}

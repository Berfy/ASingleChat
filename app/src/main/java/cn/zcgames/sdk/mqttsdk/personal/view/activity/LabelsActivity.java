package cn.zcgames.sdk.mqttsdk.personal.view.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.berfy.sdk.mvpbase.base.CommonActivity;
import cn.zcgames.sdk.im.R;
import cn.zcgames.sdk.mqttsdk.personal.presenter.LabelsPresenter;
import cn.zcgames.sdk.mqttsdk.personal.view.iView.ILabelsView;

public class LabelsActivity extends CommonActivity<ILabelsView, LabelsPresenter> implements ILabelsView{
    @BindView(R.id.et_input_note)
    EditText mETNote;
    @BindView(R.id.iv_delete_input)
    ImageView mIVDelete;
    private String note;
    private String uId;

    public static void launch(Context context, String uId, String note){
        Intent intent = new Intent(context, LabelsActivity.class);
        intent.putExtra("note", note);
        intent.putExtra("id", uId);
        context.startActivity(intent);
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_labels;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        showTitleBar();
        getTitleBar().setTitle(getString(R.string.set_note));
        getTitleBar().setOnBackListener(view ->{
            if(!TextUtils.equals(note, mETNote.getText().toString().trim())){
                showSaveDialog();
            }else{
                finishCurrActivity(note);
            }
        });
        getTitleBar().setRightTitle(true, getString(R.string.sure), view -> {
            if(!TextUtils.equals(note, mETNote.getText().toString().trim())){
                mPresenter.perosnSetLabels(uId, mETNote.getText().toString().trim());
            }else{
                finishCurrActivity(note);
            }
        });
        darkStatusBar(false);
        note = getIntent().getStringExtra("note");
        uId = getIntent().getStringExtra("id");
        mETNote.setText(note);

        initListener();
    }

    private void initListener() {
        mETNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(TextUtils.isEmpty(editable.toString().trim())){
                    mIVDelete.setVisibility(View.INVISIBLE);
                }else{
                    mIVDelete.setVisibility(View.VISIBLE);
                }
            }
        });

        mETNote.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(TextUtils.isEmpty(mETNote.getText().toString().trim())){
                    mIVDelete.setVisibility(View.INVISIBLE);
                }else{
                    mIVDelete.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });
    }

    @Override
    public LabelsPresenter initPresenter() {
        return new LabelsPresenter();
    }

    @Override
    public void setLabelsData(boolean isOk, String data) {
        if(isOk){
            finishCurrActivity(data);
        }
    }

    @Override
    public void hiddenLoadingView(String msg) {

    }

    @Override
    public void showLoadingView(String msg) {

    }

    @OnClick({R.id.iv_delete_input})
    public void onClickView(View view){
        switch (view.getId()){
            case R.id.iv_delete_input:
                mETNote.setText("");
                break;
        }
    }

    private void showSaveDialog() {
        mCommonDialog.showDialog(getString(R.string.save_edit),
                getString(R.string.no_save), getString(R.string.save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCommonDialog.dismiss();
                        finishCurrActivity(note);
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCommonDialog.dismiss();
                        mPresenter.perosnSetLabels(uId, mETNote.getText().toString().trim());
                    }
        });
    }

    public void finishCurrActivity(String name){
        Intent intent = new Intent();
        intent.putExtra("reName", name);
        setResult(0,intent);
        finish();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if(!TextUtils.equals(note, mETNote.getText().toString().trim())){
            showSaveDialog();
        }else{
            finishCurrActivity(note);
        }
    }

}

package cn.zcgames.sdk.mqttsdk.contacts.view.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.berfy.sdk.mvpbase.base.CommonActivity;
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.berfy.sdk.mvpbase.util.LogF;
import cn.berfy.sdk.mvpbase.util.ToastUtil;
import cn.berfy.sdk.mvpbase.view.BaseRecyclerView;
import cn.zcgames.sdk.im.R;
import cn.zcgames.sdk.mqttsdk.app.MyApplication;
import cn.zcgames.sdk.mqttsdk.contacts.adapter.AddFriendResultAdapter;
import cn.zcgames.sdk.mqttsdk.contacts.model.SortModel;
import cn.zcgames.sdk.mqttsdk.contacts.presenter.ContactPresenter;
import cn.zcgames.sdk.mqttsdk.contacts.view.iView.IContactView;
import cn.zcgames.sdk.mqttsdk.personal.view.activity.PersonInfoActivity;
import cn.zcgames.sdk.mqttsdk.zxing.decode.CaptureActivity;

public class AddFriendActivity extends CommonActivity<IContactView, ContactPresenter> implements IContactView {
    public static final String TAG = "AddFriendActivity";
    public static final int REQUEST_CODE_SCAN = 11;

    @BindView(R.id.et_search)
    EditText mEtSearch;
    @BindView(R.id.rv_search_result)
    BaseRecyclerView mRvSearchResult;
    @BindView(R.id.tv_scan_code)
    TextView mTvScanCode;

    private AddFriendResultAdapter resultAdapter;
    private LinearLayoutManager llManager;
    private List<SortModel> userList = new ArrayList<>();
    private int pg = 0;
    private boolean isLoad = false;
    private String mechId, hostId;

    public static void launcher(Context context) {
        Intent intent = new Intent(context, AddFriendActivity.class);
        context.startActivity(intent);
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_add_friend_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mechId = MyApplication.getCurrLoginUser().getPlayer().getMerchid();
        hostId = MyApplication.getCurrLoginUser().getPlayer().getId();
    }

    @Override
    public void initView() {
        darkStatusBar(false);
        showTitleBar();
        getTitleBar().setTitle(getString(R.string.add_friend));
        initRecyclerView();
    }


    private void initRecyclerView() {
        llManager = new LinearLayoutManager(this);
        mRvSearchResult.setLayoutManager(llManager);
        mRvSearchResult.getItemAnimator().setChangeDuration(0);//解决更新某一条数据时闪屏问题
        resultAdapter = new AddFriendResultAdapter(this, userList);
        mRvSearchResult.setAdapter(resultAdapter);

        resultAdapter.setOnListener(new AddFriendResultAdapter.OnListener() {
            @Override
            public void headClickListener(View v, int pos) {
                PersonInfoActivity.launch(AddFriendActivity.this, userList.get(pos).getId());
            }

            @Override
            public void addClickListener(View v, int pos) {
                addFriend(mechId, hostId, userList.get(pos).getId(), true);
            }
        });

        mRvSearchResult.setLoadMoreDataListener(new BaseRecyclerView.LoadMoreDataListener() {
            @Override
            public void loadMoreData() {
                isLoad = true;
                getData();
            }
        });
    }


    private void getData() {
        mPresenter.searchFriend(mEtSearch.getText().toString(), String.valueOf(pg));
    }


    private void addFriend(String merch_id, String host_id, String guest_id, boolean isList) {
        mPresenter.addFriend(merch_id, host_id, guest_id, isList);
    }


    @Override
    public ContactPresenter initPresenter() {
        return new ContactPresenter();
    }


    @OnClick({R.id.iv_clear, R.id.tv_scan_code, R.id.tv_search})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.iv_clear://清除
                mEtSearch.setText("");
                mTvScanCode.setVisibility(View.VISIBLE);
                mRvSearchResult.setVisibility(View.GONE);
                break;
            case R.id.tv_scan_code://扫描二维码
                //动态权限申请
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
                } else {
                    goScan();
                }
                break;
            case R.id.tv_search:
                if("".equals(mEtSearch.getText().toString().trim())){
                    ToastUtil.getInstances().showShort(getString(R.string.please_input_search_content));
                }else {
                    pg=0;
                    isLoad = false;
                    getData();
                }
                break;
        }
    }


    private void goScan() {
        Intent intent = new Intent(this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SCAN);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                //返回的文本内容
                Bundle bundle = data.getExtras();
                String scanResult = bundle.getString("result");
                LogF.d(TAG, "==uid==" + scanResult);
                addFriend(mechId, hostId, scanResult, false);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //扫码
                    goScan();
                } else {
                    ToastUtil.getInstances().showShort("你拒绝了权限申请，无法打开相机扫码哟！");
                }
                break;
            default:
        }
    }

    @Override
    public void getContactData(boolean isOk, List<SortModel> data, boolean isFirst) {
    }

    @Override
    public void addFriend(boolean isOk, String uid, boolean isList) {
        if (isOk) {
//            if (isList) {
//                //更新适配器,标记为已添加
//                for (int i = 0; i < userList.size(); i++) {
//                    if (uid.equals(userList.get(i).getId())) {
//                        userList.get(i).setStatus("2");
//                        resultAdapter.notifyItemChanged(i);
//                    }
//                }
//            }
            ToastUtil.getInstances().showShort(getString(R.string.add_success));
            //发送eventbus,刷新通讯录
//            EventBus.getDefault().post(new UpdateContactEvent(true));
            finish();
        } else {
            ToastUtil.getInstances().showShort(getString(R.string.add_failure));
        }
    }

    @Override
    public void searchFriend(boolean isOk, List<SortModel> data) {
        mRvSearchResult.setVisibility(View.VISIBLE);
        mTvScanCode.setVisibility(View.GONE);
        if (mRvSearchResult != null) {
            mRvSearchResult.finishLoadMore();
        }

        if (isOk) {
            if (pg == 0) {
                userList.clear();
            }
            if (data != null && data.size() > 0) {
                userList.addAll(data);
                pg++;
            } else {
                if (isLoad) {
                    ToastUtil.getInstances().showShort("没有更多啦~");
                }
            }
        } else {
            LogF.d(TAG, "===搜索失败");
        }
        resultAdapter.notifyDataSetChanged();


//        if (isOk) {
//            userList.clear();
//            userList.addAll(data);
//            mRvSearchResult.setVisibility(View.VISIBLE);
//            mTvScanCode.setVisibility(View.GONE);
//            resultAdapter.notifyDataSetChanged();
//        } else {
//            LogF.d(TAG, "===搜索失败");
//        }
    }

    @Override
    public void hiddenLoadingView(@org.jetbrains.annotations.Nullable String msg) {
    }

    @Override
    public void showLoadingView(@org.jetbrains.annotations.Nullable String msg) {
    }
}

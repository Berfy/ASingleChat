package cn.zcgames.sdk.mqttsdk.contacts.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.berfy.sdk.mvpbase.base.CommonActivity;
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.berfy.sdk.mvpbase.util.LogF;
import cn.berfy.sdk.mvpbase.util.ToastUtil;
import cn.zcgames.sdk.im.R;
import cn.zcgames.sdk.mqttsdk.contacts.adapter.SortAdapter;
import cn.zcgames.sdk.mqttsdk.contacts.model.SortModel;
import cn.zcgames.sdk.mqttsdk.contacts.util.PinyinComparator;
import cn.zcgames.sdk.mqttsdk.contacts.util.PinyinUtils;
import cn.zcgames.sdk.mqttsdk.contacts.util.SideBar;
import cn.zcgames.sdk.mqttsdk.personal.view.activity.PersonInfoActivity;


/**
 * 群聊成员
 */
public class GroupChatActivity extends CommonActivity {
    public static final String TAG = "GroupChatActivity";

    @BindView(R.id.et_search)
    EditText mEtSearch;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.friend_dialog)
    TextView mTvDialog;
    @BindView(R.id.sidebar)
    SideBar mSideBar;

    /**
     * 根据拼音来排列RecyclerView里面的数据类
     */
    private PinyinComparator pinyinComparator;
    private SortAdapter adapter;
    private LinearLayoutManager manager;
    private List<SortModel> sourceDateList = new ArrayList<>();
    private List<String> nameList = new ArrayList<>();//昵称集合
    private ArrayList<String> selectedUids = new ArrayList<>();//已选择的uid集合
    private int isAdd;//0=添加，1=删除


    /**
     * 跳转
     * @param context
     * @param sourceDateList  源数据
     * @param isAdd    0=添加，1=删除
     * @param REQUEST_CODE_OPTION  返回码
     */
    public static void luncher(Activity context, List<SortModel> sourceDateList,int isAdd, int REQUEST_CODE_OPTION) {
        Intent intent = new Intent(context, GroupChatActivity.class);
        intent.putExtra("isAdd", isAdd);
        intent.putExtra("data", (Serializable) sourceDateList);
        context.startActivityForResult(intent, REQUEST_CODE_OPTION);
    }


    @Override
    public int getContentViewId() {
        return R.layout.activity_group_chat_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        isAdd = getIntent().getIntExtra("isAdd", 0);
        sourceDateList= (List<SortModel>) getIntent().getSerializableExtra("data");
    }

    @Override
    public void initView() {
        //设置标题
        setTitleData();
        getData();
        initSilder();
    }

    @Override
    public BasePresenter initPresenter() {
        return null;
    }


    private void setTitleData() {
        showTitleBar();
        getTitleBar().setRightTitle(true, getString(R.string.complete));
        getTitleBar().setTitle(getString(R.string.group_chat_user));
        getTitleBar().setRightClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtil.getInstances().showShort(selectedUids.size() + "");
                //回传已选择的uid列表
                Intent resultIntent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("uidList", selectedUids);
                resultIntent.putExtras(bundle);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }


    private void updateRightTitle() {
        if (isAdd == 0) {//添加
            getTitleBar().setRightTitle(true, getString(R.string.add) + "(" + selectedUids.size() + ")");
        } else {//删除
            getTitleBar().setRightTitle(true, getString(R.string.delete) + "(" + selectedUids.size() + ")");
        }
    }


    //获取数据源
    private void getData() {
        if (sourceDateList != null && sourceDateList.size() > 0) {
            for (int i = 0; i < sourceDateList.size(); i++) {
                nameList.add(sourceDateList.get(i).getNickname());
            }
        }
    }


    private void initSilder() {
        pinyinComparator = new PinyinComparator();
        mSideBar.setTextView(mTvDialog);

        //设置右侧SideBar触摸监听
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    manager.scrollToPositionWithOffset(position, 0);
                }
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        sourceDateList = filledData(nameList);

        setSliderState();

        // 根据a-z进行排序源数据
        Collections.sort(sourceDateList, pinyinComparator);
        //RecyclerView社置manager
        manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.getItemAnimator().setChangeDuration(0);//解决更新某一条数据时闪屏问题
        adapter = new SortAdapter(this, sourceDateList, true);
        mRecyclerView.setAdapter(adapter);
        //item点击事件
        adapter.setOnItemClickListener(new SortAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String uid) {
//                PersonInfoActivity.launch(mContext, sourceDateList.get(position).getId());
                PersonInfoActivity.launch(mContext, uid);
            }

            @Override
            public void updateTitle(List<String> selectUids) {
                selectedUids.clear();
                selectedUids.addAll(selectUids);
                updateRightTitle();
            }
        });

        //根据输入框输入值的改变来过滤搜索
        mEtSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
                filterData(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    private void setSliderState() {
        if (sourceDateList != null && sourceDateList.size() > 0) {
            mSideBar.setVisibility(View.VISIBLE);
        } else {
            mSideBar.setVisibility(View.GONE);
        }
    }


    /**
     * 为RecyclerView填充数据
     *
     * @param list
     * @return
     */
    private List<SortModel> filledData(List<String> list) {
        List<SortModel> mSortList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            SortModel sortModel = sourceDateList.get(i);
            sortModel.setNickname(list.get(i));
            //汉字转换成拼音
            String pinyin = PinyinUtils.getPingYin(list.get(i));
            String sortString = pinyin.substring(0, 1).toUpperCase();

            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                sortModel.setLetters(sortString.toUpperCase());
            } else {
                sortModel.setLetters("#");
            }

            mSortList.add(sortModel);
        }
        return mSortList;

    }

    /**
     * 根据输入框中的值来过滤数据并更新RecyclerView
     *
     * @param filterStr
     */
    private void filterData(String filterStr) {
        List<SortModel> filterDateList = new ArrayList<>();

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = sourceDateList;
        } else {
            filterDateList.clear();
            for (SortModel sortModel : sourceDateList) {
                String name = sortModel.getNickname();
                if (name.indexOf(filterStr.toString()) != -1 ||
                        PinyinUtils.getFirstSpell(name).startsWith(filterStr.toString())
                        //不区分大小写
                        || PinyinUtils.getFirstSpell(name).toLowerCase().startsWith(filterStr.toString())
                        || PinyinUtils.getFirstSpell(name).toUpperCase().startsWith(filterStr.toString())
                        ) {
                    filterDateList.add(sortModel);
                }
            }
        }

        // 根据a-z进行排序
        Collections.sort(filterDateList, pinyinComparator);
        adapter.updateList(filterDateList);
    }


    @OnClick({R.id.iv_clear, R.id.tv_search})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.iv_clear:
                mEtSearch.setText("");
                break;
            case R.id.tv_search://搜索
                break;
        }
    }
}

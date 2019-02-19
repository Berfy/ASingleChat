package cn.zcgames.sdk.mqttsdk.contacts.view.fragment;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.berfy.sdk.mvpbase.base.CommonFragment;
import cn.berfy.sdk.mvpbase.util.GsonUtil;
import cn.berfy.sdk.mvpbase.util.LogF;
import cn.berfy.sdk.mvpbase.util.ToastUtil;
import cn.berfy.service.im.cache.db.tab.ContactModel;
import cn.berfy.service.im.manager.ContactManager;
import cn.berfy.service.im.model.MessageCustom;
import cn.berfy.service.im.model.MessageCustomType;
import cn.polaris.mqttuikit.view.fragment.MessageFragment;
import cn.zcgames.sdk.im.R;
import cn.zcgames.sdk.mqttsdk.app.MyApplication;
import cn.zcgames.sdk.mqttsdk.contacts.adapter.SortAdapter;
import cn.zcgames.sdk.mqttsdk.contacts.event.UpdateContactEvent;
import cn.zcgames.sdk.mqttsdk.contacts.event.UpdateRemarkEvent;
import cn.zcgames.sdk.mqttsdk.contacts.model.SortModel;
import cn.zcgames.sdk.mqttsdk.contacts.presenter.ContactPresenter;
import cn.zcgames.sdk.mqttsdk.contacts.util.PinyinComparator;
import cn.zcgames.sdk.mqttsdk.contacts.util.PinyinUtils;
import cn.zcgames.sdk.mqttsdk.contacts.util.SideBar;
import cn.zcgames.sdk.mqttsdk.contacts.view.activity.AddFriendActivity;
import cn.zcgames.sdk.mqttsdk.contacts.view.iView.IContactView;
import cn.zcgames.sdk.mqttsdk.home.view.activity.MainActivity;
import cn.zcgames.sdk.mqttsdk.message.activity.GroupListActivity;
import cn.zcgames.sdk.mqttsdk.message.activity.NewFriendsActivity;
import cn.zcgames.sdk.mqttsdk.personal.view.activity.PersonInfoActivity;


/**
 * 通讯录页面
 */
public class ContactsFragment extends CommonFragment<IContactView, ContactPresenter> implements IContactView {
    public static final String TAG = "ContactsFragment";

    @BindView(R.id.et_search)
    EditText mEtSearch;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.friend_dialog)
    TextView mTvDialog;
    @BindView(R.id.sidebar)
    SideBar mSideBar;
    @BindView(R.id.rl_new_friend_tip)
    RelativeLayout mRlNewFriendTip;//有新朋友时小红点提示

    /**
     * 根据拼音来排列RecyclerView里面的数据类
     */
    private PinyinComparator pinyinComparator;
    private SortAdapter adapter;
    private LinearLayoutManager manager;
    private List<SortModel> serverList = new ArrayList<>();
    private List<SortModel> sourceDateList = new ArrayList<>();
    private List<String> nameList = new ArrayList<>();//昵称集合

    private ContactManager contactManager;
    private List<ContactModel> localList = new ArrayList<>();
    private boolean isShowRed;

    public static ContactsFragment newInstance(boolean isShowRed) {
        ContactsFragment contactFragment = new ContactsFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isShowRed", isShowRed);
        contactFragment.setArguments(bundle);
        return contactFragment;
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        isShowRed = getArguments().getBoolean("isShowRed");
    }

    @Override
    public int getContentViewId() {
        return R.layout.fragment_main_contacts;
    }


    @Override
    public void initView() {
        mRlNewFriendTip.setVisibility(isShowRed ? View.VISIBLE : View.GONE);

        EventBus.getDefault().register(this);
        contactManager = ContactManager.Companion.getInstance();
        //设置标题
        setTitleData();
//        getNetData(true);
        getLocalData();
        initSilder();

        LogF.d(TAG, "==" + isShowRed);
    }

    @Override
    public ContactPresenter initPresenter() {
        return new ContactPresenter();
    }


    private void setTitleData() {
        showTitleBar();
        getTitleBar().setLeftIcon(false);
        darkStatusBar(false);
        getTitleBar().setTitleColor(ContextCompat.getColor(mContext, cn.polaris.mqttuikit.R.color.white_normal));
        getTitleBar().setTitle(getString(R.string.home_nav3));
    }

    //显示小红点
    public void showRedPoint(boolean isShow) {
        mRlNewFriendTip.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }


    //显示小红点
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateRedPoint(MessageCustom message) {
        LogF.d(TAG, "==message==" + message.getCus_type());
        if (message.getCus_type() == MessageCustomType.TYPE_ADD_FRIEND) {
            showRedPoint(true);
        } else if (message.getCus_type() == MessageCustomType.TYPE_AGREE_ADD_FRIEND) {
            getNetData(true);//刷新
        }
    }

    //当添加或删除时刷新列表
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshList(UpdateContactEvent event) {
        LogF.d(TAG, "==UpdateContactEvent==");

        if (event.isRefresh()) {
            getNetData(true);//刷新
        }
    }

    //备注修改后，刷新
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshRemark(UpdateRemarkEvent event) {
        LogF.d(TAG, "==UpdateRemarkEvent==");
        SortModel model = event.getContactModel();
        if (model != null) {
            for (int i = 0; i < sourceDateList.size(); i++) {
                if (model.getId().equals(sourceDateList.get(i).getId())) {
                    sourceDateList.remove(i);
                    sourceDateList.add(i,model);
                }
            }
            setNameList(sourceDateList);
            setSliderState();
            if (adapter != null) {
                adapter.updateList(sourceDateList);
            }
        }
    }


    //从本地取数据
    private void getLocalData() {
        if (contactManager != null) {
            localList = contactManager.getContactList();
            LogF.d(TAG, "====" + localList.size() + "通讯录表" + GsonUtil.getInstance().toJson(localList));
            if (localList != null && localList.size() > 0) {
                for (int i = 0; i < localList.size(); i++) {
                    SortModel model = new SortModel();
                    model.setId(localList.get(i).uid);
                    model.setAvatar(localList.get(i).avatar);
                    model.setNickname(localList.get(i).name);
                    model.setRemark(localList.get(i).remark);
                    sourceDateList.add(model);
                }

                setNameList(sourceDateList);
            }
            getNetData(true);
        }
    }


    //设置昵称集合，用来排序
    private void setNameList(List<SortModel> dataList){
        if (dataList != null && dataList.size() > 0) {
            nameList.clear();
            for (int i = 0; i < dataList.size(); i++) {
                if (!TextUtils.isEmpty(dataList.get(i).getRemark())) {
                    nameList.add(dataList.get(i).getRemark());
                } else {
                    nameList.add(dataList.get(i).getNickname());
                }
            }
        }
    }


    //第一次从服务器获取,以后只是获取覆盖数据库数据
    private void getNetData(boolean isFirst) {
        mPresenter.getContacts(MyApplication.getCurrLoginUser().getPlayer().getMerchid(),
                MyApplication.getCurrLoginUser().getPlayer().getId(), isFirst);
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
//        sourceDateList = filledData(nameList);

        setSliderState();

        // 根据a-z进行排序源数据
//        Collections.sort(sourceDateList, pinyinComparator);
        //RecyclerView社置manager
        manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        adapter = new SortAdapter(getActivity(), sourceDateList, false);
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

    //重置数据，排序
    private void setSliderState() {
        sourceDateList = filledData(nameList);
        if (sourceDateList != null && sourceDateList.size() > 0) {
            mSideBar.setVisibility(View.VISIBLE);
        } else {
            mSideBar.setVisibility(View.GONE);
        }
        // 根据a-z进行排序源数据
        Collections.sort(sourceDateList, pinyinComparator);
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
//            SortModel sortModel = new SortModel();
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


    @OnClick({R.id.iv_clear, R.id.tv_search, R.id.tv_new_friend, R.id.tv_group_chat, R.id.tv_add_friend})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.iv_clear:
                mEtSearch.setText("");
                break;
            case R.id.tv_search://搜索
//                getNetData(true);
                LogF.d(TAG, "===" + contactManager.getContactList().size());
                break;
            case R.id.tv_new_friend://新朋友
                NewFriendsActivity.launch(getActivity());
                ((MainActivity) getActivity()).updateConstantRedTip(false);
                showRedPoint(false);
                break;
            case R.id.tv_group_chat://群聊
                GroupListActivity.launch(getActivity());
                break;
            case R.id.tv_add_friend://添加好友
                AddFriendActivity.launcher(getActivity());
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void getContactData(boolean isOk, List<SortModel> data, boolean isFirst) {
        if (isOk) {
            LogF.d(TAG, "==获取列表成功");
            if (null != data) {
                serverList.clear();
                serverList.addAll(data);

                if (isFirst) {
                    sourceDateList.clear();
                    setNameList(serverList);
                    sourceDateList.addAll(serverList);
                    setSliderState();
                    if (adapter != null) {
                        adapter.updateList(sourceDateList);
                    }
                }

                //存入数据库
                if (contactManager != null && isFirst) {
                    contactManager.deleteList();
                }
                for (int i = 0; i < serverList.size(); i++) {
                    ContactModel contact = new ContactModel();
                    contact.uid = serverList.get(i).getId();
                    contact.avatar = serverList.get(i).getAvatar();
                    contact.name = serverList.get(i).getNickname();
                    contact.remark = serverList.get(i).getRemark();
                    localList.add(contact);
                }
                if (contactManager != null) {
                    contactManager.insertContactAsync(localList);
                }
                LogF.d(TAG, "==netList==" + isFirst);
            }
        } else {
            LogF.d(TAG, "==获取列表失败");
        }
    }

    @Override
    public void addFriend(boolean isOk, String uid, boolean isList) {
    }

    @Override
    public void searchFriend(boolean isOk, List<SortModel> data) {
    }


    @Override
    public void hiddenLoadingView(@Nullable String msg) {

    }

    @Override
    public void showLoadingView(@Nullable String msg) {

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}

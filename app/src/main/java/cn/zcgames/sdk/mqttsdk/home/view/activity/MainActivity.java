package cn.zcgames.sdk.mqttsdk.home.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hjq.permissions.OnPermission;
import com.hjq.permissions.XXPermissions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.berfy.sdk.mvpbase.base.ActivityManager;
import cn.berfy.sdk.mvpbase.base.CommonActivity;
import cn.berfy.sdk.mvpbase.iview.IBaseView;
import cn.berfy.sdk.mvpbase.model.User;
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.berfy.sdk.mvpbase.util.AppUtils;
import cn.berfy.sdk.mvpbase.util.GsonUtil;
import cn.berfy.sdk.mvpbase.util.LogF;
import cn.berfy.sdk.mvpbase.util.ToastUtil;
import cn.berfy.service.im.manager.IMManager;
import cn.berfy.service.im.manager.i.OnConnectStatusCallback;
import cn.berfy.service.im.manager.i.OnMessageListener;
import cn.berfy.service.im.model.Message;
import cn.berfy.service.im.model.MessageCustom;
import cn.berfy.service.im.model.MessageCustomType;
import cn.polaris.mqttuikit.view.fragment.MessageFragment;
import cn.zcgames.sdk.im.R;
import cn.zcgames.sdk.mqttsdk.app.MyApplication;
import cn.zcgames.sdk.mqttsdk.contacts.view.activity.AddFriendActivity;
import cn.zcgames.sdk.mqttsdk.contacts.view.fragment.ContactsFragment;
import cn.zcgames.sdk.mqttsdk.home.view.fragment.HomeFragment;
import cn.zcgames.sdk.mqttsdk.message.activity.ChatActivity;
import cn.zcgames.sdk.mqttsdk.message.activity.GroupListActivity;
import cn.zcgames.sdk.mqttsdk.message.model.FriendBean;
import cn.zcgames.sdk.mqttsdk.message.view.model.FriendPassEvent;
import cn.zcgames.sdk.mqttsdk.personal.view.fragment.MineFragment;

/**
 * 主页面
 */
public class MainActivity extends CommonActivity<IBaseView, BasePresenter<IBaseView>> implements IBaseView,
        OnConnectStatusCallback {

    public static final String TAG = "MainActivity";
    private IMManager mImManager;
    private long exitTime = 0;//退出activity计时

    //底部导航栏
    @BindView(R.id.main_nv_home)
    View mNavHome;
    @BindView(R.id.main_nv_im)
    View mNavIm;
    @BindView(R.id.main_nv_contacts)
    View mNavContacts;
    @BindView(R.id.main_nv_mine)
    View mNavMine;

    //未读小红点
    @BindView(R.id.tv_sys_msg_view)
    TextView mTvSysMsgView;//聊天消息未读小红点

    // 未读小红点
    @BindView(R.id.tv_contacts_msg_view)
    TextView mTvContactsMsgView;//通讯录消息未读小红点

    //页面pos
    private int mCurrentPage = -1;//当前页面显示位置
    //首页
    private HomeFragment mHomeFragment;
    //聊天
    private MessageFragment mMessageFragment;
    //通讯录
    private ContactsFragment contactsFragment;
    //我的
    private MineFragment mineFragment;

    private OnMessageListener messageListener;

    private final String FRAGMENT_TAG_HOME = "home";
    private final String FRAGMENT_TAG_MESSAGE = "message";
    private final String FRAGMENT_TAG_CONTACTS = "contacts";
    private final String FRAGMENT_TAG_MINE = "mine";

    private final String[] fragmentTags = new String[]{FRAGMENT_TAG_HOME, FRAGMENT_TAG_MESSAGE,
            FRAGMENT_TAG_CONTACTS, FRAGMENT_TAG_MINE};

    private final int[] tabsNormalBackIds = new int[]{R.drawable.tab_home, R.drawable.tab_message,
            R.drawable.tab_contacts, R.drawable.tab_me};

    private final int[] tabsActiveBackIds = new int[]{R.drawable.tab_home_active, R.drawable.tab_message_active,
            R.drawable.tab_contacts_active, R.drawable.tab_me_active};

    private final int[] iconId = {R.id.main_nv_home_icon, R.id.main_nv_im_icon, R.id.main_nv_contacts_icon,
            R.id.main_nv_mine_icon};

    @Override
    public int getContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
    }

    @Override
    public void initView() {
        checkPermission();
        initMqtT();
        EventBus.getDefault().register(this);
        mNavHome.performClick();//默认显示首页
    }

    @Override
    public BasePresenter<IBaseView> initPresenter() {
        return null;
    }

    private void checkPermission() {
        XXPermissions.with(this)
                .permission(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        , android.Manifest.permission.READ_PHONE_STATE
                ).request(new OnPermission() {
            @Override
            public void hasPermission(List<String> granted, boolean isAll) {

            }

            @Override
            public void noPermission(List<String> denied, boolean quick) {
                ToastUtil.getInstances().showShort("你拒绝了" + GsonUtil.getInstance().toJson(denied) + "权限");
                XXPermissions.gotoPermissionSettings(mContext);
            }
        });
    }

    @OnClick({R.id.main_nv_home, R.id.main_nv_im, R.id.main_nv_contacts, R.id.main_nv_mine})
    void onViewClicked(View view) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        hideFragments(manager, transaction);
        switch (view.getId()) {
            case R.id.main_nv_home:
                switchMainBt(0);
                if (mHomeFragment == null) {
                    mHomeFragment = new HomeFragment();
                    transaction.add(R.id.main_rl_fragment_container, mHomeFragment, FRAGMENT_TAG_HOME);
                }
                transaction.show(mHomeFragment);
                mHomeFragment.getUrl();
                break;
            case R.id.main_nv_im:
                switchMainBt(1);
                if (mMessageFragment == null) {
                    mMessageFragment = MessageFragment.newInstance();
                    transaction.add(R.id.main_rl_fragment_container, mMessageFragment, FRAGMENT_TAG_MESSAGE);
                    setListener();
                }
                transaction.show(mMessageFragment);
                break;
            case R.id.main_nv_contacts:
                switchMainBt(2);
                if (contactsFragment == null) {
                    contactsFragment = ContactsFragment.newInstance(false);
                    transaction.add(R.id.main_rl_fragment_container, contactsFragment, FRAGMENT_TAG_CONTACTS);
                }
                transaction.show(contactsFragment);
                break;
            case R.id.main_nv_mine:
                switchMainBt(3);
                if (mineFragment == null) {
                    mineFragment = new MineFragment();
                }
                if (!mineFragment.isAdded())
                    transaction.add(R.id.main_rl_fragment_container, mineFragment, FRAGMENT_TAG_MINE);

                transaction.show(mineFragment);
                break;
        }
        transaction.commit();
    }

    /**
     * 给MessageFragment开出的方法
     */
    private void setListener() {
        mMessageFragment.setMsgMenuClickListener(new MessageFragment.MsgMenuClickListener() {
            @Override
            public void addFriend() {
                AddFriendActivity.launcher(mContext);
            }

            @Override
            public void updateRedPoint(boolean isShow) {
                updateChatRedTip(isShow);
            }

            @Override
            public void startChatGroup() {
                if (!AppUtils.checkJump()) return;
                GroupListActivity.launch(mContext);
            }

            @Override
            public void launchChatActivity(String toId, String title, int conversionType) {
                if (!AppUtils.checkJump()) return;
                ChatActivity.launch(mContext, conversionType, toId, title);
            }
        });
    }

    /**
     * 切换选中的主菜单
     */
    public void switchMainBt(int pos) {
        if (mCurrentPage == pos) return;
        ((ImageView) findViewById(iconId[pos])).setImageResource(tabsActiveBackIds[pos]);
        for (int i = 0; i < iconId.length; i++) {
            if (i != pos)
                ((ImageView) findViewById(iconId[i])).setImageResource(tabsNormalBackIds[i]);
        }
        mCurrentPage = pos;
    }

    //建立mqtT连接
    private void initMqtT() {
        mImManager = IMManager.Companion.getInstance();
        mImManager.addConnectListener(this);
        messageListener = new OnMessageListener() {
            @Override
            public void newMessage(@Nullable Message message) {
                if (message == null) return;
                updateChatRedTip(true);
            }

            @Override
            public void systemMessage(@Nullable MessageCustom message) {
                if (message == null) return;
                LogF.d(TAG, "==" + message.getCus_type());
                //自定义消息通知
                LogF.d("111111", "接受的消息===" + message);
                EventBus.getDefault().post(message);
                if (message.getCus_type() == MessageCustomType.TYPE_AGREE_ADD_FRIEND) {
                    updateChatRedTip(true);
                } else if (message.getCus_type() == MessageCustomType.TYPE_ADD_FRIEND) {
                    if (contactsFragment == null) {
                        contactsFragment = ContactsFragment.newInstance(true);
                    }
                    updateConstantRedTip(true);
                }
            }

            @Override
            public void refreshConversation() {

            }

            @Override
            public void sendMessageStatus(@Nullable String message, boolean isSuc) {

            }
        };
        mImManager.addMessageListener(messageListener);
    }

    //检查mqtT连接状态
    private void checkStatus() {
        User currLoginUser = MyApplication.getCurrLoginUser();
        if (null == currLoginUser) return;
        if (null != mImManager) {
            LogF.d(TAG, "IM连接状态 isConnectting =>" + mImManager.isConnectting()
                    + "isConnected=>" + mImManager.isConnected() + " isManualClose=>"
                    + mImManager.isManualClose());
            if (!mImManager.isConnectting() && !mImManager.isConnected()) {
                mImManager.setUserInfo(currLoginUser.getPlayer().getId(), "");
                mImManager.startConnect();
            }
        }
    }


    private void hideFragments(FragmentManager fragmentManager, FragmentTransaction transaction) {
        for (String fragmentTag : fragmentTags) {
            Fragment fragment = fragmentManager.findFragmentByTag(fragmentTag);
            if (fragment != null && fragment.isVisible()) {
                transaction.hide(fragment);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        IMManager.Companion.getInstance().clearNotify("chat");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)//用户自己通过好友申请手动更新会话列表
    public void applyNewFirend(FriendPassEvent event) {
        if (event == null || event.getPerson() == null) return;
        boolean refresh = event.isRefresh();
        if (refresh) {
            FriendBean.ContactBean person = event.getPerson();//新好友信息
            String toId = person.getId();
//            if (mMessageFragment != null && !TextUtils.isEmpty(toId)) {
//                Conversation conversation = new Conversation(toId, MessageType.TYPE_P2P);
//                conversation.setLastMessageTime(System.currentTimeMillis());
//                conversation.setLastMessage("我们已成为好友,开始聊天吧!");
//                mMessageFragment.addFriendMsg(conversation);
//                updateChatRedTip(true);//显示小红点,更新会话列表
//            }
        }
    }

    //更新聊天小红点
    private void updateChatRedTip(boolean hasNewMsg) {
        mTvSysMsgView.setVisibility(hasNewMsg ? View.VISIBLE : View.GONE);
    }

    //更新通讯录小红点
    public void updateConstantRedTip(boolean hasNewMsg) {
        mTvContactsMsgView.setVisibility(hasNewMsg ? View.VISIBLE : View.GONE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkStatus();
    }

    @Override//双击退出app事件
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - exitTime > 2000) {
                try {
                    ToastUtil.getInstances().showShort("再按一次返回键确认退出");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                exitTime = System.currentTimeMillis();
            } else {
                ActivityManager.getInstance().popActivityNotFinish(this);
                close();
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void connectStart() {
        LogF.d(TAG, "正在连接");
    }

    @Override
    public void connectSuc() {
        LogF.d(TAG, "连接成功");
    }

    @Override
    public void connectFailed(@Nullable Throwable exception) {
        LogF.d(TAG, "连接失败");
        if (null != exception) LogF.d(TAG, "失败原因==>" + exception.getMessage());
    }

    @Override
    public void disConnect(@Nullable Throwable exception) {
        LogF.d(TAG, "断开连接");
        if (null != exception) LogF.d(TAG, "断连原因==>" + exception.getMessage());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        LogF.d(TAG, "销毁了");
    }

    @Override
    public void hiddenLoadingView(@Nullable String msg) {

    }

    @Override
    public void showLoadingView(@Nullable String msg) {

    }

    private void close() {
        if (null != mImManager) {
            mImManager.removeConnectListener(this);
            mImManager.removeMessageListener(messageListener);
        }
        finish();
    }
}

package cn.berfy.service.im.cache.db.tab;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import cn.berfy.service.im.cache.db.IMDatabase;

@Table(database = IMDatabase.class)
public class ContactModel extends BaseModel {
    @Column
    @PrimaryKey
    public String uid;      // 用户id 区分登录身份

    @Column
    public String name; //昵称

    @Column
    public String avatar;   //头像

    @Column
    public String remark;  //备注

}

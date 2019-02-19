package cn.berfy.sdk.mvpbase.util;

import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.widget.EditText;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ycz on 2017/12/19.
 * use:限制输入字符个数
 */

public class WatcherText implements TextWatcher {
    private int maxLen = 0;
    private EditText editText = null;

    public WatcherText(int maxLen, EditText editText) {
        this.maxLen = maxLen;
        this.editText = editText;
    }

    public void afterTextChanged(Editable arg0) {
        // TODO Auto-generated method stub

    }

    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                  int arg3) {
        // TODO Auto-generated method stub

    }

    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        try {
            // TODO Auto-generated method stub
            Editable editable = editText.getText();
            String content = editText.getText().toString();

            byte[] bt = content.getBytes("gb2312");
            if (bt.length > maxLen) {
                ToastUtil.getInstances().showShort("输入字数超过限制");
                int selEndIndex = Selection.getSelectionEnd(editable);
                String str = editable.toString();
                byte[] bt2 = str.getBytes("gb2312");
                byte[] bt3 = subBytes(bt2, 0, maxLen);
                String newStrs = gbToString(bt3);
                String temp = String.valueOf(newStrs.charAt(newStrs.length() - 1));
                boolean flag = isContainChinese(temp);
                boolean flag2 = isContainLetters(temp);
                if (!flag && !flag2) {
                    String newStrs2 = newStrs.substring(0, newStrs.length() - 1);
                    editText.setText(newStrs2);
                } else {
                    editText.setText(newStrs);
                }
                editable = editText.getText();
                // 新字符串的长度
                int newLen = editable.length();
                // 旧光标位置超过字符串长度
                if (selEndIndex >= newLen) {
                    selEndIndex = editable.length();
                }
                //设置新光标所在的位置
                Selection.setSelection(editable, selEndIndex);
            }

        } catch (Exception e) {
            //UnsupportedEncodingException
            e.printStackTrace();
        }

    }

    private byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        System.arraycopy(src, begin, bs, 0, count);
        return bs;
    }

    private String gbToString(byte[] data) {
        String str = null;
        try {
            str = new String(data, "gb2312");
        } catch (UnsupportedEncodingException e) {
        }
        return str;
    }

    /**
     * 判断字符串是否包含中文
     *
     * @param str
     * @return
     */
    private boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        return m.find();
    }

    /**
     * 判断字符串是否包含字母
     *
     * @param str
     * @return
     */
    private boolean isContainLetters(String str) {
        for (char i = 'A'; i <= 'Z'; i++) {
            if (str.contains(String.valueOf(i))) {
                return true;
            }
        }
        for (char i = 'a'; i <= 'z'; i++) {
            if (str.contains(String.valueOf(i))) {
                return true;
            }
        }
        return false;
    }
}

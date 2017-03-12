package com.hlk.wbs.espider.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.support.v7.widget.AppCompatEditText;

/**
 * 可以监控剪切复制等操作的文本框<br />
 * 作者：Hsiang Leekwok on 2015/11/09 21:47<br />
 * 邮箱：xiang.l.g@gmail.com<br />
 */
public class ClipboardCatchedEditText extends AppCompatEditText {

    public ClipboardCatchedEditText(Context context) {
        super(context);
    }

    public ClipboardCatchedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClipboardCatchedEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * <p>
     * This is where the "magic" happens.
     * </p>
     * <p>
     * The menu used to cut/copy/paste is a normal ContextMenu, which allows us
     * to overwrite the consuming method and react on the different events.
     * </p>
     *
     * @see <a
     * href="http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/2.3_r1/android/widget/TextView.java#TextView.onTextContextMenuItem%28int%29">Original
     * Implementation</a>
     */
    @Override
    public boolean onTextContextMenuItem(int id) {
        // Do your thing:
        boolean consumed = super.onTextContextMenuItem(id);
        // React:
        switch (id) {
            case android.R.id.cut:
                if (null != mOnClipboardListener) {
                    mOnClipboardListener.onTextCut(ClipboardCatchedEditText.this);
                }
                break;
            case android.R.id.paste:
                if (null != mOnClipboardListener) {
                    mOnClipboardListener.onTextPasted(ClipboardCatchedEditText.this);
                }
                break;
            case android.R.id.copy:
                if (null != mOnClipboardListener) {
                    mOnClipboardListener.onTextCopied(ClipboardCatchedEditText.this);
                }
        }
        return consumed;
    }

    private OnClipboardListener mOnClipboardListener;

    /**
     * 为EditText设置监控剪切板操作事件的回调
     */
    public void setOnClipboardListener(OnClipboardListener l) {
        mOnClipboardListener = l;
    }

    /**
     * 处理剪切板事件
     */
    public interface OnClipboardListener {
        /**
         * 剪切事件
         */
        boolean onTextCut(View view);

        /**
         * 粘贴事件
         */
        boolean onTextPasted(View view);

        /**
         * 复制事件
         */
        boolean onTextCopied(View view);
    }
}

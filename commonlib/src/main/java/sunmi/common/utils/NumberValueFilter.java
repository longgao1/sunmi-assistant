package sunmi.common.utils;

/**
 * @author yangShiJie
 * @date 2019/8/22
 * 金额输入过滤器，限制小数点后输入位数
 * <p>
 * 默认限制小数点2位
 * 默认第一位输入小数点时，转换为0.
 * 如果起始位置为0,且第二位跟的不是".",则无法后续输入
 */

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;


public class NumberValueFilter extends DigitsKeyListener {

    private static final String TAG = "NumberValueFilter";
    private int intMaxLength;//整数位的限制位数
    private int digits = 2;

    public NumberValueFilter(int maxLength) {
        super(false, true);
        this.intMaxLength = maxLength;
    }

    public NumberValueFilter setDigits(int d) {
        digits = d;
        return this;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {
        CharSequence out = super.filter(source, start, end, dest, dstart, dend);

        // if changed, replace the source
        if (out != null) {
            source = out;
            start = 0;
            end = out.length();
        }

        int len = end - start;

        // if deleting, source is empty
        // and deleting can't break anything
        if (len == 0) {
            return source;
        }

        //以点开始的时候，自动在前面添加0
        if (source.toString().equals(".") && dstart == 0) {
            return "0.";
        }
        //如果起始位置为0,且第二位跟的不是".",则无法后续输入
        if (!source.toString().equals(".") && dest.toString().equals("0")) {
            return "";
        }

        int dlen = dest.length();

        // Find the position of the decimal .
        for (int i = 0; i < dstart; i++) {
            if (dest.charAt(i) == '.') {
                // being here means, that a number has
                // been inserted after the dot¬
                // check if the amount of digits is right
                return (dlen - (i + 1) + len > digits) ?
                        "" :
                        new SpannableStringBuilder(source, start, end);
            }
        }

        for (int i = start; i < end; ++i) {
            if (source.charAt(i) == '.') {
                // being here means, dot has been inserted
                // check if the amount of digits is right
                if ((dlen - dend) + (end - (i + 1)) > digits) {
                    return "";
                } else {
                    break;  // return new SpannableStringBuilder(source, start, end);
                }
            }
        }

        //整数length的限制
        if (!(dest.toString() + source.toString()).contains(".")) {
            return (dest.toString() + source.toString()).length() > intMaxLength ?
                    "" :
                    new SpannableStringBuilder(source, start, end);
        } else {
            String strText = dest.toString() + source.toString();
            int length = strText.length();
            if (TextUtils.equals(".", source.toString())) {
                return dest.toString().length() > intMaxLength ?
                        "" :
                        new SpannableStringBuilder(source, start, end);
            } else {
                if (length == (dstart + 1)) {
                    return dest.toString().substring(0, dest.toString().indexOf(".")).length() > intMaxLength ?
                            "" :
                            new SpannableStringBuilder(source, start, end);
                } else {
                    return strText.substring(0, strText.indexOf(".")).length() > (intMaxLength - 1) ?
                            "" :
                            new SpannableStringBuilder(source, start, end);
                }
            }
        }
        // if the dot is after the inserted part,
        // nothing can break
//        return new SpannableStringBuilder(source, start, end);
    }
}

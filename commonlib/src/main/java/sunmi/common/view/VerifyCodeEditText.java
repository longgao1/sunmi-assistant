package sunmi.common.view;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class VerifyCodeEditText extends AppCompatEditText {

    private long lastTime = 0;

    public VerifyCodeEditText(Context context) {
        super(context);
    }

    public VerifyCodeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VerifyCodeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        this.setSelection(this.getText().length());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastTime < 500) {
                    lastTime = currentTime;
                    return true;
                } else {
                    lastTime = currentTime;
                }
                break;
        }
        return super.onTouchEvent(event);
    }
}
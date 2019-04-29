package com.jurestanic.quotestore;

import android.content.Context;
import android.util.AttributeSet;

import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.views.ChipsInputEditText;

public class MyChipsInput extends ChipsInput {

    private ChipsInputEditText editText;

    public MyChipsInput(Context context) {
        super(context);
    }

    public MyChipsInput(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public ChipsInputEditText getEditText() {
        editText = new ChipsInputEditText(getContext());
        return editText;
    }

    @Override
    public void setEnabled(boolean b){
        editText.setEnabled(b);
    }
}

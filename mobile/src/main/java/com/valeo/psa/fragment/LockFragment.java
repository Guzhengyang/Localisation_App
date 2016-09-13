package com.valeo.psa.fragment;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.valeo.psa.R;
import com.valeo.psa.view.BigButtonView;
import com.valeo.psa.view.Indicator;

import java.util.Stack;

/**
 * Created by l-avaratha on 13/09/2016.
 */
public class LockFragment extends Fragment implements BigButtonView.OnPressListener {
    private BigButtonView[] bigButtonViews;
    private TextView title;
    private Indicator indicator;
    private TextView leftButton;
    private TextView rightButton;

    private Stack<String> passwordStack = null;
    private int passwordLength = 4;
    private String correctPassword = null;
    private int incorrectInputTimes = 0;
    private OnPasswordInputListener onPasswordInputListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        init(container);
        passwordStack = new Stack<>();

        Resources resources = getResources();

        indicator = (Indicator) container.findViewById(R.id.indicator);
        indicator.setPasswordLength(passwordLength);

        title = (TextView) container.findViewById(R.id.title);
        title.setTextColor(ContextCompat.getColor(getContext(), R.color.default_title_text_color));
        title.setTextSize(resources.getDimension(R.dimen.default_title_text_size));

        leftButton = (TextView) container.findViewById(R.id.left_button);
        leftButton.setTextColor(ContextCompat.getColor(getContext(), R.color.default_left_button_text_color));
        leftButton.setTextSize(resources.getDimension(R.dimen.default_left_button_text_size));
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        rightButton = (TextView) container.findViewById(R.id.right_button);
        rightButton.setTextColor(ContextCompat.getColor(getContext(), R.color.default_right_button_text_color));
        rightButton.setTextSize(resources.getDimension(R.dimen.default_right_button_text_size));
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordStack.size() > 0) {
                    passwordStack.pop();
                    indicator.delete();
                }
            }
        });
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * Init.
     */
    private void init(ViewGroup container) {
        // number password
        LayoutInflater.from(getContext()).inflate(R.layout.number_blur_lock_view, container, true);

        bigButtonViews = new BigButtonView[10];
        bigButtonViews[0] = (BigButtonView) container.findViewById(R.id.button_0);
        bigButtonViews[1] = (BigButtonView) container.findViewById(R.id.button_1);
        bigButtonViews[2] = (BigButtonView) container.findViewById(R.id.button_2);
        bigButtonViews[3] = (BigButtonView) container.findViewById(R.id.button_3);
        bigButtonViews[4] = (BigButtonView) container.findViewById(R.id.button_4);
        bigButtonViews[5] = (BigButtonView) container.findViewById(R.id.button_5);
        bigButtonViews[6] = (BigButtonView) container.findViewById(R.id.button_6);
        bigButtonViews[7] = (BigButtonView) container.findViewById(R.id.button_7);
        bigButtonViews[8] = (BigButtonView) container.findViewById(R.id.button_8);
        bigButtonViews[9] = (BigButtonView) container.findViewById(R.id.button_9);

        String[] texts = getResources().getStringArray(R.array.default_big_button_text);
        String[] subTexts = getResources().getStringArray(R.array.default_big_button_sub_text);
        for (int i = 0; i < 10; i++) {
            bigButtonViews[i].setOnPressListener(this);
            bigButtonViews[i].setText(texts[i]);
            bigButtonViews[i].setSubText(subTexts[i]);
        }

        bigButtonViews[0].setSubTextVisibility(View.GONE);
        bigButtonViews[1].setSubTextVisibility(View.INVISIBLE);
    }

    @Override
    public void onPress(String string) {
        if (correctPassword == null) {
            throw new RuntimeException("The correct password has NOT been set!");
        }
        if (passwordStack.size() >= passwordLength) return;
        passwordStack.push(string);
        indicator.add();
        StringBuilder nowPassword = new StringBuilder("");
        for (String s : passwordStack) {
            nowPassword.append(s);
        }
        String nowPasswordString = nowPassword.toString();
        if (correctPassword.equals(nowPasswordString)) {
            // correct password
            if (onPasswordInputListener != null)
                onPasswordInputListener.correct(nowPasswordString);
        } else {
            if (correctPassword.length() > nowPasswordString.length()) {
                // input right now
                if (onPasswordInputListener != null)
                    onPasswordInputListener.input(nowPasswordString);
            } else {
                // incorrect password
                if (onPasswordInputListener != null)
                    onPasswordInputListener.incorrect(nowPasswordString);
                // perform the clear animation
                incorrectInputTimes++;
                indicator.clear();
                passwordStack.clear();
            }
        }
    }

    public interface OnPasswordInputListener {
        void correct(String inputPassword);

        void incorrect(String inputPassword);

        void input(String inputPassword);
    }
}

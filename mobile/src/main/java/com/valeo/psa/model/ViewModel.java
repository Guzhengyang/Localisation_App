package com.valeo.psa.model;

import android.graphics.drawable.Drawable;

public class ViewModel {
    private final String actionTitle;
    private final ViewModelId viewModelId;
    private Drawable icon;

    public ViewModel(Drawable icon, String actionTitle, ViewModelId viewModelId) {
        this.icon = icon;
        this.actionTitle = actionTitle;
        this.viewModelId = viewModelId;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getActionTitle() {
        return actionTitle;
    }

    public ViewModelId getViewModelId() {
        return viewModelId;
    }
}

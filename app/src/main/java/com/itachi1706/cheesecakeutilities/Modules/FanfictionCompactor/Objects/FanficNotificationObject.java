package com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Objects;

/**
 * Created by Kenneth on 5/6/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Objects in CheesecakeUtilities
 */
public class FanficNotificationObject {
    String message, title;
    boolean cancellable, indeterminate;
    int progress, max;

    public FanficNotificationObject(String message, String title, boolean cancellable, int progress, int max, boolean indeterminate) {
        this.message = message;
        this.title = title;
        this.cancellable = cancellable;
        this.progress = progress;
        this.max = max;
        this.indeterminate = indeterminate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCancellable() {
        return cancellable;
    }

    public void setCancellable(boolean cancellable) {
        this.cancellable = cancellable;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public boolean isIndeterminate() {
        return indeterminate;
    }

    public void setIndeterminate(boolean indeterminate) {
        this.indeterminate = indeterminate;
    }
}

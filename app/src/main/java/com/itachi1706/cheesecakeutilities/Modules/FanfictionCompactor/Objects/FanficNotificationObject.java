package com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Objects;

/**
 * Created by Kenneth on 5/6/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Objects in CheesecakeUtilities
 */
public class FanficNotificationObject {
    String message, title, altmessage;
    boolean cancellable, indeterminate;
    int progress, max;

    public FanficNotificationObject(String message, String title, boolean cancellable, int progress, int max, boolean indeterminate) {
        this.message = message;
        this.title = title;
        this.cancellable = cancellable;
        this.progress = progress;
        this.max = max;
        this.indeterminate = indeterminate;
        this.altmessage = null;
    }

    public FanficNotificationObject(String message, String title, boolean cancellable, int progress, int max, boolean indeterminate, String altmessage) {
        this.message = message;
        this.title = title;
        this.cancellable = cancellable;
        this.progress = progress;
        this.max = max;
        this.indeterminate = indeterminate;
        this.altmessage = altmessage;
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

    public String getAltmessage() {
        return altmessage;
    }

    public void setAltmessage(String altmessage) {
        this.altmessage = altmessage;
    }

    public String getNotificationMessage() {
        return (altmessage == null) ? message : altmessage;
    }
}

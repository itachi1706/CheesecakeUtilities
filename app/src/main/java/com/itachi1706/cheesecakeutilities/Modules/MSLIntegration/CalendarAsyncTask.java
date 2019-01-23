package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration;

/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model.CalendarModel;

import java.io.IOException;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * Asynchronous task that also takes care of common needs, such as displaying progress,
 * authorization, exception handling, and notifying UI when operation succeeded.
 *
 * @author Yaniv Inbar
 */
public abstract class CalendarAsyncTask extends AsyncTask<Void, Void, Boolean> {

    public final Context context;
    public final CalendarModel model;
    public final com.google.api.services.calendar.Calendar client;

    public static final String BROADCAST_MSL_ASYNC = "com.itachi1706.cheesecakeutilities.MSL_ASYNC_MSG";

    public static final String INTENT_ERROR = "error", INTENT_DATA = "data", INTENT_EXCEPTION = "exception";

    public CalendarAsyncTask(Context context, CalendarModel model, com.google.api.services.calendar.Calendar client) {
        this.context = context;
        this.model = model;
        this.client = client;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected final Boolean doInBackground(Void... ignored) {
        try {
            doInBackground();
            return true;
        } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
            Intent except = new Intent(BROADCAST_MSL_ASYNC);
            except.putExtra(INTENT_EXCEPTION, true);
            except.putExtra(INTENT_ERROR, availabilityException);
            except.putExtra(INTENT_DATA, availabilityException.getConnectionStatusCode());
            LocalBroadcastManager.getInstance(context).sendBroadcast(except);
        } catch (UserRecoverableAuthIOException userRecoverableException) {
            Intent except = new Intent(BROADCAST_MSL_ASYNC);
            except.putExtra(INTENT_EXCEPTION, true);
            except.putExtra(INTENT_ERROR, userRecoverableException);
            except.putExtra(INTENT_DATA, userRecoverableException.getIntent());
            LocalBroadcastManager.getInstance(context).sendBroadcast(except);
        } catch (IOException e) {
            Intent except = new Intent(BROADCAST_MSL_ASYNC);
            except.putExtra(INTENT_EXCEPTION, true);
            except.putExtra(INTENT_ERROR, e);
            except.putExtra(INTENT_DATA, e);
            LocalBroadcastManager.getInstance(context).sendBroadcast(except);

        }
        return false;
    }

    @Override
    protected final void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (!success) return;
        Intent except = new Intent(BROADCAST_MSL_ASYNC);
        except.putExtra("success", true);
        except.putExtra(INTENT_DATA, getTaskAction());
        LocalBroadcastManager.getInstance(context).sendBroadcast(except);
    }

    public abstract String getTaskAction();

    abstract protected void doInBackground() throws IOException;
}
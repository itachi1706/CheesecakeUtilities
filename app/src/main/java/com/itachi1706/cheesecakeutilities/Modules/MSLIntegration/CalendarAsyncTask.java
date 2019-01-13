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

import android.os.AsyncTask;

import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import java.io.IOException;

/**
 * Asynchronous task that also takes care of common needs, such as displaying progress,
 * authorization, exception handling, and notifying UI when operation succeeded.
 *
 * @author Yaniv Inbar
 */
abstract class CalendarAsyncTask extends AsyncTask<Void, Void, Boolean> {

    final MSLActivity activity;
    final CalendarModel model;
    final com.google.api.services.calendar.Calendar client;
    //private final View progressBar;

    CalendarAsyncTask(MSLActivity activity) {
        this.activity = activity;
        model = activity.model;
        client = activity.client;
        //progressBar = activity.findViewById(R.id.title_refresh_progress);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        activity.numAsyncTasks++;
        //progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected final Boolean doInBackground(Void... ignored) {
        try {
            doInBackground();
            return true;
        } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
            activity.showGPSError(availabilityException.getConnectionStatusCode());
        } catch (UserRecoverableAuthIOException userRecoverableException) {
            activity.startActivityForResult(userRecoverableException.getIntent(), MSLActivity.REQUEST_AUTHORIZATION);
        } catch (IOException e) {
            Utils.logAndShow(activity, "GCal-Async", e);
        }
        return false;
    }

    @Override
    protected final void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        --activity.numAsyncTasks;
        if (0 == --activity.numAsyncTasks) {
            //if (0 == --activity.numAsyncTasks) {
            //progressBar.setVisibility(View.GONE);
            //}
            if (success) {
                activity.update(success);
            }
        }
    }

    abstract protected void doInBackground() throws IOException;
}
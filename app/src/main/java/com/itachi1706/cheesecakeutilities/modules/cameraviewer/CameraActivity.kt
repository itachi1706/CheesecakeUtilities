/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.itachi1706.cheesecakeutilities.modules.cameraviewer

import android.os.Bundle
import com.itachi1706.cheesecakeutilities.BaseModuleActivity
import com.itachi1706.cheesecakeutilities.R

class CameraActivity(override val helpDescription: String = "A camera utility to access all cameras on the device that is made available in the Camera2 API\n\n" +
        "This includes hidden cameras such as the IR camera of the Google Pixel 4 lineup of devices") : BaseModuleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_viewer)
        savedInstanceState ?: supportFragmentManager.beginTransaction().replace(R.id.container, Camera2BasicFragment.newInstance()).commit()
    }

}

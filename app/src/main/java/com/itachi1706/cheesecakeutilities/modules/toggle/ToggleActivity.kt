package com.itachi1706.cheesecakeutilities.modules.toggle

import android.os.Bundle
import com.itachi1706.cheesecakeutilities.BaseModuleActivity
import com.itachi1706.cheesecakeutilities.R

class ToggleActivity : BaseModuleActivity() {

    override val helpDescription: String
        get() = "Basic System Toggles that is also available on the tiles menu as well"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toggle)
    }
}

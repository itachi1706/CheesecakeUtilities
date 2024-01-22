package com.itachi1706.cheesecakeutilities.modules.apkmirrordownloader

import android.graphics.Color
import android.os.AsyncTask
import com.itachi1706.cheesecakeutilities.modules.apkmirrordownloader.interfaces.AsyncResponse
import org.jsoup.Jsoup
import java.io.IOException

class PageAsync : AsyncTask<String, Int, Int>() {

    var response: AsyncResponse? = null

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg url: String): Int? {
        return try {
            val doc = Jsoup.connect(url[0]).get()
            val metaElements = doc.select("meta[name=theme-color]")
            Color.parseColor(if (metaElements.size != 0) metaElements[0].attr("content") else "#FF8B14")
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onPostExecute(result: Int?) {
        if (result != null)
            response!!.onProcessFinish(result)
        else
            response!!.onProcessFinish(Color.parseColor("#FF8B14"))
    }
}


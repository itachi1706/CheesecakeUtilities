package com.itachi1706.cheesecakeutilities.extlibs.com.scottyab.safetynet

import android.os.AsyncTask
import com.itachi1706.helperlib.helpers.LogHelper
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory

/**
 * Created by Kenneth on 31/7/2019.
 * for com.itachi1706.cheesecakeutilities.extlibs.com.scottyab.safetynet in CheesecakeUtilities
 */
class AndroidDeviceVerifierTask(private val apiKey: String, private val signatureToVerify: String, val callback: AndroidDeviceVerifier.AndroidDeviceVerifierCallback) : AsyncTask<Void, Void, Boolean>() {
    private val TAG: String = AndroidDeviceVerifier::class.java.simpleName

    private var error: Exception? = null

    override fun doInBackground(vararg params: Void?): Boolean {

        //LogHelper.d(TAG, "signatureToVerify:" + signatureToVerify)
        LogHelper.d("Ayyy", "Ayyy")

        try {
            val verifyApiUrl = URL(Companion.GOOGLE_VERIFICATION_URL + apiKey)

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, getTrustManagers(), null)

            val urlConnection = verifyApiUrl.openConnection() as HttpsURLConnection
            urlConnection.sslSocketFactory = sslContext.socketFactory

            urlConnection.requestMethod = "POST"
            urlConnection.setRequestProperty("Content-Type", "application/json")

            //build post body { "signedAttestation": "<output of getJwsResult()>" }
            val requestJsonBody: String = "{ \"signedAttestation\": \"$signatureToVerify\"}"
            val outputInBytes = requestJsonBody.toByteArray(Charsets.UTF_8)
            val os = urlConnection.outputStream
            os.write(outputInBytes)
            os.close()

            urlConnection.connect()

            //resp ={ “isValidSignature”: true }
            val inputStream = urlConnection.inputStream
            val sb = StringBuilder()
            val rd = BufferedReader(InputStreamReader(inputStream))
            rd.forEachLine {
                sb.append(it)
            }
            val response = sb.toString()
            val responseRoot = JSONObject(response)
            if (responseRoot.has("isValidSignature")) {
                return responseRoot.getBoolean("isValidSignature")
            }
        } catch (e: Exception) {
            error = e
            LogHelper.e(TAG, "problem validation JWS Message :${e.message}", e)
            return false
        }
        return false
    }

    override fun onPostExecute(aBoolean: Boolean) {
        if (error != null)
            callback.error(error!!.message)
        else
            callback.success(aBoolean)
    }

    /**
     * Provide the trust managers for the URL connection. By Default this uses the system defaults plus the GoogleApisTrustManager (SSL pinning)
     *
     * @return array of TrustManager including system defaults plus the GoogleApisTrustManager (SSL pinning)
     * @throws KeyStoreException No keystore
     * @throws NoSuchAlgorithmException No algorithm
     */
    @Throws(KeyStoreException::class, NoSuchAlgorithmException::class)
    private fun getTrustManagers(): Array<TrustManager?> {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        //init with the default system trustmanagers
        trustManagerFactory.init(null as KeyStore?)
        val defaultTrustManagers = trustManagerFactory.trustManagers
        val trustManagers = defaultTrustManagers.copyOf(defaultTrustManagers.size + 1)
        //add our Google APIs pinning TrustManager for extra security
        trustManagers[defaultTrustManagers.size] = GoogleApisTrustManager()
        return trustManagers
    }

    companion object {
        private const val GOOGLE_VERIFICATION_URL = "https://www.googleapis.com/androidcheck/v1/attestations/verify?key="
    }
}
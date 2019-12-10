package com.itachi1706.cheesecakeutilities.huh.com.scottyab.safetynet

/**
 * Validates the result with Android Device Verification API.
 *
 *
 * Note: This only validates that the provided JWS (JSON Web Signature) message was received from the actual SafetyNet service.
 * It does *not* verify that the payload data matches your original compatibility check request.
 * POST to https://www.googleapis.com/androidcheck/v1/attestations/verify?key=<your API key>
</your> *
 *
 * More info see {link https://developer.android.com/google/play/safetynet/start.html#verify-compat-check}
 */
class AndroidDeviceVerifier internal constructor(private val apiKey: String, private val signatureToVerify: String) {

    interface AndroidDeviceVerifierCallback {
        fun error(s: String?)
        fun success(isValidSignature: Boolean)
    }

    fun verify(androidDeviceVerifierCallback: AndroidDeviceVerifierCallback) {
        AndroidDeviceVerifierTask(apiKey, signatureToVerify, androidDeviceVerifierCallback).execute()
    }

}
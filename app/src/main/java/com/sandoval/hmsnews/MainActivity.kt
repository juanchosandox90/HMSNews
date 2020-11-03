package com.sandoval.hmsnews

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.view.isVisible
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.HwIdAuthProvider
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.api.entity.auth.Scope
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper

const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private val huaweiIdSignin = 1002
    private lateinit var signOutBtn: Button
    val scopes = listOf(Scope("email"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        signOutBtn = findViewById(R.id.button_signout)
        initViews()
        if (AGConnectAuth.getInstance().currentUser != null) {
            val user = AGConnectAuth.getInstance().currentUser
            Log.i(TAG, "currentUser Huawei Account Details: ${user.displayName}")
            //Toast.makeText(this, "Welcome ${user.displayName}", Toast.LENGTH_LONG).show()
            signOutBtn.isEnabled = true
        } else {
            Log.i(TAG, "no currentUser.. ")
            signIn()
        }
    }

    private fun initViews() {
        signOutBtn.setOnClickListener {
            signOut()
        }
    }

    private fun signIn() {
        val authParams = HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setIdToken()
            .setScopeList(scopes)
            .setAccessToken()
            .createParams()
        val service = HuaweiIdAuthManager.getService(this, authParams)
        startActivityForResult(service.signInIntent, huaweiIdSignin)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == huaweiIdSignin) {
            val authHuaweiIdTask =
                HuaweiIdAuthManager.parseAuthResultFromIntent(data)
            if (authHuaweiIdTask.isSuccessful) {
                val huaweiAccount = authHuaweiIdTask.result
                val accessToken = huaweiAccount.accessToken
                val credential = HwIdAuthProvider.credentialWithToken(accessToken)

                AGConnectAuth.getInstance().signIn(credential)
                    .addOnSuccessListener { signInResult -> // onSuccess
                        val user = signInResult.user
                        //Toast.makeText(this, "Welcome ${user.displayName}", Toast.LENGTH_LONG)
                        //  .show()
                        Log.i(TAG, "signIn success. Huawei Account Details: $huaweiAccount")
                        Log.d(TAG, "AccessToken: " + huaweiAccount.accessToken)
                        Log.d(TAG, "IDToken: " + huaweiAccount.idToken)
                        signOutBtn.isVisible = true

                    }.addOnFailureListener {
                        // Toast.makeText(this, "HwID signIn failed: ${it.message}", Toast.LENGTH_LONG)
                        //   .show()
                        Log.e(TAG, "signIn failed: " + it.message)
                        signOutBtn.isEnabled =
                            it.message == " code: 5 message: already sign in a user"
                        signOutBtn.isVisible = false
                    }
            } else {
                Log.e(
                    TAG, "signIn failed: " + (authHuaweiIdTask.exception as ApiException).statusCode
                )
                signOutBtn.isVisible = false
                // Toast.makeText(
                //   this,
                // "HwID signIn failed: ${authHuaweiIdTask.exception.message}",
                //Toast.LENGTH_LONG
                //)
                //  .show()
            }
        }
    }

    private fun signOut() {
        AGConnectAuth.getInstance().signOut()
        Toast.makeText(this, "SignOut Success", Toast.LENGTH_LONG).show()
        Log.i(TAG, "signOut Success")
        signOutBtn.isVisible = false
    }

    override fun onDestroy() {
        signOut()
        super.onDestroy()
    }
}
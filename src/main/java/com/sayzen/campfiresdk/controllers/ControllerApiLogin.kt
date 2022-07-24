package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API
import com.sayzen.campfiresdk.models.events.account.EventAccountEmailChanged
import com.sayzen.devsupandroidgoogle.ControllerGoogleAuth
import com.sup.dev.android.tools.ToolsStorage
import com.sup.dev.java.libs.eventBus.EventBus

object ControllerApiLogin {


    val LOGIN_NONE = 0
    val LOGIN_GOOGLE = 1
    val LOGIN_EMAIL = 2

    fun setLoginType(type:Int){
        ToolsStorage.put("ControllerApiLogin.login_type", type)
    }

    fun getLoginType() = ToolsStorage.getInt("ControllerApiLogin.login_type", LOGIN_NONE)

    fun isLoginNone() = getLoginType() == LOGIN_NONE
    fun isLoginGoogle() = getLoginType() == LOGIN_GOOGLE
    fun isLoginEmail() = getLoginType() == LOGIN_EMAIL


    fun getLoginToken(callbackSource: (String?) -> Unit){
        if(isLoginGoogle()) {
            getLoginToken_Google(callbackSource)
        } else if(isLoginEmail()){
            getLoginToken_Email(callbackSource)
        } else {
            callbackSource.invoke(null)
        }
    }

    fun clear(){
        setLoginType(LOGIN_NONE)
        clearEmailToken()
    }

    //
    //  Google
    //

    fun getLoginToken_Google(callbackSource: (String?) -> Unit){
        ControllerGoogleAuth.getToken {
            ControllerGoogleAuth.tokenPostExecutor.invoke(it) { token ->
                callbackSource.invoke(token)
            }
        }
    }

    //
    //  Email
    //

    fun getLoginToken_Email(callbackSource: (String?) -> Unit){
        callbackSource.invoke(getEmailToken())
    }

    fun getEmailToken() = ToolsStorage.getString("ControllerApiLogin.email_token", null)
    @Deprecated("use firebase")
    fun setEmailToken(email:String, passwordSha512:String){
        ToolsStorage.put("ControllerApiLogin.email_token", "${API.LOGIN_EMAIL_PREFIX}${API.LOGIN_SPLITTER}$email${API.LOGIN_SPLITTER}$passwordSha512")
        EventBus.post(EventAccountEmailChanged(email))
    }
    fun setEmailToken(firebaseToken: String) {
        ToolsStorage.put("ControllerApiLogin.email_token", "${API.LOGIN_EMAIL2_PREFIX}${API.LOGIN_SPLITTER}$firebaseToken")
    }
    fun clearEmailToken() = ToolsStorage.clear("ControllerApiLogin.email_token")

}
package com.sayzen.campfiresdk.controllers.notifications.chat

import android.content.Intent
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.notifications.chat.NotificationChatMessage
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerChats
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsNotifications
import com.sup.dev.android.tools.ToolsResources

public class NotificationChatMessageParser(override val n: NotificationChatMessage) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        val titleV: String
        val textV = text
        val publication = n.publicationChatMessage
        val tagV = n.tag.asTag()

        val chatMessages = ControllerChats.getMessages(n.tag)
        if (n.tag.chatType == API.CHAT_TYPE_PRIVATE) {
            titleV = publication.creator.name
        } else {
            titleV = publication.fandom.name
        }

        val notification = ToolsNotifications.NotificationX()
                .setIcon(icon)
                .setTitle(titleV)
                .setText(textV)
                .setIntent(intent)
                .setTag(tagV)
        if (intent.extras != null) notification.intentCancel.putExtras(intent.extras!!)

        if(chatMessages.count() > 1) notification.setBigText(chatMessages.text())

        (if (sound) ControllerNotifications.chanelChatMessages else ControllerNotifications.chanelChatMessages_salient)
                .post(notification)
    }

    override fun asString(html: Boolean): String {
        if (n.tag.chatType == API.CHAT_TYPE_FANDOM_ROOT) {
            return if (n.publicationChatMessage.resourceId != 0L && n.publicationChatMessage.text.isEmpty()) n.publicationChatMessage.creator.name + ": " + t(API_TRANSLATE.app_image)
            else if (n.publicationChatMessage.stickerId != 0L && n.publicationChatMessage.text.isEmpty()) n.publicationChatMessage.creator.name + ": " + t(API_TRANSLATE.app_sticker)
            else n.publicationChatMessage.creator.name + ": " + n.publicationChatMessage.text
        } else {
            return if (n.publicationChatMessage.resourceId != 0L && n.publicationChatMessage.text.isEmpty()) t(API_TRANSLATE.app_image)
            else if (n.publicationChatMessage.stickerId != 0L && n.publicationChatMessage.text.isEmpty()) t(API_TRANSLATE.app_sticker)
            else n.publicationChatMessage.text
        }
    }

    override fun canShow() = canShowNotificationChatMessage(n)

    override fun doAction() {
        doActionNotificationChatMessage(n)
    }

    private fun canShowNotificationChatMessage(n: NotificationChatMessage): Boolean {
        if (!n.subscribed) return false
        if (n.tag.chatType == API.CHAT_TYPE_FANDOM_ROOT && !ControllerSettings.notificationsChatMessages) return false
        if (n.tag.chatType == API.CHAT_TYPE_PRIVATE && !ControllerSettings.notificationsPM) return false
        if (SupAndroid.activityIsVisible && Navigator.getCurrent() is SChat) {
            val screen = Navigator.getCurrent() as SChat
            return screen.chat.tag != n.tag
        } else {
            return true
        }
    }

    private fun doActionNotificationChatMessage(n: NotificationChatMessage) {
        SChat.instance(ChatTag(n.publicationChatMessage.chatType, n.publicationChatMessage.fandom.id, n.publicationChatMessage.fandom.languageId), n.publicationChatMessage.id, true, Navigator.TO)
    }



}
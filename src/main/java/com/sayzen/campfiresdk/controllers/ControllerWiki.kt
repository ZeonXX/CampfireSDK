package com.sayzen.campfiresdk.controllers

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.dzen.campfire.api.requests.wiki.RWikiArticleChangeLanguage
import com.dzen.campfire.api.requests.wiki.RWikiRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.wiki.EventWikiPagesChanged
import com.sayzen.campfiresdk.models.events.wiki.EventWikiRemove
import com.sayzen.campfiresdk.screens.wiki.SWikiArticleEdit
import com.sayzen.campfiresdk.screens.wiki.SWikiItemCreate
import com.sayzen.campfiresdk.screens.wiki.history.SWikiArticleHistory
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.java.libs.eventBus.EventBus

object ControllerWiki {

    fun instanceMenu(wikiTitle: WikiTitle, languageId: Long): SplashMenu {
        return SplashMenu()
                .add(t(API_TRANSLATE.app_copy_link)) { copyLink(wikiTitle) }
                .add(t(API_TRANSLATE.app_history)) { Navigator.to(SWikiArticleHistory(wikiTitle, languageId)) }
                .spoiler(t(API_TRANSLATE.app_moderator))
                .add(t(API_TRANSLATE.wiki_edit_title)) { Navigator.to(SWikiItemCreate(wikiTitle.fandomId, wikiTitle.parentItemId, wikiTitle)) }.backgroundRes(R.color.blue_700).condition(ControllerApi.can(wikiTitle.fandomId, ControllerApi.getLanguage("en").id, API.LVL_MODERATOR_WIKI_EDIT))
                .add(t(API_TRANSLATE.app_remove)) { removeWikiItem(wikiTitle.itemId) }.backgroundRes(R.color.blue_700).condition(ControllerApi.can(wikiTitle.fandomId, ControllerApi.getLanguage("en").id, API.LVL_MODERATOR_WIKI_EDIT))
                .add(t(API_TRANSLATE.app_edit)) { toEditArticle(wikiTitle.itemId, languageId)}.backgroundRes(R.color.blue_700).condition(wikiTitle.itemType == API.WIKI_TYPE_ARTICLE && ControllerApi.can(wikiTitle.fandomId, languageId, API.LVL_MODERATOR_WIKI_EDIT))
                .add(t(API_TRANSLATE.wiki_change_language)) {changeLanguage(wikiTitle.itemId, languageId)}.backgroundRes(R.color.blue_700).condition(wikiTitle.itemType == API.WIKI_TYPE_ARTICLE && ControllerApi.can(wikiTitle.fandomId, languageId, API.LVL_MODERATOR_WIKI_EDIT))
    }

    fun showMenu(wikiTitle: WikiTitle, languageId: Long, view: View, x:Float, y:Float) {
        instanceMenu(wikiTitle, languageId).asPopupShow(view, x, y)
    }

    fun toEditArticle(itemId:Long, languageId: Long){
        if(languageId != ControllerApi.getLanguageId()){
            SplashAlert()
                    .setText(t(API_TRANSLATE.wiki_article_edit_language_alert))
                    .setOnEnter(ControllerApi.getLanguage().name){
                        SWikiArticleEdit.instance(itemId, ControllerApi.getLanguageId(), Navigator.TO)
                    }
                    .setOnCancel(ControllerApi.getLanguage(languageId).name){
                        SWikiArticleEdit.instance(itemId, languageId, Navigator.TO)
                    }
                    .asSheetShow()
        }else {
            SWikiArticleEdit.instance(itemId, languageId, Navigator.TO)
        }
    }

    private fun changeLanguage(itemId:Long, fromLanguageId: Long){
        ControllerCampfireSDK.createLanguageMenu(0, arrayOf(fromLanguageId)) { languageId ->
            ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.wiki_change_language_alert), t(API_TRANSLATE.app_change), RWikiArticleChangeLanguage(itemId, fromLanguageId, languageId)) {
                EventBus.post(EventWikiPagesChanged(itemId, fromLanguageId, emptyArray()))
                EventBus.post(EventWikiPagesChanged(itemId, languageId, it.item.pages))
                ToolsToast.show(t(API_TRANSLATE.app_done))
            }
        }.asSheetShow()
    }

    private fun copyLink(wikiTitle: WikiTitle){
        if (wikiTitle.itemType == API.WIKI_TYPE_ARTICLE) ToolsAndroid.setToClipboard(ControllerLinks.linkToWikiArticle(wikiTitle.itemId))
        else ToolsAndroid.setToClipboard(ControllerLinks.linkToWikiItemId(wikiTitle.itemId))
        ToolsToast.show(t(API_TRANSLATE.app_copied))
    }

    private fun removeWikiItem(itemId:Long) {
        ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.wiki_item_remove_confirm), t(API_TRANSLATE.app_remove), RWikiRemove(itemId)) {
            EventBus.post(EventWikiRemove(itemId))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

}
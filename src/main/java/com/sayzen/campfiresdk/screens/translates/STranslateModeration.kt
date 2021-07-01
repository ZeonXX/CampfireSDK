package com.sayzen.campfiresdk.screens.translates

import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.translate.TranslateHistory
import com.dzen.campfire.api.requests.publications.RPublicationsGetAllDeepBlocked
import com.dzen.campfire.api.requests.translates.RTranslateModerationGet
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import kotlin.reflect.KClass

class STranslateModeration() : SLoadingRecycler<CardTranslateModeration, TranslateHistory>() {

    init {
        disableNavigation()

        setTitle(t(API_TRANSLATE.translates_title_translate_moderation))
        setTextEmpty(t(API_TRANSLATE.app_empty))

        adapter.setBottomLoader { onLoad, cards ->
            RTranslateModerationGet(cards.size.toLong())
                    .onComplete { r -> onLoad.invoke(r.histories) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardTranslateModeration::class

    override fun map(item: TranslateHistory) = CardTranslateModeration(item)

}


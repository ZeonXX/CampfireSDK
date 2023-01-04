package com.sayzen.campfiresdk.screens.quests

import android.text.InputType
import androidx.recyclerview.widget.LinearLayoutManager
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.QuestDetails
import com.dzen.campfire.api.models.quests.QuestPart
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.CardAvatar
import com.sup.dev.android.views.screens.SRecycler
import com.sup.dev.android.views.splash.SplashField
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter

class SQuestDebug(
    val details: QuestDetails,
    val state: SQuestPlayer.QuestState,
    val parts: List<QuestPart>,
    val index: Int,
) : SRecycler() {
    private val adapter: RecyclerCardAdapter = RecyclerCardAdapter()

    init {
        disableNavigation()
        disableShadows()

        setTitle(t(API_TRANSLATE.quests_debug))

        vRecycler.layoutManager = LinearLayoutManager(context)
        ToolsView.setRecyclerAnimation(vRecycler)

        details.variables.forEachIndexed { idx, variable ->
            val view = CardAvatar()
            view.setTitle(variable.devName)
            view.setSubtitle(state.variables[variable.id] ?: "<не инициализирована>")
            view.setOnClick {
                SplashField()
                    .setTitle(t(API_TRANSLATE.quests_debug_change_value, variable.devName))
                    .setText(state.variables[variable.id] ?: "")
                    .setInputType(when (variable.type) {
                        API.QUEST_TYPE_TEXT -> InputType.TYPE_CLASS_TEXT
                        else -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
                    })
                    .setMax(API.QUEST_VARIABLE_MAX_VALUE_L)
                    .addChecker {
                        !(variable.type == API.QUEST_TYPE_BOOL && it != "1" && it != "0")
                    }
                    .setOnEnter(t(API_TRANSLATE.app_done)) { _, value ->
                        state.variables[variable.id] = value
                        (adapter[idx] as CardAvatar).setSubtitle(value)
                    }
                    .setOnCancel(t(API_TRANSLATE.app_cancel))
                    .asSheetShow()
            }

            adapter.add(view)
        }

        val view = CardAvatar()
        view.setTitle(t(API_TRANSLATE.quests_debug_secret))
        view.setSubtitle(
            "${details.id}/${details.creator.id}/${details.variables.size}/" +
            "${parts.size}/69/${state.savedAge}/${state.variables.size}/" +
            "$index/${details.variablesMap?.size ?: 420}",
        )
        adapter.add(view)

        vRecycler.adapter = adapter
    }

    override fun onBackPressed(): Boolean {
        Navigator.replace(SQuestPlayer(details, parts, index, state))
        return true
    }
}

package com.sayzen.campfiresdk.screens.quests

import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.quests.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.settings.SettingsCheckBox
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.android.views.views.layouts.LayoutCorned
import com.sup.dev.java.tools.ToolsMath
import com.sayzen.campfiresdk.controllers.t
import com.dzen.campfire.api.API_TRANSLATE

class SQuestPlayer(
    private val details: QuestDetails,
    private val parts: List<QuestPart>,
    private val index: Int,
    private val state: QuestState,
) : Screen(R.layout.screen_user_quest) {
    data class QuestState(
        val variables: HashMap<Long, String> = hashMapOf(),
        val dev: Boolean = false
    ) {
        // returns Long | String | Boolean
        private fun getVariableValue(details: QuestDetails, id: Long): Any {
            val v = details.variablesMap!![id]!!
            return when (v.type) {
                API.QUEST_TYPE_TEXT -> variables[id]!!
                API.QUEST_TYPE_NUMBER -> variables[id]!!.toLong()
                API.QUEST_TYPE_BOOL -> variables[id]!! == "1"
                else -> {
                    // halt and catch fire
                    throw IllegalStateException()
                }
            }
        }

        // returns Long | String | Boolean
        private fun getValue(details: QuestDetails, value: QuestConditionValue): Any {
            return when (value.type) {
                API.QUEST_CONDITION_VALUE_LITERAL_LONG -> value.value
                API.QUEST_CONDITION_VALUE_LITERAL_TEXT -> value.sValue
                API.QUEST_CONDITION_VALUE_LITERAL_BOOL -> value.value == 1L
                API.QUEST_CONDITION_VALUE_VAR -> getVariableValue(details, value.value)
                else -> 0L
            }
        }

        fun conditionFulfilled(details: QuestDetails, cond: QuestPartCondition): Boolean {
            val leftValue = getValue(details, cond.leftValue)
            val rightValue = getValue(details, cond.rightValue)

            return when (cond.cond) {
                API.QUEST_CONDITION_LESS -> (leftValue as Long) < (rightValue as Long)
                API.QUEST_CONDITION_LEQ -> (leftValue as Long) <= (rightValue as Long)
                API.QUEST_CONDITION_EQ -> leftValue == rightValue
                API.QUEST_CONDITION_NEQ -> leftValue != rightValue
                API.QUEST_CONDITION_GEQ -> (leftValue as Long) >= (rightValue as Long)
                API.QUEST_CONDITION_GREATER -> (leftValue as Long) > (rightValue as Long)
                else -> {
                    // halt and catch fire
                    throw IllegalStateException()
                }
            }
        }

        fun executeAction(details: QuestDetails, action: QuestPartAction) {
            when (action.actionType) {
                API.QUEST_ACTION_SET_LITERAL -> {
                    variables[action.varId] = action.sArg
                }
                API.QUEST_ACTION_SET_RANDOM -> {
                    if (details.variablesMap!![action.varId]!!.type == API.QUEST_TYPE_BOOL) {
                        action.lArg1 = 0
                        action.lArg2 = 1
                    }
                    variables[action.varId] = ToolsMath.randomLong(action.lArg1, action.lArg2).toString()
                }
                API.QUEST_ACTION_SET_ANOTHER -> {
                    variables[action.varId] = variables[action.lArg1]!!
                }
                API.QUEST_ACTION_ADD_LITERAL -> {
                    when (details.variablesMap!![action.varId]!!.type) {
                        API.QUEST_TYPE_BOOL -> {
                            variables[action.varId] = if (variables[action.varId]!! != "1") "1" else "0"
                        }
                        API.QUEST_TYPE_TEXT -> {
                            variables[action.varId] += action.sArg
                        }
                        API.QUEST_TYPE_NUMBER -> {
                            variables[action.varId] = (
                                variables[action.varId]!!.toLong() +
                                action.sArg.toLong()
                            ).toString()
                        }
                    }
                }
                API.QUEST_ACTION_ADD_ANOTHER -> {
                    when (details.variablesMap!![action.varId]!!.type) {
                        API.QUEST_TYPE_TEXT -> {
                            variables[action.varId] += action.sArg
                        }
                        API.QUEST_TYPE_NUMBER -> {
                            variables[action.varId] = (
                                variables[action.varId]!!.toLong() +
                                variables[action.lArg1]!!.toLong()
                            ).toString()
                        }
                    }
                }
                API.QUEST_ACTION_SET_ARANDOM -> {
                    variables[action.varId] = ToolsMath.randomLong(
                        variables[action.lArg1]!!.toLong(),
                        variables[action.lArg2]!!.toLong()
                    ).toString()
                }
                API.QUEST_ACTION_MULTIPLY -> {
                    variables[action.varId] = (
                        variables[action.varId]!!.toLong() *
                        variables[action.lArg1]!!.toLong()
                    ).toString()
                }
                API.QUEST_ACTION_DIVIDE -> {
                    variables[action.varId] = (
                        variables[action.varId]!!.toLong() /
                        variables[action.lArg1]!!.toLong()
                    ).toString()
                }
                API.QUEST_ACTION_BIT_AND -> {
                    variables[action.varId] = (
                        variables[action.varId]!!.toLong() and
                        variables[action.lArg1]!!.toLong()
                    ).toString()
                }
                API.QUEST_ACTION_BIT_OR -> {
                    variables[action.varId] = (
                        variables[action.varId]!!.toLong() or
                        variables[action.lArg1]!!.toLong()
                    ).toString()
                }
            }
        }
    }

    private val part = parts[index]

    private val vImageWrapper: LayoutCorned = findViewById(R.id.vImageWrapper)
    private val vTitleImage: ImageView = findViewById(R.id.vTitleImage)
    private val vTitle: TextView = findViewById(R.id.vTitle)
    private val vText: ViewText = findViewById(R.id.vText)
    private val vInputContainer: LinearLayout = findViewById(R.id.vInputContainer)
    private val vButtonContainer: LinearLayout = findViewById(R.id.vButtonContainer)

    init {
        disableNavigation()

        if (part !is QuestPartText) {
            throw AssertionError()
        }

        if (part.imageId > 0) {
            if (part.gifId > 0) {
                ImageLoader.loadGif(part.imageId, part.gifId, vTitleImage)
            } else {
                ImageLoader.load(part.imageId).into(vTitleImage)
            }
        }

        vTitle.text = part.title
        vText.text = part.text
        ControllerLinks.makeLinkable(vText)

        part.inputs.forEach {
            val field = if (it.type == API.QUEST_TYPE_BOOL) SettingsCheckBox(context)
            else SettingsField(context)

            if (field.layoutParams == null) {
                field.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            }
            val params = field.layoutParams as LayoutParams
            params.marginStart = ToolsView.dpToPx(16).toInt()
            params.marginEnd = ToolsView.dpToPx(16).toInt()
            params.bottomMargin = ToolsView.dpToPx(8).toInt()

            if (field is SettingsField) {
                field.setHint(it.hint)
                field.setInputType(
                    when (it.type) {
                        API.QUEST_TYPE_TEXT -> InputType.TYPE_CLASS_TEXT
                        API.QUEST_TYPE_NUMBER -> InputType.TYPE_CLASS_NUMBER
                        else -> InputType.TYPE_CLASS_TEXT
                    } or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
                )
                field.setText(it.defaultValue)
            } else if (field is SettingsCheckBox) {
                field.setTitle(it.hint)
                field.setChecked(it.defaultValue == "1")
            }

            vInputContainer.addView(field)
        }

        part.buttons.forEachIndexed { idx, it ->
            val button = Button(context, null, R.style.Button_Text)
            button.gravity = Gravity.CENTER
            button.text = it.label
            button.setTextColor(when (it.color) {
                // QUEST_BUTTON_COLOR_DEFAULT -> else
                API.QUEST_BUTTON_COLOR_RED -> R.color.red_500
                API.QUEST_BUTTON_COLOR_ORANGE -> R.color.orange_500
                API.QUEST_BUTTON_COLOR_YELLOW -> R.color.yellow_500
                API.QUEST_BUTTON_COLOR_GREEN -> R.color.green_500
                API.QUEST_BUTTON_COLOR_AQUA -> R.color.blue_400
                API.QUEST_BUTTON_COLOR_BLUE -> R.color.blue_700
                API.QUEST_BUTTON_COLOR_PURPLE -> R.color.purple_500
                API.QUEST_BUTTON_COLOR_PINK -> R.color.pink_400
                API.QUEST_BUTTON_COLOR_WHITE -> R.color.white
                else -> ToolsResources.getColorAttr(context, R.attr.colorSecondary)
            })
            button.setOnClickListener {
                println("[Quests] button $idx pressed")
                pressButton(idx)
            }

            vButtonContainer.addView(button)
        }
    }

    private fun endQuest() {
        ToolsToast.show("TODO")
        Navigator.back()
    }

    private fun updateVariables(): Boolean {
        if (part !is QuestPartText) {
            // halt and catch fire
            return false
        }
        part.inputs.forEachIndexed { idx, it ->
            val view = vInputContainer.getChildAt(idx)
            if (view is SettingsField) {
                if (it.type == API.QUEST_TYPE_NUMBER) {
                    view.getText().toLongOrNull() ?: return false
                }
                state.variables[it.varId] = view.getText()
            } else if (view is SettingsCheckBox) {
                state.variables[it.varId] = if (view.isChecked()) "1" else "0"
            } else {
                // halt and catch fire
                return false
            }
        }
        return true
    }

    private fun pressButton(index: Int) {
        if (part !is QuestPartText) {
            // halt and catch fire
            return
        }
        if (! updateVariables()) {
            println("[Quests] failed to updateVariables()")
            return
        }

        val button = part.buttons[index]
        jumpTo(button.jumpToId)
    }

    private fun jumpTo(toId: Long, fromIndex: Int = this.index, depth: Int = 0) {
		if (depth > API.QUEST_MAX_DEPTH) {
			ToolsToast.show(t(API_TRANSLATE.quests_error_depth))
			return
		}

		println("[Quests] jumping to $toId (depth $depth)")
        if (toId == -2L) { // next part
            if (parts.size - 1 == fromIndex + 1) {
                endQuest()
            } else {
                jumpTo(parts[fromIndex + 1].id, fromIndex, depth + 1)
            }
            return
        }
        if (toId == -1L) {
            endQuest()
            return
        }

        val partIdx = parts
            .indexOfFirst { it.id == toId }
            .takeIf { it != -1 } ?: run {
				println("[Quests] failed to find part w/ id $toId")
				return
			}
        when (val part = parts[partIdx]) {
            is QuestPartText -> {
                val player = SQuestPlayer(
                    details,
                    parts,
                    partIdx,
                    state
                )
                Navigator.replace(player)
            }
            is QuestPartCondition -> {
                if (state.conditionFulfilled(details, part)) {
                    jumpTo(part.trueJumpId, partIdx, depth + 1)
                } else {
                    jumpTo(part.falseJumpId, partIdx, depth + 1)
                }
            }
            is QuestPartAction -> {
                state.executeAction(details, part)
                jumpTo(part.jumpId, partIdx, depth + 1)
            }
        }
    }
}

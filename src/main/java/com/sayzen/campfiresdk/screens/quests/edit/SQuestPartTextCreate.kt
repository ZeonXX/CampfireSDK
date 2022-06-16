package com.sayzen.campfiresdk.screens.quests.edit

import android.widget.FrameLayout
import android.widget.LinearLayout
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.quests.EventQuestChanged
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.settings.SettingsTitle
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewButton
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.java.libs.eventBus.EventBus

class SQuestPartTextCreate(
    private var details: QuestDetails,
    private val container: QuestPartContainer,
    private val part: QuestPartText,
    private val onDone: (part: QuestPartText) -> Unit,
) : Screen(R.layout.screen_quest_create_text) {
    private val eventBus = EventBus
        .subscribe(EventQuestChanged::class) {
            if (it.quest.id == details.id) details = it.quest
        }

    private val vCreate: ViewButton = findViewById(R.id.vCreate)
    private val vPartDevName: SettingsField = findViewById(R.id.vPartDevName)
    private val vPartTitle: SettingsField = findViewById(R.id.vPartTitle)
    private val vTitleContent: SettingsTitle = findViewById(R.id.vTitleContent)
    private val vPartContent: ViewText = findViewById(R.id.vPartContent)
    private val vEditContent: ViewButton = findViewById(R.id.vEditContent)
    private val vTitleInputs: SettingsTitle = findViewById(R.id.vTitleInputs)
    private val vInputsContainer: LinearLayout = findViewById(R.id.vInputsContainer)
    private val vAddInput: ViewButton = findViewById(R.id.vAddInput)
    private val vTitleButtons: SettingsTitle = findViewById(R.id.vTitleButtons)
    private val vButtonsContainer: LinearLayout = findViewById(R.id.vButtonsContainer)
    private val vAddButton: ViewButton = findViewById(R.id.vAddButton)
    private val vTitleEffects: SettingsTitle = findViewById(R.id.vTitleEffects)
    private val vEffectsContainer: LinearLayout = findViewById(R.id.vEffectsContainer)
    private val vAddEffect: ViewButton = findViewById(R.id.vAddEffect)

    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.quests_part_text))

        // labels
        vCreate.text = t(API_TRANSLATE.app_create)
        vPartDevName.setHint(t(API_TRANSLATE.quests_edit_dev_name))
        vPartTitle.setHint(t(API_TRANSLATE.quests_edit_text_title))
        vTitleContent.setTitle(t(API_TRANSLATE.quests_edit_text_content))
        vEditContent.text = t(API_TRANSLATE.app_edit)
        vTitleInputs.setTitle(t(API_TRANSLATE.quests_edit_text_inputs))
        vAddInput.text = t(API_TRANSLATE.app_add)
        vTitleButtons.setTitle(t(API_TRANSLATE.quests_edit_text_buttons))
        vAddButton.text = t(API_TRANSLATE.app_add)
        vTitleEffects.setTitle(t(API_TRANSLATE.quests_edit_text_effects))
        vAddEffect.text = t(API_TRANSLATE.app_add)

        // data from part and input constraints
        vPartDevName.setText(part.devLabel)
        vPartDevName.setMaxLength(API.QUEST_DEV_LABEL_MAX_L)
        vPartTitle.setText(part.title)
        vPartTitle.setMaxLength(API.QUEST_TEXT_TITLE_MAX_L)
        vPartContent.text = part.text.ifEmpty { t(API_TRANSLATE.app_empty) }
        ControllerLinks.makeLinkable(vPartContent)
        vEditContent.setOnClickListener {
            Navigator.to(SQuestPartTextEditor(this.part.text) {
                this.part.text = it
                vPartContent.text = part.text.ifEmpty { t(API_TRANSLATE.app_empty) }
                ControllerLinks.makeLinkable(vPartContent)
            })
        }

        for (input in part.inputs) {
            val root: FrameLayout = ToolsView.inflate(vInputsContainer, R.layout.card_quest_vat)
            updateInputFor(root, input)
            vInputsContainer.addView(root)
        }
        vAddInput.setOnClickListener {
            SplashQuestInput(this.details)
                .setInput(QuestInput())
                .setOnEnter(t(API_TRANSLATE.app_add)) { input ->
                    part.inputs += input
                    val root: FrameLayout = ToolsView.inflate(vInputsContainer, R.layout.card_quest_vat)
                    updateInputFor(root, input)
                    vInputsContainer.addView(root)
                }
                .asSheetShow()
        }

        for (button in part.buttons) {
            val root: FrameLayout = ToolsView.inflate(vInputsContainer, R.layout.card_quest_vat)
            updateButtonFor(root, button)
            vButtonsContainer.addView(root)
        }
        vAddButton.setOnClickListener {
            SplashQuestButton(this.container)
                .setButton(QuestButton())
                .setOnEnter(t(API_TRANSLATE.app_add)) { button ->
                    part.buttons += button
                    val root: FrameLayout = ToolsView.inflate(vInputsContainer, R.layout.card_quest_vat)
                    updateButtonFor(root, button)
                    vButtonsContainer.addView(root)
                }
                .asSheetShow()
        }
    }

    // todo: reorder for inputs and buttons

    private fun updateInputFor(root: FrameLayout, input: QuestInput) {
        val vAvatar: ViewAvatarTitle = root.findViewById(R.id.vAvatar)
        vAvatar.setTitle(input.hint)
        vAvatar.setSubtitle(t(
            API_TRANSLATE.quests_edit_text_input_subtitle,
            t(API_TRANSLATE.forQuestType(input.type)),
            details.variables.find { it.id == input.varId }?.devName ?: t(API_TRANSLATE.quests_placeholder_error)
        ))
        vAvatar.vAvatar.visibility = GONE

        root.setOnClickListener {
            SplashQuestInput(this.details)
                .setInput(input)
                .setOnEnter(t(API_TRANSLATE.app_change)) { newInput ->
                    part.inputs[part.inputs.indexOf(input)] = newInput
                    updateInputFor(root, newInput)
                    ToolsToast.show(t(API_TRANSLATE.app_changed))
                }
                .asSheetShow()
        }

        makeRemove(root, t(API_TRANSLATE.quests_edit_text_input_remove_q)) {
            part.inputs = part.inputs.filterNot { it == input }.toTypedArray() // help
            vInputsContainer.removeView(root)
        }
    }

    private fun updateButtonFor(root: FrameLayout, button: QuestButton) {
        val vAvatar: ViewAvatarTitle = root.findViewById(R.id.vAvatar)
        vAvatar.setTitle(button.label)
        vAvatar.setSubtitle(t(
            API_TRANSLATE.quests_edit_text_button_jump_to,
            jumpToIdToString(button.jumpToId, container)
        ))
        vAvatar.vAvatar.visibility = GONE

        root.setOnClickListener {
            SplashQuestButton(container)
                .setButton(button)
                .setOnEnter(t(API_TRANSLATE.app_change)) { newButton ->
                    part.buttons[part.buttons.indexOf(button)] = newButton
                    updateButtonFor(root, newButton)
                    ToolsToast.show(t(API_TRANSLATE.app_changed))
                }
                .asSheetShow()
        }

        makeRemove(root, t(API_TRANSLATE.quests_edit_text_button_remove_q)) {
            part.buttons = part.buttons.filterNot { it == button }.toTypedArray()
            vButtonsContainer.removeView(root)
        }
    }

    private fun makeRemove(root: FrameLayout, text: String, onEnter: () -> Unit) {
        val vRemove: ViewIcon = root.findViewById(R.id.vRemove)
        vRemove.setOnClickListener {
            SplashAlert()
                .setText(text)
                .setOnEnter(t(API_TRANSLATE.app_remove)) {
                    onEnter()
                }
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .asSheetShow()
        }
    }
}

package at.guger.moneybook.core.ui.viewmodel

import androidx.annotation.StringRes

class MessageEvent(@StringRes stringRes: Int, text: String? = null) : Event<Message>(Message(stringRes, text))

data class Message(@StringRes val stringRes: Int, val text: String? = null)
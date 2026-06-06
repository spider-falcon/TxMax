package assistant.models

data class AssistantIntent(

    val action: String,

    val value: String? = null,

    val extra: String? = null

)

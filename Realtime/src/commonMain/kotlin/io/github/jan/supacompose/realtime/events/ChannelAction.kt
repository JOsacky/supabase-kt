package io.github.jan.supacompose.realtime.events

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Contains information about a column
 * @property name the name of the column
 * @property type the type of the column
 */
@Serializable
data class Column(val name: String, val type: String)

sealed interface ChannelAction {

    /**
     * Contains data of the row's columns
     */
    val columns: List<Column>

    /**
     * The time when the action was committed
     */
    val commitTimestamp: Instant

    /**
     * The old record, if the action has one
     */
    val oldRecord: JsonObject? get() = null

    /**
     * The new record, if the action has one
     */
    val record: JsonObject? get() = null

    @Serializable
    data class Insert(
        override val record: JsonObject,
        override val columns: List<Column>,
        @SerialName("commit_timestamp")
        override val commitTimestamp: Instant
    ): ChannelAction

    @Serializable
    data class Update(
        override val record: JsonObject,
        @SerialName("old_record")
        override val oldRecord: JsonObject,
        override val columns: List<Column>,
        @SerialName("commit_timestamp")
        override val commitTimestamp: Instant,
    ): ChannelAction

    @Serializable
    data class Delete(
        @SerialName("old_record")
        override val oldRecord: JsonObject,
        override val columns: List<Column>,
        @SerialName("commit_timestamp")
        override val commitTimestamp: Instant,
    ): ChannelAction

    @Serializable
    data class Select(
        override val record: JsonObject,
        override val columns: List<Column>,
        @SerialName("commit_timestamp")
        override val commitTimestamp: Instant,
    ): ChannelAction

}
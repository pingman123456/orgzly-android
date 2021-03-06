package com.orgzly.android.query.dotted

import com.orgzly.android.query.*

open class DottedQueryParser : QueryParser() {

    override val groupOpen   = "("
    override val groupClose  = ")"

    override val operatorsAnd = listOf("and")
    override val operatorsOr = listOf("or")

    override val conditions = listOf(
            ConditionMatch("""^(\.)?b\.(.+)""", { matcher ->
                Condition.InBook(unQuote(matcher.group(2)), matcher.group(1) != null)
            }),

            ConditionMatch("""^(\.)?i\.(.+)""", { matcher ->
                Condition.HasState(unQuote(matcher.group(2)), matcher.group(1) != null)
            }),

            ConditionMatch("""^(\.)?it\.(todo|done|none)""", { matcher ->
                val stateType = StateType.valueOf(matcher.group(2).toUpperCase())
                Condition.HasStateType(stateType, matcher.group(1) != null)
            }),

            ConditionMatch("""^(\.)?p\.([a-zA-Z])""", { matcher ->
                Condition.HasPriority(matcher.group(2), matcher.group(1) != null)
            }),

            ConditionMatch("""^(\.)?ps\.([a-zA-Z])""", { matcher ->
                Condition.HasSetPriority(matcher.group(2), matcher.group(1) != null)
            }),

            ConditionMatch("""^(\.)?t\.(.+)""", { matcher ->
                Condition.HasTag(unQuote(matcher.group(2)), matcher.group(1) != null)
            }),

            ConditionMatch("""^tn\.(.+)""", { matcher ->
                Condition.HasOwnTag(unQuote(matcher.group(1)))
            }),

            ConditionMatch("""^([sdc])(?:\.(eq|ne|lt|le|gt|ge))?\.(.+)""", { matcher ->
                val timeTypeMatch = matcher.group(1)
                val relationMatch = matcher.group(2)
                val intervalMatch = matcher.group(3)

                val relation = when (relationMatch) {
                    "eq" -> Relation.EQ
                    "ne" -> Relation.NE
                    "lt" -> Relation.LT
                    "le" -> Relation.LE
                    "gt" -> Relation.GT
                    "ge" -> Relation.GE
                    else -> if (timeTypeMatch == "c") Relation.EQ else Relation.LE // Default if there is no relation
                }

                val interval = QueryInterval.parse(unQuote(intervalMatch))

                if (interval != null) {
                    when (timeTypeMatch) {
                        "d"  -> Condition.Deadline(interval, relation)
                        "c"  -> Condition.Closed(interval, relation)
                        else -> Condition.Scheduled(interval, relation)
                    }
                } else {
                    null // Ignore this match
                }
            })
    )

    override val sortOrders = listOf(
            SortOrderMatch("""^(\.)?o\.(?:scheduled|sched|s)$""", { matcher ->
                SortOrder.Scheduled(matcher.group(1) != null)
            }),
            SortOrderMatch("""^(\.)?o\.(?:deadline|dead|d)$""", { matcher ->
                SortOrder.Deadline(matcher.group(1) != null)
            }),
            SortOrderMatch("""^(\.)?o\.(?:closed|close|c)$""", { matcher ->
                SortOrder.Closed(matcher.group(1) != null)
            }),
            SortOrderMatch("""^(\.)?o\.(?:priority|prio|pri|p)$""", { matcher ->
                SortOrder.Priority(matcher.group(1) != null)
            }),
            SortOrderMatch("""^(\.)?o\.(?:notebook|book|b)$""", { matcher ->
                SortOrder.Book(matcher.group(1) != null)
            }),
            SortOrderMatch("""^(\.)?o\.(?:state|st)$""", { matcher ->
                SortOrder.State(matcher.group(1) != null)
            })
    )

    override val supportedOptions = listOf(
            OptionMatch("""^ad\.(\d+)$""", { matcher, options ->
                val days = matcher.group(1).toInt()
                if (days > 0) options.copy(agendaDays = days) else null
            })
    )
}

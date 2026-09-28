package com.birthdayreminder.domain.model

/**
 * The relationship a person can have with the user.
 *
 * One vocabulary, used by two surfaces: the chip row on the add wizard's
 * Identity step, and the filter chips on Search. They were previously
 * independent -- the wizard offered Family/Friend/Work/Acquaintance/Other while
 * Search filtered by Name/Month -- so a person saved as "Parents" could not be
 * found by the relationship they were tagged with. A value that cannot be
 * searched for is not a filter, it is a label.
 *
 * The list matches the approved concept: Family, Parents, Friends, Partner,
 * Colleagues, Other. Parents is separate from Family because the app's whole
 * premise is remembering the people whose dates matter most, and a parent is
 * not just another relative.
 *
 * @property stored the value persisted on [com.birthdayreminder.data.local.entity.Birthday.relationship],
 *   and what [matches] compares against
 * @property label what the chip and the row both show
 */
enum class Relationship(
    val stored: String,
    val label: String,
) {
    FAMILY(stored = "Family", label = "Family"),
    PARENTS(stored = "Parents", label = "Parents"),
    FRIENDS(stored = "Friends", label = "Friends"),
    PARTNER(stored = "Partner", label = "Partner"),
    COLLEAGUES(stored = "Colleagues", label = "Colleagues"),
    OTHER(stored = "Other", label = "Other"),
    ;

    companion object {
        /** Every option, in the order the concept shows them. */
        val all: List<Relationship> = entries.toList()

        /**
         * The chip labels, for rendering a row directly.
         */
        val labels: List<String> = all.map { it.label }

        /** The persisted values, in the same order. */
        val values: List<String> = all.map { it.stored }

        /**
         * Resolves a stored value to a known relationship.
         *
         * Legacy saves used values this list does not contain -- "Friend",
         * "Work", "Acquaintance" -- and the schema is free-text, so a person
         * saved under the old wizard must still resolve. Those map onto the
         * closest current option rather than becoming invisible to a filter.
         *
         * @param value the persisted relationship, or null when unset
         */
        fun fromStored(value: String?): Relationship? = resolve(value)
    }

    /**
     * Whether a person belongs under this relationship.
     *
     * [OTHER] is a catch-all, so it matches only people explicitly tagged
     * Other. Treating it as "everything else" would silently widen the filter
     * whenever a person has no relationship at all, which is the opposite of
     * what choosing Other means.
     *
     * @param stored the person's persisted relationship
     */
    fun matches(stored: String?): Boolean = resolve(stored) == this
}

/**
 * Resolves a persisted value to a known relationship, tolerating the legacy
 * wizard's vocabulary.
 *
 * Top level so both the enum's [Relationship.matches] and any caller that only
 * holds a string can use it.
 *
 * @param value the persisted relationship, or null when unset
 */
private fun resolve(value: String?): Relationship? {
    val trimmed = value?.trim().orEmpty()
    if (trimmed.isEmpty()) return null
    Relationship.entries.firstOrNull { it.stored.equals(trimmed, ignoreCase = true) }
        ?.let { return it }
    return when (trimmed.lowercase()) {
        // The wizard, and every relationship the app has ever offered, wrote
        // singular relationship words rather than the six chips. These all have
        // to land somewhere, because a person whose relationship does not
        // resolve is invisible to every filter except All.
        //
        // Mother and Father map to PARENTS rather than FAMILY: they are the
        // distinction the chip exists to make, and collapsing them into Family
        // would make the Parents chip match nothing the user ever saved.
        "mother", "father", "parent", "parents" -> Relationship.PARENTS
        "sister", "brother", "sibling", "child", "son", "daughter",
        "family", "relative", "cousin", "uncle", "aunt", "grandmother", "grandfather",
        -> Relationship.FAMILY
        "friend", "friends" -> Relationship.FRIENDS
        "partner", "spouse", "wife", "husband", "girlfriend", "boyfriend" -> Relationship.PARTNER
        "work", "colleague", "colleagues", "client" -> Relationship.COLLEAGUES
        "acquaintance", "neighbour", "neighbor", "other" -> Relationship.OTHER
        else -> null
    }
}

/**
 * How Search is scoped.
 *
 * Not a relationship: [ALL] and [THIS_MONTH] answer "which slice of the list"
 * rather than "which person to them". [MONTH] exists alongside [THIS_MONTH] to
 * cover a search field typed as a month name.
 */
enum class SearchFilter(
    val label: String,
) {
    ALL(label = "All"),
    FAMILY(label = "Family"),
    FRIENDS(label = "Friends"),
    THIS_MONTH(label = "This month"),
}

package at.guger.moneybook.data.provider.legacy

/**
 * Class providing database, table & column informations.
 *
 * @author Daniel Guger
 * @version 1.0
 */
object LegacyDatabase {

    const val DATABASE_NAME = "MoneyBookDB.db"

    object Table {
        const val BOOKENTRIES = "bookentries"
        const val BOOKENTRYCONTACTS = "bookentrycontacts"
        const val CATEGORIES = "categories"
        const val REMINDERS = "reminders"
    }

    object Column {
        const val ID = "id"
        const val TITLE = "title"
        const val DATE = "date"
        const val VALUE = "value"
        const val IS_PAID = "is_paid"
        const val NOTES = "notes"
        const val ENTRYTYPE = "entrytype"

        const val CONTACTS_COUNT = "contacts_count"

        const val PREFIX_CATEGORY = "cg_"
        const val CATEGORY_ID = "category_id"
        const val NAME = "name"
        const val ICON_ID = "icon_id"
        const val COLOR = "color"

        const val BOOKENTRY_ID = "bookentry_id"
        const val FIREDATE = "fire_date"
        const val CONTACT_ID = "contact_id"
        const val HAS_PAID = "has_paid"
    }
}
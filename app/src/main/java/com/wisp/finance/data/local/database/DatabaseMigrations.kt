package com.wisp.finance.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE recurring_expenses ADD COLUMN kind TEXT NOT NULL DEFAULT 'REGULAR'"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_recurring_expenses_kind ON recurring_expenses(kind)"
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS savings_goals (
                    id TEXT NOT NULL,
                    title TEXT NOT NULL,
                    target_minor_units INTEGER NOT NULL,
                    currency TEXT NOT NULL,
                    saved_minor_units INTEGER NOT NULL,
                    deadline INTEGER,
                    completed INTEGER NOT NULL,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_savings_goals_completed ON savings_goals(completed)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_savings_goals_deadline ON savings_goals(deadline)"
            )
        }
    }


    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""CREATE TABLE IF NOT EXISTS loans (
                id TEXT NOT NULL, title TEXT NOT NULL, principal_minor_units INTEGER NOT NULL,
                remaining_minor_units INTEGER NOT NULL, currency TEXT NOT NULL,
                annual_interest_rate REAL NOT NULL, term_months INTEGER NOT NULL,
                start_date INTEGER NOT NULL, next_payment_date INTEGER,
                monthly_payment_minor_units INTEGER, note TEXT NOT NULL, closed INTEGER NOT NULL,
                created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL, PRIMARY KEY(id))""")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_loans_closed ON loans(closed)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_loans_next_payment_date ON loans(next_payment_date)")
            db.execSQL("""CREATE TABLE IF NOT EXISTS personal_debts (
                id TEXT NOT NULL, person_name TEXT NOT NULL, direction TEXT NOT NULL,
                original_minor_units INTEGER NOT NULL, remaining_minor_units INTEGER NOT NULL,
                currency TEXT NOT NULL, due_date INTEGER, note TEXT NOT NULL, closed INTEGER NOT NULL,
                created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL, PRIMARY KEY(id))""")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_personal_debts_closed ON personal_debts(closed)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_personal_debts_due_date ON personal_debts(due_date)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_personal_debts_direction ON personal_debts(direction)")
        }
    }


    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS loan_payments (
                    id TEXT NOT NULL,
                    loan_id TEXT NOT NULL,
                    amount_minor_units INTEGER NOT NULL,
                    paid_at INTEGER NOT NULL,
                    note TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    PRIMARY KEY(id),
                    FOREIGN KEY(loan_id) REFERENCES loans(id) ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_loan_payments_loan_id ON loan_payments(loan_id)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_loan_payments_paid_at ON loan_payments(paid_at)")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS personal_debt_payments (
                    id TEXT NOT NULL,
                    debt_id TEXT NOT NULL,
                    amount_minor_units INTEGER NOT NULL,
                    paid_at INTEGER NOT NULL,
                    note TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    PRIMARY KEY(id),
                    FOREIGN KEY(debt_id) REFERENCES personal_debts(id) ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_personal_debt_payments_debt_id ON personal_debt_payments(debt_id)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_personal_debt_payments_paid_at ON personal_debt_payments(paid_at)")
        }
    }


    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE personal_debts ADD COLUMN annual_interest_rate REAL NOT NULL DEFAULT 0.0")
            db.execSQL("ALTER TABLE personal_debts ADD COLUMN interest_start_date INTEGER")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS mortgages (
                    id TEXT NOT NULL,
                    title TEXT NOT NULL,
                    property_price_minor_units INTEGER NOT NULL,
                    down_payment_minor_units INTEGER NOT NULL,
                    principal_minor_units INTEGER NOT NULL,
                    currency TEXT NOT NULL,
                    annual_interest_rate REAL NOT NULL,
                    term_months INTEGER NOT NULL,
                    start_date INTEGER NOT NULL,
                    monthly_payment_minor_units INTEGER NOT NULL,
                    note TEXT NOT NULL,
                    active INTEGER NOT NULL,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_mortgages_active ON mortgages(active)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_mortgages_start_date ON mortgages(start_date)")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS asset_holdings (
                    id TEXT NOT NULL,
                    kind TEXT NOT NULL,
                    name TEXT NOT NULL,
                    symbol TEXT NOT NULL,
                    quantity REAL NOT NULL,
                    average_cost_minor_units INTEGER NOT NULL,
                    current_price_minor_units INTEGER NOT NULL,
                    currency TEXT NOT NULL,
                    note TEXT NOT NULL,
                    archived INTEGER NOT NULL,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_asset_holdings_kind ON asset_holdings(kind)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_asset_holdings_archived ON asset_holdings(archived)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_asset_holdings_symbol ON asset_holdings(symbol)")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS tax_entries (
                    id TEXT NOT NULL,
                    type TEXT NOT NULL,
                    title TEXT NOT NULL,
                    amount_minor_units INTEGER NOT NULL,
                    currency TEXT NOT NULL,
                    tax_year INTEGER NOT NULL,
                    occurred_on INTEGER NOT NULL,
                    note TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_tax_entries_tax_year ON tax_entries(tax_year)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_tax_entries_type ON tax_entries(type)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_tax_entries_occurred_on ON tax_entries(occurred_on)")
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
}

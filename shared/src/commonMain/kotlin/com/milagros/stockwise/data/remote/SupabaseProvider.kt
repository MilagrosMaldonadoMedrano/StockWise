package com.milagros.stockwise.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

private const val SUPABASE_URL = "https://texhbazzvujaokfjyjvr.supabase.co"
private const val SUPABASE_ANON_KEY = "sb_publishable_6Lkp2idD_B_ONtTBv-7LnA_SnwP-zff"

val supabaseClient: SupabaseClient = createSupabaseClient(
    supabaseUrl = SUPABASE_URL,
    supabaseKey = SUPABASE_ANON_KEY,
) {
    install(Postgrest)
}

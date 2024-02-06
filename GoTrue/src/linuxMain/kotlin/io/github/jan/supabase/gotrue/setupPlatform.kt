package io.github.jan.supabase.gotrue

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.logging.w

@SupabaseInternal
actual fun Auth.setupPlatform() {
    logger.w { "Linux support is experimental, please report any bugs you find!" }
}
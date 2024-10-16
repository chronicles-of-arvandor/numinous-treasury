package net.arvandor.numinoustreasury.web.error

import org.http4k.core.Body
import org.http4k.format.Gson.auto

data class ErrorResponse(
    val error: String,
) {
    companion object {
        val lens = Body.auto<ErrorResponse>().toLens()
    }
}

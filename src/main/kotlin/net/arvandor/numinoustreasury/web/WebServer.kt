package net.arvandor.numinoustreasury.web

import net.arvandor.numinoustreasury.NuminousTreasury
import net.arvandor.numinoustreasury.web.droptable.dropTableRoute
import net.arvandor.numinoustreasury.web.droptable.dropTablesRoute
import net.arvandor.numinoustreasury.web.item.itemRoute
import net.arvandor.numinoustreasury.web.item.itemsRoute
import net.arvandor.numinoustreasury.web.node.nodeRoute
import net.arvandor.numinoustreasury.web.node.nodesRoute
import net.arvandor.numinoustreasury.web.recipe.recipeRoute
import net.arvandor.numinoustreasury.web.recipe.recipesRoute
import org.http4k.contract.bind
import org.http4k.contract.contract
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.core.Method.GET
import org.http4k.core.Method.OPTIONS
import org.http4k.format.Gson
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer

class WebServer(private val plugin: NuminousTreasury) {
    private val contract =
        contract {
            renderer = OpenApi3(ApiInfo(plugin.name, plugin.description.version), Gson)
            descriptionPath = "/openapi.json"
            routes += itemsRoute(GET)
            routes += itemsRoute(OPTIONS)
            routes += itemRoute(GET)
            routes += itemRoute(OPTIONS)
            routes += dropTablesRoute(GET)
            routes += dropTablesRoute(OPTIONS)
            routes += dropTableRoute(GET)
            routes += dropTableRoute(OPTIONS)
            routes += nodesRoute(GET)
            routes += nodesRoute(OPTIONS)
            routes += nodeRoute(GET)
            routes += nodeRoute(OPTIONS)
            routes += recipesRoute(GET)
            routes += recipesRoute(OPTIONS)
            routes += recipeRoute(GET)
            routes += recipeRoute(OPTIONS)
        }
    private val handler = routes("/api/v1" bind contract)

    val server = handler.asServer(Undertow(plugin.config.getInt("web.port")))

    fun start() {
        server.start()
    }
}

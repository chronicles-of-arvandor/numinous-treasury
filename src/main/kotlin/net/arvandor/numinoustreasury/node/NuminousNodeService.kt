package net.arvandor.numinoustreasury.node

import com.rpkit.core.service.Service
import net.arvandor.numinoustreasury.NuminousTreasury
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

class NuminousNodeService(private val plugin: NuminousTreasury, private val nodeRepository: NuminousNodeRepository) :
    Service {
    private val nodesById: MutableMap<String, NuminousNode>
    private val nodesByName: MutableMap<String, NuminousNode>
    private val nodeCreationSessions: MutableMap<String, NuminousNodeCreationSession>

    init {
        val nodes = nodeRepository.nodes
        nodesById = ConcurrentHashMap()
        nodesById.putAll(
            nodes.associateBy { node -> node.id },
        )
        nodesByName = ConcurrentHashMap()
        nodesByName.putAll(
            nodes.associateBy { node -> node.name },
        )
        plugin.logger.info("Loaded " + nodesById.size + " nodes")

        nodeCreationSessions = HashMap()
    }

    override fun getPlugin(): NuminousTreasury {
        return plugin
    }

    val nodes: List<NuminousNode>
        get() = nodesById.values.toList()

    fun getNodeById(id: String?): NuminousNode? {
        return nodesById[id]
    }

    fun getNodeByName(name: String?): NuminousNode? {
        return nodesByName[name]
    }

    fun getNodeWithEntranceAtLocation(location: Location): NuminousNode? {
        return nodes.firstOrNull { node: NuminousNode -> node.entranceArea.contains(location) }
    }

    fun getNodeWithExitAtLocation(location: Location): NuminousNode? {
        return nodes
            .firstOrNull { node: NuminousNode -> node.exitArea.contains(location) }
    }

    fun getNodeWithAreaAtLocation(location: Location): NuminousNode? {
        return nodes
            .firstOrNull { node: NuminousNode -> node.area.contains(location) }
    }

    fun delete(
        node: NuminousNode,
        callback: Runnable? = null,
    ) {
        plugin.server.scheduler.runTaskAsynchronously(
            plugin,
            Runnable {
                nodeRepository.delete(node.id)
                nodesById.remove(node.id)
                nodesByName.remove(node.name)
                if (callback != null) {
                    plugin.server.scheduler.runTask(plugin, callback)
                }
            },
        )
    }

    fun save(
        node: NuminousNode,
        callback: Runnable? = null,
    ) {
        plugin.server.scheduler.runTaskAsynchronously(
            plugin,
            Runnable {
                nodeRepository.upsert(node)
                nodesById[node.id] = node
                nodesByName[node.name] = node
                if (callback != null) {
                    plugin.server.scheduler.runTask(plugin, callback)
                }
            },
        )
    }

    fun getNodeCreationSession(player: Player): NuminousNodeCreationSession? {
        return nodeCreationSessions[player.uniqueId.toString()]
    }

    fun setNodeCreationSession(
        player: Player,
        session: NuminousNodeCreationSession?,
    ) {
        if (session == null) {
            nodeCreationSessions.remove(player.uniqueId.toString())
        } else {
            nodeCreationSessions[player.uniqueId.toString()] = session
        }
    }
}

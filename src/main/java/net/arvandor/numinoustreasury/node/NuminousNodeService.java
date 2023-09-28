package net.arvandor.numinoustreasury.node;

import com.rpkit.core.service.Service;
import net.arvandor.numinoustreasury.NuminousTreasury;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class NuminousNodeService implements Service {

    private final NuminousTreasury plugin;
    private final NuminousNodeRepository nodeRepository;
    private final Map<String, NuminousNode> nodesById;
    private final Map<String, NuminousNode> nodesByName;
    private final Map<String, NuminousNodeCreationSession> nodeCreationSessions;

    public NuminousNodeService(NuminousTreasury plugin, NuminousNodeRepository nodeRepository) {
        this.plugin = plugin;
        this.nodeRepository = nodeRepository;
        List<NuminousNode> nodes = nodeRepository.getNodes();
        nodesById = new ConcurrentHashMap<>();
        nodesById.putAll(nodes.stream().collect(Collectors.toMap(NuminousNode::getId, node -> node)));
        nodesByName = new ConcurrentHashMap<>();
        nodesByName.putAll(nodes.stream().collect(Collectors.toMap(NuminousNode::getName, node -> node)));
        plugin.getLogger().info("Loaded " + nodesById.size() + " nodes");

        nodeCreationSessions = new HashMap<>();
    }

    @Override
    public NuminousTreasury getPlugin() {
        return plugin;
    }

    public List<NuminousNode> getNodes() {
        return nodesById.values().stream().toList();
    }

    public NuminousNode getNodeById(String id) {
        return nodesById.get(id);
    }

    public NuminousNode getNodeByName(String name) {
        return nodesByName.get(name);
    }

    public NuminousNode getNodeWithEntranceAtLocation(Location location) {
        return nodesById.values().stream()
                .filter(node -> node.getEntranceArea().contains(location))
                .findFirst()
                .orElse(null);
    }

    public NuminousNode getNodeWithExitAtLocation(Location location) {
        return nodesById.values().stream()
                .filter(node -> node.getExitArea().contains(location))
                .findFirst()
                .orElse(null);
    }

    public NuminousNode getNodeWithAreaAtLocation(Location location) {
        return nodesById.values().stream()
                .filter(node -> node.getArea().contains(location))
                .findFirst()
                .orElse(null);
    }

    public void delete(NuminousNode node, Runnable callback) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            nodeRepository.delete(node.getId());
            nodesById.remove(node.getId());
            nodesByName.remove(node.getName());
            plugin.getServer().getScheduler().runTask(plugin, callback);
        });
    }

    public void save(NuminousNode node, Runnable callback) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            nodeRepository.upsert(node);
            nodesById.put(node.getId(), node);
            nodesByName.put(node.getName(), node);
            plugin.getServer().getScheduler().runTask(plugin, callback);
        });
    }

    public NuminousNodeCreationSession getNodeCreationSession(Player player) {
        return nodeCreationSessions.get(player.getUniqueId().toString());
    }

    public void setNodeCreationSession(Player player, NuminousNodeCreationSession session) {
        if (session == null) {
            nodeCreationSessions.remove(player.getUniqueId().toString());
        } else {
            nodeCreationSessions.put(player.getUniqueId().toString(), session);
        }
    }

}

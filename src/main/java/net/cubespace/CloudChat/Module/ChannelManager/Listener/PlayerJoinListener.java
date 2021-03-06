package net.cubespace.CloudChat.Module.ChannelManager.Listener;

import net.cubespace.CloudChat.Config.Main;
import net.cubespace.CloudChat.Event.PlayerJoinEvent;
import net.cubespace.CloudChat.Module.ChannelManager.ChannelManager;
import net.cubespace.CloudChat.Module.PlayerManager.Database.PlayerDatabase;
import net.cubespace.CloudChat.Module.PlayerManager.PlayerManager;
import net.cubespace.lib.CubespacePlugin;
import net.cubespace.lib.EventBus.EventHandler;
import net.cubespace.lib.EventBus.EventPriority;
import net.cubespace.lib.EventBus.Listener;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class PlayerJoinListener implements Listener {
    private final CubespacePlugin plugin;
    private final PlayerManager playerManager;
    private final ChannelManager channelManager;

    public PlayerJoinListener(CubespacePlugin plugin) {
        this.plugin = plugin;
        this.playerManager = plugin.getManagerRegistry().getManager("playerManager");
        this.channelManager = plugin.getManagerRegistry().getManager("channelManager");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerDatabase playerDatabase = playerManager.get(event.getPlayer().getName());

        for(String channel : playerDatabase.JoinedChannels) {
            channelManager.join(event.getPlayer(), channelManager.get(channel));
        }

        channelManager.joinForcedChannels(event.getPlayer());

        //Check for broken focus (its a leftover from the global channel changes)
        if(!channelManager.exists(playerDatabase.Focus.toLowerCase())) {
            playerDatabase.Focus = ((Main) plugin.getConfigManager().getConfig("main")).Global.toLowerCase();
        }
    }
}
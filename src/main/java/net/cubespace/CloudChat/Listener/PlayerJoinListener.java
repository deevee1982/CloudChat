package net.cubespace.CloudChat.Listener;

import net.cubespace.CloudChat.CloudChatPlugin;
import net.cubespace.CloudChat.Database.ChannelDatabase;
import net.cubespace.CloudChat.Database.PlayerDatabase;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 * @date Last changed: 26.11.13 23:38
 */
public class PlayerJoinListener implements Listener {
    private final CloudChatPlugin plugin;

    public PlayerJoinListener(CloudChatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPostLogin(final PostLoginEvent event) {
        //Load the Player
        plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
            @Override
            public void run() {
                PlayerDatabase playerDatabase = plugin.getPlayerManager().get(event.getPlayer());

                for(String channel : playerDatabase.JoinedChannels) {
                    ChannelDatabase channelDatabase = plugin.getChannelManager().get(channel);
                    if(channelDatabase != null) {
                        plugin.getChannelManager().join(event.getPlayer(), channelDatabase);
                    }
                }

                plugin.getChannelManager().joinForcedChannels(event.getPlayer());
            }
        });
    }
}
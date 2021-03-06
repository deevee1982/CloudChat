package net.cubespace.CloudChat.Module.IRC;

import net.cubespace.CloudChat.Command.Binder.Binder;
import net.cubespace.CloudChat.Config.CommandAliases;
import net.cubespace.CloudChat.Config.IRC;
import net.cubespace.CloudChat.Module.IRC.Bot.Bot;
import net.cubespace.CloudChat.Module.IRC.Listener.ChatMessageListener;
import net.cubespace.CloudChat.Module.IRC.Listener.IRCChatMessageListener;
import net.cubespace.CloudChat.Module.IRC.Listener.PMListener;
import net.cubespace.CloudChat.Module.IRC.Listener.PlayerChangeAFKListener;
import net.cubespace.CloudChat.Module.IRC.Listener.PlayerJoinListener;
import net.cubespace.CloudChat.Module.IRC.Listener.PlayerQuitListener;
import net.cubespace.CloudChat.Module.IRC.Listener.PluginMessageListener;
import net.cubespace.CloudChat.Module.IRC.Permission.PermissionManager;
import net.cubespace.PluginMessages.DispatchScmdMessage;
import net.cubespace.PluginMessages.RespondScmdMessage;
import net.cubespace.lib.CubespacePlugin;
import net.cubespace.lib.Module.Module;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class IRCModule extends Module {
    private Bot ircBot;

    public Bot getIrcBot() {
        return ircBot;
    }

    public void setIrcBot(Bot ircBot) {
        this.ircBot = ircBot;
    }

    public PermissionManager getPermissions() {
        return ircBot.getIrcManager().getPermissionManager();
    }

    public CubespacePlugin getPlugin() {
        return plugin;
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        if (((IRC) plugin.getConfigManager().getConfig("irc")).Enabled) {
            CommandAliases commandAliases = plugin.getConfigManager().getConfig("commandAliases");

            ircBot = new Bot(this, plugin);

            plugin.getAsyncEventBus().addListener(this, new ChatMessageListener(this, plugin));
            plugin.getAsyncEventBus().addListener(this, new PlayerJoinListener(this, plugin));
            plugin.getAsyncEventBus().addListener(this, new PlayerQuitListener(this, plugin));
            plugin.getAsyncEventBus().addListener(this, new PlayerChangeAFKListener(this, plugin));
            plugin.getAsyncEventBus().addListener(this, new PMListener(this, plugin));
            plugin.getAsyncEventBus().addListener(this, new IRCChatMessageListener(plugin));

            plugin.getBindManager().bind(commandAliases.BaseCommands.get("irc:reconnect"), Binder.class, commandAliases.IRCReconnect.toArray(new String[commandAliases.IRCReconnect.size()]));
            plugin.getBindManager().bind(commandAliases.BaseCommands.get("irc:mute"), Binder.class, commandAliases.IRCMute.toArray(new String[commandAliases.IRCMute.size()]));
            plugin.getBindManager().bind(commandAliases.BaseCommands.get("irc:unmute"), Binder.class, commandAliases.IRCUnmute.toArray(new String[commandAliases.IRCUnmute.size()]));
            plugin.getCommandExecutor().add(this, new net.cubespace.CloudChat.Module.IRC.Command.IRC(this, plugin));

            plugin.getPluginMessageManager("CloudChat").addPacketToRegister(this, DispatchScmdMessage.class);
            plugin.getPluginMessageManager("CloudChat").addPacketToRegister(this, RespondScmdMessage.class);
            plugin.getPluginMessageManager("CloudChat").addListenerToRegister(this, new PluginMessageListener(this));
        }
    }

    @Override
    public void onDisable() {
        if (ircBot != null) {
            CommandAliases commandAliases = plugin.getConfigManager().getConfig("commandAliases");

            ircBot.shutdown();

            plugin.getAsyncEventBus().removeListener(this);

            plugin.getBindManager().unbind(commandAliases.BaseCommands.get("irc:reconnect"));
            plugin.getBindManager().unbind(commandAliases.BaseCommands.get("irc:mute"));
            plugin.getBindManager().unbind(commandAliases.BaseCommands.get("irc:unmute"));
            plugin.getCommandExecutor().remove(this);

            plugin.getPluginMessageManager("CloudChat").removeListener(this);
            plugin.getPluginMessageManager("CloudChat").removePacket(this);

            ircBot = null;
        }
    }
}

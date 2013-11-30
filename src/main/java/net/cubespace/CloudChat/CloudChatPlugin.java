package net.cubespace.CloudChat;

import net.craftminecraft.bungee.bungeeyaml.bukkitapi.InvalidConfigurationException;
import net.cubespace.CloudChat.Command.Binder.Binder;
import net.cubespace.CloudChat.Command.Binder.ChannelBinder;
import net.cubespace.CloudChat.Command.Binder.JoinedChannelBinder;
import net.cubespace.CloudChat.Command.Binder.PlayerBinder;
import net.cubespace.CloudChat.Command.Channels;
import net.cubespace.CloudChat.Command.Handler.CommandExecutor;
import net.cubespace.CloudChat.Command.Nick;
import net.cubespace.CloudChat.Command.PM;
import net.cubespace.CloudChat.Command.Reload;
import net.cubespace.CloudChat.Config.IRC;
import net.cubespace.CloudChat.Config.Main;
import net.cubespace.CloudChat.IRC.Bot;
import net.cubespace.CloudChat.Listener.*;
import net.cubespace.CloudChat.Manager.ChannelManager;
import net.cubespace.CloudChat.Manager.PlayerManager;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 * @date Last changed: 24.11.13 18:02
 */
public class CloudChatPlugin extends Plugin {
    private CommandExecutor commandExecutor;
    private ChannelManager channelManager;
    private PlayerManager playerManager;
    private Main config;
    private IRC ircConfig;
    private Bot ircBot;
    private ScheduledTask botTask;

    @Override
    public void onEnable() {
        //Load the Config
        config = new Main(this);
        ircConfig = new IRC(this);
        try {
            config.init();
            ircConfig.init();
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException("Could not load Main or IRC Config", e);
        }

        //Load the Managers
        channelManager = new ChannelManager(this);
        playerManager = new PlayerManager(this);

        //Register the Commands
        //Create a new CommandExecutor
        commandExecutor = new CommandExecutor();
        commandExecutor.add(new Nick(this));
        commandExecutor.add(new Reload(this));
        commandExecutor.add(new net.cubespace.CloudChat.Command.IRC(this));
        commandExecutor.add(new Channels(this));
        commandExecutor.add(new PM(this));

        //Tell BungeeCord to bind this plugin to this commands
        //User Commands
        getProxy().getPluginManager().registerCommand(this, new ChannelBinder(this, "join", "cloudchat.command.join"));
        getProxy().getPluginManager().registerCommand(this, new JoinedChannelBinder(this, "leave", "cloudchat.command.leave"));
        getProxy().getPluginManager().registerCommand(this, new JoinedChannelBinder(this, "focus", "cloudchat.command.focus"));
        getProxy().getPluginManager().registerCommand(this, new PlayerBinder(this, "msg", "cloudchat.command.msg", "m"));
        getProxy().getPluginManager().registerCommand(this, new Binder(this, "reply", "cloudchat.command.reply", "r"));
        if(!getConfig().DoNotBind.contains("nick")) getProxy().getPluginManager().registerCommand(this, new Binder(this, "nick", "cloudchat.command.nick"));

        //IRC Commands
        getProxy().getPluginManager().registerCommand(this, new Binder(this, "irc:connect", "cloudchat.command.irc.connect"));
        getProxy().getPluginManager().registerCommand(this, new Binder(this, "irc:reconnect", "cloudchat.command.irc.reconnect"));
        getProxy().getPluginManager().registerCommand(this, new Binder(this, "irc:disconnect", "cloudchat.command.irc.disconnect"));

        //CloudChat (cc) Commands
        getProxy().getPluginManager().registerCommand(this, new Binder(this, "cc:reload", "cloudchat.command.cc.reload"));

        //Register the Listeners
        getProxy().getPluginManager().registerListener(this, new PlayerJoinListener(this));
        getProxy().getPluginManager().registerListener(this, new PlayerQuitListener(this));
        getProxy().getPluginManager().registerListener(this, new ChatListener(this));
        getProxy().getPluginManager().registerListener(this, new CloudChatFormattedListener(this));
        getProxy().getPluginManager().registerListener(this, new IRCListener(this));

        //Register the Plugin Message Channels
        getProxy().registerChannel("CloudChat");
        getProxy().getPluginManager().registerListener(this, new PluginMessageListener(this));

        //Start the IRC Bot
        if(ircConfig.Enabled) {
            ircBot = new Bot(this);
            botTask = getProxy().getScheduler().runAsync(this, ircBot);
        }
    }

    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public Main getConfig() {
        return config;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public IRC getIrcConfig() {
        return ircConfig;
    }

    public Bot getIrcBot() {
        return ircBot;
    }

    public void setIrcBot(Bot ircBot) {
        this.ircBot = ircBot;
    }

    public ScheduledTask getBotTask() {
        return botTask;
    }

    public void setBotTask(ScheduledTask botTask) {
        this.botTask = botTask;
    }
}
package net.cubespace.CloudChat.Module.PlayerManager;

import net.cubespace.CloudChat.Command.Binder.Binder;
import net.cubespace.CloudChat.Command.Binder.PlayerBinder;
import net.cubespace.CloudChat.Config.Main;
import net.cubespace.CloudChat.Module.PlayerManager.Command.Nick;
import net.cubespace.CloudChat.Module.PlayerManager.Command.Realname;
import net.cubespace.CloudChat.Module.PlayerManager.Listener.AsyncChatListener;
import net.cubespace.CloudChat.Module.PlayerManager.Listener.PlayerChangeAFKListener;
import net.cubespace.CloudChat.Module.PlayerManager.Listener.PlayerJoinListener;
import net.cubespace.CloudChat.Module.PlayerManager.Listener.PlayerNickchangeListener;
import net.cubespace.CloudChat.Module.PlayerManager.Listener.PlayerQuitListener;
import net.cubespace.CloudChat.Module.PlayerManager.Listener.PluginMessageListener;
import net.cubespace.CloudChat.Module.PlayerManager.Listener.ServerConnectListener;
import net.cubespace.PluginMessages.AFKMessage;
import net.cubespace.PluginMessages.AffixMessage;
import net.cubespace.PluginMessages.IgnoreMessage;
import net.cubespace.PluginMessages.OutputMessage;
import net.cubespace.PluginMessages.WorldMessage;
import net.cubespace.lib.CubespacePlugin;
import net.cubespace.lib.Module.Module;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 * @date Last changed: 28.12.13 12:19
 */
public class PlayerManagerModule extends Module {
    private boolean boundNick = false;
    private boolean boundRealname = false;

    public PlayerManagerModule(CubespacePlugin plugin) {
        super(plugin);
    }

    @Override
    public void onLoad() {
        //Register the PlayerManager
        plugin.getManagerRegistry().registerManager("playerManager", new PlayerManager(plugin));
    }

    @Override
    public void onEnable() {
        if(!((Main) plugin.getConfigManager().getConfig("main")).DoNotBind.contains("nick")) {
            //Register the correct Binder
            plugin.getBindManager().bind("nick", Binder.class);

            //Register this as a Command Handler
            plugin.getCommandExecutor().add(this, new Nick(plugin));

            boundNick = true;
        }

        if(!((Main) plugin.getConfigManager().getConfig("main")).DoNotBind.contains("realname")) {
            //Register the correct Binder
            plugin.getBindManager().bind("realname", PlayerBinder.class);

            //Register this as a Command Handler
            plugin.getCommandExecutor().add(this, new Realname(plugin));

            boundRealname = true;
        }

        //Register the Listener
        plugin.getAsyncEventBus().addListener(this, new PlayerJoinListener(plugin));
        plugin.getAsyncEventBus().addListener(this, new PlayerQuitListener(plugin));
        plugin.getAsyncEventBus().addListener(this, new PlayerNickchangeListener(plugin));
        plugin.getAsyncEventBus().addListener(this, new ServerConnectListener(plugin));
        plugin.getAsyncEventBus().addListener(this, new PlayerChangeAFKListener(plugin));
        plugin.getAsyncEventBus().addListener(this, new AsyncChatListener(plugin));

        //Register the Packets and the Listeners
        plugin.getPluginMessageManager("CloudChat").addPacketToRegister(this, AffixMessage.class);
        plugin.getPluginMessageManager("CloudChat").addPacketToRegister(this, AFKMessage.class);
        plugin.getPluginMessageManager("CloudChat").addPacketToRegister(this, WorldMessage.class);
        plugin.getPluginMessageManager("CloudChat").addPacketToRegister(this, IgnoreMessage.class);
        plugin.getPluginMessageManager("CloudChat").addPacketToRegister(this, OutputMessage.class);

        plugin.getPluginMessageManager("CloudChat").addListenerToRegister(this, new PluginMessageListener(plugin));
    }

    @Override
    public void onDisable() {
        if(boundNick) {
            plugin.getBindManager().unbind("nick");
        }

        if(boundRealname) {
            plugin.getBindManager().unbind("realname");
        }

        plugin.getCommandExecutor().remove(this);

        plugin.getAsyncEventBus().removeListener(this);

        plugin.getPluginMessageManager("CloudChat").removePacket(this);
        plugin.getPluginMessageManager("CloudChat").removeListener(this);
    }
}

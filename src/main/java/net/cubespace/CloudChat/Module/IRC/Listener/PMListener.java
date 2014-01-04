package net.cubespace.CloudChat.Module.IRC.Listener;

import net.cubespace.CloudChat.CloudChatPlugin;
import net.cubespace.CloudChat.Config.IRC;
import net.cubespace.CloudChat.Module.FormatHandler.Format.FontFormat;
import net.cubespace.CloudChat.Module.IRC.IRCModule;
import net.cubespace.CloudChat.Module.IRC.PMSession;
import net.cubespace.CloudChat.Module.PM.Event.PMEvent;
import net.cubespace.CloudChat.Module.PlayerManager.PlayerManager;
import net.cubespace.lib.EventBus.EventHandler;
import net.cubespace.lib.EventBus.EventPriority;
import net.cubespace.lib.EventBus.Listener;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 * @date Last changed: 03.01.14 21:46
 */
public class PMListener implements Listener {
    private CloudChatPlugin plugin;
    private IRCModule ircModule;
    private PlayerManager playerManager;

    public PMListener(IRCModule ircModule, CloudChatPlugin plugin) {
        this.plugin = plugin;
        this.ircModule = ircModule;
        this.playerManager = plugin.getManagerRegistry().getManager("playerManager");
    }

    @EventHandler(priority = EventPriority.HIGHEST, canVeto = true)
    public boolean onPM(PMEvent event) {
        IRC config = plugin.getConfigManager().getConfig("irc");

        if(FontFormat.stripColor(event.getFrom()).contains(FontFormat.stripColor(config.IngameName))) {
            //Check if there is a " " in the name
            String ircNick;
            if(event.getFrom().contains(" ")) {
                ircNick = FontFormat.stripColor(event.getFrom()).split(" ")[1];
            } else {
                ircNick = FontFormat.stripColor(event.getMessage().split(" ")[0]);
            }

            ProxiedPlayer sen = plugin.getProxy().getPlayer(event.getTo());

            if(sen == null) {
                ircModule.getIrcBot().sendToChannel("Player " + event.getTo() + " is offline", ircNick);
                return true;
            }

            if(ircModule.getIrcBot().getIrcManager().hasPmSession(ircNick)) {
                PMSession pmSession = ircModule.getIrcBot().getIrcManager().getPmSession(ircNick);
                pmSession.setTo(sen.getName());
            } else {
                ircModule.getIrcBot().getIrcManager().newPMSession(ircNick);
                PMSession pmSession = ircModule.getIrcBot().getIrcManager().getPmSession(ircNick);
                pmSession.setTo(sen.getName());
            }

            playerManager.get(sen.getName()).Reply = FontFormat.stripColor(config.IngameName) + " " + ircNick;
            sen.sendMessage(FontFormat.translateString("&6"+ config.IngameName + " " + ircNick + "&8 -> &6You&8:&7 " + event.getMessage().replace(ircNick + " ", "")));
            plugin.getPluginLogger().info(event.getFrom() + " -> " + event.getTo() + ": " + event.getMessage().replace(ircNick + " ", ""));

            return true;
        }

        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, canVeto = true)
    public boolean onPM2(PMEvent event) {
        IRC config = plugin.getConfigManager().getConfig("irc");

        if(FontFormat.stripColor(event.getTo()).contains(FontFormat.stripColor(config.IngameName))) {
            //Check if there is a " " in the name
            String ircNick;
            if(event.getTo().contains(" ")) {
                ircNick = FontFormat.stripColor(event.getTo()).split(" ")[1];
            } else {
                ircNick = FontFormat.stripColor(event.getMessage().split(" ")[0]);
            }

            event.setTo(config.IngameName + " " + ircNick);
            ProxiedPlayer sen = plugin.getProxy().getPlayer(event.getFrom());

            //Check if sender can do this
            if(!sen.hasPermission("cloudchat.pm.irc")) {
                sen.sendMessage("You can not send PMs to IRC");
                return true;
            }

            if(ircModule.getIrcBot().getIrcManager().isNickOnline(ircNick)) {
                if(ircModule.getIrcBot().getIrcManager().hasPmSession(ircNick)) {
                    PMSession pmSession = ircModule.getIrcBot().getIrcManager().getPmSession(ircNick);
                    pmSession.setTo(sen.getName());
                } else {
                    ircModule.getIrcBot().getIrcManager().newPMSession(ircNick);
                    PMSession pmSession = ircModule.getIrcBot().getIrcManager().getPmSession(ircNick);
                    pmSession.setTo(sen.getName());
                }

                playerManager.get(sen.getName()).Reply = FontFormat.stripColor(config.IngameName) + " " + ircNick;

                ircModule.getIrcBot().sendToChannel(event.getFrom() + ": " + event.getMessage().replace(ircNick + " ", ""), ircNick);
                sen.sendMessage(FontFormat.translateString("&6You&8 -> &6"+ config.IngameName + " " + ircNick + "&8:&7 " + event.getMessage().replace(ircNick + " ", "")));
                plugin.getPluginLogger().info(event.getFrom() + " -> " + event.getTo() + ": " + event.getMessage().replace(ircNick + " ", ""));
            } else {
                sen.sendMessage(FontFormat.translateString(FontFormat.translateString("&7The IRC Nick is not online")));
            }


            return true;
        }

        return false;
    }
}

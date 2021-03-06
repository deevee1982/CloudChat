package net.cubespace.CloudChat.Module.ChannelManager.Command;

import net.cubespace.CloudChat.Config.Messages;
import net.cubespace.CloudChat.Module.ChannelManager.ChannelManager;
import net.cubespace.CloudChat.Module.ChannelManager.Database.ChannelDatabase;
import net.cubespace.CloudChat.Module.FormatHandler.Format.FontFormat;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.lib.Chat.MessageBuilder.MessageBuilder;
import net.cubespace.lib.Command.CLICommand;
import net.cubespace.lib.Command.Command;
import net.cubespace.lib.CubespacePlugin;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class CreateChannel implements CLICommand {
    private final CubespacePlugin plugin;
    private final ChannelManager channelManager;

    public CreateChannel(CubespacePlugin plugin) {
        this.plugin = plugin;
        this.channelManager = plugin.getManagerRegistry().getManager("channelManager");
    }

    @Command(command = "createchannel", arguments = 1)
    public void createChannelCommand(CommandSender sender, String[] args) {
        Messages messages = plugin.getConfigManager().getConfig("messages");

        String name = args[0];
        String password = "";
        if(args.length > 1) {
            password = args[1];
        }

        //Check if name collides
        for(ChannelDatabase channel : channelManager.getChannels()) {
            if(channel.Name.equalsIgnoreCase(name)) {
                MessageBuilder messageBuilder = new MessageBuilder();
                messageBuilder.setText(FontFormat.translateString(messages.Command_Channel_Create_AlreadyExists)).send(sender);

                return;
            }
        }

        //Get the Alias
        String alias = name.substring(0 , 1).toUpperCase();

        //Save the new Channel
        ChannelDatabase channelDatabase = new ChannelDatabase(plugin, name.toLowerCase());
        channelDatabase.Name = name;
        channelDatabase.Password = password;
        channelDatabase.Forced = false;
        channelDatabase.Format = "&8[&2%channel_short&8] %prefix%nick{click:playerMenu}%suffix&r: %message";
        channelDatabase.Short = alias;
        channelDatabase.CanInvite.add(sender.getName());

        try {
            channelDatabase.save();
        } catch (InvalidConfigurationException e) {
            MessageBuilder messageBuilder = new MessageBuilder();
            messageBuilder.setText(FontFormat.translateString(messages.Command_Channel_Create_ErrorInSave)).send(sender);
            plugin.getPluginLogger().warn("Error creating new Channel", e);
            return;
        }

        //Join the Channel
        channelManager.reload();

        if(sender instanceof ProxiedPlayer) {
            sender.setPermission("cloudchat.channel." + channelDatabase.Name, true);
            channelManager.join((ProxiedPlayer) sender, channelDatabase);
        }

        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setText(FontFormat.translateString(messages.Command_Channel_Create_CreatedChannel.replace("%channel", channelDatabase.Name).replace("%password", channelDatabase.Password))).send(sender);
    }
}

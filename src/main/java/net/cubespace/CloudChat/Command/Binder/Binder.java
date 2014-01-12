package net.cubespace.CloudChat.Command.Binder;

import net.cubespace.CloudChat.Config.Messages;
import net.cubespace.CloudChat.Module.FormatHandler.Format.FontFormat;
import net.cubespace.lib.Chat.MessageBuilder.MessageBuilder;
import net.cubespace.lib.CubespacePlugin;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class Binder extends Command {
    protected String commandName;
    protected CubespacePlugin plugin;

    public Binder(CubespacePlugin plugin, String name, String... aliases) {
        super(name, null, aliases);

        this.plugin = plugin;
        this.commandName = name;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if(plugin.getPermissionManager().has(commandSender, "cloudchat.command.*") || plugin.getPermissionManager().has(commandSender, "cloudchat.command." + commandName.replace(":", "."))) {
            plugin.getCommandExecutor().onCommand(commandSender, commandName, strings);
        } else {
            MessageBuilder messageBuilder = new MessageBuilder();
            messageBuilder.setText(FontFormat.translateString(((Messages) plugin.getConfigManager().getConfig("messages")).NoPermission)).send(commandSender);
        }
    }
}

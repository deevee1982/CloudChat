package net.cubespace.CloudChat.Config;

import net.cubespace.Yamler.Config.Config;
import net.cubespace.lib.CubespacePlugin;

import java.io.File;
import java.util.ArrayList;

public class Main extends Config {
    public Main(CubespacePlugin plugin) {
        CONFIG_FILE = new File(plugin.getDataFolder(), "config.yml");
        CONFIG_HEADER = new String[]{"CloudChat by geNAZt"};
    }

    public int MaxChannelsPerChatter = 3;
    public String Global = "global";
    public Boolean PrivateMode = false;
    public ArrayList<String> DoNotBind = new ArrayList<>();
    public boolean Announce_PlayerJoin = false;
    public boolean Announce_PlayerQuit = false;
    public ArrayList<String> DontHandleForServers = new ArrayList<>();
    public Integer DelayFor = 0;
}

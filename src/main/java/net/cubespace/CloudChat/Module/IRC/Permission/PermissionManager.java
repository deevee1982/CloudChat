package net.cubespace.CloudChat.Module.IRC.Permission;

import net.cubespace.CloudChat.Config.IRCPermissions;
import net.cubespace.CloudChat.Config.Sub.IRCPermissionGroup;
import net.cubespace.CloudChat.Config.Sub.IRCPermissionUser;
import net.cubespace.CloudChat.Module.IRC.IRCManager;
import net.cubespace.lib.CubespacePlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class PermissionManager {
    private class SortGroup implements Comparator<IRCPermissionGroup> {
        @Override
        public int compare(IRCPermissionGroup a1, IRCPermissionGroup a2) {
            return a1.Rank - a2.Rank;
        }
    }

    private final CubespacePlugin plugin;
    private final IRCManager ircManager;

    //Loaded Auth permissions
    private final HashMap<String, PermissionEntry> loadedPermisisons = new HashMap<>();

    public PermissionManager(IRCManager ircManager, CubespacePlugin plugin) {
        this.plugin = plugin;
        this.ircManager = ircManager;
    }

    private PermissionEntry calcEffectivePermissions(String auth) {
        //Get the IRCPermissions
        IRCPermissions ircPermissions = plugin.getConfigManager().getConfig("ircPermissions");

        //Get the Groups
        ArrayList<IRCPermissionGroup> groups = new ArrayList<>();

        //Aditional User Permissions
        ArrayList<String> userPermissions = new ArrayList<>();

        //Permissions for this auth
        ArrayList<String> permissions = new ArrayList<>();

        if(auth == null) {
            IRCPermissionGroup group = ircPermissions.Groups.get("default");

            groups.add(group);

            //Check for inherits
            if(group.Inherits.size() > 0) {
                for(String inheritGroup : group.Inherits) {
                    IRCPermissionGroup ircPermissionGroup = ircPermissions.Groups.get(inheritGroup);

                    if(ircPermissionGroup != null) {
                        groups.add(ircPermissionGroup);
                    }
                }
            }
        } else {
            //Get auth groups
            IRCPermissionUser ircPermissionUser = ircPermissions.Users.get(auth);

            //No record found
            if(ircPermissionUser == null) {
                IRCPermissionGroup group = ircPermissions.Groups.get("default");

                groups.add(group);

                //Check for inherits
                if(group.Inherits.size() > 0) {
                    for(String inheritGroup : group.Inherits) {
                        IRCPermissionGroup ircPermissionGroup = ircPermissions.Groups.get(inheritGroup);

                        if(ircPermissionGroup != null) {
                            groups.add(ircPermissionGroup);
                        }
                    }
                }
            } else {
                ArrayList<String> userGroups = ircPermissionUser.Groups;

                for(String group : userGroups) {
                    IRCPermissionGroup ircPermissionGroup = ircPermissions.Groups.get(group);

                    if(ircPermissionGroup != null) {
                        groups.add(ircPermissionGroup);

                        //Check for inherits
                        if(ircPermissionGroup.Inherits.size() > 0) {
                            for(String inheritGroup : ircPermissionGroup.Inherits) {
                                IRCPermissionGroup ircPermissionGroupInherit = ircPermissions.Groups.get(inheritGroup);

                                if(ircPermissionGroupInherit != null) {
                                    groups.add(ircPermissionGroupInherit);
                                }
                            }
                        }
                    }
                }

                userPermissions = ircPermissionUser.Permissions;
            }
        }

        //Sort the groups depending on the Rank
        Collections.sort(groups, new SortGroup());

        //Calc the Permissions
        IRCPermissionGroup lastGroup = null;
        for(IRCPermissionGroup group : groups) {
            if(group.Permissions.size() > 0) {
                for(String permission : group.Permissions) {
                    if(permission.startsWith("-")) {
                        //Remove a Permission
                        String removePermission = permission.substring(1);

                        if(permissions.contains(removePermission)) {
                            permissions.remove(removePermission);
                        }
                    } else {
                        //Add a Permission
                        if(!permissions.contains(permission))
                            permissions.add(permission);
                    }
                }
            }

            lastGroup = group;
        }

        //Check User permissions
        if(userPermissions.size() > 0) {
            for(String permission : userPermissions) {
                if(permission.startsWith("-")) {
                    //Remove a Permission
                    String removePermission = permission.substring(1);

                    if(permissions.contains(removePermission)) {
                        permissions.remove(removePermission);
                    }
                } else {
                    //Add a Permission
                    if(!permissions.contains(permission))
                        permissions.add(permission);
                }
            }
        }

        PermissionEntry permissionEntry = new PermissionEntry();
        permissionEntry.setPermissions(permissions);
        permissionEntry.setPrimaryGroup(lastGroup);

        return permissionEntry;
    }

    public void load(String nickname, String auth) {
        PermissionEntry permissions = calcEffectivePermissions(auth);
        loadedPermisisons.put(nickname, permissions);
    }

    public void move(String oldNick, String newNick) {
        PermissionEntry permissions = loadedPermisisons.get(oldNick);
        loadedPermisisons.remove(oldNick);
        loadedPermisisons.put(newNick, permissions);
    }

    public void reload(String auth) {
        String nickname = ircManager.getNickForAuth(auth);

        if(nickname != null) {
            load(nickname, auth);
        }
    }

    public void remove(String nickname) {
        loadedPermisisons.remove(nickname);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean has(String nickname, String permission) {
        return loadedPermisisons.containsKey(nickname) && loadedPermisisons.get(nickname).getPermissions().contains(permission);
    }

    public IRCPermissionGroup getGroup(String nickname) {
        return loadedPermisisons.get(nickname).getPrimaryGroup();
    }
}

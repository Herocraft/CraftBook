package com.sk89q.craftbook.util;

import java.util.Locale;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public enum PlayerType {

    NAME('p'), UUID('u'), CBID('i'), GROUP('g'), PERMISSION_NODE('n'), TEAM('t'), VILLAGE('v'), EMPIRE('e'), ALL('a');

    private PlayerType(char prefix) {

        this.prefix = prefix;
    }

    char prefix;

    public static PlayerType getFromChar(char c) {

        c = Character.toLowerCase(c);
        for (PlayerType t : values()) { if (t.prefix == c) return t; }
        return PlayerType.NAME;
    }

    public boolean doesPlayerPass(Player player, String line) {

        switch(this) {
            case GROUP:
                return CraftBookPlugin.inst().inGroup(player, line);
            case CBID:
                return CraftBookPlugin.inst().getUUIDMappings().getCBID(player.getUniqueId()).equals(line);
            case NAME:
                return player.getName().toLowerCase(Locale.ENGLISH).startsWith(line.toLowerCase(Locale.ENGLISH));
            case UUID:
                return player.getUniqueId().toString().toUpperCase(Locale.ENGLISH).startsWith(line.toUpperCase(Locale.ENGLISH));
            case PERMISSION_NODE:
                return player.hasPermission(line);
            case TEAM:
                try {
                    return Bukkit.getScoreboardManager().getMainScoreboard().getTeam(line).hasPlayer(player);
                } catch(Exception e) {}
                break;
            case VILLAGE: // Towny Town, because "t" was taken
                try {
                    return TownyUniverse.getDataSource().getTown(line).getResidents().contains(TownyUniverse.getDataSource().getResident(player.getName())); // Towny gets residents by name =/
                }
                catch(NotRegisteredException ex) {
                    // Ignore, it'll go on to the return false later anyway
                }
                break;
            case EMPIRE: // Towny Nation, because "n" was taken
                try {
                    return TownyUniverse.getDataSource().getNation(line).getResidents().contains(TownyUniverse.getDataSource().getResident(player.getName())); // Towny gets residents by name =/
                }
                catch(NotRegisteredException ex) {
                    // Ignore, it'll go on to the return false later anyway
                }
                break;
            case ALL:
                return true;
            default:
                return false;
        }

        return false;
    }
}
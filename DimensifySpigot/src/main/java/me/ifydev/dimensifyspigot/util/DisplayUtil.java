package me.ifydev.dimensifyspigot.util;

import me.ifydev.dimensify.api.DimensifyConstants;
import me.ifydev.dimensify.api.backend.ConnectionError;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Innectic
 * @since 06/30/2018
 */
public class DisplayUtil implements me.ifydev.dimensify.api.util.DisplayUtil {

    @Override
    public void displayError(ConnectionError error, Optional<Exception> exception) {
        String reportable = shouldReport(error) ? ChatColor.GREEN + "" + ChatColor.BOLD + "Yes": ChatColor.RED + "" + ChatColor.BOLD + "No";
        List<String> messages = DimensifyConstants.DIMENSIFY_ERROR.stream()
                .map(part -> part.replace("<ERROR_TYPE>", error.getDisplay()))
                .map(part -> part.replace("<SHOULD_REPORT>", reportable))
                .map(ColorUtil::makeReadable).collect(Collectors.toList());

        List<Player> players = Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission(DimensifyConstants.DIMENSIFY_ADMIN)).collect(Collectors.toList());
        messages.forEach(message -> players.forEach(player -> player.sendMessage(message)));
    }

    private boolean shouldReport(ConnectionError error) {
        return error != ConnectionError.REJECTED && error == ConnectionError.DATABASE_EXCEPTION;
    }
}

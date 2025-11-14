package com.blockycraft.blockytips.geoip;

import com.blockycraft.blockygeoip.BlockyGeoIP;
import com.blockycraft.blockygeoip.BlockyGeoIPAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GeoIPManager {
    private boolean geoIPEnabled = false;
    private BlockyGeoIPAPI api;
    private boolean initialized = false;

    public GeoIPManager() {
        // Constructor is now empty. Initialization is deferred.
    }

    private void setupAPI() {
        if (initialized) {
            return;
        }
        initialized = true;
        if (Bukkit.getPluginManager().isPluginEnabled("BlockyGeoIP")) {
            try {
                api = BlockyGeoIP.getInstance().getApi();
                geoIPEnabled = true;
            } catch (Exception e) {
                geoIPEnabled = false;
                Bukkit.getLogger().severe("Error while getting BlockyGeoIP API instance: " + e.getMessage());
            }
        } else {
            geoIPEnabled = false;
        }
    }

    public String getPlayerLanguage(Player player) {
        setupAPI(); // Attempt to setup the API on first use

        if (geoIPEnabled && api != null) {
            try {
                String lang = api.getPlayerLanguage(player.getUniqueId());
                if (lang != null) {
                    if (lang.contains("-")) {
                        return lang.split("-")[0].toLowerCase();
                    }
                    return lang.toLowerCase();
                }
                return "en";
            } catch (Exception e) {
                // API might not be available or something went wrong
                return "en";
            }
        }
        return "en";
    }
}

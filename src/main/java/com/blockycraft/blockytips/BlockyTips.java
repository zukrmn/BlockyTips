package com.blockycraft.blockytips;

import com.blockycraft.blockytips.geoip.GeoIPManager;
import com.blockycraft.blockytips.lang.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlockyTips extends JavaPlugin {

    private LanguageManager languageManager;
    private GeoIPManager geoIPManager;
    private Properties properties;
    private File configFile;
    private int interval = 1800;
    private int tipIndex = 0;
    private Set<String> tipsOff = ConcurrentHashMap.newKeySet();
    private File tipsOffFile;
    private final int CHAT_WRAP = 50;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadProperties();

        languageManager = new LanguageManager(this);
        geoIPManager = new GeoIPManager();

        interval = getInt("interval", 1800);

        tipsOffFile = new File(getDataFolder(), "disabled.yml");
        loadTipsOff();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::sendTip, interval * 20L, interval * 20L);
        getServer().getLogger().info("Tip scheduling started (interval: " + interval + " seconds).");
    }

    @Override
    public void onDisable() {
        saveTipsOff();
    }

    private int getInt(String key, int def) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(def)));
        } catch (Exception e) {
            return def;
        }
    }

    private String getLastColor(String text) {
        String color = "";
        for (int i = text.length() - 1; i >= 0; i--) {
            if (text.charAt(i) == '§' && i + 1 < text.length()) {
                color = text.substring(i, i + 2);
                break;
            }
        }
        return color;
    }

    private List<String> wrapTextWithColor(String text, int maxLength, String baseColor) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        String color = baseColor;
        for (String word : words) {
            if (line.length() + word.length() + 1 > maxLength) {
                lines.add(color + line.toString());
                color = getLastColor(line.toString().isEmpty() ? baseColor : line.toString());
                line = new StringBuilder();
            }
            if (line.length() > 0) line.append(" ");
            line.append(word);
        }
        if (line.length() > 0) lines.add(color + line.toString());
        return lines;
    }

    private void sendTip() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!tipsOff.contains(p.getName().toLowerCase())) {
                String lang = geoIPManager.getPlayerLanguage(p);
                List<String> tips = languageManager.getTips(lang);
                if (tips.isEmpty()) continue;

                String tip = tips.get(tipIndex % tips.size());
                tip = ChatColor.translateAlternateColorCodes('&', tip);
                String extra = languageManager.get(lang, "command.tip-disabled");

                String baseColor = getLastColor(tip.length() > 0 ? tip.substring(0, 2) : "§e");
                for (String linha : wrapTextWithColor(tip, CHAT_WRAP, baseColor.isEmpty() ? "§e" : baseColor)) {
                    p.sendMessage(linha);
                }
                for (String linha : wrapTextWithColor(extra, CHAT_WRAP, "§e")) {
                    p.sendMessage(linha);
                }
            }
        }
        tipIndex++;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(languageManager.get("en", "command.only-players"));
            return true;
        }
        Player player = (Player) sender;
        String name = player.getName().toLowerCase();
        String lang = geoIPManager.getPlayerLanguage(player);

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("off")) {
                tipsOff.add(name);
                player.sendMessage(languageManager.get(lang, "command.tips.off"));
                saveTipsOff();
                return true;
            }
            if (args[0].equalsIgnoreCase("on")) {
                tipsOff.remove(name);
                player.sendMessage(languageManager.get(lang, "command.tips.on"));
                saveTipsOff();
                return true;
            }
        }
        player.sendMessage(languageManager.get(lang, "command.tips.usage"));
        return true;
    }

    private void loadTipsOff() {
        tipsOff.clear();
        if (tipsOffFile.exists()) {
            try {
                BufferedReader r = new BufferedReader(new FileReader(tipsOffFile));
                String line;
                while ((line = r.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) tipsOff.add(line.toLowerCase());
                }
                r.close();
            } catch (Exception ignored) {
            }
        }
    }

    private void saveTipsOff() {
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(tipsOffFile, false));
            for (String name : tipsOff) {
                w.write(name);
                w.newLine();
            }
            w.close();
        } catch (Exception ignored) {
        }
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public GeoIPManager getGeoIPManager() {
        return geoIPManager;
    }

    public Properties getProperties() {
        if (properties == null) {
            reloadProperties();
        }
        return properties;
    }

    public void reloadProperties() {
        if (configFile == null) {
            configFile = new File(getDataFolder(), "config.properties");
        }
        properties = new Properties();
        try (InputStream input = new FileInputStream(configFile)) {
            properties.load(input);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDefaultConfig() {
        if (configFile == null) {
            configFile = new File(getDataFolder(), "config.properties");
        }
        if (!configFile.exists()) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties")) {
                if (in != null) {
                    if (!getDataFolder().exists()) {
                        getDataFolder().mkdirs();
                    }
                    java.nio.file.Files.copy(in, configFile.toPath());
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }
}


package com.blockycraft.blockytips;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlockyTips extends JavaPlugin {

    private Properties config;
    private List<String> tips = new ArrayList<String>();
    private int interval = 1800;
    private int tipIndex = 0;
    private Set<String> tipsOff = ConcurrentHashMap.newKeySet();
    private File tipsOffFile;

    @Override
    public void onEnable() {
        File configFile = new File(getDataFolder(), "tips.properties");
        if (!configFile.exists()) {
            try {
                getDataFolder().mkdirs();
                InputStream in = getClass().getClassLoader().getResourceAsStream("tips.properties");
                if (in != null) {
                    OutputStream out = new FileOutputStream(configFile);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                    out.close();
                    in.close();
                } else {
                    configFile.createNewFile();
                }
            } catch (Exception e) {
                getServer().getLogger().severe("Erro ao gerar tips.properties: " + e.getMessage());
            }
        }

        config = new Properties();
        try {
            FileInputStream input = new FileInputStream(configFile);
            config.load(input);
            input.close();
        } catch (Exception e) {
            getServer().getLogger().severe("Erro ao carregar tips.properties: " + e.getMessage());
        }

        // Carregar dicas
        tips.clear();
        for (int i = 1; ; i++) {
            String tip = config.getProperty("tip." + i);
            if (tip == null) break;
            tips.add(tip.replace("&", "§"));
        }
        interval = getInt("interval", 1800);

        getServer().getLogger().info("BlockyTips carregou " + tips.size() + " dicas.");
        if (tips.isEmpty()) {
            getServer().getLogger().warning("Nenhuma dica foi encontrada em tips.properties!");
        }

        tipsOffFile = new File(getDataFolder(), "disabled.yml");
        loadTipsOff();

        if (!tips.isEmpty()) {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                public void run() {
                    sendTip();
                }
            }, interval * 20L, interval * 20L);
            getServer().getLogger().info("Agendamento das dicas iniciado (intervalo: " + interval + " segundos).");
        } else {
            getServer().getLogger().warning("Dicas nao agendadas pois nenhuma foi carregada.");
        }
    }

    @Override
    public void onDisable() {
        saveTipsOff();
    }

    private int getInt(String key, int def) {
        try {
            return Integer.parseInt(config.getProperty(key, String.valueOf(def)));
        } catch (Exception e) {
            return def;
        }
    }

    private void sendTip() {
        if (tips.isEmpty()) return;
        String tip = tips.get(tipIndex);
        tipIndex = (tipIndex + 1) % tips.size();
        String extra = "§fUtilize §c/dicas off §fpara desabilitar as dicas.";
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!tipsOff.contains(p.getName().toLowerCase())) {
                p.sendMessage(tip);
                p.sendMessage(extra);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Apenas jogadores podem configurar dicas.");
            return true;
        }
        Player player = (Player) sender;
        String name = player.getName().toLowerCase();

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("off")) {
                tipsOff.add(name);
                player.sendMessage("§cVoce desativou as dicas automaticas.");
                saveTipsOff();
                return true;
            }
            if (args[0].equalsIgnoreCase("on")) {
                tipsOff.remove(name);
                player.sendMessage("§aVoce ativou as dicas automaticas!");
                saveTipsOff();
                return true;
            }
        }
        player.sendMessage("§Use §b/dicas on§e para ativar ou §b/dicas off§e para desativar as dicas automaticas.");
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
            } catch (Exception ignored) {}
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
        } catch (Exception ignored) {}
    }
}

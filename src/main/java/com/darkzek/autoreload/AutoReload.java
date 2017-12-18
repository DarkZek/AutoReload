package com.darkzek.autoreload;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Time;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * Created by darkzek on 18/12/17.
 */
public class AutoReload extends JavaPlugin {
    HashMap<String, Long> timeSinceLastChanged = new HashMap<String, Long>();
    HashMap<String, String> fileToPluginName = new HashMap<String, String>();

    String pluginsFolder;

    @Override
    public void onEnable() {
        //Get location of plugins folder
        pluginsFolder = this.getDataFolder().getAbsolutePath().replace("AutoReload", "");
        //Generate list of times
        LogTimes();
        //Get plugin jar to plugin name
        GetPlugins();
        //Set it to check every so often
        new BukkitRunnable() {
            public void run() {
                CheckIfModified();
            }
        }.runTaskTimerAsynchronously(this, 1, 20); //auto complete this statement
    }

    void GetPlugins() {
        Plugin[] plugins = Bukkit.getServer().getPluginManager().getPlugins();
        for (int i = 0; i < plugins.length; i++) {
            Plugin plugin = plugins[i];
            String location = plugin.getClass().getProtectionDomain().getCodeSource().getLocation() + "";
            String[] temp = location.split("/");
            location = temp[temp.length - 1];
            try {
                location = URLDecoder.decode(location, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                getLogger().log(Level.SEVERE, "Your java does not support converting to UTF-8, you really need to get a better pc/java");
                return;
            }

            fileToPluginName.put(location, plugin.getName());
        }
    }

    void CheckIfModified() {
        File folder = new File(pluginsFolder);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            String fileName = listOfFiles[i].getName();
            if (listOfFiles[i].isFile() && fileName.endsWith(".jar")) {
                //Its a plugin, check if its on our list
                if (timeSinceLastChanged.containsKey(fileName)) {
                    //Should be checking this one
                    long time = timeSinceLastChanged.get(fileName);
                    if (time < listOfFiles[i].lastModified()) {
                        //Has been modified!
                        String pluginName = fileToPluginName.get(fileName);
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "plugman reload " + pluginName);
                        Bukkit.getServer().broadcastMessage("Successfully reloaded " + pluginName + "!");
                        timeSinceLastChanged.remove(fileName);
                        timeSinceLastChanged.put(fileName, listOfFiles[i].lastModified());
                    }
                }
            }
        }
    }

    void LogTimes() {
        File folder = new File(pluginsFolder);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".jar")) {
                System.out.println("Found plugin " + listOfFiles[i].getName());
                timeSinceLastChanged.put(listOfFiles[i].getName(), listOfFiles[i].lastModified());
            }
        }
    }

}

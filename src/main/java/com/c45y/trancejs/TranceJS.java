/*
 * The MIT License
 *
 * Copyright 2015 c45y.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.c45y.trancejs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 *
 * @author c45y
 */
public class TranceJS extends JavaPlugin {
    private static TranceJS _instance;
    private JedisPool _jedis;
    private HashMap<String, String> _cmdlets = new HashMap<String, String>();

    @Override
    public void onEnable() {
        _instance = this;
        this.getConfig().options().copyDefaults(true);
        this.getConfig().addDefault("forcePermissions", true);
        this.getConfig().addDefault("forceAsync", true);
        this.saveConfig();
        
        /* Set up folders */
        _cmdlets = getScriptMap("cmdlets");
        
        this.getCommand("js").setExecutor(new TranceCommandHandler(this));
        
        /* Set up redis pub/sub */
        String server = getConfig().getString("redis.server", "localhost");
        Integer port = getConfig().getInt("redis.port", 6379);
        JedisPoolConfig poolconfig = new JedisPoolConfig();

        if (getConfig().isSet("redis.timeout") && getConfig().isSet("redis.password")) {
            _jedis = new JedisPool(poolconfig, server, port, getConfig().getInt("redis.timeout"), getConfig().getString("redis.password"));
        } else if (getConfig().isSet("redis.timeout")) {
            _jedis = new JedisPool(poolconfig, server, port, getConfig().getInt("redis.timeout"));
        } else {
            _jedis = new JedisPool(poolconfig, server, port);
        }
        getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                try (Jedis jedis = _jedis.getResource()) {
                    jedis.psubscribe(new TranceSub(), "trance.*");
                    _instance.getLogger().log(Level.INFO, "Subscribed to redis channel trance.*");
                }
            }
        });
        
    }

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        _jedis.destroy();
    }
    
    public static TranceJS getInstance() {
        return _instance;
    }
    
    public HashMap<String, String> getCmdlets() {
        return _cmdlets;
    }
    
    public boolean shouldForcePermissions() {
        return getConfig().getBoolean("forcePermissions");
    }
    
    public boolean shouldForceAsync() {
        return getConfig().getBoolean("forceAsync");
    }
    
    private HashMap<String, String> getScriptMap(String type) {
        HashMap<String, String> scriptMap = new HashMap<String, String>();
        File[] files = getScripts(type);
        if (files != null) {
            for( File f: files) {
                String cmd = f.getName().replace(".cmd.js", "");
                scriptMap.put(cmd, f.getAbsolutePath());
                getLogger().log(Level.INFO, "Loaded {0} as command {1}", new String[] {f.getAbsolutePath(), cmd});
            }
        }
        return scriptMap;
    }
    
    private File[] getScripts(String type) {
        File cmdscripts = new File(this.getDataFolder(), type);
        if (!cmdscripts.exists()) {
            try {
                cmdscripts.createNewFile();
            } catch (IOException ex) {
                this.getLogger().log(Level.SEVERE, "Failed to create \"{0}\" directory!", type);
            }
        }
        return cmdscripts.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".cmd.js");
            }
        });
    }
    
    public String getScript(String scriptName) throws FileNotFoundException {
        File script = new File(scriptName);
        try {
            getLogger().log(Level.INFO, "Searching for script {0}", script.getCanonicalPath());
        } catch (IOException ex) {
            getLogger().log(Level.INFO, "Failed searching for script {0}", ex.getMessage());
        }
        Scanner scanner = new Scanner(script);
        return scanner.useDelimiter("\\A").next();
    }
}

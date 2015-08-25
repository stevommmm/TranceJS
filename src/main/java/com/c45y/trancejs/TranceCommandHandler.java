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
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author c45y
 */
public class TranceCommandHandler implements CommandExecutor {

    private final TranceJS _plugin;
    
    public TranceCommandHandler(TranceJS plugin) {
        _plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] rargs) {  
        if (rargs.length == 0) {
            sender.sendMessage(ChatColor.RED + "Failed to run script: No script found.");
            return true;
        }
        
        /* Create a copy of our args, minus the first element which is our command name */
        String [] args = Arrays.copyOfRange(rargs, 1, rargs.length);
        String jsCommand = rargs[0];
        
        if (_plugin.shouldForcePermissions()) {
            if (!sender.hasPermission("trance.js." + jsCommand)) {
                sender.sendMessage(ChatColor.RED + "Failed to run script: Permission " + ChatColor.GOLD + "trance.js." + jsCommand + ChatColor.RED + " not found.");
                return true;
            }
        }
        
        
        try {
            String script = getScript(jsCommand);
            RunnableJS task = new RunnableJS(_plugin, sender, args, script);
            if (_plugin.shouldForceAsync()) {
                _plugin.getServer().getScheduler().runTaskAsynchronously(_plugin, task);
            } else {
                _plugin.getServer().getScheduler().runTask(_plugin, task);
            }
        } catch (FileNotFoundException e) {
            sender.sendMessage(ChatColor.RED + "Failed to run script: No script found.");
        }
        
        return true;
    }
    
    private String getScript(String scriptName) throws FileNotFoundException {
        File script = new File(_plugin.getDataFolder(), scriptName + ".cmd.js");
        try {
            _plugin.getLogger().log(Level.INFO, "Searching for script {0}", script.getCanonicalPath());
        } catch (IOException ex) {
            _plugin.getLogger().log(Level.INFO, "Failed searching for script {0}", ex.getMessage());
        }
        Scanner scanner = new Scanner(script);
        return scanner.useDelimiter("\\A").next();
    }
}

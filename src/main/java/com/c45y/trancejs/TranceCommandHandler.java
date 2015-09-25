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

import java.io.FileNotFoundException;
import java.util.Arrays;
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
        
        if (!_plugin.getCmdlets().containsKey(jsCommand)) {
            sender.sendMessage(ChatColor.RED + "Failed to run script: No script found.");
            return true;
        }
        
        try {
            // Get the absolute path to our file and read it to a String
            String script = _plugin.getScript(_plugin.getCmdlets().get(jsCommand));
            
            RunnableJS task = new RunnableJS(_plugin, sender, args, script, _plugin.shouldForceAsync(), "COMMAND");
            
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
}

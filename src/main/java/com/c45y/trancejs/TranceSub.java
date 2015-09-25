/*
 * The MIT License
 *
 * Copyright 2015 smcgregor.
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
import java.util.logging.Level;
import org.bukkit.command.CommandSender;
import redis.clients.jedis.JedisPubSub;

/**
 *
 * @author c45y
 */
public class TranceSub extends JedisPubSub {
    private TranceJS _plugin;
    
    public TranceSub() {
        _plugin = TranceJS.getInstance();
    }
    
    @Override
    final public void onPMessage(String pattern, String channel, String message) {
        this.handleCommand(channel, message);
    }

    @Override
    public void onMessage(String channel, String message) {
        this.handleCommand(channel, message);
    }
    
    private void handleCommand(String channel, String message) {
        _plugin.getLogger().log(Level.INFO, "SUB - {0}[{1}]", new String[]{channel, message});
        String jsCommand = channel.replace("trance.", "");
        CommandSender sender = _plugin.getServer().getConsoleSender();
        String[] args = message.split(" ");
        _plugin.getLogger().log(Level.INFO, "Command run by SUB - {0}[{1}] as {2}", new String[]{jsCommand, String.join(",", args), sender.getName()});
        
        
        if (!_plugin.getCmdlets().containsKey(jsCommand)) {
            _plugin.getLogger().log(Level.INFO, "Failed to run script: Not found in cmdlets");
            return;
        }
        
        try {
            // Get the absolute path to our file and read it to a String
            String script = _plugin.getScript(_plugin.getCmdlets().get(jsCommand));
            
            RunnableJS task = new RunnableJS(_plugin, sender, args, script, _plugin.shouldForceAsync(), "PUBSUB");
            
            if (_plugin.shouldForceAsync()) {
                _plugin.getServer().getScheduler().runTaskAsynchronously(_plugin, task);
            } else {
                _plugin.getServer().getScheduler().runTask(_plugin, task);
            }
        } catch (FileNotFoundException e) {
            _plugin.getLogger().log(Level.INFO, "Failed to run script: {0}", e.getMessage());
        }
    }

    @Override public void onSubscribe(String string, int i) {    }
    @Override public void onUnsubscribe(String string, int i) {    }
    @Override public void onPUnsubscribe(String string, int i) {    }
    @Override public void onPSubscribe(String string, int i) {    }
}

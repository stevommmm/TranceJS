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

import com.c45y.trancejs.js.JSCommand;
import com.c45y.trancejs.js.JSServer;
import java.util.logging.Level;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author c45y
 */
public class RunnableJS implements Runnable {
    private TranceJS _plugin;
    private CommandSender _sender;
    private String[] _args;
    private String _script;
    private boolean _isAsync;
    
    
    public RunnableJS(TranceJS plugin, CommandSender sender, String[] args, String script, boolean isAsync) {
        _plugin = plugin;
        _sender = sender;
        _args = args;
        _script = script;
        _isAsync = isAsync;
    }
    
    @Override
    public void run() {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            engine.put("Command", new JSCommand(_sender, _args));
            engine.put("Server", new JSServer(_plugin.getServer()));
            engine.put("isAsync", _isAsync);
            engine.eval(_script);
        } catch (ScriptException ex) {
            _plugin.getLogger().log(Level.SEVERE, "Failed to run script: " + ex.getMessage());
            _sender.sendMessage(ChatColor.RED + "Failed to run script: " + ex.getMessage());
        }
    }
    
}

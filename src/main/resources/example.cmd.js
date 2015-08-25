/* 
 * Example JavaScript usage
 * 
 * Global Scope
 *     Command
 *     Server
 * 
 */
var s = Command.getSender().getPlayer();
if (s !== null) {
    print(Command.getSender().getPlayer().getName());
} else {
    print('Console command sender.');
} 

var pl = Server.getOnlinePlayers();
for( var i = 0; i < pl.length; i++) {
	print(pl.getName());
}
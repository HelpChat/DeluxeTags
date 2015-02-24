package me.clip.deluxetags;

import java.util.ArrayList;
import java.util.List;

import me.clip.deluxechat.events.DeluxeChatEvent;
import me.clip.deluxechat.objects.DeluxeFormat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

	DeluxeTags plugin;

	public ChatListener(DeluxeTags i) {
		plugin = i;
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		if (!DeluxeTag.hasTagLoaded(e.getPlayer())) {
			
			String identifier = plugin.playerFile.getTagIdentifier(e.getPlayer().getUniqueId().toString());
			
			if (identifier != null 
					&& DeluxeTag.getLoadedTag(identifier) != null 
					&& DeluxeTag.getLoadedTag(identifier).hasTagPermission(e.getPlayer())) {
				
				DeluxeTag.getLoadedTag(identifier).setPlayerTag(e.getPlayer());
			} else {
			
				plugin.dummy.setPlayerTag(e.getPlayer());
			}
		}
	}

	@EventHandler
	public void onChat(DeluxeChatEvent e) {

		if (e.getDeluxeFormat() == null) {
			return;
		}

		String uuid = e.getPlayer().getUniqueId().toString();

		String tag = DeluxeTag.getPlayerDisplayTag(uuid);

		DeluxeFormat format = e.getDeluxeFormat();
		
		if (format.getChannel() != null && !format.getChannel().isEmpty()) {
			if (format.getChannel().contains("%deluxetag%")) {
				format.setChannel(format.getChannel().replace("%deluxetag%", tag));
			}
		}
		
		if (format.getPrefix() != null && !format.getPrefix().isEmpty()) {
			if (format.getPrefix().contains("%deluxetag%")) {
				format.setPrefix(format.getPrefix().replace("%deluxetag%", tag));
			}
		}
		
		if (format.getName() != null && !format.getName().isEmpty()) {
			if (format.getName().contains("%deluxetag%")) {
				format.setName(format.getName().replace("%deluxetag%", tag));
			}
		}
		
		if (format.getSuffix() != null && !format.getSuffix().isEmpty()) {
			if (format.getSuffix().contains("%deluxetag%")) {
				format.setSuffix(format.getSuffix().replace("%deluxetag%", tag));
			}
		}

		if (format.showChannelTooltip() && format.getChannelTooltip() != null) {
			List<String> temp = new ArrayList<String>();
			
			for (String line : format.getChannelTooltip()) {
				if (line.contains("%deluxetag%")) {
					temp.add(line.replace("%deluxetag%", tag));
				} else {
					temp.add(line);
				}
			}
			
			format.setChannelTooltip(temp);
		}
		if (format.showPreTooltip() && format.getPrefixTooltip() != null) {
			List<String> temp = new ArrayList<String>();
			
			for (String line : format.getPrefixTooltip()) {
				if (line.contains("%deluxetag%")) {
					temp.add(line.replace("%deluxetag%", tag));
				} else {
					temp.add(line);
				}
			}
			
			format.setPrefixTooltip(temp);
		}
		if (format.showNameTooltip() && format.getNameTooltip() != null) {
			List<String> temp = new ArrayList<String>();
			
			for (String line : format.getNameTooltip()) {
				if (line.contains("%deluxetag%")) {
					temp.add(line.replace("%deluxetag%", tag));
				} else {
					temp.add(line);
				}
			}
			
			format.setNameTooltip(temp);
		}
		if (format.showSuffixTooltip() && format.getSuffixTooltip() != null) {
			List<String> temp = new ArrayList<String>();
			
			for (String line : format.getSuffixTooltip()) {
				if (line.contains("%deluxetag%")) {
					temp.add(line.replace("%deluxetag%", tag));
				} else {
					temp.add(line);
				}
			}
			
			format.setSuffixTooltip(temp);
		}
		
		e.setDeluxeFormat(format);
	}
}

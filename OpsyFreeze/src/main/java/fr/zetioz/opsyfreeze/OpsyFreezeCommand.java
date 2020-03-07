package fr.zetioz.opsyfreeze;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class OpsyFreezeCommand implements CommandExecutor {

	private OpsyFreezeMain main;
	private YamlConfiguration messagesFile;
	private YamlConfiguration configsFile;
	private Map<UUID, Freeze> playerFrozen;
	private String prefix;

	public OpsyFreezeCommand(OpsyFreezeMain main)
	{
		this.main = main;
		this.messagesFile = main.getFilesManager().getMessagesFile();
		this.configsFile = main.getFilesManager().getConfigsFile();
		this.playerFrozen = new HashMap<>();
		this.prefix = ChatColor.translateAlternateColorCodes('&',  this.messagesFile.getString("prefix"));
	}
	
	public void setPlayerFrozen(Map<UUID, Freeze> playerFrozen)
	{
		this.playerFrozen = playerFrozen;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("opsyfreeze"))
		{
			if(args.length == 0)
			{
				sendHelpPage(sender);
			}
			else if(args.length >= 1)
			{
				if(args[0].equalsIgnoreCase("help"))
				{
					sendHelpPage(sender);
				}
				else if(args[0].equalsIgnoreCase("reload"))
				{
					if(sender.hasPermission("opsyfreeze.reload"))
					{						
						Bukkit.getPluginManager().disablePlugin(main);
						Bukkit.getPluginManager().enablePlugin(main);
						for(String line : messagesFile.getStringList("plugin-reload"))
						{
							line = ChatColor.translateAlternateColorCodes('&', line);
							sender.sendMessage(prefix + line);
						}
					}
					else
					{
						for(String line : messagesFile.getStringList("errors.not-enough-permissions"))
						{
							line = ChatColor.translateAlternateColorCodes('&', line);
							sender.sendMessage(prefix + line);
						}
					}
				}
				else if(Bukkit.getOfflinePlayer(args[0]).isOnline())
				{
					if(sender.hasPermission("opsyfreeze.freeze"))
					{	
						Player player = Bukkit.getPlayer(args[0]);
						if(!player.getName().equals(sender.getName()))
						{
							if(playerFrozen.containsKey(Bukkit.getPlayer(args[0]).getUniqueId()))
							{
								playerFrozen.remove(player.getUniqueId());
								try
								{
									player.playSound(player.getLocation(), Sound.valueOf(configsFile.getString("unfreeze-sound")), 5, 5);
								}
								catch(IllegalArgumentException ex)
								{
									for(String line : messagesFile.getStringList("errors.invalid-sound"))
									{
										line = line.replace("{sound}", configsFile.getString("unfreeze-sound"));
										line = ChatColor.translateAlternateColorCodes('&', line);
										main.getLogger().warning(line);
									}
								}
								for(String line : messagesFile.getStringList("player-unfrozen"))
								{
									line = line.replace("{player}", player.getName());
									line = ChatColor.translateAlternateColorCodes('&', line);
									sender.sendMessage(prefix + line);
								}
								for(String line : messagesFile.getStringList("target-unfrozen"))
								{
									line = line.replace("{freezer}", sender.getName());
									line = ChatColor.translateAlternateColorCodes('&', line);
									player.sendMessage(prefix + line);
								}
							}
							else
							{
								String reason;
								if(args.length >= 2)
								{
									StringBuilder reasonBuilder = new StringBuilder();
									String[] argsToKeep = Arrays.copyOfRange(args, 1, args.length);
									boolean i = true;
									for(String arg : argsToKeep)
									{
										if(i)
										{
											reasonBuilder.append(arg);
											i = false;
										}
										else
										{
											reasonBuilder.append(" " + arg);
										}
									}
									reason = reasonBuilder.toString();
								}
								else
								{
									reason =  messagesFile.getString("no-reason");
								}
								playerFrozen.put(player.getUniqueId(), new Freeze(sender.getName(), reason, player.getLocation()));
								try
								{
									player.playSound(player.getLocation(), Sound.valueOf(configsFile.getString("freeze-sound")), 5, 5);
								}
								catch(IllegalArgumentException ex)
								{
									for(String line : messagesFile.getStringList("errors.invalid-sound"))
									{
										line = line.replace("{sound}", configsFile.getString("freeze-sound"));
										line = ChatColor.translateAlternateColorCodes('&', line);
										main.getLogger().warning(line);
									}
								}
								for(String line : messagesFile.getStringList("player-frozen"))
								{
									line = line.replace("{player}", player.getName());
									line = line.replace("{reason}", reason);
									line = ChatColor.translateAlternateColorCodes('&', line);
									sender.sendMessage(prefix + line);
								}
								for(String line : messagesFile.getStringList("target-frozen"))
								{
									line = line.replace("{freezer}", sender.getName());
									line = line.replace("{reason}", reason);
									line = ChatColor.translateAlternateColorCodes('&', line);
									player.sendMessage(prefix + line);
								}
							}
							main.getOFE().setPlayerFrozen(playerFrozen);
						}
						else
						{
							for(String line : messagesFile.getStringList("errors.self-freeze"))
							{
								line = ChatColor.translateAlternateColorCodes('&', line);
								sender.sendMessage(prefix + line);
							}
						}
					}
					else
					{
						for(String line : messagesFile.getStringList("errors.not-enough-permissions"))
						{
							line = ChatColor.translateAlternateColorCodes('&', line);
							sender.sendMessage(prefix + line);
						}
					}
				}
				else
				{
					for(String line : messagesFile.getStringList("errors.player-offline"))
					{
						line = ChatColor.translateAlternateColorCodes('&', line);
						sender.sendMessage(prefix + line);
					}
				}
			}
		}
		return false;
	}

	private void sendHelpPage(CommandSender sender)
	{
		for(String line : messagesFile.getStringList("help-page"))
		{
			line = ChatColor.translateAlternateColorCodes('&', line);
			sender.sendMessage(prefix + line);
		}
	}

}
package me.regadpole.plumbot.command;

import me.regadpole.plumbot.PlumBot;
import me.regadpole.plumbot.bot.KookBot;
import me.regadpole.plumbot.config.Config;
import me.regadpole.plumbot.config.DataBase;
import me.regadpole.plumbot.internal.WhitelistHelper;
import me.regadpole.plumbot.internal.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import snw.jkook.command.ConsoleCommandSender;
import snw.jkook.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Commands implements CommandExecutor {

    private PlumBot plugin;

    public Commands(PlumBot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(plugin.getEnvironment().prefix + "§c请使用/pb help查看命令使用方法");
            return true;
        }

        switch (args[0]) {
            case "info":
                StringBuilder stringbuilder = new StringBuilder();
                stringbuilder.append(plugin.getEnvironment().prefix).append("§1---PlumBot 信息---").append("\n");
                stringbuilder.append(plugin.getEnvironment().prefix).append("§a作者: ").append("§f").append(plugin.getEnvironment().author).append("\n");
                stringbuilder.append(plugin.getEnvironment().prefix).append("§a版本: ").append("§f").append(plugin.getEnvironment().version).append("\n");
                stringbuilder.append(plugin.getEnvironment().prefix).append("§a机器人平台: ").append("§f").append(Config.getBotMode()).append("\n");
                stringbuilder.append(plugin.getEnvironment().prefix).append("§a数据库模式: ").append("§f").append(DataBase.type()).append("\n");
                stringbuilder.append(plugin.getEnvironment().prefix).append("§a服务端版本: ").append("§f").append(Bukkit.getVersion()).append("\n");
                stringbuilder.append(plugin.getEnvironment().prefix).append("§a兼容插件: ").append("\n");
                stringbuilder.append(plugin.getEnvironment().prefix).append("\t").append("§bAuthMe: ").append(plugin.getEnvironment().authme).append("\n");
                stringbuilder.append(plugin.getEnvironment().prefix).append("\t").append("§bGriefDefender: ").append(plugin.getEnvironment().griefdefender).append("\n");
                stringbuilder.append(plugin.getEnvironment().prefix).append("\t").append("§bResidence: ").append(plugin.getEnvironment().residence).append("\n");
                stringbuilder.append(plugin.getEnvironment().prefix).append("\t").append("§bQuickShop: ").append(plugin.getEnvironment().quickshop).append("\n");
                stringbuilder.append(plugin.getEnvironment().prefix).append("\t").append("§bQuickShopHikari: ").append(plugin.getEnvironment().quickshophikari).append("\n");
                sender.sendMessage(stringbuilder.toString());
                break;
            case "reload":
                if (args.length != 1) return true;
                DatabaseManager.close();
                if (PlumBot.getBot() != null) {
                    PlumBot.getBot().shutdown();
                }
                Config.loadConfig();
                DatabaseManager.start();
                if (PlumBot.getBot() != null) {
                    PlumBot.getBot().start();
                }
                sender.sendMessage(plugin.getEnvironment().prefix + "§aPlumBot已重载");
                break;
            case "help":
                if (args.length != 1) return true;
                sender.sendMessage(plugin.getEnvironment().prefix + "§6PlumBot 机器人帮助菜单");
                sender.sendMessage(plugin.getEnvironment().prefix + "§6/pb reload :§f重载插件");
                sender.sendMessage(plugin.getEnvironment().prefix + "§6/pb help :§f获取插件帮助");
                sender.sendMessage(plugin.getEnvironment().prefix + "§6/pb info :§f插件基本信息");
                sender.sendMessage(plugin.getEnvironment().prefix + "§6/pb kook help :§f获取kook帮助");
                sender.sendMessage(plugin.getEnvironment().prefix + "§6/pb kook plugins :§f获取kook插件列表");
                sender.sendMessage(plugin.getEnvironment().prefix + "§6/pb queryBind <id:ID或qq:qq> :§f查询id或qq绑定数据");
                sender.sendMessage(plugin.getEnvironment().prefix + "§6/pb addBind <qq> <id> :§f为qq添加ID白名单");
                break;
            case "queryBind":
                if (args.length == 1) {
                    sender.sendMessage(plugin.getEnvironment().prefix + "§c命令错误，格式：/plumbot queryBind <id:ID或qq:QQ>");
                    return true;
                }
                if (args.length > 2) {
                    sender.sendMessage(plugin.getEnvironment().prefix + "§c命令错误，格式：/plumbot queryBind <id:ID或qq:QQ>");
                    return true;
                }
                if (args.length == 2) {
                    if (args[1].startsWith("id:")) {
                        String name = args[1].substring(3);
                        if (name.isEmpty()) {
                            sender.sendMessage(plugin.getEnvironment().prefix + "§cid不能为空");
                            return true;
                        }
                        PlumBot.getScheduler().runTaskAsynchronously(() -> {
                            long qq = DatabaseManager.getBindId(name, DataBase.type().toLowerCase(), PlumBot.getDatabase());
                            if (qq == 0L) {
                                sender.sendMessage(plugin.getEnvironment().prefix + "§cID尚未申请白名单");
                                return;
                            }
                            sender.sendMessage(plugin.getEnvironment().prefix + "§a" + name + "的申请用户为" + qq);
                        });
                        return true;
                    } else if (args[1].startsWith("qq:")) {
                        String qq = args[1].substring(3);
                        if (qq.isEmpty()) {
                            sender.sendMessage(plugin.getEnvironment().prefix + "§cQQ不能为空");
                            return true;
                        }
                        PlumBot.getScheduler().runTaskAsynchronously(() -> {
                            List<String> id = DatabaseManager.getBind(qq, DataBase.type().toLowerCase(), PlumBot.getDatabase());
                            if (id.isEmpty()) {
                                sender.sendMessage(plugin.getEnvironment().prefix + "§c" + qq + "尚未申请白名单");
                                return;
                            }
                            sender.sendMessage(plugin.getEnvironment().prefix + "§a" + qq + "拥有白名单ID：" + id);
                        });
                        return true;
                    }
                    break;
                }
            case "addBind":
                if (args.length < 3) {
                    sender.sendMessage(plugin.getEnvironment().prefix + "§c命令错误，格式：/plumbot addBind <qq> <id>");
                    return true;
                }
                if (args.length > 3) {
                    sender.sendMessage(plugin.getEnvironment().prefix + "§c命令错误，格式：/plumbot addBind <qq> <id>");
                    return true;
                }
                if (args.length == 3) {
                    if (!WhitelistHelper.checkIDNotExist(args[2])) {
                        sender.sendMessage(plugin.getEnvironment().prefix + "§c绑定失败，此ID已绑定用户" + DatabaseManager.getBindId(args[2], DataBase.type().toLowerCase(), PlumBot.getDatabase()));
                        return true;
                    }
                    List<String> id = WhitelistHelper.addAndGet(args[2], args[1], DataBase.type().toLowerCase(), PlumBot.getDatabase());
                    sender.sendMessage(plugin.getEnvironment().prefix + "§a成功申请白名单，" + args[1] + "目前的白名单为" + id);
                }
                break;
            case "kook":
                if (args.length == 1) {
                    sender.sendMessage(plugin.getEnvironment().prefix + "§c命令错误，格式：/plumbot kook <value>");
                    sender.sendMessage(plugin.getEnvironment().prefix + "§cvalue可选值：plugins，help");
                    return true;
                }
                if (args.length > 2) return true;
                if (args.length == 2) {
                    if (!KookBot.isKookEnabled()) {
                        sender.sendMessage(plugin.getEnvironment().prefix + "§ckook客户端未启动");
                        return true;
                    }
                    switch (args[1]) {
                        case "plugins":
                            Plugin[] plugins = KookBot.getKookClient().getCore().getPluginManager().getPlugins();
                            String result = String.format("%s (%d): %s", sender instanceof ConsoleCommandSender ? "Installed and running plugins" : "已安装并正在运行的插件", plugins.length, String.join(", ", (Iterable) Arrays.stream(plugins).map((plugin) -> {
                                return plugin.getDescription().getName();
                            }).collect(Collectors.toSet())));
                            sender.sendMessage(result);
                            return true;
                    }
                }
                break;
            default:
                if (args.length != 1) return true;
                sender.sendMessage(plugin.getEnvironment().prefix + "§c错误的指令用法，请使用/pb help查看命令使用方法");
                break;
        }
        return true;
    }
}

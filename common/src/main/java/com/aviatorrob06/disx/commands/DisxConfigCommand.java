package com.aviatorrob06.disx.commands;

import com.aviatorrob06.disx.commands.suggestionProviders.DisxBlacklistSuggestionProvider;
import com.aviatorrob06.disx.commands.suggestionProviders.DisxWhitelistSuggestionProvider;
import com.aviatorrob06.disx.utils.DisxUUIDUtil;
import com.aviatorrob06.disx.commands.suggestionProviders.DisxPropertyTypeSuggestionProvider;
import com.aviatorrob06.disx.config.DisxConfigHandler;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DisxConfigCommand {
    public static void registerCommand(){
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            dispatcher.register(Commands.literal("disxconfig")
                            .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                    .then(Commands.literal("setProperty")
                            .then(Commands.argument("property", StringArgumentType.string()).suggests(DisxPropertyTypeSuggestionProvider::getSuggestions)
                                    .then(Commands.argument("propertyValue", StringArgumentType.string())
                                            .executes(DisxConfigCommand::runSetProperty))))
                    .then(Commands.literal("getProperty")
                            .then(Commands.argument("property", StringArgumentType.string()).suggests(DisxPropertyTypeSuggestionProvider::getSuggestions)
                                    .executes(DisxConfigCommand::runGetProperty)))
                    .then(Commands.literal("getUseWhitelist")
                            .executes(DisxConfigCommand::runGetUseWhitelist))
                    .then(Commands.literal("modifyUseWhitelist")
                            .then(Commands.literal("add")
                                    .then(Commands.argument("username", StringArgumentType.string())
                                            .suggests(DisxWhitelistSuggestionProvider::getAdditionSuggestions)
                                            .executes(DisxConfigCommand::runAddUseWhitelist)))
                            .then(Commands.literal("remove")
                                    .then(Commands.argument("username", StringArgumentType.string())
                                            .suggests(DisxWhitelistSuggestionProvider::getRemovalSuggestions)
                                            .executes(DisxConfigCommand::runRemoveUseWhitelist))))
                    .then(Commands.literal("getUseBlacklist")
                            .executes(DisxConfigCommand::runGetUseBlacklist))
                    .then(Commands.literal("modifyUseBlacklist")
                            .then(Commands.literal("add")
                                    .then(Commands.argument("username", StringArgumentType.string())
                                            .suggests(DisxBlacklistSuggestionProvider::getAdditionSuggestions)
                                            .executes(DisxConfigCommand::runAddUseBlacklist)))
                            .then(Commands.literal("remove")
                                    .then(Commands.argument("username", StringArgumentType.string())
                                            .suggests(DisxBlacklistSuggestionProvider::getRemovalSuggestions)
                                            .executes(DisxConfigCommand::runRemoveUseBlacklist))))
                    .then(Commands.literal("getDimensionBlacklist")
                            .executes(DisxConfigCommand::runGetDimensionBlacklist))
                    .then(Commands.literal("modifyDimensionBlacklist")
                            .then(Commands.literal("add")
                                    .then(Commands.argument("dimension", DimensionArgument.dimension())
                                            .executes(DisxConfigCommand::runAddDimensionBlacklist)))
                            .then(Commands.literal("remove")
                                    .then(Commands.argument("dimension", DimensionArgument.dimension())
                                            .executes(DisxConfigCommand::runRemoveDimensionBlacklist))))
            );
        });
    }

    private static int runSetProperty(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(2)){
            context.getSource().sendFailure(Component.literal("You don't have permission to do that!"));
        } else {
            String property = context.getArgument("property", String.class);
            String value = context.getArgument("propertyValue", String.class);
            if (DisxConfigHandler.SERVER.getProperty(property) == null){
                context.getSource().sendFailure(Component.literal("Invalid property provided!"));
            } else {
                DisxConfigHandler.SERVER.updateProperty(property, value);
                context.getSource().sendSystemMessage(Component.literal("Property '" + property + "' successfully set to '" + value + "'!"));
            }
        }
        return 1;
    }

    private static int runGetProperty(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(2)){
            context.getSource().sendFailure(Component.literal("You don't have permission to do that!"));
        } else {
            String property = context.getArgument("property", String.class);
            String value = DisxConfigHandler.SERVER.getProperty(property);
            if (value == null){
                context.getSource().sendFailure(Component.literal("Invalid property provided!"));
            } else {
                context.getSource().sendSystemMessage(Component.literal("Current value of the property '" + property + "' is '" + value + "'"));
            }
        }
        return 1;
    }

    private static int runGetUseWhitelist(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(2)){
            context.getSource().sendFailure(Component.literal("You don't have permission to do that!"));
        } else {
            List<Object> list = DisxConfigHandler.SERVER.getUseWhitelist();
            ArrayList<Component> toSend = new ArrayList<>();
            toSend.add(Component.literal("Disx Whitelisted Players:").withStyle(ChatFormatting.BOLD));
            toSend.add(Component.literal("Whitelist Status: " + DisxConfigHandler.SERVER.getProperty("player_use_whitelist_enabled")).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            for (Object o : list){
                String name = "";
                try{
                    System.out.println("TRYING TO CONVERT STRING TO UUID: " + o.toString());
                    name = DisxUUIDUtil.getUsernameFromUuid(UUID.fromString(o.toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                    context.getSource().sendFailure(Component.literal("Error in trying to load whitelist. Is it corrupted?"));
                    return 1;
                }
                toSend.add(Component.literal("- " + name));
            }
            if (list.isEmpty()){
                toSend.add(Component.literal("NONE").withStyle(ChatFormatting.ITALIC));
            }
            for (Component c : toSend){
                context.getSource().sendSystemMessage(c);
            }
        }
        return 1;
    }

    private static int runAddUseWhitelist(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(2)){
            context.getSource().sendFailure(Component.literal("You don't have permission to do that!"));
        } else {
            String username = context.getArgument("username", String.class);
            String result = DisxConfigHandler.SERVER.addToUseWhitelist(username);
            if (result.equals("success")){
                context.getSource().sendSystemMessage(Component.literal("Added '" + username + "' to the Player-Use Whitelist!"));
            } else if (result.equals("failure")){
                context.getSource().sendFailure(Component.literal("Player not found. Is that username a registered Java Edition account?"));
            } else if (result.equals("duplicate")){
                context.getSource().sendFailure(Component.literal("This player is already on the whitelist!"));
            }
        }
        return 1;
    }

    private static int runRemoveUseWhitelist(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(2)){
            context.getSource().sendFailure(Component.literal("You don't have permission to do that!"));
        } else {
            String username = context.getArgument("username", String.class);
            String result = DisxConfigHandler.SERVER.removeFromUseWhitelist(username);
            if (result.equals("success")){
                context.getSource().sendSystemMessage(Component.literal("Removed '" + username + "' from the Player-Use Whitelist!"));
            } else if (result.equals("failure")){
                context.getSource().sendFailure(Component.literal("Player not found. Is that username a registered Java Edition account?"));
            } else if (result.equals("notfoundonit")){
                context.getSource().sendFailure(Component.literal("This player is not on the whitelist!"));
            }
        }
        return 1;
    }

    private static int runGetUseBlacklist(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(2)){
            context.getSource().sendFailure(Component.literal("You don't have permission to do that!"));
        } else {
            List<Object> list = DisxConfigHandler.SERVER.getUseBlacklist();
            ArrayList<Component> toSend = new ArrayList<>();
            toSend.add(Component.literal("Disx Blacklisted Players:").withStyle(ChatFormatting.BOLD));
            for (Object o : list){
                String name = "";
                try{
                    name = DisxUUIDUtil.getUsernameFromUuid(UUID.fromString(o.toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                    context.getSource().sendFailure(Component.literal("Error in trying to load blacklist. Is it corrupted?"));
                    return 1;
                }
                toSend.add(Component.literal("- " + name));
            }
            if (list.isEmpty()){
                toSend.add(Component.literal("NONE").withStyle(ChatFormatting.ITALIC));
            }
            for (Component c : toSend){
                context.getSource().sendSystemMessage(c);
            }
        }
        return 1;
    }

    private static int runAddUseBlacklist(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(2)){
            context.getSource().sendFailure(Component.literal("You don't have permission to do that!"));
        } else {
            String username = context.getArgument("username", String.class);
            String result = DisxConfigHandler.SERVER.addToUseBlacklist(username);
            if (result.equals("success")){
                context.getSource().sendSystemMessage(Component.literal("Added '" + username + "' to the Player-Use Blacklist >:)"));
            } else if (result.equals("failure")){
                context.getSource().sendFailure(Component.literal("Player not found. Is that username a registered Java Edition account?"));
            } else if (result.equals("duplicate")){
                context.getSource().sendFailure(Component.literal("This player is already on the blacklist!"));
            }
        }
        return 1;
    }

    private static int runRemoveUseBlacklist(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(2)){
            context.getSource().sendFailure(Component.literal("You don't have permission to do that!"));
        } else {
            String username = context.getArgument("username", String.class);
            String result = DisxConfigHandler.SERVER.removeFromUseBlacklist(username);
            if (result.equals("success")){
                context.getSource().sendSystemMessage(Component.literal("Removed '" + username + "' from the Player-Use Blacklist!"));
            } else if (result.equals("failure")){
                context.getSource().sendFailure(Component.literal("Player not found. Is that username a registered Java Edition account?"));
            } else if (result.equals("notfoundonit")){
                context.getSource().sendFailure(Component.literal("This player is not on the blacklist!"));
            }
        }
        return 1;
    }

    private static int runGetDimensionBlacklist(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(2)){
            context.getSource().sendFailure(Component.literal("You don't have permission to do that!"));
        } else {
            List<Object> list = DisxConfigHandler.SERVER.getDimensionBlacklist();
            ArrayList<Component> toSend = new ArrayList<>();
            toSend.add(Component.literal("Disx Blacklisted Dimensions:").withStyle(ChatFormatting.BOLD));
            for (Object o : list){
                toSend.add(Component.literal("- " + o.toString()));
            }
            if (list.isEmpty()){
                toSend.add(Component.literal("NONE").withStyle(ChatFormatting.ITALIC));
            }
            for (Component c : toSend){
                context.getSource().sendSystemMessage(c);
            }
        }
        return 1;
    }

    private static int runAddDimensionBlacklist(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(2)){
            context.getSource().sendFailure(Component.literal("You don't have permission to do that!"));
        } else {
            ResourceLocation dimensionLocation = context.getArgument("dimension", ResourceLocation.class);
            String result = DisxConfigHandler.SERVER.addToDimensionBlacklist(dimensionLocation);
            if (result.equals("success")){
                context.getSource().sendSystemMessage(Component.literal("Added '" + dimensionLocation + "' to the Dimension Blacklist!"));
            } else if (result.equals("failure")){
                context.getSource().sendFailure(Component.literal("Dimension not found. Is that a valid dimension?"));
            } else if (result.equals("duplicate")){
                context.getSource().sendFailure(Component.literal("This dimension is already on the blacklist!"));
            }
        }
        return 1;
    }

    private static int runRemoveDimensionBlacklist(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(2)){
            context.getSource().sendFailure(Component.literal("You don't have permission to do that!"));
        } else {
            ResourceLocation dimensionLocation = context.getArgument("dimension", ResourceLocation.class);
            String result = DisxConfigHandler.SERVER.removeFromDimensionBlacklist(dimensionLocation);
            if (result.equals("success")){
                context.getSource().sendSystemMessage(Component.literal("Removed '" + dimensionLocation + "' from the Dimension Blacklist!"));
            } else if (result.equals("failure")){
                context.getSource().sendFailure(Component.literal("Dimension not found. Is that a valid dimension?"));
            } else if (result.equals("notfoundonit")){
                context.getSource().sendFailure(Component.literal("This dimension is not on the blacklist!"));
            }
        }
        return 1;
    }
}

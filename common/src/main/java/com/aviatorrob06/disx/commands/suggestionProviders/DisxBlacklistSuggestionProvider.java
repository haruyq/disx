package com.aviatorrob06.disx.commands.suggestionProviders;

import com.aviatorrob06.disx.DisxServerAudioPlayerRegistry;
import com.aviatorrob06.disx.config.DisxConfigHandler;
import com.aviatorrob06.disx.utils.DisxUUIDUtil;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DisxBlacklistSuggestionProvider {
    public static CompletableFuture<Suggestions> getRemovalSuggestions(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder) {
        List<Object> list = DisxConfigHandler.SERVER.getUseWhitelist();
        for (Object o : list){
            try {
                String username = DisxUUIDUtil.getUsernameFromUuid(UUID.fromString(o.toString()));
                suggestionsBuilder.suggest(username);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return suggestionsBuilder.buildFuture();
    }

    public static CompletableFuture<Suggestions> getAdditionSuggestions(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder) {
        for (Player player : DisxServerAudioPlayerRegistry.getMcPlayers()){
            suggestionsBuilder.suggest(player.getName().getString());
        }
        return suggestionsBuilder.buildFuture();
    }
}

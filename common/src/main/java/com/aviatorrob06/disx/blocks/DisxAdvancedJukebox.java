package com.aviatorrob06.disx.blocks;


import com.aviatorrob06.disx.*;
import com.aviatorrob06.disx.entities.DisxAdvancedJukeboxEntity;
import com.aviatorrob06.disx.items.DisxBlankDisc;
import com.aviatorrob06.disx.items.DisxCustomDisc;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.aviatorrob06.disx.DisxMain.debug;

public class DisxAdvancedJukebox extends BaseEntityBlock {

    public static RegistrySupplier<Block> blockRegistration;
    public static RegistrySupplier<Item> blockItemRegistration;

    Logger logger = LoggerFactory.getLogger("disx");
    protected DisxAdvancedJukebox(Properties properties) {
        super(properties);
    }

    BlockEntity blockEntity;

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        this.blockEntity = new DisxAdvancedJukeboxEntity(blockPos, blockState);
        return this.blockEntity;
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {

        if (!level.isClientSide() && interactionHand == InteractionHand.MAIN_HAND){
            ItemStack stack = player.getMainHandItem();
            Item item = null;
            if (stack != null){
                item = stack.getItem();
            }
            DisxAdvancedJukeboxEntity entity = (DisxAdvancedJukeboxEntity) this.blockEntity;
            // check Internet
            try {
                HttpRequest testRequest = HttpRequest.newBuilder().uri(new URI("http://www.google.com")).build();
                HttpResponse testResponse = null;
                testResponse = HttpClient.newHttpClient().send(testRequest, HttpResponse.BodyHandlers.ofString());
                if (testResponse == null){
                    DisxSystemMessages.noInternetErrorMessage(player);
                    return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
            if (entity.isHas_record() && !DisxJukeboxUsageCooldownManager.isOnCooldown(blockPos)){
                if (debug) logger.info("does have record, taking it out");
                DisxJukeboxUsageCooldownManager.updateCooldown(blockPos);
                String discType = entity.getDiscType();
                Item newItem = DisxMain.REGISTRAR_MANAGER.get().get(Registries.ITEM).get(new ResourceLocation("disx","custom_disc_" + discType));
                ItemStack newStack = new ItemStack(newItem, 1);
                CompoundTag compoundTag = newStack.getOrCreateTag();
                String videoId = entity.getVideoId();
                compoundTag.putString("videoId",videoId);
                newStack.setTag(compoundTag);
                newStack.setHoverName(Component.literal(entity.getDiscName()));
                entity.setHas_record(false);
                player.getInventory().add(newStack);
                entity.setChanged();
                DisxServerAudioPlayerRegistry.removeFromRegistry(blockPos, videoId);
                if (debug) logger.info("[advanced jukebox] current has record value: " + entity.isHas_record());
            } else if (!entity.isHas_record() && !DisxJukeboxUsageCooldownManager.isOnCooldown(blockPos)){
                DisxJukeboxUsageCooldownManager.updateCooldown(blockPos);
                if (item instanceof DisxCustomDisc){
                    if (stack.getTag() != null && stack.getTag().get("videoId") != null && stack.getTag().getString("videoId") != ""){
                        if (debug) logger.info("[advanced jukebox] doesn't have record, putting it in");
                        stack.setCount(stack.getCount() - 1);
                        String videoId = stack.getTag().getString("videoId");
                        String discType = ((DisxCustomDisc) item).getDiscType();
                        String discName = stack.getHoverName().getString();
                        entity.setVideoId(videoId);
                        entity.setDiscType(discType);
                        entity.setHas_record(true);
                        entity.setDiscName(discName);
                        entity.setChanged();
                        entity.saveWithFullMetadata();
                        DisxServerAudioPlayerRegistry.addToRegistry(blockPos, videoId, false, player);
                        DisxServerPacketIndex.ServerPackets.nowPlayingMessage(videoId, player);
                        if (debug) logger.info("[advanced jukebox] current has record value: " + entity.isHas_record());
                    }
                }
            }
        }
        return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    public void onBlockDestroy(LevelAccessor levelAccessor, BlockPos blockPos){
        DisxAdvancedJukeboxEntity entity = (DisxAdvancedJukeboxEntity) this.blockEntity;
        if (entity == null){
            if (debug) logger.info("[advanced jukebox] block destroy initialized, no block entity found");
        } else
        if (!levelAccessor.isClientSide() && entity.isHas_record()) {
            if (debug) logger.info("[advanced jukebox] block destroy initialized, has record; removing record");
            String discType = entity.getDiscType();
            String videoId = entity.getVideoId();
            String discName = entity.getDiscName();
            Item newItem = DisxMain.REGISTRAR_MANAGER.get().get(Registries.ITEM).get(new ResourceLocation("disx","custom_disc_" + discType));
            ItemStack newItemStack = new ItemStack(newItem, 1);
            CompoundTag compoundTag = newItemStack.getOrCreateTag();
            compoundTag.putString("videoId", videoId);
            newItemStack.setTag(compoundTag);
            newItemStack.setHoverName(Component.literal(discName));
            entity.setHas_record(false);
            ItemEntity itemEntity = new ItemEntity(entity.getLevel(), blockPos.getX(), blockPos.getY(), blockPos.getZ(), newItemStack);
            levelAccessor.addFreshEntity(itemEntity);

            DisxServerAudioPlayerRegistry.removeFromRegistry(blockPos, videoId);
        }
    }

    @Override
    public void wasExploded(Level level, BlockPos blockPos, Explosion explosion) {
        onBlockDestroy(level, blockPos);
        super.wasExploded(level, blockPos, explosion);
    }

    @Override
    public void destroy(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        onBlockDestroy(levelAccessor, blockPos);
        super.destroy(levelAccessor, blockPos, blockState);
    }

    public static void registerBlock(Registrar<Block> registry){
        blockRegistration = registry.register(new ResourceLocation("disx", "advanced_jukebox"), () -> new DisxAdvancedJukebox(BlockBehaviour.Properties.of()));
    }

    public static void registerBlockItem(Registrar<Item> registry){
        blockItemRegistration = registry.register(new ResourceLocation("disx","advanced_jukebox"), () -> new BlockItem(blockRegistration.get(), new Item.Properties()));
    }
}

package xyz.ar06.disx.blocks;

import dev.architectury.event.EventResult;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.ar06.disx.*;
import xyz.ar06.disx.config.DisxConfigHandler;
import xyz.ar06.disx.entities.DisxAdvancedJukeboxEntity;
import xyz.ar06.disx.entities.DisxEnderAdvancedJukeboxEntity;
import xyz.ar06.disx.items.DisxCustomDisc;
import xyz.ar06.disx.utils.DisxInternetCheck;
import xyz.ar06.disx.utils.DisxJukeboxUsageCooldownManager;

import java.util.concurrent.CompletableFuture;

public class DisxEnderAdvancedJukebox extends BaseEntityBlock {
    protected DisxEnderAdvancedJukebox(Properties properties) {
        super(properties);
    }

    public static RegistrySupplier<Block> blockRegistration;
    public static RegistrySupplier<Item> blockItemRegistration;

    private boolean debounce = false;
    private int power;

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DisxEnderAdvancedJukeboxEntity(blockPos, blockState);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        /*
        if (!level.isClientSide() && interactionHand == InteractionHand.MAIN_HAND && !debounce && !DisxJukeboxUsageCooldownManager.isOnCooldown(blockPos, level.dimension())){
            ItemStack stack = player.getMainHandItem();
            Item item = null;
            if (stack != null){
                item = stack.getItem();
            }
            DisxEnderAdvancedJukeboxEntity entity = (DisxEnderAdvancedJukeboxEntity) level.getBlockEntity(blockPos);
            if (entity.isHas_record()){
                debounce = true;
                DisxLogger.debug("does have record, taking it out");
                DisxJukeboxUsageCooldownManager.updateCooldown(blockPos, level.dimension());
                ItemStack newItemStack = entity.removeItem(0, 1);
                player.getInventory().add(newItemStack);
                entity.setChanged();
                DisxLogger.debug("current has record value: " + entity.isHas_record());
                debounce = false;
                return InteractionResult.SUCCESS;
            } else if (!entity.isHas_record()){
                if (item instanceof DisxCustomDisc){
                    if (stack.getTag() != null && stack.getTag().get("videoId") != null && !stack.getTag().getString("videoId").isEmpty()){
                        if (!isPassingAudioPrerequisiteChecks(player, level)){
                            return InteractionResult.FAIL;
                        }
                        DisxLogger.debug("[advanced jukebox] doesn't have record, putting it in");
                        debounce = true;
                        String discName = stack.getTag().getString("discName");
                        String videoId = stack.getTag().getString("videoId");
                        //alpha discs compatibility
                        if (discName.isBlank() || discName.isEmpty()){
                            CompoundTag updatedTag = stack.getTag();
                            updatedTag.putString("discName", stack.getHoverName().getString());
                            stack.setTag(updatedTag);
                        }
                        //player.getCooldowns().addCooldown(item, 999999999);
                        entity.setItem(0, stack.copy(), player);
                        stack.setCount(stack.getCount() - 1);
                        DisxJukeboxUsageCooldownManager.updateCooldown(blockPos, level.dimension());
                        DisxServerPacketIndex.ServerPackets.loadingVideoIdMessage(videoId, player);
                        DisxLogger.debug("current has record value: " + entity.isHas_record());
                        debounce = false;
                        //player.getCooldowns().removeCooldown(item);
                        entity.setChanged();
                        //attempt to get the latest video title if disc was made with no proper title
                        CompletableFuture.runAsync(() -> entity.tryGetUpdatedDiscName(player));
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        } */
        return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
        if (blockGetter.getBlockEntity(blockPos) instanceof DisxEnderAdvancedJukeboxEntity){
            DisxEnderAdvancedJukeboxEntity entity = (DisxEnderAdvancedJukeboxEntity) blockEntity;
            ItemStack stack = new ItemStack(blockItemRegistration.get());
            stack.setCount(1);
            CompoundTag compoundTag = entity.saveWithoutMetadata();
            if (!compoundTag.isEmpty()){
                stack.getOrCreateTag().put("BlockEntityTag", compoundTag);
            }
            return stack;
        }

        return super.getCloneItemStack(blockGetter, blockPos, blockState);
    }

    private static boolean isPassingAudioPrerequisiteChecks(Player player, Level level){
        // check Internet
        if (!DisxInternetCheck.checkInternet()){
            DisxSystemMessages.noInternetErrorMessage(player);
            return false;
        }
        //Check Use-Blacklist, Use-Whitelist, and Dimension-Blacklist
        if (DisxConfigHandler.SERVER.isOnUseBlacklist(player.getUUID())){
            DisxSystemMessages.blacklistedByServer(player);
            return false;
        }
        if (!DisxConfigHandler.SERVER.isOnUseWhitelist(player.getUUID())){
            DisxSystemMessages.notWhitelistedByServer(player);
            return false;
        }
        if (DisxConfigHandler.SERVER.isOnDimensionBlacklist(level.dimension())){
            DisxSystemMessages.dimensionBlacklisted(player);
            return false;
        }
        //Check Audio Player Count
        int maxAudioPlayerCt = Integer.valueOf(DisxConfigHandler.SERVER.getProperty("max_audio_players"));
        int currentAudioPlayerCt = DisxServerAudioRegistry.getRegistryCount();
        if (currentAudioPlayerCt >= maxAudioPlayerCt){
            DisxSystemMessages.maxAudioPlayerCtReached(player);
            return false;
        }
        return true;
    }

    public static void registerBlock(Registrar<Block> registry){
        blockRegistration = registry.register(new ResourceLocation("disx", "ender_advanced_jukebox"), () -> new DisxEnderAdvancedJukebox(BlockBehaviour.Properties.copy(Blocks.JUKEBOX)));
    }

    public static void registerBlockItem(Registrar<Item> registry, RegistrySupplier<CreativeModeTab> tab){
        blockItemRegistration = registry.register(new ResourceLocation("disx","ender_advanced_jukebox"), () -> new BlockItem(blockRegistration.get(), new Item.Properties().arch$tab(tab)));
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        if (((DisxEnderAdvancedJukeboxEntity) level.getBlockEntity(blockPos)).isRecordPlaying()){
            DisxLogger.debug("Comparator Reading: yes there is a record playing");
            return 16;
        }
        DisxLogger.debug("Comparator Reading: no there is not a record playing");
        return 0;
    }

    public static EventResult leverListener(Player player, InteractionHand interactionHand, BlockPos blockPos, Direction direction) {
        Level level = player.level();
        if (!player.level().isClientSide() && interactionHand.equals(InteractionHand.MAIN_HAND)){
            BlockState blockState = level.getBlockState(blockPos);
            Block block = blockState.getBlock();
            if (block instanceof LeverBlock){
                Direction facingDirection = blockState.getValue(LeverBlock.FACING).getOpposite();
                BlockState adjacentBlockState = level.getBlockState(blockPos.relative(facingDirection));
                if (adjacentBlockState.is(DisxAdvancedJukebox.blockRegistration.get())){
                    DisxServerPacketIndex.ServerPackets.loopMsg(player, !blockState.getValue(LeverBlock.POWERED));
                }
            }
        }

        return EventResult.pass();
    }

    private boolean pauseResumeDebounce = false;
    @Override
    public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {

        if (!level.isClientSide() && !pauseResumeDebounce) {
            boolean audioExists = DisxServerAudioRegistry.isNodeAtLocation(blockHitResult.getBlockPos(), level.dimension());
            if (!audioExists){
                return;
            }
            pauseResumeDebounce = true;
            boolean paused = DisxServerAudioRegistry.pauseOrPlayNode(blockHitResult.getBlockPos(), level.dimension());
            if (projectile.getOwner() != null) {
                if (projectile.getOwner().getType().equals(EntityType.PLAYER) && audioExists){
                    ServerPlayer player = level.getServer().getPlayerList().getPlayer(projectile.getOwner().getUUID());
                    DisxServerPacketIndex.ServerPackets.pauseMsg(player, paused);
                }
            }
            if (paused){
                DisxLogger.debug("Playing static sound?");
                level.playSound(null, blockHitResult.getBlockPos(), DisxSoundEvents.SoundInstances.ADVANCED_JUKEBOX_STATIC.get(), SoundSource.RECORDS, 1.0f, 1.0f);
            }
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(6500);
                    pauseResumeDebounce = false;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        super.onProjectileHit(level, blockState, blockHitResult, projectile);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        super.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);
        if (!level.isClientSide()){
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof DisxEnderAdvancedJukeboxEntity jukeboxEntity){
                CompoundTag compoundTag = itemStack.getTag();
                if (compoundTag != null && !compoundTag.isEmpty() && compoundTag.contains("BlockEntityTag")){
                    jukeboxEntity.load(compoundTag.getCompound("BlockEntityTag"));
                }
            }
        }
    }

    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        for(int i = 0; i < 3; ++i) {
            int j = randomSource.nextInt(2) * 2 - 1;
            int k = randomSource.nextInt(2) * 2 - 1;
            double d = (double)blockPos.getX() + (double)0.5F + (double)0.25F * (double)j;
            double e = (double)((float)blockPos.getY() + randomSource.nextFloat());
            double f = (double)blockPos.getZ() + (double)0.5F + (double)0.25F * (double)k;
            double g = (double)(randomSource.nextFloat() * (float)j);
            double h = ((double)randomSource.nextFloat() - (double)0.5F) * (double)0.125F;
            double l = (double)(randomSource.nextFloat() * (float)k);
            level.addParticle(ParticleTypes.PORTAL, d, e, f, g, h, l);
        }

    }
}

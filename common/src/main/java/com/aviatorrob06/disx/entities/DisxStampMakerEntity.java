package com.aviatorrob06.disx.entities;

import com.aviatorrob06.disx.DisxInternetCheck;
import com.aviatorrob06.disx.DisxMain;
import com.aviatorrob06.disx.DisxSystemMessages;
import com.aviatorrob06.disx.DisxYoutubeTitleScraper;
import com.aviatorrob06.disx.blocks.DisxLacquerBlock;
import com.aviatorrob06.disx.blocks.DisxStampMaker;
import com.aviatorrob06.disx.items.DisxRecordStamp;
import com.mojang.authlib.minecraft.TelemetrySession;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.utils.Env;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.ContainerSingleItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class DisxStampMakerEntity extends BlockEntity implements ContainerSingleItem {

    private NonNullList<ItemStack> items;

    private String videoId;


    public DisxStampMakerEntity(BlockPos blockPos, BlockState blockState) {
        super(
                DisxMain.REGISTRAR_MANAGER.get().get(Registries.BLOCK_ENTITY_TYPE).get(new ResourceLocation("disx","stamp_maker_entity")),
                blockPos,
                blockState);
        items = NonNullList.withSize(1, ItemStack.EMPTY);
        DisxMain.LOGGER.info("MAKING NEW ENTITY AT " + blockPos);
        System.out.println(this.items);
    }

    @Override
    public ItemStack getItem(int i) {
        return this.items.get(i);
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        ItemStack stack = this.items.get(i).copy();
        this.items.set(0, ItemStack.EMPTY);
        this.setChanged();
        return stack;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        this.setChanged();
    }

    @Override
    public void load(CompoundTag compoundTag) {
        this.items = NonNullList.withSize(1, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(compoundTag, this.items);
        super.load(compoundTag);
    }

    public void setItem(int i, ItemStack itemStack, Player player) {
        this.items.set(i, itemStack);
        if (itemStack.getItem().equals(DisxLacquerBlock.itemRegistration.get())){
            if (!this.isVideoIdNull()){
                produceStamp(player);
            }
        }
    }

    @Override
    public void setChanged() {
        this.getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 1);
        super.setChanged();
    }



    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        ContainerHelper.saveAllItems(compoundTag, this.items);
        super.saveAdditional(compoundTag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithFullMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void setVideoId(String videoId, Player player) {
        if (videoId.equals("") || videoId.equals(" ")){
            this.videoId = null;
        } else {
            this.videoId = videoId;
            if (this.getItem(0).is(DisxLacquerBlock.itemRegistration.get())){
                produceStamp(player);
            }
        }
    }

    public String getVideoId() {
        return videoId;
    }

    public Boolean isVideoIdNull(){
        if (videoId == null){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    public void produceStamp(Player player){
        if (!DisxInternetCheck.checkInternet()){
            DisxSystemMessages.noInternetErrorMessage(player);
        } else {
            String videoName = DisxYoutubeTitleScraper.getYouTubeVideoTitle(this.videoId);
            if (videoName.equals("Video Not Found")){
                DisxSystemMessages.noVideoFound(player);
                return;
            }
            this.removeItem(0, 1);
            BlockPos blockPos = this.getBlockPos();
            Level lvl = getLevel();
            lvl.playSound(null, blockPos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.BLOCKS);
            DisxRecordStamp stamp = (DisxRecordStamp) DisxMain.REGISTRAR_MANAGER.get().get(Registries.ITEM).get(new ResourceLocation("disx", "record_stamp"));
            CompoundTag tag = new CompoundTag();
            tag.putString("videoId", this.videoId);
            tag.putString("videoName", videoName);
            ItemStack stampStack = new ItemStack(stamp);
            stampStack.setTag(tag);
            stampStack.setCount(1);
            ItemEntity itemEntity = new ItemEntity(getLevel(), ((double) blockPos.getX()) + 0.5, ((double) blockPos.getY()) + 0.2, ((double) blockPos.getZ()) + 0.5, stampStack);
            itemEntity.setDefaultPickUpDelay();
            itemEntity.setTarget(player.getUUID());
            lvl.playSound(null, blockPos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS);
            level.addFreshEntity(itemEntity);
            this.videoId = null;
        }
    }

}

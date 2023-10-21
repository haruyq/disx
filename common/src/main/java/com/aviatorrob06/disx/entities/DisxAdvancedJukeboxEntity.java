package com.aviatorrob06.disx.entities;

import com.aviatorrob06.disx.DisxMain;
import com.aviatorrob06.disx.blocks.DisxAdvancedJukebox;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class DisxAdvancedJukeboxEntity extends BlockEntity {

    private boolean has_record = false;
    private String discType;
    private String discName;

    private String videoId;

    public DisxAdvancedJukeboxEntity(BlockPos blockPos, BlockState blockState) {
        super(
                DisxMain.REGISTRAR_MANAGER.get().get(Registries.BLOCK_ENTITY_TYPE).get(new ResourceLocation("disx", "advanced_jukebox_entity")),
                blockPos,
                blockState
        );
    }

    public boolean isHas_record() {
        return has_record;
    }

    public void setHas_record(boolean has_record) {
        this.has_record = has_record;
    }

    public String getDiscName() {
        return discName;
    }

    public void setDiscName(String discName) {
        this.discName = discName;
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        compoundTag.putBoolean("has_record", has_record);
        if (videoId != null){
            compoundTag.putString("videoId", videoId);
            compoundTag.putString("discType",discType);
            compoundTag.putString("discName", discName);
        }
        super.saveAdditional(compoundTag);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        has_record = compoundTag.getBoolean("has_record");
        videoId = compoundTag.getString("videoId");
        discType = compoundTag.getString("discType");
        discName = compoundTag.getString("discName");
        super.load(compoundTag);
    }

    public void setDiscType(String discType) {
        this.discType = discType;
    }

    public String getDiscType() {
        return discType;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getVideoId() {
        return videoId;
    }

    public static void registerEntity(Registrar<BlockEntityType<?>> registry){
        RegistrySupplier<BlockEntityType<?>> registration = registry.register(new ResourceLocation("disx","advanced_jukebox_entity"), () -> BlockEntityType.Builder.of(DisxAdvancedJukeboxEntity::new, DisxAdvancedJukebox.blockRegistration.get()).build(null));
    }
}

package xyz.ar06.disx.entities;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import xyz.ar06.disx.DisxAudioMotionType;
import xyz.ar06.disx.DisxMain;
import xyz.ar06.disx.DisxServerAudioRegistry;
import xyz.ar06.disx.blocks.DisxAdvancedJukebox;
import xyz.ar06.disx.blocks.DisxEnderAdvancedJukebox;
import xyz.ar06.disx.items.DisxCustomDisc;

import java.util.UUID;

public class DisxEnderAdvancedJukeboxEntity extends BlockEntity {
    public DisxEnderAdvancedJukeboxEntity(BlockPos blockPos, BlockState blockState) {
        super(
                DisxMain.REGISTRAR_MANAGER.get().get(Registries.BLOCK_ENTITY_TYPE).get(new ResourceLocation("disx", "ender_advanced_jukebox_entity")),
                blockPos,
                blockState
        );
    }

    public static void registerEntity(Registrar<BlockEntityType<?>> registry){
        RegistrySupplier<BlockEntityType<?>> registration = registry.register(new ResourceLocation("disx","ender_advanced_jukebox_entity"), () -> BlockEntityType.Builder.of(DisxEnderAdvancedJukeboxEntity::new, DisxEnderAdvancedJukebox.blockRegistration.get()).build(null));
    }


    public boolean isRecordPlaying() {
        return false;
    }
}

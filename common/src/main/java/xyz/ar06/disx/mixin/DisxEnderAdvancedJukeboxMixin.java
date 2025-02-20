package xyz.ar06.disx.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.ar06.disx.utils.DisxEnderAdvancedJukeboxInventoryHelper;

@Mixin(Player.class)
public abstract class DisxEnderAdvancedJukeboxMixin implements DisxEnderAdvancedJukeboxInventoryHelper {
    @Unique
    CompoundTag disx$enderAdvancedJukeboxInventoryTag = new CompoundTag();

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    public void addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci){
        compoundTag.put("EnderAdvancedJukeboxInventory.disx", this.disx$enderAdvancedJukeboxInventoryTag);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    public void readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci){
        if (compoundTag.contains("EnderAdvancedJukeboxInventory.disx")){
            this.disx$enderAdvancedJukeboxInventoryTag = compoundTag.getCompound("EnderAdvancedJukeboxInventory.disx");
        }
    }

    @Unique
    public CompoundTag disx$getEnderAdvancedJukeboxInventory(){
        return this.disx$enderAdvancedJukeboxInventoryTag;
    }

    @Unique
    public void disx$setEnderAdvancedJukeboxInventory(CompoundTag enderAdvancedJukeboxInventoryTag) {
        this.disx$enderAdvancedJukeboxInventoryTag = enderAdvancedJukeboxInventoryTag;
    }
}

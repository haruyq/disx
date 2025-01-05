package xyz.ar06.disx.entities.vehicle;


import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import xyz.ar06.disx.blocks.DisxAdvancedJukebox;

import java.util.Properties;

public class DisxAdvancedJukeboxMinecart extends Minecart {

    public static RegistrySupplier<EntityType<?>> entityTypeRegistration;
    public DisxAdvancedJukeboxMinecart(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        if (interactionHand.equals(InteractionHand.MAIN_HAND) && player.level().isClientSide()){

        }
        return super.interact(player, interactionHand);
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return DisxAdvancedJukebox.blockRegistration.get().defaultBlockState();
    }

    @Override
    protected boolean canRide(Entity entity) {
        return false;
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return false;
    }

    @Override
    public void setDisplayBlockState(BlockState blockState) {
        super.setDisplayBlockState(blockState);
    }

    public static void registerEntityType(Registrar<EntityType<?>> registrar){
        entityTypeRegistration = registrar.register(
                new ResourceLocation("disx","advanced_jukebox_minecart"),
                () -> EntityType.Builder.of(DisxAdvancedJukeboxMinecart::new, MobCategory.MISC).build("advanced_jukebox_minecart")
        );
    }
}

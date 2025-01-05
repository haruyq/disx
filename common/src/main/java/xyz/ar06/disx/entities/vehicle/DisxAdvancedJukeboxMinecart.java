package xyz.ar06.disx.entities.vehicle;


import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
import net.minecraft.world.ticks.ContainerSingleItem;
import org.jetbrains.annotations.Nullable;
import xyz.ar06.disx.DisxLogger;
import xyz.ar06.disx.blocks.DisxAdvancedJukebox;
import xyz.ar06.disx.items.DisxAdvancedJukeboxMinecartItem;
import xyz.ar06.disx.items.DisxCustomDisc;

import java.util.Properties;

public class DisxAdvancedJukeboxMinecart extends Minecart implements ContainerEntity {
    NonNullList<ItemStack> items;

    public static RegistrySupplier<EntityType<?>> entityTypeRegistration;
    public DisxAdvancedJukeboxMinecart(EntityType<?> entityType, Level level) {
        super(entityType, level);
        items = NonNullList.withSize(1, ItemStack.EMPTY);
    }

    private boolean isHas_Record(){
        return !this.items.get(0).equals(ItemStack.EMPTY);
    }
    @Override
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        if (interactionHand.equals(InteractionHand.MAIN_HAND) && !player.level().isClientSide()){
            ItemStack handStack = player.getMainHandItem();
            if (handStack.getItem() instanceof DisxCustomDisc){
                if (!isHas_Record()){
                    DisxLogger.debug("Does not have record, taking and putting in");
                    setItem(0, handStack.copyWithCount(1));
                    handStack.setCount(handStack.getCount() - 1);
                } else {
                    DisxLogger.debug("Has record, taking out and putting in inventory");
                    ItemStack stack = this.items.get(0).copyWithCount(1);
                    removeItem(0, 1);
                    player.getInventory().add(stack);
                }
            } else if (handStack.isEmpty()) {
                if (isHas_Record()){
                    DisxLogger.debug("Has record, taking out and putting in inventory");
                    ItemStack stack = this.items.get(0).copyWithCount(1);
                    removeItem(0, 1);
                    player.getInventory().add(stack);
                }
            }
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

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(DisxAdvancedJukeboxMinecartItem.itemRegistration.get(), 1);
    }

    @Override
    protected Item getDropItem() {
        return DisxAdvancedJukeboxMinecartItem.itemRegistration.get();
    }

    @Override
    public void destroy(DamageSource damageSource) {
        super.destroy(damageSource);
    }

    @Override
    public void remove(RemovalReason removalReason) {
        ItemStack stack = getItem(0);
        if (!stack.isEmpty()){
            ItemStack drop = stack.copyWithCount(1);
            ItemEntity itemEntity = new ItemEntity(level(), getX(), getY(), getZ(), drop);
            level().addFreshEntity(itemEntity);
        }
        super.remove(removalReason);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        ContainerHelper.loadAllItems(compoundTag, items);
        super.load(compoundTag);
    }

    @Override
    public boolean save(CompoundTag compoundTag) {
        ContainerHelper.saveAllItems(compoundTag, items);
        return super.save(compoundTag);
    }
    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public ItemStack getItem(int i) {
        return items.get(i);
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        ItemStack stack = items.get(i);
        items.set(0, ItemStack.EMPTY);
        this.setChanged();
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return items.remove(i);
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        items.set(i, itemStack);
        this.setChanged();
    }

    @Override
    public void setChanged() {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        ContainerHelper.saveAllItems(compoundTag, this.items);
        super.addAdditionalSaveData(compoundTag);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        ContainerHelper.loadAllItems(compoundTag, this.items);
        super.readAdditionalSaveData(compoundTag);
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {

    }

    @Nullable
    @Override
    public ResourceLocation getLootTable() {
        return null;
    }

    @Override
    public void setLootTable(@Nullable ResourceLocation resourceLocation) {

    }

    @Override
    public long getLootTableSeed() {
        return 0;
    }

    @Override
    public void setLootTableSeed(long l) {

    }

    @Override
    public NonNullList<ItemStack> getItemStacks() {
        return null;
    }

    @Override
    public void clearItemStacks() {

    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return null;
    }
}

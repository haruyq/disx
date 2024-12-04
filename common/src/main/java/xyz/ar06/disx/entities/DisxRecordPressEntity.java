package xyz.ar06.disx.entities;

import xyz.ar06.disx.DisxMain;
import xyz.ar06.disx.DisxSystemMessages;
import xyz.ar06.disx.items.DisxCustomDisc;
import xyz.ar06.disx.recipe_types.DisxCustomDiscRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Optional;

public class DisxRecordPressEntity extends BlockEntity implements Container {

    private NonNullList<ItemStack> items;

    private static HashMap<Integer, String> itemIndex = new HashMap<Integer, String>();

    private int powerInput = 0;

    public DisxRecordPressEntity(BlockPos blockPos, BlockState blockState) {
        super(
                DisxMain.REGISTRAR_MANAGER.get().get(Registries.BLOCK_ENTITY_TYPE).get(new ResourceLocation("disx","record_press_entity")),
                blockPos,
                blockState
        );
        if (itemIndex.isEmpty()){
            itemIndex.put(0, "blank_disc");
            itemIndex.put(1, "record_stamp");
            itemIndex.put(2, "variant_factor");
        }
        this.items = NonNullList.withSize(3, ItemStack.EMPTY);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        this.items = NonNullList.withSize(3, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(compoundTag, this.items);
        this.setPowerInput(compoundTag.getInt("powerInput"));
        super.load(compoundTag);
    }

    public void tryProduceRecord(){
        if (this.hasConditionsForRecord() && (this.getPowerInput() > 0)){
            ItemStack recordStamp = this.items.get(1);
            if (!recordStamp.isEmpty()){
                CompoundTag stampTag = recordStamp.getTag();
                String videoId = stampTag.getString("videoId");
                String videoName = stampTag.getString("videoName");
                if (videoId.isBlank() || videoName.isBlank()){
                    throw new RuntimeException("[Disx Record Press] No video id found on provided record stamp!");
                } else {
                    ItemStack modifiedRecordStamp = this.items.get(1).copyWithCount(1);
                    modifiedRecordStamp.setDamageValue(modifiedRecordStamp.getDamageValue() + 1);
                    ItemStack variantFactorStack = this.items.get(2);
                    Item factorItem = variantFactorStack.getItem();

                    /*
                    Item recordItem = DisxCustomDisc.discFactorDiscTypes.get(factorItem);
                    if (recordItem == null){
                        recordItem = DisxMain.REGISTRAR_MANAGER.get().get(Registries.ITEM).get(new ResourceLocation("disx","custom_disc_default"));
                    }

                     */
                    ItemStack record = getRecordVariant();
                    CompoundTag compoundTag = new CompoundTag();
                    compoundTag.putString("videoId", videoId);
                    compoundTag.putString("discName", videoName);
                    record.setTag(compoundTag);
                    BlockPos blockPos = getBlockPos();
                    double x = blockPos.getX();
                    double y = blockPos.getY();
                    double z = blockPos.getZ();
                    x += 0.5;
                    y += 0.2;
                    z += 0.5;
                    ItemEntity record_drop = new ItemEntity(level, x, y, z, record);
                    record_drop.setDefaultPickUpDelay();
                    ItemEntity stamp_drop = new ItemEntity(level, x, y, z, modifiedRecordStamp);
                    stamp_drop.setDefaultPickUpDelay();
                    this.removeItem(0, 1);
                    this.removeItem(1, 1);
                    this.removeItem(2, 1);
                    level.playSound(null, blockPos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS);
                    level.addFreshEntity(record_drop);
                    if (modifiedRecordStamp.getDamageValue() < 3){
                        level.addFreshEntity(stamp_drop);
                    }
                }
            }
        }
    }


    private ItemStack getRecordVariant(){
        Optional<DisxCustomDiscRecipe> recipeOptional = level.getRecipeManager().getRecipeFor(DisxCustomDiscRecipe.DisxCustomDiscRecipeType.INSTANCE, this, level);
        if (!recipeOptional.isEmpty()){
            ItemStack recordStack = recipeOptional.get().getResultItem();
            return recordStack;
        } else {
            ItemStack recordStack = new ItemStack(DisxMain.REGISTRAR_MANAGER.get().get(Registries.ITEM).get(new ResourceLocation("disx","custom_disc_default")));
            return recordStack;
        }
    }

    public boolean hasConditionsForRecord(){
        boolean cond1 = (!this.items.get(0).isEmpty());
        boolean cond2 = (!this.items.get(1).isEmpty());
        return (cond1 && cond2);
    }

    @Override
    public int getContainerSize() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    @Override
    public void setChanged() {
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        this.tryProduceRecord();
        super.setChanged();
    }


    @Override
    public ItemStack getItem(int i) {
        return this.items.get(i);
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        ItemStack copy = this.items.get(i).copy();
        this.items.set(i, ItemStack.EMPTY);
        return copy;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return null;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void setPowerInput(int i){
        this.powerInput = i;
    }

    public int getPowerInput(){
        return this.powerInput;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        this.items.set(i, itemStack.copy());
        this.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        ContainerHelper.saveAllItems(compoundTag, this.items);
        compoundTag.putInt("powerInput", this.getPowerInput());
        super.saveAdditional(compoundTag);
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public int countItem(Item item) {
        return Container.super.countItem(item);
    }

    @Override
    public void clearContent() {
        this.items = NonNullList.withSize(3, ItemStack.EMPTY);
    }
}

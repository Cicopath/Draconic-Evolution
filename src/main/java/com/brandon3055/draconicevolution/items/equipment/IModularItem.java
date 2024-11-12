package com.brandon3055.draconicevolution.items.equipment;

import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.utils.EnergyUtils;
import com.brandon3055.draconicevolution.api.capability.DECapabilities;
import com.brandon3055.draconicevolution.api.capability.ModuleHost;
import com.brandon3055.draconicevolution.api.capability.PropertyProvider;
import com.brandon3055.draconicevolution.api.config.BooleanProperty;
import com.brandon3055.draconicevolution.api.config.ConfigProperty.BooleanFormatter;
import com.brandon3055.draconicevolution.api.config.ConfigProperty.DecimalFormatter;
import com.brandon3055.draconicevolution.api.config.ConfigProperty.IntegerFormatter;
import com.brandon3055.draconicevolution.api.config.DecimalProperty;
import com.brandon3055.draconicevolution.api.config.IntegerProperty;
import com.brandon3055.draconicevolution.api.crafting.IFusionDataTransfer;
import com.brandon3055.draconicevolution.api.event.ModularItemInitEvent;
import com.brandon3055.draconicevolution.api.modules.ModuleCategory;
import com.brandon3055.draconicevolution.api.modules.ModuleTypes;
import com.brandon3055.draconicevolution.api.modules.data.AOEData;
import com.brandon3055.draconicevolution.api.modules.data.SpeedData;
import com.brandon3055.draconicevolution.api.modules.lib.ModularOPStorage;
import com.brandon3055.draconicevolution.api.modules.lib.ModuleHostImpl;
import com.brandon3055.draconicevolution.api.modules.lib.StackModuleContext;
import com.brandon3055.draconicevolution.client.keybinding.KeyBindings;
import com.brandon3055.draconicevolution.init.EquipCfg;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Created by brandon3055 on 16/6/20
 */
public interface IModularItem extends IItemExtension, IFusionDataTransfer {

    TechLevel getTechLevel();

    default ModuleHost createHostCapForRegistration(ItemStack stack) {
        ModuleHostImpl host = instantiateHost(stack);
        if (this instanceof IModularEnergyItem) {
            host.addCategories(ModuleCategory.ENERGY);
        }

        if (this instanceof IModularMiningTool) {
            host.addCategories(ModuleCategory.MINING_TOOL);
            host.addPropertyBuilder(props -> {
                props.add(new DecimalProperty("mining_speed", 1).range(0, 1).setFormatter(DecimalFormatter.PERCENT_1));
                AOEData aoe = host.getModuleData(ModuleTypes.AOE);
                if (aoe != null) {
                    props.add(new IntegerProperty("mining_aoe", aoe.aoe()).range(0, aoe.aoe()).setFormatter(IntegerFormatter.AOE));
                    props.add(new BooleanProperty("aoe_safe", false).setFormatter(BooleanFormatter.ENABLED_DISABLED));
                }
            });
        }

        if (this instanceof IModularMelee) {
            host.addCategories(ModuleCategory.MELEE_WEAPON);
            host.addPropertyBuilder(props -> {
                AOEData aoe = host.getModuleData(ModuleTypes.AOE);
                if (aoe != null) {
                    props.add(new DecimalProperty("attack_aoe", aoe.aoe() * 1.5).range(0, aoe.aoe() * 1.5).setFormatter(DecimalFormatter.AOE_1));
                }
            });
        }

        NeoForge.EVENT_BUS.post(new ModularItemInitEvent(stack, host, host));
        return host;
    }



//    idk about this. I may want to re implement my own optional type delio...
//    Or, maybe just split this up into seperate capabiluty builders that can be overidden in order to add things?
//    Yea, I think i like that.
//    Then MultiCapabilityProvider can probably go away.
//    Yea, will have an init for each capability, and a modify capability method for each that can be used to add additional stuff.
//    default MultiCapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
//        MultiCapabilityProvider provider = new MultiCapabilityProvider()
//        ModuleHostImpl host = createHost(stack);
//        provider.addCapability(host, "module_host", DECapabilities.Host.ITEM, DECapabilities.Properties.ITEM);
//        ModularOPStorage opStorage = createOPStorage(stack, host);
//        if (opStorage != null) {
//            provider.addCapability(opStorage, "energy", CapabilityOP.ITEM, Capabilities.EnergyStorage.ITEM);
//            host.addCategories(ModuleCategory.ENERGY);
//        }

//        if (this instanceof IModularMiningTool) {
//            host.addCategories(ModuleCategory.MINING_TOOL);
//            host.addPropertyBuilder(props -> {
//                props.add(new DecimalProperty("mining_speed", 1).range(0, 1).setFormatter(DecimalFormatter.PERCENT_1));
//                AOEData aoe = host.getModuleData(ModuleTypes.AOE);
//                if (aoe != null) {
//                    props.add(new IntegerProperty("mining_aoe", aoe.aoe()).range(0, aoe.aoe()).setFormatter(IntegerFormatter.AOE));
//                    props.add(new BooleanProperty("aoe_safe", false).setFormatter(BooleanFormatter.ENABLED_DISABLED));
//                }
//            });
//        }

//        if (this instanceof IModularMelee) {
//            host.addCategories(ModuleCategory.MELEE_WEAPON);
//            host.addPropertyBuilder(props -> {
//                AOEData aoe = host.getModuleData(ModuleTypes.AOE);
//                if (aoe != null) {
//                    props.add(new DecimalProperty("attack_aoe", aoe.aoe() * 1.5).range(0, aoe.aoe() * 1.5).setFormatter(DecimalFormatter.AOE_1));
//                }
//            });
//        }

//        initCapabilities(stack, host, provider);
//        NeoForge.EVENT_BUS.post(new ModularItemInitEvent(stack, host, host));
//        return provider;
//    }

//    default void initHostCapability(ItemStack stack) {
//        ModuleHostImpl host = createHost(stack);
//        wait... how do i save changes?...
//        ron can deal with this.
//
//    }

//    default void initCapabilities(ItemStack stack, ModuleHostImpl host, MultiCapabilityProvider provider) {
//    }

//    public static void register(RegisterCapabilitiesEvent event) {
//        capability(event, DEContent.TILE_ENERGY_TRANSFUSER, CapabilityOP.BLOCK);
//        capability(event, DEContent.TILE_ENERGY_TRANSFUSER, Capabilities.ItemHandler.BLOCK);
//    }

//    static <T, C> void capability(RegisterCapabilitiesEvent event, Supplier<ItemLike> type, ItemCapability<T, C> capability) {
//        event.registerItem(capability, (stack, context) -> , type.get());
//    }

    @NotNull
    ModuleHostImpl instantiateHost(ItemStack stack);

    @NotNull
    ModularOPStorage instantiateOPStorage(ItemStack stack, Supplier<ModuleHost> hostSupplier);

    @OnlyIn (Dist.CLIENT)
    default void addModularItemInformation(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("[Modular Item]").withStyle(ChatFormatting.BLUE));
        }

        ModuleHost host = stack.getCapability(DECapabilities.Host.ITEM);
        if (host != null) {
            host.getModuleEntities().forEach(e -> e.addHostHoverText(stack, worldIn, tooltip, flagIn));
            host.getInstalledTypes().map(host::getModuleData).filter(Objects::nonNull).forEach(data -> data.addHostHoverText(stack, worldIn, tooltip, flagIn));
        }

        EnergyUtils.addEnergyInfo(stack, tooltip);
        if (EnergyUtils.isEnergyItem(stack) && EnergyUtils.getMaxEnergyStored(stack) == 0) {
            tooltip.add(Component.translatable("modular_item.draconicevolution.requires_energy").withStyle(ChatFormatting.RED));
            if (KeyBindings.toolModules != null && KeyBindings.toolModules.getTranslatedKeyMessage() != null) {
                tooltip.add(Component.translatable("modular_item.draconicevolution.requires_energy_press", KeyBindings.toolModules.getTranslatedKeyMessage().getString()).withStyle(ChatFormatting.BLUE));
            }
        }
    }

    @Override
    default Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
//        if (stack.getCapability(MODULE_HOST_CAPABILITY).isPresent()) { //Because vanilla calls this before capabilities are registered.
//            ModuleHost host = stack.getCapability(MODULE_HOST_CAPABILITY).orElseThrow(IllegalStateException::new);
//            host.getAttributeModifiers(slot, stack, map);
//        }
        return map;
    }

    default void handleTick(ItemStack stack, LivingEntity entity, @Nullable EquipmentSlot slot, boolean inEquipModSlot) {
        ModuleHost host = stack.getCapability(DECapabilities.Host.ITEM);
        StackModuleContext context = new StackModuleContext(stack, entity, slot).setInEquipModSlot(inEquipModSlot);
        host.handleTick(context);
    }

    /**
     * This is used to determine if a modular item is in a valid slot for its modules to operate.
     *
     * @param stack       The stack
     * @param slot        The equipment slot or null if this item is in the players general main inventory.
     * @param inEquipSlot In equipment slot such as curio
     * @return true if this stack is in a valid slot.
     */
    default boolean isEquipped(ItemStack stack, @Nullable EquipmentSlot slot, boolean inEquipSlot) {
        if (this instanceof IModularArmor) return (slot != null && slot.getType() == EquipmentSlot.Type.ARMOR) || inEquipSlot;
        return true;
    }

    default float getDestroySpeed(ItemStack stack, BlockState state) {
        ModuleHost host = stack.getCapability(DECapabilities.Host.ITEM);
        SpeedData data = host.getModuleData(ModuleTypes.SPEED);
        float moduleValue = data == null ? 0 : (float) data.speedMultiplier();
        //The way vanilla handles efficiency is kinda dumb. So this is far from perfect but its kinda close... ish.
        float multiplier = MathHelper.map((moduleValue + 1F) * (moduleValue + 1F), 1F, 2F, 1F, 1.65F);
        float propVal = 1F;
        //Module host should always be a property provider because it needs to provide module properties.
        if (host instanceof PropertyProvider && ((PropertyProvider) host).hasDecimal("mining_speed")) {
            propVal = (float) ((PropertyProvider) host).getDecimal("mining_speed").getValue();
            propVal *= propVal; //Make this exponential
        }

        float aoe = host.getModuleData(ModuleTypes.AOE, new AOEData(0)).aoe();
        if (host instanceof PropertyProvider && ((PropertyProvider) host).hasInt("mining_aoe")) {
            aoe = ((PropertyProvider) host).getInt("mining_aoe").getValue();
        }

        if (getEnergyStored(stack) < EquipCfg.energyHarvest) {
            multiplier = 0;
        } else if (aoe > 0) {
            float userTarget = multiplier * propVal;
            multiplier = Math.min(userTarget, multiplier / (1 + (aoe * 10)));
        } else {
            multiplier *= propVal;
        }

        if (isCorrectToolForDrops(stack, state) && (multiplier > 0 || propVal == 0)) {
            return getBaseEfficiency() * multiplier;
        } else {
            return propVal == 0 ? 0 : 1F;
        }
    }

    @Override
    default boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return IItemExtension.super.isCorrectToolForDrops(stack, state);
    }

    default float getBaseEfficiency() {
        return 1F;
    }

//    @Nullable
//    @Override
//    default CompoundTag getShareTag(ItemStack stack) {
//        return DECapabilities.writeToShareTag(stack, stack.getTag());
//    }
//
//    @Override
//    default void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
//        stack.setTag(nbt);
//        DECapabilities.readFromShareTag(stack, nbt);
//    }

    default long getEnergyStored(ItemStack stack) {
        return EnergyUtils.getEnergyStored(stack);
    }

    default long extractEnergy(Player player, ItemStack stack, long amount) {
        if (player != null && player.getAbilities().instabuild) {
            return amount;
        }
        IOPStorage storage = EnergyUtils.getStorage(stack);
        if (storage != null) {
            return storage.modifyEnergyStored(-amount);
        }
        return 0;
    }

    default boolean damageBarVisible(ItemStack stack) {
        long max = EnergyUtils.getMaxEnergyStored(stack);
        return max > 0 && EnergyUtils.getEnergyStored(stack) < max;
    }

    default int damageBarWidth(ItemStack stack) {
        float charge = (float) EnergyUtils.getEnergyStored(stack) / EnergyUtils.getMaxEnergyStored(stack);
        return Math.round(13.0F * charge);
    }

    default int damageBarColour(ItemStack stack) {
        float maxEnergy = EnergyUtils.getMaxEnergyStored(stack);
        float energy = EnergyUtils.getEnergyStored(stack);
        float f = Math.max(0.0F, (energy) / maxEnergy);
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    default boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return oldStack.getItem() != newStack.getItem() || slotChanged;
    }
}

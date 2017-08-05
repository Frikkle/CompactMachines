package org.dave.compactmachines3.item;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.init.Fluidss;
import org.dave.compactmachines3.miniaturization.MiniaturizationPotion;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.misc.CreativeTabCompactMachines3;

import javax.annotation.Nullable;
import java.util.List;

public class ItemMiniFluidDrop extends ItemFood {
    public ItemMiniFluidDrop() {
        super(1, 0.1F, false);

        this.setAlwaysEdible();
        this.setCreativeTab(CreativeTabCompactMachines3.COMPACTMACHINES3_TAB);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public Item setUnlocalizedName(String name) {
        if(!name.startsWith(CompactMachines3.MODID + ".")) {
            name = CompactMachines3.MODID + "." + name;
        }
        return super.setUnlocalizedName(name);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack itemStack = player.getHeldItem(hand);

        if(world.isRemote || !(player instanceof EntityPlayerMP)) {
            return super.onItemRightClick(world, player, hand);
        }
        Vec3d eyeVec = new Vec3d(player.posX, player.posY + (double)player.getEyeHeight(), player.posZ);
        Vec3d lookVec = player.getLook(0.0f);
        Vec3d maxVec = eyeVec.addVector(lookVec.x * 4.5d, lookVec.y * 4.5d, lookVec.z * 4.5d);

        RayTraceResult trace = world.rayTraceBlocks(eyeVec, maxVec, false, false, true);

        if(trace == null || trace.getBlockPos() == null) {
            return super.onItemRightClick(world, player, hand);
        }

        TileEntity te = world.getTileEntity(trace.getBlockPos());
        if(te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, trace.sideHit)) {
            IFluidHandler fluidHandler = world.getTileEntity(trace.getBlockPos()).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, trace.sideHit);
            if(fluidHandler.fill(new FluidStack(Fluidss.miniaturizationFluid, 125), false) == 125) {
                fluidHandler.fill(new FluidStack(Fluidss.miniaturizationFluid, 125), true);
                itemStack.setCount(itemStack.getCount()-1);

                return new ActionResult(EnumActionResult.SUCCESS, itemStack);
            }
        }

        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        if(GuiScreen.isShiftKeyDown()) {
            tooltip.add(TextFormatting.YELLOW + I18n.format("tooltip." + CompactMachines3.MODID + ".minifluiddrop.hint"));
        }
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        super.onFoodEaten(stack, worldIn, player);

        if(worldIn.isRemote) {
            return;
        }

        int duration = ConfigurationHandler.PotionSettings.onEatDuration;
        int amplifier = ConfigurationHandler.PotionSettings.onEatAmplifier;
        MiniaturizationPotion.applyPotion(player, duration, amplifier);
    }
}
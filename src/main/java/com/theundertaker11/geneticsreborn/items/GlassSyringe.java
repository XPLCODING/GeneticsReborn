package com.theundertaker11.geneticsreborn.items;

import com.theundertaker11.geneticsreborn.GeneticsReborn;
import com.theundertaker11.geneticsreborn.api.capability.genes.EnumGenes;
import com.theundertaker11.geneticsreborn.api.capability.genes.Genes;
import com.theundertaker11.geneticsreborn.api.capability.genes.IGenes;
import com.theundertaker11.geneticsreborn.api.capability.maxhealth.IMaxHealth;
import com.theundertaker11.geneticsreborn.util.ModUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class GlassSyringe extends ItemBase {
	public GlassSyringe(String name) {
		super(name);
		this.setMaxStackSize(1);
		this.setMaxDamage(0);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add("Right click to draw blood, shift right click to inject blood");
		if (stack.getTagCompound() != null && stack.getItemDamage() == 1) {
			if (stack.getTagCompound().getBoolean("pure")) tooltip.add("This blood is purified");
			else tooltip.add("This blood is contaminated");
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		ItemStack stack = playerIn.getHeldItemMainhand();
		if (worldIn.isRemote || ModUtils.getIGenes(playerIn) == null || hand != EnumHand.MAIN_HAND || ModUtils.getIMaxHealth(playerIn) == null)
			return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
		NBTTagCompound tag = ModUtils.getTagCompound(stack);
		if (!playerIn.isSneaking() && stack.getItemDamage() == 0) {
			stack.setItemDamage(1);
			playerIn.attackEntityFrom(DamageSource.GENERIC, 2);
			tag.setBoolean("pure", false);
			tag.setString("owner", playerIn.getUUID(playerIn.getGameProfile()).toString());
			Genes.setNBTStringsFromGenes(stack, playerIn);
		} else if (playerIn.isSneaking()) {
			Boolean configallows = true;
			if (!GeneticsReborn.playerGeneSharing) {
				configallows = tag.getString("owner").equals(playerIn.getUUID(playerIn.getGameProfile()).toString());
			}
			if (configallows && stack.getItemDamage() == 1 && tag.getBoolean("pure")) {
				stack.setItemDamage(0);
				playerIn.addPotionEffect((new PotionEffect(Potion.getPotionById(ModUtils.blindness), 60, 1)));
				playerIn.attackEntityFrom(DamageSource.GENERIC, 1);
				IGenes genes = ModUtils.getIGenes(playerIn);
				IMaxHealth hearts = ModUtils.getIMaxHealth((EntityLivingBase) playerIn);
				tag.removeTag("pure");
				tag.removeTag("owner");
				for (int i = 0; i < Genes.TotalNumberOfGenes; i++) {
					String nbtname = "Null";
					if (tag.hasKey(Integer.toString(i))) {
						nbtname = tag.getString(Integer.toString(i));
						tag.removeTag(Integer.toString(i));
					}
					EnumGenes gene = Genes.getGeneFromString(nbtname);
					if (gene != null) {
						if (gene.equals(EnumGenes.MORE_HEARTS) && !genes.hasGene(EnumGenes.MORE_HEARTS)) {
							hearts.setBonusMaxHealth(20);
						}
						genes.addGene(gene);
					}
				}
				for (int i = 0; i < Genes.TotalNumberOfGenes; i++) {
					String nbtname = "Null";
					if (tag.hasKey(i + "anti")) {
						nbtname = tag.getString(i + "anti");
						tag.removeTag(i + "anti");
					}
					EnumGenes gene = Genes.getGeneFromString(nbtname);
					if (gene != null) {
						genes.removeGene(gene);
						if (gene.equals(EnumGenes.MORE_HEARTS)) {
							hearts.setBonusMaxHealth(0);
						}
					}
				}
			}
		}
		return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
	}
}

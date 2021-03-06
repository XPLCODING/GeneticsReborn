package com.theundertaker11.geneticsreborn.event;

import com.theundertaker11.geneticsreborn.GeneticsReborn;
import com.theundertaker11.geneticsreborn.api.capability.genes.EnumGenes;
import com.theundertaker11.geneticsreborn.api.capability.genes.IGenes;
import com.theundertaker11.geneticsreborn.items.GRItems;
import com.theundertaker11.geneticsreborn.util.ModUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * This handles the enabling/disabling of flight based on player genes.
 */
public class OnWorldTickEvent {

	private static List<String> PlayersWithFlight = new ArrayList<String>();

	@SubscribeEvent
	public void WorldTick(WorldTickEvent event) {
		if ((GeneticsReborn.enableFlight || !GeneticsReborn.allowGivingEntityGenes) && GREventHandler.flightticktimer > 30 && event.world.provider.getDimension() == 0 && !event.world.isRemote) {
			GREventHandler.flightticktimer = 0;
			for (EntityPlayerMP player : event.world.getMinecraftServer().getPlayerList().getPlayers()) {
				IGenes genes = ModUtils.getIGenes(player);
				if (player != null && genes != null) {
					if (!player.isCreative()) {
						String username = player.getName();
						if (genes.hasGene(EnumGenes.FLY)) {
							boolean shouldFly = true;
							for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
								ItemStack stack = player.inventory.getStackInSlot(i);
								if (stack.getItem() == GRItems.AntiField && stack.getItemDamage() == 1) {
									shouldFly = false;
									if (player.capabilities.isFlying) player.capabilities.isFlying = false;
									break;
								}
							}
							player.capabilities.allowFlying = shouldFly;
							player.sendPlayerAbilities();
							if (!PlayersWithFlight.contains(username)) PlayersWithFlight.add(username);
						} else if (PlayersWithFlight.contains(username)) {
							PlayersWithFlight.remove(username);
							player.capabilities.allowFlying = false;
							player.capabilities.isFlying = false;
							player.sendPlayerAbilities();
						}
					}
					if (!GeneticsReborn.allowGivingEntityGenes) {
						worldTickGeneLogic(genes, player);
					}
				}
			}
		}

		if (GeneticsReborn.allowGivingEntityGenes && GREventHandler.worldTickTimer > 38 && !event.world.isRemote) {
			for (Entity ent : event.world.loadedEntityList) {
				if (ent != null && ent instanceof EntityLivingBase) {
					EntityLivingBase entity = (EntityLivingBase) ent;
					worldTickGeneLogic(ModUtils.getIGenes(entity), entity);
				}
			}
		}
	}

	/**
	 * This handles a few genes just so I don't have to copy paste them.
	 *
	 * @param genes  IGenes of the entity
	 * @param entity An EntityLivingBase
	 */
	public void worldTickGeneLogic(IGenes genes, EntityLivingBase entity) {
		if (genes != null) {
			if (GeneticsReborn.enableWaterBreathing && genes.hasGene(EnumGenes.WATER_BREATHING)) {
				entity.setAir(300);
			}
			if (GeneticsReborn.enableNightVision && entity instanceof EntityPlayer && genes.hasGene(EnumGenes.NIGHT_VISION)) {
				entity.addPotionEffect((new PotionEffect(Potion.getPotionById(ModUtils.nightVision), 328, 0, false, false)));
			}
			if (GeneticsReborn.enableJumpBoost && genes.hasGene(EnumGenes.JUMP_BOOST)) {
				entity.addPotionEffect((new PotionEffect(Potion.getPotionById(ModUtils.jumpBoost), 110, 1, false, false)));
			}
			if (GeneticsReborn.enableSpeed && genes.hasGene(EnumGenes.SPEED)) {
				entity.addPotionEffect((new PotionEffect(Potion.getPotionById(ModUtils.moveSpeed), 110, 1, false, false)));
			}
			if (GeneticsReborn.enableResistance && genes.hasGene(EnumGenes.RESISTANCE)) {
				entity.addPotionEffect((new PotionEffect(Potion.getPotionById(ModUtils.resistance), 110, 1, false, false)));
			}
			if (GeneticsReborn.enableStrength && genes.hasGene(EnumGenes.STRENGTH)) {
				entity.addPotionEffect((new PotionEffect(Potion.getPotionById(ModUtils.strength), 110, 0, false, false)));
			}
		}
	}
}

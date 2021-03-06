package com.theundertaker11.geneticsreborn.event;

import com.theundertaker11.geneticsreborn.GeneticsReborn;
import com.theundertaker11.geneticsreborn.api.capability.genes.EnumGenes;
import com.theundertaker11.geneticsreborn.api.capability.genes.IGenes;
import com.theundertaker11.geneticsreborn.blocks.GRBlocks;
import com.theundertaker11.geneticsreborn.items.GRItems;
import com.theundertaker11.geneticsreborn.util.ModUtils;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Iterator;

public class PlayerTickEvent {

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		EntityPlayer player = event.player;
		IGenes genes = ModUtils.getIGenes(event.player);
		World world = event.player.getEntityWorld();
		if (player == null || genes == null || world.isRemote) return;

		tryItemMagnet(player, world, genes);
		tryXPMagnet(player, world, genes);
		tryPhotosynthesis(player, world, genes);
	}

	/**
	 * Checks all the requirements and tries to do the item magnet gene(Suck in all items from an area).
	 *
	 * @param player
	 * @param world
	 * @param genes
	 */
	public static void tryItemMagnet(EntityPlayer player, World world, IGenes genes) {
		if (GeneticsReborn.enableItemMagnet && genes.hasGene(EnumGenes.ITEM_MAGNET) && !player.inventory.hasItemStack(new ItemStack(GRItems.AntiField))
				&& !player.isSneaking()) {
			Iterator iterator = ModUtils.getEntitiesInRange(EntityItem.class, world, player.posX, player.posY,
					player.posZ, 6.5).iterator();
			while (iterator.hasNext()) {
				EntityItem itemToGet = (EntityItem) iterator.next();

				if (!itemToGet.getTags().contains("geneticsrebornLOL") && shouldPickupItem(world, itemToGet.getPosition())) {
					EntityItemPickupEvent pickupEvent = new EntityItemPickupEvent(player, itemToGet);
					MinecraftForge.EVENT_BUS.post(pickupEvent);
					ItemStack itemStackToGet = itemToGet.getItem();
					int stackSize = itemStackToGet.getCount();

					if (pickupEvent.getResult() == Result.ALLOW || stackSize <= 0 || !player.inventory.addItemStackToInventory(itemStackToGet)) {
						player.onItemPickup(itemToGet, stackSize);
						world.playSound(player, player.getPosition(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.AMBIENT,
								0.15F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
					}
				} else if (!itemToGet.getTags().contains("geneticsrebornLOL")) itemToGet.addTag("geneticsrebornLOL");
			}
		}
	}

	/**
	 * Checks all the requirements and tries to do the xp magnet gene(Suck in all xp from an area).
	 *
	 * @param player
	 * @param world
	 * @param genes
	 */
	public static void tryXPMagnet(EntityPlayer player, World world, IGenes genes) {
		if (GeneticsReborn.enableXPMagnet && genes.hasGene(EnumGenes.XP_MAGNET) && !player.inventory.hasItemStack(new ItemStack(GRItems.AntiField))
				&& !player.isSneaking()) {
			Iterator iterator = ModUtils.getEntitiesInRange(EntityXPOrb.class, world, player.posX, player.posY, player.posZ,
					6.5).iterator();
			while (iterator.hasNext()) {
				EntityXPOrb xpToGet = (EntityXPOrb) iterator.next();

				if (xpToGet.isDead || xpToGet.isInvisible()) {
					continue;
				}
				player.xpCooldown = 0;
				xpToGet.delayBeforeCanPickup = 0;
				xpToGet.setPosition(player.posX, player.posY, player.posZ);
				PlayerPickupXpEvent xpEvent = new PlayerPickupXpEvent(player, xpToGet);
				MinecraftForge.EVENT_BUS.post(xpEvent);
				if (xpEvent.getResult() == Result.ALLOW) {
					xpToGet.onCollideWithPlayer(player);
				}

			}
		}
	}

	/**
	 * Checks all conditions and tries to give the player hunger when no blocks are above them, and it is day.
	 *
	 * @param player
	 * @param world
	 * @param genes
	 */
	public static void tryPhotosynthesis(EntityPlayer player, World world, IGenes genes) {
		if (GeneticsReborn.enablePhotosynthesis && genes.hasGene(EnumGenes.PHOTOSYNTHESIS)) {
			if (world.isDaytime() && world.getHeight(player.getPosition()).getY() < (player.getPosition().getY() + 1)) {
				double rand = Math.random();
				if (rand < 0.01) {
					player.getFoodStats().addStats(1, 0.5F);
				}
			}
		}
	}

	public static boolean shouldPickupItem(World world, BlockPos pos) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		for (int i = -5; i <= 5; i++) {
			BlockPos newpos = new BlockPos(x, y + i, z);
			if (world.getBlockState(newpos).getBlock() == GRBlocks.AntiFieldBlock) {
				return false;
			}
		}
		return true;
	}
}

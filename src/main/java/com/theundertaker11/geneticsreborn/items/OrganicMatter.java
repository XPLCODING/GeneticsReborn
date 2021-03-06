package com.theundertaker11.geneticsreborn.items;

import com.theundertaker11.geneticsreborn.util.ModUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class OrganicMatter extends ItemBase {

	public OrganicMatter(String name) {
		super(name);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (stack.getTagCompound() != null) {
			tooltip.add("Cell type: " + ModUtils.getTagCompound(stack).getString("entityName"));
		} else tooltip.add("Cell type: Blank");
	}
	
	/* Code for use later to spawn the type of mob the organic matter was caught from

	  NBTBase mobCompound = tagCompound.getTag("mob");
                String type = tagCompound.getString("type");
                EntityLivingBase entityLivingBase = createEntity(player, world, type);
                if (entityLivingBase == null) {
             		Do some kind of fail code.
                }
                entityLivingBase.readEntityFromNBT((NBTTagCompound) mobCompound);
                entityLivingBase.setLocationAndAngles(x, y, z, 0, 0);
                
                world.spawnEntityInWorld(entityLivingBase);
	 */
}

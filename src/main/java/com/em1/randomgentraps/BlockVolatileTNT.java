package com.em1.randomgentraps;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockVolatileTNT extends Block {

    public BlockVolatileTNT() {
        super(Material.TNT);
        setHardness(0.0F);
        setResistance(0.0F);
        setLightLevel(0.0F);
        setCreativeTab(CreativeTabs.REDSTONE);
        setSoundType(SoundType.PLANT);
    }

    @Override
    public void onBlockDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        if (!world.isRemote) {
            EntityVolatileTNT ent = new EntityVolatileTNT(world,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5);
            ent.setFuse(world.rand.nextInt(3) + 1); // very short fuse for chain
            world.spawnEntity(ent);
        }
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
        // Ensure chain reaction still triggers (calls above) then remove block
        if (!world.isRemote) {
            onBlockDestroyedByExplosion(world, pos, explosion);
        }
        world.setBlockToAir(pos);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(this); // Drop the block item when broken
    }

    @Override
    public int quantityDropped(Random random) {
        return 1; // Drop 1 block
    }

    @Override
    public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {
        // Only drop when broken by hand, not by explosion
        if (chance >= 1.0F) {
            super.dropBlockAsItemWithChance(world, pos, state, chance, fortune);
        }
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosion) {
        return false; // Don't drop from explosions
    }

    // Explicitly non-flammable / non-spreading
    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 0;
    }

    @Override
    public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return false;
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 0;
    }

}

package org.dave.cm2.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;
import org.dave.cm2.init.Blockss;
import org.dave.cm2.reference.EnumMachineSize;
import org.dave.cm2.utility.Logz;
import org.dave.cm2.world.tools.StructureTools;

import java.util.Random;


public class WorldGenMachines implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if(world.provider.getDimension() != 0) {
            return;
        }
        // TODO: Add config option to disable world gen

        // TODO: Make chance configurable
        //if(random.nextInt(100) <= 66) {
        if(random.nextInt(1000) != 0) {
            return;
        }

        Logz.debug("Generating cube in overworld: chunkX=%d, chunkZ=%d", chunkX, chunkZ);

        // Choose any random machine size but the biggest three
        EnumMachineSize size = EnumMachineSize.getFromMeta(random.nextInt(EnumMachineSize.values().length - 3));
        int dim = size.getDimension();
        int x = (chunkX << 4) + random.nextInt(16-dim) + dim;
        int z = (chunkZ << 4) + random.nextInt(16-dim) + dim;
        int y = world.getHeightmapHeight(x, z) + dim;

        // Select one of the four top corners
        int cxSphere = x - (random.nextBoolean() ? dim : 0);
        int cySphere = y;
        int czSphere = z - (random.nextBoolean() ? dim : 0);
        int rSphere  = dim - random.nextInt(3);

        IBlockState state = Blockss.wallBreakable.getDefaultState();
        IBlockState fluidState = Blockss.miniaturizationFluidBlock.getDefaultState();
        for(BlockPos pos : StructureTools.getCubePositions(new BlockPos(x, y, z), dim+1, dim+1, dim+1, true)) {
            // Cut out a sphere at the selected corner
            float xx = pos.getX() - cxSphere;
            float yy = pos.getY() - cySphere;
            float zz = pos.getZ() - czSphere;

            if(Math.pow(xx, 2) + Math.pow(yy, 2) + Math.pow(zz, 2) < Math.pow(rSphere, 2)) {
                continue;
            }

            if(random.nextInt(100) <= 5) {
                world.setBlockState(pos, fluidState);
            } else {
                world.setBlockState(pos, state);
            }
        }
    }
}
package com.github.hoqhuuep.islandcraft.realestate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;

public class Geometry {
    private final int islandGap;
    private final int islandSize;
    private final int islandSeparation;
    private final int innerRadius;
    private final int outerRadius;
    private final int magicNumber;
    private final int resourceIslandRarity;

    public Geometry(final int islandSizeChunks, final int islandGapChunks, final int resourceIslandRarity) {
        islandSize = islandSizeChunks << 4;
        islandGap = islandGapChunks << 4;
        islandSeparation = islandSize + islandGap;
        innerRadius = islandSizeChunks << 3;
        outerRadius = innerRadius + islandGap;
        magicNumber = (islandSize - islandGap) / 2;
        this.resourceIslandRarity = resourceIslandRarity;
    }

    public final Location getInnerIsland(final Location location) {
        if (location == null) {
            return null;
        }
        final int zz = location.getBlockZ() + (islandSize >> 1);
        final int rz = ifloormod(zz, islandSeparation);
        if (rz >= islandSize) {
            return null;
        }
        final int xx = location.getBlockX() + (islandSize >> 1);
        final int row = ifloordiv(zz, islandSeparation);
        final int xxx;
        if (0 == row % 2) {
            xxx = xx;
        } else {
            xxx = xx + (islandSeparation >> 1);
        }
        final int rx = ifloormod(xxx, islandSeparation);
        if (rx >= islandSize) {
            return null;
        }
        final int cz = row * islandSeparation;
        final int col = ifloordiv(xxx, islandSeparation);
        final int cx;
        if (0 == row % 2) {
            cx = col * islandSeparation;
        } else {
            cx = col * islandSeparation - (islandSeparation >> 1);
        }
        return new Location(location.getWorld(), cx, 0, cz);
    }

    // Numbers represent how many island regions a location overlaps.
    // Arrows point towards the centers of the overlapped regions.
    // @-------+-----------+-------+-----------+
    // |...^...|.....^.....|..\./..|.....^.....|
    // |...3...|.....2.....|...3...|.....2.....|
    // |../.\..|.....v.....|...v...|.....v.....|
    // +-------+-----------+-------+-----------+
    // |.......|...............................|
    // |.......|...............................|
    // |.......|...............................|
    // |.......|...............................|
    // |.......|...............................|
    // |.......|...............................|
    // |..<2>..|...............#...............|
    // |.......|...............................|
    // |.......|...............................|
    // |.......|...............................|
    // |.......|...............................|
    // |.......|...............................|
    // |.......|...............................|
    // +-------+-----------+-------+-----------+
    // |..\./..|.....^.....|...^...|.....^.....|
    // |...3...|.....2.....|...3...|.....2.....|
    // |...v...|.....v.....|../.\..|.....v.....|
    // +-------+-----------+-------+-----------+
    // |...................|.......|...........|
    // |...................|.......|...........|
    // |...................|.......|...........|
    // |...................|.......|...........|
    // |...................|.......|...........|
    // |...................|.......|...........|
    // |...1...............|..<2>..|.......1>..|
    // |...................|.......|...........|
    // |...................|.......|...........|
    // |...................|.......|...........|
    // |...................|.......|...........|
    // |...................|.......|...........|
    // |...................|.......|...........|
    // +-------------------+-------+-----------+
    public final List<Location> getOuterIslands(final Location location) {
        if (location == null) {
            return Collections.emptyList();
        }
        final World world = location.getWorld();
        final int x = location.getBlockX();
        final int z = location.getBlockZ();

        final int regionPatternXSize = outerRadius + innerRadius;
        final int regionPatternZSize = regionPatternXSize * 2;
        // # relative to @
        final int relativeHashX = outerRadius;
        final int relativeHashZ = outerRadius;
        // @ relative to world origin
        final int absoluteAtX = ifloordiv(x + relativeHashX, regionPatternXSize) * regionPatternXSize - relativeHashX;
        final int absoluteAtZ = ifloordiv(z + relativeHashZ, regionPatternZSize) * regionPatternZSize - relativeHashZ;
        // # relative to world origin
        final int absoluteHashX = absoluteAtX + relativeHashX;
        final int absoluteHashZ = absoluteAtZ + relativeHashZ;
        // Point to test relative to @
        final int relativeX = x - absoluteAtX;
        final int relativeZ = z - absoluteAtZ;

        final List<Location> result = new ArrayList<Location>();

        // Top
        if (relativeZ < islandGap) {
            final int centerZ = absoluteHashZ - islandSeparation;
            // Left
            if (relativeX < magicNumber + islandGap * 2) {
                final int centerX = absoluteHashX - islandSeparation / 2;
                result.add(new Location(world, centerX, 0, centerZ));
            }
            // Right
            if (relativeX >= magicNumber + islandGap) {
                final int centerX = absoluteHashX + islandSeparation / 2;
                result.add(new Location(world, centerX, 0, centerZ));
            }
        }
        // Middle
        if (relativeZ < outerRadius * 2) {
            // Left
            if (relativeX < islandGap) {
                final int centerX = absoluteHashX - islandSeparation;
                result.add(new Location(world, centerX, 0, absoluteHashZ));
            }
            // Right
            result.add(new Location(world, absoluteHashX, 0, absoluteHashZ));
        }
        // Bottom
        if (relativeZ >= islandSize + islandGap) {
            final int centerZ = absoluteHashZ + islandSeparation;
            // Left
            if (relativeX < magicNumber + islandGap * 2) {
                final int centerX = absoluteHashX - islandSeparation / 2;
                result.add(new Location(world, centerX, 0, centerZ));
            }
            // Right
            if (relativeX >= magicNumber + islandGap) {
                final int centerX = absoluteHashX + islandSeparation / 2;
                result.add(new Location(world, centerX, 0, centerZ));
            }
        }

        return result;
    }

    public final boolean isOcean(final Location island) {
        return island == null;
    }

    public final boolean isSpawn(final Location island) {
        return island.getX() == 0 && island.getZ() == 0;
    }

    public final boolean isResource(final Location island, final long worldSeed) {
        if (isSpawn(island)) {
            return false;
        }
        final int x = island.getBlockX();
        final int z = island.getBlockZ();
        if (Math.abs(x) <= islandSeparation && Math.abs(z) <= islandSeparation) {
            // One of the 6 islands adjacent to spawn
            return true;
        }
        return random(x, z, worldSeed) * 100 < resourceIslandRarity;
    }

    public final double random(final int x, final int z, final long worldSeed) {
        final long seed = worldSeed ^ ((((long) z) << 32) | x);
        final Random random = new Random(seed);
        return random.nextDouble();
    }

    public static int ifloordiv(int n, int d) {
        // Credit to Mark Dickinson
        // http://stackoverflow.com/a/10466453
        if (d >= 0)
            return n >= 0 ? n / d : ~(~n / d);
        else
            return n <= 0 ? n / d : (n - 1) / d - 1;
    }

    public static int ifloormod(int n, int d) {
        // Credit to Mark Dickinson
        // http://stackoverflow.com/a/10466453
        if (d >= 0)
            return n >= 0 ? n % d : d + ~(~n % d);
        else
            return n <= 0 ? n % d : d + 1 + (n - 1) % d;
    }
}
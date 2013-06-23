package com.github.hoqhuuep.islandcraft.common.generator;

public class IslandBiomes {
    private final String name;
    private final String ocean;
    private final String shore;
    private final String flats;
    private final String hills;

    public IslandBiomes(final String name, final String ocean, final String shore, final String flats, final String hills) {
        this.name = name;
        this.ocean = ocean;
        if (flats == null) {
            this.flats = ocean;
        } else {
            this.flats = flats;
        }
        if (shore == null) {
            this.shore = this.flats;
        } else {
            this.shore = shore;
        }
        if (hills == null) {
            this.hills = this.flats;
        } else {
            this.hills = hills;
        }
    }

    public String getName() {
        return this.name;
    }

    public String getOcean() {
        return this.ocean;
    }

    public String getShore() {
        return this.shore;
    }

    public String getFlats() {
        return this.flats;
    }

    public String getHills() {
        return this.hills;
    }
}
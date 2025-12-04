package com.ryankshah.skyrimcharacters.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Arrays;
import java.util.List;

public class Race
{
    public static Race ALTMER = new Race(1, "High Elf"); //, createStartingSkillsFromStartingLevels(15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 25, 20, 20, 20, 20, 20));
    public static Race ARGONIAN = new Race(2, "Argonian"); //, createStartingSkillsFromStartingLevels(15, 15, 15, 15, 15, 15, 20, 20, 25, 20, 15, 15, 15, 15, 15, 20, 20, 15));
    public static Race BOSMER = new Race(3, "Wood Elf"); //, createStartingSkillsFromStartingLevels(15, 15, 15, 15, 15, 25, 20, 20, 20, 20, 15, 15, 15, 15, 15, 20, 20, 15));
    public static Race BRETON = new Race(4, "Breton"); //, createStartingSkillsFromStartingLevels(15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 20, 20, 20, 25, 15, 20, 20, 15));
    public static Race DUNMER = new Race(5, "Dark Elf"); //, createStartingSkillsFromStartingLevels(15, 15, 15, 15, 15, 15, 20, 20, 15, 15, 15, 20, 20, 15, 25, 15, 20, 15));
    public static Race IMPERIAL = new Race(6, "Imperial"); //, createStartingSkillsFromStartingLevels(15, 20, 20, 15, 20, 15, 15, 15, 15, 15, 15, 15, 15, 15, 20, 25, 15, 20));
    public static Race KHAJIIT = new Race(7, "Khajiit"); //, createStartingSkillsFromStartingLevels(15, 15, 15, 15, 20, 20, 15, 25, 20, 20, 15, 20, 15, 15, 15, 15, 15, 15));
    public static Race NORD = new Race(8, "Nord"); //, createStartingSkillsFromStartingLevels(20, 15, 20, 25, 20, 15, 20, 15, 15, 15, 20, 15, 15, 15, 15, 15, 15, 15));
    public static Race ORSIMER = new Race(9, "Orc"); //, createStartingSkillsFromStartingLevels(20, 25, 20, 20, 20, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 20));
    public static Race REDGUARD = new Race(10, "Redguard"); //, createStartingSkillsFromStartingLevels(20, 15, 20, 15, 25, 20, 15, 15, 15, 15, 15, 15, 15, 15, 20, 15, 20, 15));

    private int id;
    private String name;

    public static Codec<Race> RACE_CODEC = RecordCodecBuilder.create(race -> race.group(
            Codec.INT.fieldOf("identifier").forGetter(Race::getId),
            Codec.STRING.fieldOf("name").forGetter(Race::getName)
    ).apply(race, Race::new));

    public static StreamCodec<ByteBuf, Race> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            Race::getId,
            ByteBufCodecs.STRING_UTF8,
            Race::getName,
            Race::new
    );

    public Race(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static List<Race> getRaces() {
        return Arrays.asList(ALTMER, ARGONIAN, BOSMER, BRETON, DUNMER, IMPERIAL, KHAJIIT, NORD, ORSIMER, REDGUARD);
    }
}
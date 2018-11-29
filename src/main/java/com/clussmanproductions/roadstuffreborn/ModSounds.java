package com.clussmanproductions.roadstuffreborn;

import com.clussmanproductions.roadstuffreborn.util.LoopableTileEntitySound;

import net.minecraft.client.audio.ISound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class ModSounds {
	public static SoundEvent gateEvent;
	public static SoundEvent safetranType3Event;
	
	public static void initSounds()
	{
		gateEvent = new SoundEvent(new ResourceLocation(ModRoadStuffReborn.MODID + ":gate"));
		safetranType3Event = new SoundEvent(new ResourceLocation(ModRoadStuffReborn.MODID + ":safetran_type_3"));
	}
}

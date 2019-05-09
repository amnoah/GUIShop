package com.pablo67340.guishop.definition;

public enum MobType {
	
	// I get the feeling SilkSpawners is moving away from mob name support. Just in case, 
	// I have prepared a dictionary of names -> id's

	ELDER_GUARDIAN(4), WITHER_SKELETON(5), STRAY(6), HUSK(23), ZOMBIE_VILLAGER(27), SKELETON_HORSE(28),
	ZOMBIE_HORSE(29), DONKEY(31), MULE(32), EVOKER(34), VEX(35), VINDICATOR(36), ILLUSIONER(37), CREEPER(50),
	SKELETON(51), SPIDER(52), GIANT(53), ZOMBIE(54), SLIME(55), GHAST(56), ZOMBIE_PIGMAN(57), ENDERMAN(58),
	CAVE_SPIDER(59), SILVERFISH(60), BLAZE(61), MAGMA_CUBE(62), ENDER_DRAGON(63), WITHER(64), BAT(65), WITCH(66),
	ENDERMITE(67), GUARDIAN(68), SHULKER(69), PIG(90), SHEEP(91), COW(92), CHICKEN(93), SQUID(94), WOLF(95),
	MOOSHROOM(96), SNOWMAN(97), OCELOT(98), VILLAGER_GOLEM(99), HORSE(100), RABBIT(101), POLAR_BEAR(102), PARROT(105),
	VILLAGER(120);
	
	private final int mobId;

	private MobType(Integer id) {
		this.mobId = id;
	}
	
	public int getMobId() {
		return this.mobId;
	}

}

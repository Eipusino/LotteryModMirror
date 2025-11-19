package lottery.copy.content;

import arc.graphics.Color;
import arc.struct.Seq;
import lottery.copy.world.blocks.LotteryBlock;
import lottery.copy.world.blocks.ResBlock;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.world.Block;

import static mindustry.type.ItemStack.with;

public class LBlocks {
	public static LotteryBlock resBag, unitBag, bag, finalLot;

	public static Seq<LotteryBlock> lotteryBlocks = new Seq<>(LotteryBlock.class);

	public static void load() {
		resBag = new LotteryBlock("res-bag") {{
			size = 2;
			health = 1000;
			requirements(Category.effect, with(Items.graphite, 300, Items.silicon, 100));
			pop = new int[]{70, 20, 9, 1};
			popLen = 4;

			cutAmount = 1;
			cut = 30;

			lotteryItem = Items.graphite;
			perLottery = 110;

			alwaysUnlocked = true;
		}};

		unitBag = new LotteryBlock("unit-bag") {{
			size = 3;
			health = 1500;
			requirements(Category.effect, with(Items.silicon, 300, Items.graphite, 100));

			lotteryItem = Items.silicon;
			perLottery = 100;

			pop = new int[]{80, 15, 4, 1};
			popLen = 4;

			cutAmount = 2;
			cut = 60;

			alwaysUnlocked = true;
		}
			@Override
			public void init() {
				super.init();
				for (int i = 0; i < Vars.content.units().size; i++) {
					UnitType u = Vars.content.unit(i);
					if (u != null && u.getFirstRequirements() != null && u.getFirstRequirements().length > 0) {
						if (u.getFirstRequirements().length == 1 && u.getFirstRequirements()[0].item == Items.graphite && u.getFirstRequirements()[0].amount == 1)
							continue;
						if (u.armor <= 3 && u.health <= 180) {
							pops.get(0).add(u);
						} else if (u.armor <= 7 && u.health <= 400) {
							pops.get(1).add(u);
						} else if (u.armor <= 11 && u.health <= 1200) {
							pops.get(2).add(u);
						} else if (u.armor <= 20 && u.health <= 10000) {
							pops.get(3).add(u);
						}
					}
				}
			}
		};

		bag = new LotteryBlock("bag") {{
			size = 3;
			requirements(Category.effect, with(Items.silicon, 250, Items.graphite, 250, Items.thorium, 180));
			pop = new int[]{70, 28, 2};

			colors = new Color[]{
					new Color(0xdddddd),
					new Color(0xdeb0ff),
					new Color(0xfbffb0)
			};
			lotteryItem = Items.silicon;
			perLottery = 150;
			cutAmount = 1;
			health = 1000;

			alwaysUnlocked = true;
		}};
		finalLot = new LotteryBlock("final-lot") {{
			size = 3;
			requirements(Category.effect, with(Items.surgeAlloy, 300, Items.phaseFabric, 300, Items.graphite, 600, Items.silicon, 600));
			health = 2000;

			alwaysUnlocked = true;
		}};

		lotteryBlocks.addAll(resBag, unitBag, bag, finalLot);
	}

	public static void initItemRes() {
		for (Item i : Vars.content.items()) {
			if (i.buildable) {
				new ResBlock(i, 30);
				new ResBlock(i, 200);
				new ResBlock(i, 2000);
			}
		}

		for (int i = 0; i < Vars.content.blocks().size; i++) {
			Block b = Vars.content.block(i);
			ItemStack[] items = b.requirements;

			if (items.length > 2) continue;
			float buildCost = 0;
			if (items.length > 0) {
				buildCost = 0f;
				for (ItemStack stack : items) {
					buildCost += stack.amount * stack.item.cost;
				}
			}

			if (buildCost == 0) continue;
			if (buildCost <= 30) {
				resBag.pops.get(0).add(b);
			} else if (buildCost <= 60) {
				resBag.pops.get(1).add(b);
			} else if (buildCost <= 180) {
				resBag.pops.get(2).add(b);
			} else if (buildCost <= 500) {
				resBag.pops.get(3).add(b);
			}
		}
	}
}

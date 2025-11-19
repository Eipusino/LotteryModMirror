package lottery.copy.world.blocks;

import arc.Core;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.struct.IntMap;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Strings;
import lottery.copy.content.PopAll;
import lottery.copy.net.LCall;
import lottery.copy.ui.ItemDisplay;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.meta.Stat;

import static lottery.copy.content.LUI.beforeLottery;
import static lottery.copy.content.PopAll.canRelease;
import static lottery.copy.content.PopAll.popRandom;
import static mindustry.Vars.net;
import static mindustry.Vars.state;

public class LotteryBlock extends Block {
	//抽卡需要
	public Item lotteryItem = Items.surgeAlloy;
	public int perLottery = 90;

	/** 概率表，最多和{@link #pops}的尺寸对齐 */
	public int[] pop = {50, 24, 15, 8, 2, 1};
	/** 底数 */
	public int under = 100;
	//自定义卡池
	public int popLen = -1;
	public IntMap<Seq<UnlockableContent>> pops;
	//TODO 保底机制
	public int cut = 50;
	public int cutAmount = 2;
	/** 色号 */
	public Color[] colors = {
			new Color(0xdddddd),
			new Color(0xafe2ff),
			new Color(0xdeb0ff),
			new Color(0xfbffb0),
			new Color(0xffe5B0),
			new Color(0xffb0B0)
	};

	public LotteryBlock(String name) {
		super(name);
		solid = false;
		destructible = true;
		update = true;
		configurable = true;
		logicConfigurable = false;
		saveConfig = false;
		copyConfig = false;

		config(Object[].class, (LotteryBuild b, Object[] os) -> b.cg = os);
	}

	@Override
	public void init() {
		super.init();

		//lotteryRes = new LotteryRes();

		if (popLen > 0) {
			pops = new IntMap<>();
			for (int i = 0; i < popLen; i++) {
				pops.put(i, new Seq<>(UnlockableContent.class));
			}
		}

		if (pops == null || pops.size == 0) pops = PopAll.popMap;
	}

	public void reInit(int under, int[] pop) {
		this.under = under;
		this.pop = pop;
		reStat();
	}

	public void reStat() {
		stats.remove(Stat.abilities);
		stats.add(Stat.abilities, table -> {
			table.row();
			table.table(img -> img.add(Core.bundle.get("stat.lottery.p-p"))).pad(20);
			table.row();
			for (int i = 0; i < pop.length; i++) {
				table.row();
				float p = pop[i];
				Seq<UnlockableContent> m = pops.get(i);
				int j = i;
				table.table(t -> t.add("[accent]概率: []" + Strings.autoFixed((p / under) * 100, 3) + " %" + "(" + "level " + (j + 1) + ")").left()).left().pad(8);
				table.row();
				table.table(all -> {
					for (int k = 0; k < m.size; k++) {
						UnlockableContent u = m.get(k);
						all.table(s -> {
							s.add(new Image(u.uiIcon)).size(20).left().pad(2);
							s.row();
							s.add(u.localizedName).left().pad(2);
						}).left().pad(3);

						if ((k + 1) % 6 == 0) all.row();
					}
				}).padBottom(10).left();
			}
		});
	}

	@Override
	public void setStats() {
		super.setStats();

		reStat();
	}

	@Override
	protected void initBuilding() {
		if (buildType == null) buildType = LotteryBuild::new;
	}

	public class LotteryBuild extends Building {
		public int howMany = 0;
		public Seq<UnlockableContent> us = new Seq<>(UnlockableContent.class);
		public Seq<Color> uc = new Seq<>(Color.class);
		public IntSeq tier = new IntSeq();
		public Object[] cg = null;


		@Override
		public void buildConfiguration(Table table) {
			table.button(l -> {
				l.add(new ItemDisplay(lotteryItem, perLottery)).left();
				l.row();
				l.add(Core.bundle.get("stat.lottery.single-draws"));
			}, Styles.cleari, this::oneLottery).width(150);
			table.button(r -> {
				r.add(new ItemDisplay(lotteryItem, perLottery * 10)).left();
				r.row();
				r.add(Core.bundle.get("stat.lottery.ten-draws"));
			}, Styles.cleari, this::tenLottery).width(150);
		}

		public void lottery() {
			if (pops == null || pop.length > pops.size) return;
			howMany++;

			int cutOver = Math.max(0, howMany - cut);
			int cutChance = cutAmount;
			int r = Mathf.random(1, under);
			int max = under;
			for (int p = pop.length - 1; p >= 0; p--) {
				max -= pop[p];
				if (cutChance > 0) {
					cutChance--;
					max -= (pop[p] * cutOver);
				}
				if (r > max) {
					us.add(popRandom(pops.get(p)));
					uc.add(colors[p]);
					tier.add(p);
					if (pop.length - p <= cutAmount) howMany = 0;
					break;
				}
			}
		}

		public boolean coreCanConsume(int amount) {
			if (core() == null) return false;
			if (state.rules.infiniteResources || cheating()) return true;
			return core().items.get(lotteryItem) >= amount;
		}

		public void consumeLottery(int amount) {
			if (core() == null || state.rules.infiniteResources || cheating()) return;
			LCall.removeStack(core(), lotteryItem, amount);
		}

		public void oneLottery() {
			if (coreCanConsume(perLottery)) {
				consumeLottery(perLottery);

				us.clear();
				uc.clear();
				tier.clear();

				lottery();

				beforeLottery.show(us, uc, tier, this);
			}
		}

		public void tenLottery() {
			if (coreCanConsume(perLottery * 10)) {
				consumeLottery(perLottery * 10);

				us.clear();
				uc.clear();
				tier.clear();

				for (int i = 0; i < 10; i++) lottery();

				beforeLottery.show(us, uc, tier, this);
			}
		}

		public void release() {
			if (cg.length <= 0 || core() == null) return;
			for (Object o : cg) {
				if (o instanceof Block b) {
					ItemStack[] item = b.requirements;

					for (ItemStack stack : item) {
						//if(net.server() || !net.active()){
						core().handleStack(stack.item, (int) (stack.amount * Vars.state.rules.buildCostMultiplier), this);
						//}
					}
				} else if (o instanceof UnitType u) {
					if (canRelease(u, this)) {
						ItemStack[] item = u.getFirstRequirements();

						for (ItemStack stack : item) {
							//if(net.server() || !net.active()){
							core().handleStack(stack.item, (int) (stack.amount * Vars.state.rules.unitCostMultiplier), this);
							//}
						}
					} else {
						if (!net.client()) {
							Unit unit = u.create(team);

							unit.set(x + Mathf.range(0.001f), y + Mathf.range(0.001f));
							unit.rotation = 90f;
							unit.add();
						}
					}
				}
			}
		}

		@Override
		public void updateTile() {
			if (cg != null) {
				release();
				cg = null;
			}
		}

		@Override
		public Object[] config() {
			return us.items;
		}
	}
}

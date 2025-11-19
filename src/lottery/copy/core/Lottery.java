package lottery.copy.core;

import arc.Core;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.scene.ui.Image;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Strings;
import lottery.copy.content.LBlocks;
import lottery.copy.content.LUI;
import lottery.copy.content.PopAll;
import lottery.copy.net.LCall;
import lottery.copy.ui.LotteryRes;
import mindustry.Vars;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Icon;
import mindustry.mod.Mod;
import mindustry.mod.Mods;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

public class Lottery extends Mod {
	public static final String MOD_NAME = "lottery-copy";

	public static boolean onlyPlugIn;

	private static final Seq<UnlockableContent> us = new Seq<>(UnlockableContent.class);
	private static final Seq<Color> uc = new Seq<>(Color.class);
	private static final IntSeq tier = new IntSeq();

	private static final int[] pop = {50, 24, 15, 8, 2, 1};

	private static final Color[] colors = {
			new Color(0xdddddd),
			new Color(0xafe2ff),
			new Color(0xdeb0ff),
			new Color(0xfbffb0),
			new Color(0xffe5b0),
			new Color(0xffb0b0)
	};

	public Lottery() {}

	@Override
	public void init() {
		onlyPlugIn = Core.settings.getBool("lottery-copy-plug-in-mode");
		Core.settings.defaults("lottery-copy-plug-in-mode", false);

		Mods.LoadedMod mod = Vars.mods.locateMod(MOD_NAME);

		if (mod != null) {
			mod.meta.hidden = onlyPlugIn;

			if (onlyPlugIn) {
				mod.meta.displayName = mod.meta.displayName + "-Plug-In";
				mod.meta.version = Vars.mods.locateMod(MOD_NAME).meta.version + "-plug-in";
			}
		}

		if (!onlyPlugIn) {
			LUI.init();

			LCall.registerPackets();
			LBlocks.initItemRes();
		}
		PopAll.init();

		if (Vars.ui != null) {
			Vars.ui.settings.addCategory(Core.bundle.get("lottery-copy-settings"), "lottery-copy-bag", settingsTable -> {
				settingsTable.checkPref("lottery-copy-plug-in-mode", false);

				settingsTable.row();
				settingsTable.add("[accent]-----------------------[]" + Core.bundle.get("lottery-copy-settings") + "[accent]-----------------------[]");
				settingsTable.row();
				settingsTable.button(Core.bundle.get("stat.lottery-copy.single-draws"), Styles.defaultt, Lottery::oneLottery).margin(14).width(200f).pad(6);
				settingsTable.row();
				settingsTable.button(Core.bundle.get("stat.lottery-copy.ten-draws"), Styles.defaultt, Lottery::tenLottery).margin(14).width(200f).pad(6);
				settingsTable.row();
				settingsTable.button(b -> b.add(new Image(Icon.file)),
						Styles.cleari, Lottery::showPop).margin(6).pad(6).tooltip(Core.bundle.get("stat.lottery-copy.p-p"));
				settingsTable.row();

				if (onlyPlugIn) return;
				settingsTable.row();
//                settingsTable.table(Styles.grayPanel, m -> {
//                    int i = 0;
//                    for(var b : LBlocks.mains){
//                        if(b instanceof LotteryBlock mb)
//                        m.button(bt -> {
//                            bt.add(new Image(b.uiIcon)).size(48);
//                        }, Styles.cleari, () -> mdfUI.show(mb)).pad(8);
//                        i ++;
//                        if(i % 4 == 0) m.row();
//                    }
//                }).pad(10).margin(5);
			});
		}
	}

	public static void showPop() {
		BaseDialog dialog = new BaseDialog("All! All! All!") {{
			cont.pane(table -> {
				table.row();
				table.table(img -> img.add(Core.bundle.get("stat.lottery-copy.p-p"))).pad(20);
				table.row();
				for (int i = 0; i < pop.length; i++) {
					table.row();
					float p = pop[i];
					var m = PopAll.popMap.get(i);
					int finalI = i;
					table.table(t -> t.add("[accent]概率: []" + Strings.autoFixed((p / 100) * 100, 3) + " %" + "(" + "level " + (finalI + 1) + ")").left()).left().pad(8);
					table.row();
					table.table(all -> {
						for (int k = 0; k < m.size; k++) {
							var u = m.get(k);
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
			addCloseButton(210);
		}};
		dialog.show();
	}

	@Override
	public void loadContent() {
		if (!onlyPlugIn) {
			LBlocks.load();
		}
	}

	private static void lottery() {
		int r = Mathf.random(1, 100);
		int max = 100;
		for (int p = pop.length - 1; p >= 0; p--) {
			max -= pop[p];
			if (r > max) {
				us.add(PopAll.popRandom(PopAll.popMap.get(p)));
				uc.add(colors[p]);
				tier.add(p);
				break;
			}
		}
	}

	public static void oneLottery() {
		us.clear();
		uc.clear();
		tier.clear();

		lottery();

		new LotteryRes().show(us, uc, tier, null);
	}

	public static void tenLottery() {
		us.clear();
		uc.clear();
		tier.clear();

		for (int i = 0; i < 10; i++) lottery();

		new LotteryRes().show(us, uc, tier, null);
	}
}

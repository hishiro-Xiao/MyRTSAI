package model.decreasing;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import DataCollection.Combat;
import DataCollection.CombatList;
import rts.units.Unit;
import weka.core.Summarizable;

/*
 * effectiveDPF定义为:
 * 			worker	light	ranged	heavy	base	barracks
 * worker	float	float	float	float	float	float
 * light	...		...		...		...		...		...	
 * ranged	...		...		...		...		...		...
 * heavy	...		...		...		...		...		...
 * 
 * effectiveDPF[i][j]表示单位i攻击单位j所需要的DPF(Damage per frame)，我觉得就是平均所需帧数（平均所需时间）
  *  所以effectiveDPF的大小为effectiveDPF[4][6]
 * */

public class Decresing {

	private List<Combat> Combats = new LinkedList<Combat>();
	private float[][] effectiveDPF = new float[4][6];
	private float[][] damageToType = new float[4][6];
	private float[][] timeAttackingType = new float[4][6];
	private HashMap<String, Integer> targetSelection = new HashMap<String, Integer>();

	public static final int WORKER = 0;
	public static final int LIGHT = 1;
	public static final int RANGED = 2;
	public static final int HEAVY = 3;
	public static final int BASE = 4;
	public static final int BARRACKS = 5;

	public static HashMap<String, Integer> TYPE = new HashMap<String, Integer>();

	public Decresing() {

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 6; j++) {
				effectiveDPF[i][j] = 0;
				damageToType[i][j] = 0;
				timeAttackingType[i][j] = 0;
			}
		}

		TYPE.put("WORKER", WORKER);
		TYPE.put("LIGHT", LIGHT);
		TYPE.put("RANGED", RANGED);
		TYPE.put("HEAVY", HEAVY);
		TYPE.put("BASE", BASE);
		TYPE.put("BARRACKS", BARRACKS);

		targetSelection.put("WORKER", 0);
		targetSelection.put("LIGHT", 0);
		targetSelection.put("RANGED", 0);
		targetSelection.put("HEAVY", 0);
		targetSelection.put("BASE", 0);
		targetSelection.put("BARRACKS", 0);
	}

	public HashMap<String, Integer> getTargetSelection() {
		return targetSelection;
	}

	public void setTargetSelection(HashMap<String, Integer> targetSelection) {
		this.targetSelection = targetSelection;
	}

	public float[][] getEffectiveDPF() {
		return effectiveDPF;
	}

	public void setEffectiveDPF(float[][] effectiveDPF) {
		this.effectiveDPF = effectiveDPF;
	}

	public float[][] getDamageToType() {
		return damageToType;
	}

	public void setDamageToType(float[][] damageToType) {
		this.damageToType = damageToType;
	}

	public float[][] getTimeAttackingType() {
		return timeAttackingType;
	}

	public void setTimeAttackingType(float[][] timeAttackingType) {
		this.timeAttackingType = timeAttackingType;
	}

	// 如何计算effectiveDPF矩阵（略微修改）
	// 第一步：每一个Combat对象，计算可以攻击空中单位、可以攻击水中单位、可以攻击陆地单位的单位数量，
	// 因为microRTS没有这么多单位类型，所以这一步只计算Combat中的可攻击单位的数量u_canattack即可
	// 第二步，根据killedUnits中的每一个被杀死的单位u，计算杀死这个单位所需的攻击力d_total，在此，即为u的hitpoint
	// 然后计算d_split = d_total / u_canattack
	// 对于u_canattack中的每一个单位，计算两个值
	// damageToType(u_canattack_i, u) += d_split
	// timeAttackingType(u_attack_i, u) += t_i-1 - t_i
	// 第三步，DPF(i, j) = damageToType(i, j) / timeAttackingType(i, j)
	public void trainEffectiveDPF() {
		if (this.Combats.size() <= 0)
			return;

		for (Combat c : this.Combats) {

			if (c.getKilledUnits().size() <= 0)
				continue; // 如果一场Combat没有单位死亡，那就无法计算了，也无需计算

			List<Unit> canAttack = new LinkedList<Unit>();
			for (Unit u : c.getInvolved_ALL()) {
				if (u.getType().canAttack)
					canAttack.add(u);
			}

			List<Unit> killedUnits = new LinkedList<Unit>();
			// 此处按照被杀时间的顺序放入List中，方便后面处理
			for (Unit u : c.getKilledUnits().keySet()) {
				if (killedUnits.size() == 0)
					killedUnits.add(u);
				else {
					int pos = killedUnits.size() - 1;
					while (pos >= 0 && c.getKilledUnits().get(killedUnits.get(pos)) > c.getKilledUnits().get(u)) {
						pos--;
					}
					killedUnits.add(pos + 1, u);
				}
			}

			for (int i = 0; i < killedUnits.size(); i++) {
				float d = killedUnits.get(i).getType().hp;
				float d_split = d / canAttack.size();

				int killed_time = c.getKilledUnits().get(killedUnits.get(i));
				int last_killed_time = 0;

				if (i - 1 >= 0)
					last_killed_time = c.getKilledUnits().get(killedUnits.get(i - 1));

				for (Unit u : canAttack) {
					String type = u.getType().name.toUpperCase();
					String killed_type = killedUnits.get(i).getType().name.toUpperCase();
					this.damageToType[TYPE.get(type)][TYPE.get(killed_type)] += d_split;
//					this.timeAttackingType[TYPE.get(type)][TYPE.get(killed_type)] += killed_time - last_killed_time;
					this.timeAttackingType[TYPE.get(type)][TYPE.get(killed_type)] += killed_time - c.getStart_time();
				}
			}

		}

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 6; j++) {
				if (this.timeAttackingType[i][j] == 0)
					this.effectiveDPF[i][j] = this.damageToType[i][j] / 1;
				else
					this.effectiveDPF[i][j] = this.damageToType[i][j] / this.timeAttackingType[i][j];
			}
		}

	}

	// 训练targetSelection参数
	// 采用波达计数法，对于每一场有单位被杀死的Combat，统计参与单位的数量n，然后按照被杀死单位的时间“投票计数”，
	// 每个被杀死的单位获得的“投票数”为n-i，i为单位被杀死的顺序
	public void trainTargetSelection() {
		for (Combat c : this.Combats) {
			if (c.getKilledUnits().isEmpty())
				continue;

			// 统计Combat中有多少种单位参与
			HashMap<String, Integer> num_units = new HashMap<String, Integer>();
			for (Unit u : c.getInvolved_ALL()) {
				num_units.put(u.getType().name, 1);
			}
			int n = num_units.keySet().size();

			// 按照被杀时间的顺序放入List中，方便后面处理
			List<Unit> killedUnits = new LinkedList<Unit>();
			for (Unit u : c.getKilledUnits().keySet()) {
				if (killedUnits.size() == 0)
					killedUnits.add(u);
				else {
					int pos = killedUnits.size() - 1;
					while (pos >= 0 && c.getKilledUnits().get(killedUnits.get(pos)) > c.getKilledUnits().get(u)) {
						pos--;
					}
					killedUnits.add(pos + 1, u);
				}
			}

			for (int i = 0; i < killedUnits.size(); i++) {
				Unit killedUnit = killedUnits.get(i);
				String name = killedUnit.getType().name.toUpperCase();
				int old_value = this.targetSelection.get(name);
				this.targetSelection.put(name, old_value + n - i);
			}

		}
	}

	// 按照targetSelection对enemy的单位进行排序
	public List<Unit> sortByTargetSelection(List<Unit> enemy) {
		// 将hashmap转换成从大到小的数组
		int[] order = new int[6];
		int max = -1;
		String max_type = "";
		for (String str : this.targetSelection.keySet()) {
			if (this.targetSelection.get(str) > max) {
				max = this.targetSelection.get(str);
				max_type = str;
			}
		}
		order[TYPE.get(max_type)] = 6;

		for (int i = 5; i > 0; i--) {
			int tmp_max = max;
			max = 0;
			for (String str : this.targetSelection.keySet()) {
				if (this.targetSelection.get(str) >= max && this.targetSelection.get(str) < tmp_max) {
					max = this.targetSelection.get(str);
					max_type = str;
				}
			}
			order[TYPE.get(max_type)] = i;
		}

//		System.out.println(Arrays.toString(order));

		// 统计enemy的各个单位数量
		int[] num_enemy = { 0, 0, 0, 0, 0, 0 };
		for (Unit u : enemy) {
			num_enemy[TYPE.get(u.getType().name.toUpperCase())]++;
		}

		// 记录每种单位的存储首地址
		int last = 0;
		int restore_order[] = { -1, -1, -1, -1, -1, -1 };
		for (int i = 0; i < 6; i++) {
			if (order[i] == 6) {
				last = i;
				if (num_enemy[i] != 0)
					restore_order[i] = 0;
				break;
			}
		}
		for (int index = 5; index > 0; index--) {
			for (int i = 0; i < 6; i++) {
				if (order[i] == index) {
					if (num_enemy[i] != 0) {
						restore_order[i] = restore_order[last] + num_enemy[last];
						last = i;
						break;
					}
				}
			}
		}

//		System.out.println(Arrays.toString(restore_order));
//		System.out.println(Arrays.toString(num_enemy));

		// 将单位按照targetSelection给出的优先级进行排序
		List<Unit> orderedUnits = new LinkedList<Unit>();
		orderedUnits.addAll(enemy);
		for (Unit u : enemy) {
			int insert_pos = restore_order[TYPE.get(u.getType().name.toUpperCase())];
			orderedUnits.remove(insert_pos);
			orderedUnits.add(insert_pos, u);
			restore_order[TYPE.get(u.getType().name.toUpperCase())]++;
		}

		return orderedUnits;
	}

	// enemy杀死敌方单位u所需时间(frames)
	// 这个时间要怎么算我在论文里找了半天都没有找到，我猜测的计算方式有以下几种：
	// 1. 让enemy中的第一个单位去攻击u，但是在某些情况下，会出现双方都有可攻击单位剩余，但是预测无法继续推进的情况（无法预测谁胜谁负）
	// 2.让enemy中的所有单位去攻击u，计算所需时间的总和/平均值
	// 暂时采用第二种的计算总和方法，后续优化时再尝试其他情况
	public int timeToKillUnit(Unit u, List<Unit> enemy) {

		// 如果enemy只剩建筑单位，则攻击时间为无穷大（-1来表示）
		boolean contains_melee_units = false;
		for (Unit unit : enemy) {
			if (unit.getType().canAttack) {
				contains_melee_units = true;
				break;
			}
		}
		if(!contains_melee_units) return -1;

		float u_hp = u.getHitPoints();
		String u_name = u.getType().name.toUpperCase();

		int total_time = 0;
		for (Unit attack_unit : enemy) {
			if (attack_unit.getType().canAttack) {
				String attack_name = attack_unit.getType().name.toUpperCase();
				int needed_frames = (int) (u_hp / this.getEffectiveDPF()[TYPE.get(attack_name)][TYPE.get(u_name)]);
//				System.out.println(needed_frames);
				total_time += needed_frames;
			}
		}
//		System.out.println(total_time / enemy.size());

		return total_time / enemy.size();
	}

	// 根据游戏的初始状态给出预测，预测会赢的一方
	public int predict(List<Unit> enemy1, List<Unit> enemy2) {
		List<Unit> sorted_enemy1 = this.sortByTargetSelection(enemy1);
		List<Unit> sorted_enemy2 = this.sortByTargetSelection(enemy2);

		int i = 0, j = 0; // i为enemy1的index，j为enemy2的index
		while (true) {
			int time_to_kill_enmey2_unit = this.timeToKillUnit(sorted_enemy2.get(j), enemy1);
			int time_to_kill_enmey1_unit = this.timeToKillUnit(sorted_enemy1.get(i), enemy2);

			while(time_to_kill_enmey2_unit == -1 && j < sorted_enemy2.size()) {
				j++;
				time_to_kill_enmey2_unit = this.timeToKillUnit(sorted_enemy2.get(j), enemy1);
			}
			while(time_to_kill_enmey2_unit == -1 && i < sorted_enemy1.size()) {
				i++;
				time_to_kill_enmey1_unit = this.timeToKillUnit(sorted_enemy1.get(i), enemy2);
			}
			
			if(time_to_kill_enmey1_unit == -1 && time_to_kill_enmey2_unit == -1)
				break;
			//平局
			if(time_to_kill_enmey1_unit == time_to_kill_enmey2_unit) {
				sorted_enemy1.remove(i);
				sorted_enemy2.remove(j);
			}
			else {
				//enemy1赢
				if(time_to_kill_enmey2_unit < time_to_kill_enmey1_unit) {
					Unit killedUnit = sorted_enemy2.get(j);
					
					float total_damage = 0;
					for(int index = 0; index < 4;index++) {
						total_damage += this.effectiveDPF[index][TYPE.get(killedUnit.getType().name.toUpperCase())];
					}
					float avg_damage_to_kill = total_damage / 4;
					
					int hp_loss = (int) (avg_damage_to_kill * time_to_kill_enmey2_unit);
					if(sorted_enemy1.get(i).getHitPoints() - hp_loss > 0)
						sorted_enemy1.get(i).setHitPoints(sorted_enemy1.get(i).getHitPoints());
					else
						sorted_enemy1.remove(i);
					
					sorted_enemy2.remove(j);
				}
				else {
					Unit killedUnit = sorted_enemy1.get(i);
					
					float total_damage = 0;
					for(int index = 0; index < 4;index++) {
						total_damage += this.effectiveDPF[index][TYPE.get(killedUnit.getType().name.toUpperCase())];
					}
					float avg_damage_to_kill = total_damage / 4;
					
					int hp_loss = (int) (avg_damage_to_kill * time_to_kill_enmey2_unit);
					if(sorted_enemy2.get(j).getHitPoints() - hp_loss > 0)
						sorted_enemy2.get(j).setHitPoints(sorted_enemy2.get(j).getHitPoints());
					else
						sorted_enemy2.remove(j);
					
					sorted_enemy1.remove(i);
				}
			}
			
			if(i >= sorted_enemy1.size() || j >= sorted_enemy2.size())
				break;
		}
		
		return sorted_enemy1.size() - sorted_enemy2.size();
	}

	// 打印出所有的参数
	public void printReadableArray() {

		System.out.println("DamageToType: ");
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 6; j++) {
				System.out.print(this.damageToType[i][j] + "\t");
			}
			System.out.println();
		}
		System.out.println();

		System.out.println("TimeAttackingType: ");
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 6; j++) {
				System.out.print(this.timeAttackingType[i][j] + "\t");
			}
			System.out.println();
		}
		System.out.println();

		System.out.println("EffectiveDPF: ");
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 6; j++) {
				System.out.print(this.effectiveDPF[i][j] + "\t");
			}
			System.out.println();
		}
		System.out.println();

		System.out.println("TargetSelection: ");
		for (String type : this.targetSelection.keySet()) {
			System.out.println(type + " : " + this.targetSelection.get(type));
		}
		System.out.println();
	}

	// 从目录下的所有json文件中读取战斗数据
	public void readDataSets() throws IOException {

		String filePath = "./dataset/8x8/";
		File file = new File(filePath);

		File[] files = file.listFiles();

		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {

				String filename = files[i].getName();
				int len = filename.length();
				String suffix = filename.substring(len - 4, len); // 获取文件的后缀，判断当前文件是否是json文件

				if (suffix.equals("json") && !filename.equals("Decresing.json")) {
					String content = new String(Files.readAllBytes(Paths.get(files[i].getAbsolutePath())));

					CombatList combats = JSON.parseObject(content, CombatList.class);
					if (combats != null)
						this.Combats.addAll(combats.getCombats());
				}
			}
		}
	}

	// 将训练好的参数保存到JSON文件中
	public void saveParameters(Decresing dec) throws IOException {
		String filePath = "./dataset/8x8/Decresing.json";
		File f = new File(filePath);

		synchronized (f) {
			FileWriter fw = new FileWriter(f);
			String json = JSON.toJSONString(dec);
			fw.write(json);
			fw.close();
		}
	}

	// 从文件中读取训练好的参数
	public boolean getParameters() throws IOException {
		String filePath = "./dataset/8x8/Decresing.json";
		File f = new File(filePath);
		if (!f.exists())
			return false;

		synchronized (f) {
			String content = new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())));
			Decresing dec = JSON.parseObject(content, Decresing.class);
			if (dec != null) {
				this.setDamageToType(dec.getDamageToType());
				this.setTimeAttackingType(dec.getTimeAttackingType());
				this.setEffectiveDPF(dec.getEffectiveDPF());
				this.setTargetSelection(dec.getTargetSelection());
			}
		}
		return true;
	}

	public static void main(String[] args) throws IOException {
		Decresing dec = new Decresing();
		dec.readDataSets();
//		dec.trainEffectiveDPF();
//		dec.trainTargetSelection();
//		dec.saveParameters(dec);

		dec.getParameters();
		dec.printReadableArray();

		List<Unit> test = dec.Combats.get(150).getInvolved_ALL();
		List<Unit> test2 = dec.Combats.get(0).getInvolved_ALL();
		
		System.out.println(test.toString());
		List<Unit> ordered = dec.sortByTargetSelection(test);
		System.out.println(ordered);
		System.out.println(test2.toString());
		List<Unit> ordered2 = dec.sortByTargetSelection(test2);
		System.out.println(ordered2);

//		dec.predict(test, test2);
	}
}

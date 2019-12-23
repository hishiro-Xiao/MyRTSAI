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
 * effectiveDPF����Ϊ:
 * 			worker	light	ranged	heavy	base	barracks
 * worker	float	float	float	float	float	float
 * light	...		...		...		...		...		...	
 * ranged	...		...		...		...		...		...
 * heavy	...		...		...		...		...		...
 * 
 * effectiveDPF[i][j]��ʾ��λi������λj����Ҫ��DPF(Damage per frame)���Ҿ��þ���ƽ������֡����ƽ������ʱ�䣩
  *  ����effectiveDPF�Ĵ�СΪeffectiveDPF[4][6]
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

	// ��μ���effectiveDPF������΢�޸ģ�
	// ��һ����ÿһ��Combat���󣬼�����Թ������е�λ�����Թ���ˮ�е�λ�����Թ���½�ص�λ�ĵ�λ������
	// ��ΪmicroRTSû����ô�൥λ���ͣ�������һ��ֻ����Combat�еĿɹ�����λ������u_canattack����
	// �ڶ���������killedUnits�е�ÿһ����ɱ���ĵ�λu������ɱ�������λ����Ĺ�����d_total���ڴˣ���Ϊu��hitpoint
	// Ȼ�����d_split = d_total / u_canattack
	// ����u_canattack�е�ÿһ����λ����������ֵ
	// damageToType(u_canattack_i, u) += d_split
	// timeAttackingType(u_attack_i, u) += t_i-1 - t_i
	// ��������DPF(i, j) = damageToType(i, j) / timeAttackingType(i, j)
	public void trainEffectiveDPF() {
		if (this.Combats.size() <= 0)
			return;

		for (Combat c : this.Combats) {

			if (c.getKilledUnits().size() <= 0)
				continue; // ���һ��Combatû�е�λ�������Ǿ��޷������ˣ�Ҳ�������

			List<Unit> canAttack = new LinkedList<Unit>();
			for (Unit u : c.getInvolved_ALL()) {
				if (u.getType().canAttack)
					canAttack.add(u);
			}

			List<Unit> killedUnits = new LinkedList<Unit>();
			// �˴����ձ�ɱʱ���˳�����List�У�������洦��
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

	// ѵ��targetSelection����
	// ���ò��������������ÿһ���е�λ��ɱ����Combat��ͳ�Ʋ��뵥λ������n��Ȼ���ձ�ɱ����λ��ʱ�䡰ͶƱ��������
	// ÿ����ɱ���ĵ�λ��õġ�ͶƱ����Ϊn-i��iΪ��λ��ɱ����˳��
	public void trainTargetSelection() {
		for (Combat c : this.Combats) {
			if (c.getKilledUnits().isEmpty())
				continue;

			// ͳ��Combat���ж����ֵ�λ����
			HashMap<String, Integer> num_units = new HashMap<String, Integer>();
			for (Unit u : c.getInvolved_ALL()) {
				num_units.put(u.getType().name, 1);
			}
			int n = num_units.keySet().size();

			// ���ձ�ɱʱ���˳�����List�У�������洦��
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

	// ����targetSelection��enemy�ĵ�λ��������
	public List<Unit> sortByTargetSelection(List<Unit> enemy) {
		// ��hashmapת���ɴӴ�С������
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

		// ͳ��enemy�ĸ�����λ����
		int[] num_enemy = { 0, 0, 0, 0, 0, 0 };
		for (Unit u : enemy) {
			num_enemy[TYPE.get(u.getType().name.toUpperCase())]++;
		}

		// ��¼ÿ�ֵ�λ�Ĵ洢�׵�ַ
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

		// ����λ����targetSelection���������ȼ���������
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

	// enemyɱ���з���λu����ʱ��(frames)
	// ���ʱ��Ҫ��ô���������������˰��춼û���ҵ����Ҳ²�ļ��㷽ʽ�����¼��֣�
	// 1. ��enemy�еĵ�һ����λȥ����u��������ĳЩ����£������˫�����пɹ�����λʣ�࣬����Ԥ���޷������ƽ���������޷�Ԥ��˭ʤ˭����
	// 2.��enemy�е����е�λȥ����u����������ʱ����ܺ�/ƽ��ֵ
	// ��ʱ���õڶ��ֵļ����ܺͷ����������Ż�ʱ�ٳ����������
	public int timeToKillUnit(Unit u, List<Unit> enemy) {

		// ���enemyֻʣ������λ���򹥻�ʱ��Ϊ�����-1����ʾ��
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

	// ������Ϸ�ĳ�ʼ״̬����Ԥ�⣬Ԥ���Ӯ��һ��
	public int predict(List<Unit> enemy1, List<Unit> enemy2) {
		List<Unit> sorted_enemy1 = this.sortByTargetSelection(enemy1);
		List<Unit> sorted_enemy2 = this.sortByTargetSelection(enemy2);

		int i = 0, j = 0; // iΪenemy1��index��jΪenemy2��index
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
			//ƽ��
			if(time_to_kill_enmey1_unit == time_to_kill_enmey2_unit) {
				sorted_enemy1.remove(i);
				sorted_enemy2.remove(j);
			}
			else {
				//enemy1Ӯ
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

	// ��ӡ�����еĲ���
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

	// ��Ŀ¼�µ�����json�ļ��ж�ȡս������
	public void readDataSets() throws IOException {

		String filePath = "./dataset/8x8/";
		File file = new File(filePath);

		File[] files = file.listFiles();

		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {

				String filename = files[i].getName();
				int len = filename.length();
				String suffix = filename.substring(len - 4, len); // ��ȡ�ļ��ĺ�׺���жϵ�ǰ�ļ��Ƿ���json�ļ�

				if (suffix.equals("json") && !filename.equals("Decresing.json")) {
					String content = new String(Files.readAllBytes(Paths.get(files[i].getAbsolutePath())));

					CombatList combats = JSON.parseObject(content, CombatList.class);
					if (combats != null)
						this.Combats.addAll(combats.getCombats());
				}
			}
		}
	}

	// ��ѵ���õĲ������浽JSON�ļ���
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

	// ���ļ��ж�ȡѵ���õĲ���
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

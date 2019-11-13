package DataCollection;

import java.util.LinkedList;
import java.util.List;

import rts.GameState;
import rts.PhysicalGameState;
import rts.units.Unit;

public class CombatFrame implements java.io.Serializable{

	// 这一帧开始时间
	private int frame_time = 0;
	// 这一帧包含的所有单位
	private List<Unit> units = new LinkedList<Unit>();
	// 这一帧所有的Combat
	private List<Combat> Combats = new LinkedList<Combat>();

	public CombatFrame(GameState gs) {
		this.frame_time = gs.getTime();
		this.units = gs.getPhysicalGameState().getUnits();
		getCombatOfFrame(gs);
	}

	public List<Combat> getCombats() {
		return this.Combats;
	}

	public List<Combat> getCombatOfFrame(GameState gs) {
		// 说明：本class只记录一帧的combat信息，因此只能记录combat开始时的参与单位，
		// 其他信息，如combat何时结束，在所有游戏帧获取后，在DataCollection类中处理分析获得
		if (gs == null)
			return null;
		for (Unit u : this.units) {
			List<Unit> unitsInRange = getUnitsInRange(u, gs);
			if (unitsInRange != null) {
				Combat combat = new Combat();
				combat.setStart_time(this.frame_time);
				combat.setEnd_time(this.frame_time + 1);	//第一次假设所有的Combat都只持续一帧

				List<Unit> inRange_A = new LinkedList<Unit>();
				List<Unit> inRange_B = new LinkedList<Unit>();
				for (Unit unit : unitsInRange) {
					if (unit.getPlayer() == 0) {
						inRange_A.add(unit);
					} else {
						inRange_B.add(unit);
					}
				}
				combat.setInvolved_A(inRange_A);
				combat.setInvolved_B(inRange_B);

				List<Unit> involved_all = new LinkedList<Unit>();
				involved_all.addAll(inRange_A);
				involved_all.addAll(inRange_B);
				combat.setInvolved_ALL(involved_all);
				
				//防止重复加入Combat（如果一个Combat的参与单位是另一个Combat参与单位的子集，则算作同一个Combat）
				int subset = 0;
				for(int i = 0;i < this.Combats.size();i++) {
					Combat c = this.Combats.get(i);
					if(c.getInvolved_ALL().containsAll(combat.getInvolved_ALL())) {
						combat = null;
						break;
					}
					else if(combat.getInvolved_ALL().containsAll(c.getInvolved_ALL())){
						if(subset == 0) {
							this.Combats.get(i).setInvolved_ALL(combat.getInvolved_ALL());
							this.Combats.get(i).setInvolved_A(combat.getInvolved_A());
							this.Combats.get(i).setInvolved_B(combat.getInvolved_B());
							subset++;
						}
						else {
							this.Combats.remove(i);
						}
					}
				}
				
				if(combat != null && subset == 0) this.Combats.add(combat);
			}
		}
		
		return this.Combats;
	}

	public int getDistanceBetweenUnits(Unit u1, Unit u2) {
		return Math.abs(u1.getX() - u2.getX()) + Math.abs(u1.getY() - u2.getY());
	}

	// 获取单位u的攻击范围内的单位（代码可以简化）
	public List<Unit> getUnitsInRange(Unit u, GameState gs) {

		/*
		 * 根据单位的攻击范围确定D 设定A为player1在D中的单位，B为player2在D中的单位
		 * D中的所有单位为本次combat的参与单位，不管该单位是否有攻击其他单位） 如果没有攻击行为，则判定为这次combat的passive单位
		 */
		List<Unit> inRange = new LinkedList<Unit>();
		List<Unit> notInRange = new LinkedList<Unit>();
		PhysicalGameState pgs = gs.getPhysicalGameState();

		// 第一次遍历，获取u的攻击范围内的单位
		// 此时只查看地方单位是否在攻击范围内
		for (Unit unit : pgs.getUnits()) {
			if (!unit.getType().isResource) {
				if (getDistanceBetweenUnits(u, unit) <= u.getAttackRange()) {
					inRange.add(unit);
				} else {
					notInRange.add(unit);
				}
			}
		}

		if (inRange.size() == 0)
			return null;

		// 因为如果攻击范围D内只有友方单位，说明并没有触发combat
		int sample = inRange.get(0).getPlayer();
		boolean only_contain_self_units = true;
		for (Unit unit : inRange) {
			if (sample != unit.getPlayer())
				only_contain_self_units = false;
		}
		if (only_contain_self_units)
			return null;

		// 获取在D中的单位（这里应该可以优化）
		// 此时需要遍历友方单位，因为combat已经触发
		if (inRange.size() > 1) {
			List<Unit> tmp = new LinkedList<Unit>();
			for (Unit notinrange : notInRange) {
				for (Unit inrange : inRange) {
					if (getDistanceBetweenUnits(notinrange, inrange) <= inrange.getAttackRange()) {
						tmp.add(notinrange);
						break;
					}
				}
			}
			inRange.addAll(tmp);
		} else {
			return null;
		}

		return inRange;
	}

	public String toString() {
		String combatframe = "";

		for (Combat combat : this.Combats) {
			combatframe += combat.toString() + "\n";
		}

		return combatframe;
	}

	// 本战斗帧中是否有重复的combat（不同单位引发的Combat可能是同一个Combat）
	public boolean contains(Combat cb) {
		boolean contain = false;
		if (this.Combats == null)
			return false;
		for (Combat c : this.Combats) {
			if (c.isSameCombat(cb)) {
				return true;
			}
		}
		return contain;
	}
}

package DataCollection;

import java.util.LinkedList;
import java.util.List;

import rts.GameState;
import rts.PhysicalGameState;
import rts.units.Unit;

public class CombatFrame implements java.io.Serializable{

	// ��һ֡��ʼʱ��
	private int frame_time = 0;
	// ��һ֡���������е�λ
	private List<Unit> units = new LinkedList<Unit>();
	// ��һ֡���е�Combat
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
		// ˵������classֻ��¼һ֡��combat��Ϣ�����ֻ�ܼ�¼combat��ʼʱ�Ĳ��뵥λ��
		// ������Ϣ����combat��ʱ��������������Ϸ֡��ȡ����DataCollection���д���������
		if (gs == null)
			return null;
		for (Unit u : this.units) {
			List<Unit> unitsInRange = getUnitsInRange(u, gs);
			if (unitsInRange != null) {
				Combat combat = new Combat();
				combat.setStart_time(this.frame_time);
				combat.setEnd_time(this.frame_time + 1);	//��һ�μ������е�Combat��ֻ����һ֡

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
				
				//��ֹ�ظ�����Combat�����һ��Combat�Ĳ��뵥λ����һ��Combat���뵥λ���Ӽ���������ͬһ��Combat��
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

	// ��ȡ��λu�Ĺ�����Χ�ڵĵ�λ��������Լ򻯣�
	public List<Unit> getUnitsInRange(Unit u, GameState gs) {

		/*
		 * ���ݵ�λ�Ĺ�����Χȷ��D �趨AΪplayer1��D�еĵ�λ��BΪplayer2��D�еĵ�λ
		 * D�е����е�λΪ����combat�Ĳ��뵥λ�����ܸõ�λ�Ƿ��й���������λ�� ���û�й�����Ϊ�����ж�Ϊ���combat��passive��λ
		 */
		List<Unit> inRange = new LinkedList<Unit>();
		List<Unit> notInRange = new LinkedList<Unit>();
		PhysicalGameState pgs = gs.getPhysicalGameState();

		// ��һ�α�������ȡu�Ĺ�����Χ�ڵĵ�λ
		// ��ʱֻ�鿴�ط���λ�Ƿ��ڹ�����Χ��
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

		// ��Ϊ���������ΧD��ֻ���ѷ���λ��˵����û�д���combat
		int sample = inRange.get(0).getPlayer();
		boolean only_contain_self_units = true;
		for (Unit unit : inRange) {
			if (sample != unit.getPlayer())
				only_contain_self_units = false;
		}
		if (only_contain_self_units)
			return null;

		// ��ȡ��D�еĵ�λ������Ӧ�ÿ����Ż���
		// ��ʱ��Ҫ�����ѷ���λ����Ϊcombat�Ѿ�����
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

	// ��ս��֡���Ƿ����ظ���combat����ͬ��λ������Combat������ͬһ��Combat��
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

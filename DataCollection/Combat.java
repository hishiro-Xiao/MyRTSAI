package DataCollection;

import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import rts.units.Unit;

public class Combat {

	// һ��combat��Ҫ��¼����Ϣ��
	// ��ʼʱ�䣬����ʱ�䣬����ԭ�򣨷�Ϊ�����е��˱��ݻ١��涨֡����û���κι�����Ϊ������
	// �µĳ�ͻ��λ���롢��Ϸ������
	// combat��ʼʱ˫���Ĳ��뵥λ��combat����ʱ˫����ʣ�൥λ
	// ��ɱ���ĵ�λ�����Ǳ�ɱ����ʱ��
	// δ�����ͻ�ĵ�λ����һֱ�ڲɿ�ĵ�λ

	private int start_time = 0;
	private int end_time = 0;
	private String reason = null;
	private List<Unit> involved_A = new LinkedList<Unit>();
	private List<Unit> involved_B = new LinkedList<Unit>();
	private List<Unit> involved_ALL = new LinkedList<Unit>();
	private List<Unit> remained_A = new LinkedList<Unit>();
	private List<Unit> remained_B = new LinkedList<Unit>();
	private List<Unit> remained_ALL = new LinkedList<Unit>();
	private Map<Unit, Integer> killedUnits = new HashMap<Unit, Integer>();
	private List<Unit> passiveUnits = new LinkedList<Unit>();
	
	private boolean visited = false;	//�����־���ڴ������ݣ���Ӱ�����ݱ���

	public List<Unit> getRemained_ALL() {
		return remained_ALL;
	}

	public void setRemained_ALL(List<Unit> remained_ALL) {
		this.remained_ALL = remained_ALL;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public int getStart_time() {
		return start_time;
	}

	public void setStart_time(int start_time) {
		this.start_time = start_time;
	}

	public int getEnd_time() {
		return end_time;
	}

	public void setEnd_time(int end_time) {
		this.end_time = end_time;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public List<Unit> getInvolved_A() {
		return involved_A;
	}

	public void setInvolved_A(List<Unit> involved_A) {
		this.involved_A = involved_A;
	}

	public List<Unit> getInvolved_B() {
		return involved_B;
	}

	public void setInvolved_B(List<Unit> involved_B) {
		this.involved_B = involved_B;
	}

	public List<Unit> getInvolved_ALL() {
		return involved_ALL;
	}

	public void setInvolved_ALL(List<Unit> involved_ALL) {
		this.involved_ALL = involved_ALL;
	}

	public List<Unit> getRemained_A() {
		return remained_A;
	}

	public void setRemained_A(List<Unit> remained_A) {
		this.remained_A = remained_A;
	}

	public List<Unit> getRemained_B() {
		return remained_B;
	}

	public void setRemained_B(List<Unit> remained_B) {
		this.remained_B = remained_B;
	}

	public Map<Unit, Integer> getKilledUnits() {
		return killedUnits;
	}

	public void setKilledUnits(Map<Unit, Integer> killedUnits) {
		this.killedUnits = killedUnits;
	}

	public List<Unit> getPassiveUnits() {
		return passiveUnits;
	}

	public void setPassiveUnits(List<Unit> passiveUnits) {
		this.passiveUnits = passiveUnits;
	}

	public String toString() {
		String combat = "Combat :\n";

		combat += "start time :" + this.start_time + "\n";
		combat += "end time : " + this.end_time + "\n";
		combat += "Combat Ended Because of :" + this.reason + "\n";

		combat += "Involved_A :";
		for (Unit u : this.involved_A) {
			combat += u.toString();
		}

		combat += "\nInvolved_B :";
		for (Unit u : this.involved_B) {
			combat += u.toString();
		}

		combat += "\nInvolved_ALL :";
		for (Unit u : this.involved_ALL) {
			combat += u.toString();
		}

		combat += "\nRemained_A :";
		for (Unit u : this.remained_A) {
			combat += u.toString();
		}

		combat += "\nRemained_B :";
		for (Unit u : this.remained_B) {
			combat += u.toString();
		}

		combat += "\nRemained_ALL :";
		for (Unit u : this.remained_ALL) {
			combat += u.toString();
		}

		combat += "\nKilledUnits :";
		for (Unit u : this.killedUnits.keySet()) {
			combat += "(" + u.toString() + ", " + this.killedUnits.get(u) + ")";
		}

		combat += "\nPassiveUnits :";
		for (Unit u : this.passiveUnits) {
			combat += u.toString();
		}

		combat += "\n";
		return combat;
	}

	// �ж�����Combat�Ƿ���ͬһ��Combat���ж������ǣ����Combat�Ĳ��뵥λһ������ΪͬһCombat
	// ��Ϊ��һ�����겻���ܺ���������λ
	public boolean isSameCombat(Combat combat) {
		if (this.start_time != combat.getStart_time())
			return false;
		if (this.end_time != combat.getEnd_time())
			return false;
		if (this.involved_A.size() != combat.getInvolved_A().size())
			return false;
		if (this.involved_B.size() != combat.getInvolved_B().size())
			return false;

		for (Unit u_a : this.involved_A) {
			if (!combat.involved_A.contains(u_a))
				return false;
		}
		for (Unit u_b : this.involved_B) {
			if (!combat.involved_B.contains(u_b))
				return false;
		}

		return true;
	}

	public boolean isSameUnit(Unit a, Unit b) {
		boolean issameunit = true;

		if (a == null || b == null)
			return false;
		if (a.getX() != b.getX())
			issameunit = false;
		if (a.getY() != b.getY())
			issameunit = false;

		return issameunit;
	}

	//������ս��֡��ȡ���󣬸��ݱ�Combat��Involved��Remained��ȡKilledUnits
	public void processKilledUnits() {
		
		if(this.involved_ALL.size() <= 0)
			return;
		
		//���û��ʣ�൥λ���������е�λ�ڱ�֡�Ľ���ʱ�䱻killed
		if(this.remained_ALL.size() <= 0) {
			Map<Unit, Integer> killedUnits = new HashMap<Unit, Integer>();
			for(Unit u: this.involved_ALL) {
				killedUnits.put(u, this.end_time);
			}
			this.setKilledUnits(killedUnits);
			return;
		}
		
		//�����������ȡinvovled��remianed�Ĳ
		if(this.involved_ALL.size() != this.remained_ALL.size()) {
			Map<Unit, Integer> killedUnits = new HashMap<Unit, Integer>();
			for(Unit u: this.involved_ALL) {
				if(!containsUnit(remained_ALL, u)) {
					killedUnits.put(u, this.end_time);
				}
			}
			this.setKilledUnits(killedUnits);
			return;
		}
		
	}
	
	//�ж�unitList���Ƿ������λu
	public boolean containsUnit(List<Unit> unitList, Unit u) {
		boolean contains = false;
		
		for(Unit unit: unitList) {
			if(unit.getID() == u.getID()) {
				return true;
			}
		}
		
		return contains;
	}
	
	//����unitList�Ƿ��������һ���ĵ�λ(����unitList�Ƿ�һ��)
	public boolean containSameUnits(List<Unit> unitList1, List<Unit> unitList2) {
		boolean contains = true;
		
		if(unitList1.size() != unitList2.size()) return false;
		
		for(Unit u_1: unitList1) {
			if(!this.containsUnit(unitList2, u_1))
				return false;
		}
		
		return contains;
	}
	
	public void toJSON(Writer w) throws Exception{
		w.write("\"Combat\" : {");
			
		w.write("\"start_time\":\"" + this.start_time + "\",");
		w.write("\"end_time\":\"" + this.end_time + "\",");
		w.write("\"reason\":\"" + this.reason + "\",");
		
		w.write("\"involved_A\": {");
		for(Unit u: this.involved_A) {
			w.write("\"unit\":");
			u.toJSON(w);
		}
		w.write("},");
		
		w.write("\"involved_B\": {");
		for(Unit u: this.involved_B) {
			w.write("\"unit\":");
			u.toJSON(w);
		}
		w.write("},");
		
		w.write("\"involved_ALL\": {");
		for(Unit u: this.involved_ALL) {
			w.write("\"unit\":");
			u.toJSON(w);
		}
		w.write("},");
		
		w.write("\"remained_A\": {");
		for(Unit u: this.remained_A) {
			w.write("\"unit\":");
			u.toJSON(w);
		}
		w.write("},");
		
		w.write("\"remained_B\": {");
		for(Unit u: this.remained_B) {
			w.write("\"unit\":");
			u.toJSON(w);
		}
		w.write("},");
		
		w.write("\"remained_ALL\": {");
		for(Unit u: this.remained_ALL) {
			w.write("\"unit\":");
			u.toJSON(w);
		}
		w.write("},");
		
		w.write("\"killedUnits\": {");
		for(Unit u: this.killedUnits.keySet()) {
			w.write("\"killedUnit\":");
			u.toJSON(w);
			w.write(", \"killedTime\": \"" + this.killedUnits.get(u) + "\"");
			w.write("}");
		}
		w.write("},");
		
		w.write("\"passiveUnits\": {");
		for(Unit u: this.passiveUnits) {
			w.write("\"unit\":");
			u.toJSON(w);
		}
		w.write("}");
		
		w.write("}");
	}
	
}
















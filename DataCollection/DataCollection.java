package DataCollection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import rts.GameState;
import rts.UnitAction;
import rts.UnitActionAssignment;
import rts.units.Unit;

public class DataCollection {

	private List<CombatFrame> CombatFrame = new LinkedList<CombatFrame>();	//����һ����Ϸ������ս��֡
	private List<GameState> gameState = new LinkedList<GameState>(); // ����һ����Ϸ��������Ϸ֡
	private List<Combat> Combats = new LinkedList<Combat>(); //����������Ϸ������Combat�������Combat��ʱ��洢��������processCombatFrame()֮ǰ����������Combat
	
	public List<Combat> getCombats() {
		return Combats;
	}

	public void setCombats(List<Combat> combats) {
		Combats = combats;
	}

	public List<CombatFrame> getCombatFrame() {
		return CombatFrame;
	}

	public void setCombatFrame(List<CombatFrame> CombatFrame) {
		this.CombatFrame = CombatFrame;
	}

	public List<GameState> getGameState() {
		return gameState;
	}

	public void setGameState(List<GameState> gameState) {
		this.gameState = gameState;
	}

	public DataCollection(GameState gs) {
		this.gameState.add(gs.clone());
	}

	//ÿ����Ϸ�仯ʱ�����ô˺�������¼��Ϸ�ı仯
	public void updateCombatFrame(GameState gs) {
		// ����޸�this.GameState��ʱʱ�򣬱���add gs.clone,����ֱ��add gs,
		// ��Ȼ���������е�gs��һ��仯����Ҳ��֪��Ϊʲô��������������������gs�ĵ�ַ
		// �������ǣ�JAVAû��ָ��������
		if (gs != null) {
			this.gameState.add(gs.clone());
			
			CombatFrame cf = new CombatFrame(gs.clone());
			this.CombatFrame.add(cf);
			
			// ��ȡ����Ҫ�����Combat����
			this.Combats.addAll(cf.getCombats());
		}
	}

	//ȫ�������ռ������󣬽��е�Ĭ�ϲ�����
	//��Ϊ�趨ÿ��Combatֻ����һ֡����¼ʣ�൥λΪ��һ֡�ĵ�λ
	public void dataCollected() {
		//��¼ʣ�൥λΪ��һ֡�ĵĵ�λ
		for (Combat c : this.Combats) {
			GameState next_frame = null;
			for (GameState gs : this.gameState) {
				if (gs.getTime() == c.getStart_time() + 1) {
					next_frame = gs;
					break;
				}
			}
			
			List<Unit> remained_all = new LinkedList<Unit>();
			for(Unit u: next_frame.getPhysicalGameState().getUnits()) {
				if(c.containsUnit(c.getInvolved_ALL(), u)) {
					remained_all.add(u);
				}
			}
			c.setRemained_ALL(remained_all);
			
			List<Unit> remained_A = new LinkedList<Unit>();
			List<Unit> remained_B = new LinkedList<Unit>();
			for(Unit u: remained_all) {
				if(u.getPlayer() == 0)
					remained_A.add(u);
				else if(u.getPlayer() == 1)
					remained_B.add(u);
			}
			c.setRemained_A(remained_A);
			c.setRemained_B(remained_B);
			
			c.processKilledUnits();
		}
			
	}
	
	//���ռ�������Combat֮֡�󣬽��д������������Combat����
	//����˼·�����ǰ����֡����ͬһ��Combat����ǰһ֡��Combat��ʣ�൥λ������һ֡�Ĳ��뵥λ��ǰһ֡�Ľ���ʱ�������һ֡�Ľ���ʱ��
	//��ô����Combat����ԭ��ֱ��Ӧ�����Ϊ��
	//���е��˱��ݻ٣�Combat��ʣ�൥λ��һ��Ϊ�գ���ʱ����Ҫ�Ƚ�ǰ����֡
	//�涨֡����û��ս����Ϊ�������涨֡����û���κε�λ��ʧ
	//�µĳ�ͻ��λ���룺ǰ����֡����������ǰһ֡��ʣ�൥λ�Ǻ�һ֡�Ĳ��뵥λ���Ӽ�
	//��Ϸ������Combat����һ֡������Ϸʱ��
	public void processCombatFrame() {

		if (this.Combats .size() <= 0) return;
		
		//����˼·���趨һ���µ�Combat������м�¼�����±�i�����ռ����ģ������г����������Combat���ϣ�
		//�Ƚ�real_combat���³�ǰһ֡����Combats[i]���³���һ֡������Ϣ�����ǰһ֡�Ľ���ʱ�����һ֡��һ�����Ǳ�Ȼ�����β�ͬ��Combat
		//Ȼ��͸���real_combatΪ�µ�combat��ʼ�µıȽ�
		List<Combat> real_Combats = new LinkedList<Combat>();
		Combat real_combat = null;
		
		for(int i = 0; i < this.Combats.size(); i++) {
			//�����֡���ڣ��������ͬһ��combat��Ҳ�п������µ�combat����һ��combat��������
			//�����ͬһ��combat����������㣺
			//1.ǰһ֡��ʣ�൥λ������һ֡�Ĳ��뵥λ����Ϊ��λ��hp���ٻᱻ�ж�Ϊ��ͬ��λ������ͨ����λid�ж��Ƿ���ͬһ��λ��������ʹ��Object��equal()��
			//2.ǰһ֡�Ľ���ʱ�������һ֡�Ŀ�ʼʱ��
			//��ô��ִ�в���������ǰһ֡�Ľ���ʱ�䣬�ϲ����뵥λΪǰһ֡�Ĳ��뵥λ��ʣ�൥λΪ��һ֡��ʣ�൥λ����ɱ��λΪ��֡�ı�ɱ��λ����
			Combat next_frame = this.Combats.get(i);
			
			if(next_frame.isVisited()) continue;
			if(real_combat == null)	{
				real_combat = next_frame;
				this.Combats.get(i).setVisited(true);
				continue;
			}
			
			if(real_combat.getEnd_time() == next_frame.getStart_time() && real_combat.containSameUnits(real_combat.getRemained_ALL(), next_frame.getInvolved_ALL())) {
				real_combat.setEnd_time(next_frame.getEnd_time());
				real_combat.setRemained_ALL(next_frame.getRemained_ALL());
				real_combat.setRemained_A(next_frame.getRemained_A());
				real_combat.setRemained_B(next_frame.getRemained_B());
				
				Map<Unit, Integer> killedUnits = real_combat.getKilledUnits();
				killedUnits.putAll(next_frame.getKilledUnits());
				real_combat.setKilledUnits(killedUnits);
				
				this.Combats.get(i).setVisited(true);
			}
			else if(real_combat.getEnd_time() == next_frame.getStart_time() && !real_combat.containSameUnits(real_combat.getRemained_ALL(), next_frame.getInvolved_ALL())) {
				continue;
			}
			else if(real_combat.getEnd_time() == next_frame.getEnd_time() && !real_combat.containSameUnits(real_combat.getRemained_ALL(), next_frame.getInvolved_ALL())) {
				continue;
			}
			else {	//ս������
				real_Combats.add(real_combat);
				real_combat = null;
				i = getFirstUnvisited() - 1;
			}
			
		}
		
		this.Combats = real_Combats;
		
		//��ȡ����Combat�Ľ���ԭ��
		for(Combat c: this.Combats) {
			if(c.getEnd_time() < 5000 && c.getRemained_A().size() <= 0 || c.getRemained_B().size() <= 0)
				c.setReason("Destroyed");
			else if(c.getEnd_time() < 5000 && c.getRemained_ALL().size() > 0) {
				int last_killed = -1;
				for(Unit u: c.getKilledUnits().keySet()) {
					if(c.getKilledUnits().get(u) > last_killed)
						last_killed = c.getKilledUnits().get(u);
				}
				if(last_killed != -1 && c.getEnd_time() - last_killed >= 144) 
					c.setReason("Peace");
				else
					c.setReason("Reinforcement");
			}
			else if(c.getEnd_time() >= 5000 && c.getRemained_ALL().size() > 0) {
				c.setReason("Game End");
			}
		}
		
		//��ȡ����Combat��Passive��λ
		for(Combat c: this.Combats) {
			List<Unit> passive = new LinkedList<Unit>();
			for(Unit u: c.getInvolved_ALL()) {
				if(!hasAttackBetween(u, c.getStart_time(), c.getEnd_time()))
					passive.add(u);
			}
			c.setPassiveUnits(passive);
		}
	}
	
	//�жϵ�λu��ָ��ʱ�����Ƿ�����˹�����Ϊ
	public boolean hasAttackBetween(Unit u, int start, int end) {
		for(int i = start; i < end; i++) {
			GameState gs = this.gameState.get(i);
			Map<Unit,UnitActionAssignment> uas = gs.getUnitActions();
			for(Unit unit: uas.keySet()) {
				if(unit.getID() == u.getID()) {
					UnitAction ua = uas.get(unit).action;
					if(ua != null && ua.getActionName() == "attack_location") return true;
					break;
				}
			}
		}
		
		return false;
	}
	
	//��ȡ��һ��δ�����ʵ�Combat�ڵ�
	public int getFirstUnvisited() {
		for(int pos = 0; pos < this.Combats.size(); pos++) {
			if(!this.Combats.get(pos).isVisited()) {
				return pos;
			}
		}
		
		return this.Combats.size();
	}
	
	//�����ݱ��浽Ӳ��
	public void saveCombatFrame() throws Exception {
		long time = new Date().getTime();
		String filePath = "./dataset/8x8/" + Long.toString(time) + ".txt";
		File file = new File(filePath);
		synchronized (file) {
			FileWriter fw = new FileWriter(filePath);
//			this.toJSON(fw);
			fw.write(this.toString() + "\n");
//			fw.write("Total records: " + this.Combats.size());
			fw.close();
		}
	}

	public String toString() {
		String string = "";
		for (Combat c : this.Combats) {
			string += c.toString() + "\n";
		}
		return string;
	}
	
	public void toJSON(Writer w) throws Exception {
		w.write("{");
		for (Combat c : this.Combats) {
			c.toJSON(w);
		}
		w.write("}");
	}
}

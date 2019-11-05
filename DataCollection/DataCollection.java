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

	private List<CombatFrame> CombatFrame = new LinkedList<CombatFrame>();	//保存一场游戏的所有战斗帧
	private List<GameState> gameState = new LinkedList<GameState>(); // 保存一场游戏的所有游戏帧
	private List<Combat> Combats = new LinkedList<Combat>(); //保存整场游戏的所有Combat，最初的Combat按时间存储，而且在processCombatFrame()之前不是真正的Combat
	
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

	//每次游戏变化时，调用此函数，记录游戏的变化
	public void updateCombatFrame(GameState gs) {
		// 这边修改this.GameState的时时候，必须add gs.clone,不能直接add gs,
		// 不然后续的所有的gs会一起变化，我也不知道为什么，可能是他传进来的是gs的地址
		// 但问题是，JAVA没有指针这个概念啊
		if (gs != null) {
			this.gameState.add(gs.clone());
			
			CombatFrame cf = new CombatFrame(gs.clone());
			this.CombatFrame.add(cf);
			
			// 提取出需要处理的Combat对象
			this.Combats.addAll(cf.getCombats());
		}
	}

	//全部数据收集结束后，进行的默认操作，
	//人为设定每场Combat只持续一帧，记录剩余单位为下一帧的单位
	public void dataCollected() {
		//记录剩余单位为下一帧的的单位
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
	
	//在收集完所有Combat帧之后，进行处理，获得真正的Combat数据
	//基本思路：如果前后两帧属于同一场Combat，则前一帧的Combat的剩余单位就是下一帧的参与单位，前一帧的结束时间就是下一帧的结束时间
	//那么四种Combat结束原因分别对应的情况为：
	//所有敌人被摧毁：Combat的剩余单位有一个为空，此时不需要比较前后两帧
	//规定帧数内没有战斗行为产生：规定帧数内没有任何单位消失
	//新的冲突单位加入：前后两帧相连，但是前一帧的剩余单位是后一帧的参与单位的子集
	//游戏结束：Combat的下一帧大于游戏时间
	public void processCombatFrame() {

		if (this.Combats .size() <= 0) return;
		
		//处理思路：设定一个新的Combat对象进行记录，用下标i遍历收集到的，并进行初步处理过的Combat集合，
		//比较real_combat（下称前一帧）和Combats[i]（下称下一帧）的信息，如果前一帧的结束时间和下一帧不一样，那必然是两次不同的Combat
		//然后就更新real_combat为新的combat开始新的比较
		List<Combat> real_Combats = new LinkedList<Combat>();
		Combat real_combat = null;
		
		for(int i = 0; i < this.Combats.size(); i++) {
			//如果两帧相邻，则可能是同一场combat，也有可能是新的combat与这一场combat正好相邻
			//如果是同一场combat，则必须满足：
			//1.前一帧的剩余单位等于下一帧的参与单位（因为单位的hp减少会被判断为不同单位，所以通过单位id判断是否是同一单位，而不能使用Object的equal()）
			//2.前一帧的结束时间等于下一帧的开始时间
			//那么，执行操作：更新前一帧的结束时间，合并参与单位为前一帧的参与单位，剩余单位为下一帧的剩余单位，被杀单位为两帧的被杀单位并集
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
			else {	//战斗结束
				real_Combats.add(real_combat);
				real_combat = null;
				i = getFirstUnvisited() - 1;
			}
			
		}
		
		this.Combats = real_Combats;
		
		//获取所有Combat的结束原因
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
		
		//获取所有Combat的Passive单位
		for(Combat c: this.Combats) {
			List<Unit> passive = new LinkedList<Unit>();
			for(Unit u: c.getInvolved_ALL()) {
				if(!hasAttackBetween(u, c.getStart_time(), c.getEnd_time()))
					passive.add(u);
			}
			c.setPassiveUnits(passive);
		}
	}
	
	//判断单位u在指定时间内是否产生了攻击行为
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
	
	//获取第一个未被访问的Combat节点
	public int getFirstUnvisited() {
		for(int pos = 0; pos < this.Combats.size(); pos++) {
			if(!this.Combats.get(pos).isVisited()) {
				return pos;
			}
		}
		
		return this.Combats.size();
	}
	
	//将数据保存到硬盘
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

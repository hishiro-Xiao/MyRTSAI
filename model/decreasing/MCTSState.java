package model.decreasing;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import rts.GameState;
import rts.PhysicalGameState;
import rts.units.Unit;

/*
 * MCTS搜索游戏的状态，记录Nod节点下的状态数据，包含当前游戏的得分、round数、从开始到当前的执行记录
 * 需要实现，判断当前状态是否到达游戏结束状态，支持从Action集合中随机取出操作
 * */

public class MCTSState {
	
	private GameState gs;
	private int current_value;
	private int current_round;
	private String[] choices = {"MOVETOP", "MOVERIGHT", "MOVEBOT", "MOVELEFT", "ATTACK", "IDLE"};
	private List<String> cumulative_choice = new LinkedList<String>();
	
	public GameState getGs() {
		return gs;
	}

	public void setGs(GameState gs) {
		this.gs = gs;
	}

	public int getCurrent_value() {
		return current_value;
	}

	public void setCurrent_value(int current_value) {
		this.current_value = current_value;
	}

	public int getCurrent_round() {
		return current_round;
	}

	public void setCurrent_round(int current_round) {
		this.current_round = current_round;
	}

	public String[] getChoices() {
		return choices;
	}

	public void setChoices(String[] choices) {
		this.choices = choices;
	}

	public List<String> getCumulative_choice() {
		return cumulative_choice;
	}

	public void setCumulative_choice(List<String> cumulative_choice) {
		this.cumulative_choice = cumulative_choice;
	}

	//构造函数
	public MCTSState() {
		this.current_value = 0;
		this.current_round = 0;
		this.gs = null;
	}
	
	public MCTSState(GameState gs) {
		this.current_value = 0;
		this.current_round = 0;
		this.gs = gs;
	}
	
	//判断游戏是否结束
	public boolean isTerminal() {
		if(this.current_round == 5000)
			return true;
		else
			return false;
	}
	
	/*
	 * 根据Decresing的predict来计算reward
	*/
	public int compute_reward(GameState gs) throws IOException {
		List<Unit> enemy1 = new LinkedList<Unit>();
		List<Unit> enemy2 = new LinkedList<Unit>();
		
		PhysicalGameState pgs = gs.getPhysicalGameState();
		for(Unit u: pgs.getUnits()) {
			if(u.getPlayer() == 1)
				enemy1.add(u);
			else
				enemy2.add(u);	
		}
		
		Decresing dec = new Decresing();
		if(dec.getParameters())
			return dec.predict(enemy1, enemy2);
		else {
			dec.readDataSets();
			dec.trainEffectiveDPF();
			dec.trainTargetSelection();
			dec.saveParameters(dec);
			return dec.predict(enemy1, enemy2);
		}
	}
	
	public MCTSState get_next_state_with_random_choice() {
		Random rand = new Random(6);
		String random_choice = this.choices[rand.nextInt()];
		
		MCTSState next_state = new MCTSState();
		next_state.setCurrent_value(this.current_value);
		next_state.setCurrent_round(this.current_round + 1);
		
		List<String> cumulative_choice = new LinkedList<String>();
		cumulative_choice.addAll(this.cumulative_choice);
		cumulative_choice.add(random_choice);
		next_state.setCumulative_choice(cumulative_choice);
		
		return next_state;
	}
}

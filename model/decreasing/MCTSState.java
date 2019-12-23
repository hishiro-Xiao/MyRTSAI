package model.decreasing;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import rts.GameState;
import rts.PhysicalGameState;
import rts.units.Unit;

/*
 * MCTS������Ϸ��״̬����¼Nod�ڵ��µ�״̬���ݣ�������ǰ��Ϸ�ĵ÷֡�round�����ӿ�ʼ����ǰ��ִ�м�¼
 * ��Ҫʵ�֣��жϵ�ǰ״̬�Ƿ񵽴���Ϸ����״̬��֧�ִ�Action���������ȡ������
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

	//���캯��
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
	
	//�ж���Ϸ�Ƿ����
	public boolean isTerminal() {
		if(this.current_round == 5000)
			return true;
		else
			return false;
	}
	
	/*
	 * ����Decresing��predict������reward
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

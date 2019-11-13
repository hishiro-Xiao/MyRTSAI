package DataCollection;

import java.util.LinkedList;
import java.util.List;

// 此类无实际用途，用于从json文件中读取List<Combat>时，提供class参数

public class CombatList {
	private List<Combat> combats = new LinkedList<Combat>();

	public CombatList(List<Combat> combats) {
		this.combats = combats;
	}

	public List<Combat> getCombats() {
		return combats;
	}

	public void setCombats(List<Combat> combats) {
		this.combats = combats;
	}
	
}

package DataCollection;

import java.util.LinkedList;
import java.util.List;

// ������ʵ����;�����ڴ�json�ļ��ж�ȡList<Combat>ʱ���ṩclass����

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

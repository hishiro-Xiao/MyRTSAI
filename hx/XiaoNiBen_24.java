package hx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ai.abstraction.*;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.core.AI;
import ai.core.ParameterSpecification;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.PlayerAction;
import rts.UnitActionAssignment;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

public class XiaoNiBen_24 extends AbstractionLayerAI {

	UnitTypeTable m_utt = null;
	UnitType workerType;
	UnitType baseType;
	UnitType barracksType;
	UnitType lightType;
	UnitType heavyType;
	UnitType rangedType;

	// default constructor
	public XiaoNiBen_24(UnitTypeTable utt) {
		this(utt, new AStarPathFinding());
	}

	public XiaoNiBen_24(UnitTypeTable utt, AStarPathFinding aStarPathFinding) {
		super(aStarPathFinding);
		reset(utt);
	}

	// create new instance
	public AI clone() {
		return new XiaoNiBen_24(m_utt);
	}

	public void reset() {
		super.reset();
	}

	public void reset(UnitTypeTable utt) {
		m_utt = utt;
		if (m_utt != null) {
			workerType = m_utt.getUnitType("Worker");
			baseType = m_utt.getUnitType("Base");
			barracksType = utt.getUnitType("Barracks");
			rangedType = utt.getUnitType("Ranged");
			lightType = utt.getUnitType("Light");
			heavyType = utt.getUnitType("Heavy");
		}
	}

	// call by microRTS at each game cycle, returns the action the bot wants to
	// excute
	public PlayerAction getAction(int player, GameState gs) {

		PhysicalGameState pgs = gs.getPhysicalGameState();
		Player p = gs.getPlayer(player);
		PlayerAction pa = new PlayerAction();

		List<Unit> bases = new LinkedList<Unit>();

		// 所有基地
		for (Unit u : pgs.getUnits()) {
			if (u.getType() == baseType && u.getPlayer() == player && gs.getActionAssignment(u) == null) {
				bases.add(u);
			}
		}
		for (Unit u : bases) {
			baseBehavior(u, p, gs);
		}

		// 所有进攻单位
		for (Unit u : pgs.getUnits()) {
			if (u.getType().canAttack && !u.getType().canHarvest && u.getPlayer() == player
					&& gs.getActionAssignment(u) == null) { // 这个条件表示该单位未注册一个行为
				meleeUnitBehavior(u, p, gs);
			}
		}

		// 所有工人单位（包括已经注册了行为的工人单位）
		List<Unit> workers = new LinkedList<Unit>();
		for (Unit u : pgs.getUnits()) {
			if (u.getType().canHarvest && u.getPlayer() == player) {
				workers.add(u);
			}
		}
		workerBehavior(workers, p, gs);

		// 兵营单位
		for (Unit u : pgs.getUnits()) {
			if (u.getType() == barracksType && u.getPlayer() == p.getID() && gs.getActionAssignment(u) == null) {
				barracksBehavior(u, p, gs);
			}
		}

		return translateActions(player, gs);
	}

	// 基地的行动
	public void baseBehavior(Unit u, Player p, GameState gs) {
		
		//如果两方基地很近，优先采用workerRush
		int enemy_worker_num = getEnemyInfo(p, gs).get("num_worker");
		int worker_num = getHarvestWorkerNum(p, gs);
		if(worker_num <= enemy_worker_num)
			train(u, workerType);	
		
//		//如果敌人是workerRush，则我们也先workerRush
//		if(getEnemyBarracksNum(p, gs) < 1 && p.getResources() > workerType.cost) {
//			train(u, workerType);
//		}
//		else {
//			if (getHarvestWorkerNum(p, gs) < 2 && p.getResources() > workerType.cost)
//				train(u, workerType);
//			if (getBarracksNum(p, gs) < 1 && getHarvestWorkerNum(p, gs) < 2 && p.getResources() > workerType.cost)
//				train(u, workerType);
//		}
	}

	// 进攻单位的行动
	public void meleeUnitBehavior(Unit u, Player p, GameState gs) {

		PhysicalGameState pgs = gs.getPhysicalGameState();
		Unit closestEnemy = null;
		Unit closestBase = null;
		Unit closestBarracks = null;
		int closestEnemyDistance = 65535;
		int closestBaseDistance = 65535;
		int closestBarracksDistance = 65535;

		for (Unit unit : pgs.getUnits()) {
			if (unit.getType() != baseType && unit.getType() != barracksType && unit.getPlayer() >= 0 && unit.getPlayer() != p.getID()) {
				int dis = getDistanceBetweenUnits(u, unit);
				if (dis < closestEnemyDistance || closestEnemy == null) {
					closestEnemyDistance = dis;
					closestEnemy = unit;
				}
			} else if (unit.getType() == baseType && unit.getPlayer() >= 0 && unit.getPlayer() != p.getID()) {
				int dis = getDistanceBetweenUnits(u, unit);
				if (dis < closestBaseDistance || closestBase == null) {
					closestBaseDistance = dis;
					closestBase = unit;
				}
			} else if (unit.getType() == barracksType && unit.getPlayer() >= 0 && unit.getPlayer() != p.getID()) {
				int dis = getDistanceBetweenUnits(u, unit);
				if (dis < closestBarracksDistance || closestBarracks == null) {
					closestBarracksDistance = dis;
					closestBarracks = unit;
				}
			}
		}

		// 策略：优先打最近的基地，否则打最近的敌人
//		if(closestBase != null){
//			attack(u, closestBase);
//		}
//		else if(closestEnemy != null){
//			attack(u, closestEnemy);
//		}
		
		// 策略：优先打最近的敌人，否则打最近的基地
		if(closestEnemy != null && closestEnemyDistance < closestBarracksDistance && closestEnemyDistance < closestBaseDistance){
			attack(u, closestEnemy);
		}
		else if(closestBase != null && closestBaseDistance < closestBarracksDistance){
			attack(u, closestBase);
		}
		else if(closestBarracks != null) {
			attack(u, closestBarracks);
		}

		// 策略：优先打基地，基地没了再打最近的敌人
//		if (closestBase != null) {
//			attack(u, closestBase);
//		} else {
//			attack(u, closestEnemy);
//		}
		
	}

	// 工人的行动
	public void workerBehavior(List<Unit> workers, Player p, GameState gs) {
		PhysicalGameState pgs = gs.getPhysicalGameState();

		// 挖矿的工人
		List<Unit> harvestWorkers = new LinkedList<Unit>();
		int num_harvest = 2; // 需求量
		if(getEnemyBarracksNum(p, gs) < 1) {
			num_harvest = 1;
		}

		// 建造的工人
		List<Unit> buildWorkers = new LinkedList<Unit>();
		int num_build = 1;
		// 如果对方没有建造兵营，那么我们也不造,如果已经有一个兵营了，那么也不造
		if (getEnemyBarracksNum(p, gs) == 0)
			num_build = 0; // 需求barracks建筑量
		else if(getBarracksNum(p, gs) == 1)
			num_build = 0;

		// 免费的工人
		List<Unit> freeWorkers = new LinkedList<Unit>();

		if (workers.isEmpty())
			return;

		// 分配工人
		Map<Unit, String> workerActions = getWorkerActions(workers);
		if (workerActions.isEmpty())
			return;

		List<Unit> previousHarvest = new LinkedList<Unit>();
		for (Unit u : workerActions.keySet()) {
			if (workerActions.get(u) == "Harvest") {
				previousHarvest.add(u);
				workers.remove(u);
			}
		}

		List<Unit> previousBuild = new LinkedList<Unit>();
		for (Unit u : workerActions.keySet()) {
			if (workerActions.get(u) == "Build")
				previousBuild.add(u);
		}

		while (num_harvest != 0) {
			if (previousHarvest.size() > 0) {
				harvestWorkers.add(previousHarvest.remove(0));
				num_harvest--;
			} else if (workers.size() > 0) {
				harvestWorkers.add(workers.remove(workers.size() - 1));
				num_harvest--;
			} else
				break;
		}

		while (num_build != 0) {
			if (previousBuild.size() > 0) {
				buildWorkers.add(previousBuild.remove(0));
				num_build--;
			} else if (workers.size() > 0) {
				buildWorkers.add(workers.remove(workers.size() - 1));
				num_build--;
			} else
				break;
		}

		freeWorkers.addAll(workers);
		freeWorkers.addAll(previousHarvest);
		freeWorkers.addAll(previousBuild);

		// 执行
		// 挖矿
		for (Unit worker : harvestWorkers) {
			makeWorkerHarvest(worker, p, gs);
		}

		// 建造
		for (Unit worker : buildWorkers) {
			makeWorkerBuild(worker, barracksType, p, gs);
		}

		// 进攻
		for (Unit u : freeWorkers) {
			meleeUnitBehavior(u, p, gs);
		}
	}

	// 兵营的行动
	public void barracksBehavior(Unit barracks, Player p, GameState gs) {
		Map<String, Integer> enemy = getEnemyInfo(p, gs);

		int max_num = enemy.get("max_num");
		String max_type = null;
		for (String key : enemy.keySet()) {
			if (enemy.get(key) == -1)
				max_type = key;
		}

		switch (max_type) {
		case "worker":
			train(barracks, rangedType);
			break;
		case "light":
			train(barracks, lightType);
			break;
		case "ranged":
			train(barracks, heavyType);
			break;
		case "heavy":
			train(barracks, lightType);
			break;
		}
	}

	// call by mcroRTS GUI to get the list of parameters that this bot wants exposed
	// in the GUI
	public List<ParameterSpecification> getParameters() {
		return new ArrayList<>();
	}

	// 获取当前工人数量
	public int getWorkerNum(Player p, GameState gs) {
		PhysicalGameState pgs = gs.getPhysicalGameState();
		int num_of_workers = 0;
		for (Unit u : pgs.getUnits()) {
			if (u.getType() == workerType && u.getPlayer() == p.getID()) {
				num_of_workers++;
			}
		}
		return num_of_workers;
	}

	// 获取两个棋子之间的距离
	// 距离：(x_1 - x_2) + (y_1 + y_2)
	public int getDistanceBetweenUnits(Unit u1, Unit u2) {
		return Math.abs(u1.getX() - u2.getX()) + Math.abs(u1.getY() - u2.getY());
	}

	// 获取距离单位u最近的我方基地base
	public Unit getCloestBase(Unit u, Player p, GameState gs) {
		PhysicalGameState pgs = gs.getPhysicalGameState();
		Unit cloestBase = null;
		int cloestDistance = 0;

		for (Unit unit : pgs.getUnits()) {
			if (unit.getType().isStockpile && unit.getPlayer() == p.getID()) {
				int dis = getDistanceBetweenUnits(u, unit);
				if (cloestBase == null || dis < cloestDistance) {
					cloestBase = unit;
					cloestDistance = dis;
				}
			}
		}

		return cloestBase;
	}

	// 获取距离单位u最近的资源res
	public Unit getCloestResource(Unit u, Player p, GameState gs) {

		PhysicalGameState pgs = gs.getPhysicalGameState();
		Unit cloestResource = null;
		int cloestDistance = 0;

		for (Unit unit : gs.getUnits()) {
			if (unit.getType().isResource) {
				int dis = getDistanceBetweenUnits(u, unit);
				if (dis < cloestDistance || cloestResource == null) {
					cloestResource = unit;
					cloestDistance = dis;
				}
			}
		}

		return cloestResource;
	}

	// 让一个矿工去最近的资源挖矿
	public void makeWorkerHarvest(Unit worker, Player p, GameState gs) {
		if (worker != null) {
			Unit cloestBase = getCloestBase(worker, p, gs);
			Unit cloestRescource = getCloestResource(worker, p, gs);

			if (cloestBase != null && cloestRescource != null) {
				AbstractAction aa = getAbstractAction(worker);
				if (aa instanceof Harvest) {
					Harvest h_aa = (Harvest) aa;
					if (h_aa.getTarget() != cloestRescource || h_aa.getBase() != cloestBase)
						harvest(worker, cloestRescource, cloestBase);
				} else
					harvest(worker, cloestRescource, cloestBase);
			}
		}
	}

	// 让矿工去建造一个建筑
	public void makeWorkerBuild(Unit worker, UnitType utype, Player p, GameState gs) {
		if (worker != null && p.getResources() > utype.cost) {
			PhysicalGameState pgs = gs.getPhysicalGameState();
			int barracks_x = -1;
			int barracks_y = -1;
			for (Unit u : pgs.getUnits()) {
				if (u.getType() == baseType && u.getPlayer() == p.getID()) {
					barracks_x = u.getX() - 2;
					barracks_y = u.getY();
					while (pgs.getUnitAt(barracks_x, barracks_y) != null) {
						barracks_x = barracks_x - 1;
						barracks_y = barracks_y - 1;
					}
				}
			}
			if (utype.equals(baseType)) {
				barracks_x = 18;
				barracks_y = 18;
			}
			
			List<Integer> reservedPositions = new LinkedList<Integer>();
			buildIfNotAlreadyBuilding(worker, barracksType, worker.getX(), worker.getY(), reservedPositions,p,pgs);
//			build(worker, utype, barracks_x, barracks_y);
		}
	}

	// 获取我方兵营数量（包括正在建造的建筑）
	public int getBarracksNum(Player p, GameState gs) {
		int num = 0;
		PhysicalGameState pgs = gs.getPhysicalGameState();

		for (Unit u : pgs.getUnits()) {
			if (u.getPlayer() == p.getID()) {
				if(u.getType() == barracksType)
					num++;
				else {
					UnitActionAssignment uaa = gs.getActionAssignment(u);
					if(uaa != null && uaa.action.getActionName() == "produce" && uaa.action.getUnitType().equals(barracksType))
						num++;
				}
			}
		}
		return num;
	}
	
	// 获取敌方兵营数量（包括正在建造的建筑）, p为我方
	public int getEnemyBarracksNum(Player p, GameState gs) {
		int num = 0;
		PhysicalGameState pgs = gs.getPhysicalGameState();

		for (Unit u : pgs.getUnits()) {
			if (u.getPlayer() != p.getID()) {
				if(u.getType() == barracksType)
					num++;
				else{
					UnitActionAssignment uaa = gs.getActionAssignment(u);
					if(uaa != null && uaa.action.getActionName() == "produce" && uaa.action.getUnitType().equals(barracksType))
						num++;
				}
			}
		}
		return num;
	}

	// 获取我方基地数量
	public int getBaseNum(Player p, GameState gs) {
		int num = 0;
		PhysicalGameState pgs = gs.getPhysicalGameState();
		for (Unit u : pgs.getUnits()) {
			if (u.getPlayer() == p.getID() && u.getType() == baseType) {
				num++;
			}
		}
		return num;
	}

	// 获取敌方信息
	public Map<String, Integer> getEnemyInfo(Player p, GameState gs) {
		Map<String, Integer> hashmap = new HashMap<>();

		hashmap.put("num_base", 0);
		hashmap.put("num_worker", 0);
		hashmap.put("num_light", 0);
		hashmap.put("num_heavy", 0);
		hashmap.put("num_ranged", 0);
		hashmap.put("num_barracks", 0);

		PhysicalGameState pgs = gs.getPhysicalGameState();
		for (Unit u : pgs.getUnits()) {
			if (u.getPlayer() != p.getID()) {
				switch (u.getType().name) {
				case "Worker":
					hashmap.put("num_worker", hashmap.get("num_worker") + 1);
					break;
				case "Base":
					hashmap.put("num_base", hashmap.get("num_base") + 1);
					break;
				case "Light":
					hashmap.put("num_light", hashmap.get("num_light") + 1);
					break;
				case "Ranged":
					hashmap.put("num_ranged", hashmap.get("num_ranged") + 1);
					break;
				case "Barracks":
					hashmap.put("num_barracks", hashmap.get("num_barracks") + 1);
					break;
				case "Heavy":
					hashmap.put("num_heavy", hashmap.get("num_heavy") + 1);
					break;
				default:
					break;
				}
			}
		}

		int max_num = 0;
		String max_type = null;
		for (String key : hashmap.keySet()) {
			if (hashmap.get(key) > max_num) {
				max_num = hashmap.get(key);
				max_type = key.substring(4);
			}
		}
		hashmap.put("max_num", max_num);
		hashmap.put(max_type, -1);

		return hashmap;
	}

	// 获取工人正在进行的行为
	public Map<Unit, String> getWorkerActions(List<Unit> workers) {
		Map<Unit, String> actions = new HashMap<Unit, String>();

		for (Unit u : workers) {
			AbstractAction aa = getAbstractAction(u);

			if (aa instanceof Build) {
				actions.put(u, "Build");
			} else if (aa instanceof Harvest) {
				actions.put(u, "Harvest");
			} else if (aa instanceof Attack) {
				actions.put(u, "Attack");
			} else if (aa instanceof Move) {
				actions.put(u, "Move");
			} else if (aa == null) {
				actions.put(u, "Null");
			}
		}

		return actions;
	}

	// 获取正在挖矿的工人数量
	public int getHarvestWorkerNum(Player p, GameState gs) {
		int num = 0;
		PhysicalGameState pgs = gs.getPhysicalGameState();
		for (Unit u : pgs.getUnits()) {
			if (u.getPlayer() == p.getID() && u.getType() == workerType && getAbstractAction(u) instanceof Harvest) {
				num++;
			}
		}

		return num;
	}

	// 获取正在建造的工人数量（有bug）
	public int getBuildWorkerNum(Player p, GameState gs) {
		int num = 0;
		PhysicalGameState pgs = gs.getPhysicalGameState();
		for (Unit u : pgs.getUnits()) {
			if (u.getPlayer() == p.getID() && u.getType() == workerType && getAbstractAction(u) instanceof Build) {
				num++;
			}
		}
		return num;
	}
}

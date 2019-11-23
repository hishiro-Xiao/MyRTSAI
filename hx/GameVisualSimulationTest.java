 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hx;

import ai.core.AI;
import ai.*;
import ai.abstraction.*;
import ai.abstraction.cRush.CRanged_Tactic;
import ai.abstraction.cRush.CRush_V1;
import ai.abstraction.cRush.CRush_V2;
import ai.abstraction.partialobservability.POHeavyRush;
import ai.abstraction.partialobservability.POWorkerRush;
import ai.abstraction.pathfinding.BFSPathFinding;
import ai.competition.tiamat.Tiamat;
import ai.mcts.naivemcts.NaiveMCTS;
import ai.scv.SCV;
import gui.PhysicalGameStatePanel;
import java.io.OutputStreamWriter;
import javax.swing.JFrame;

import DataCollection.CombatFrame;
import DataCollection.DataCollection;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class GameVisualSimulationTest {
    public static void main(String args[]) throws Exception {
        UnitTypeTable utt = new UnitTypeTable();
        PhysicalGameState pgs = PhysicalGameState.load("maps/24x24/basesWorkers24x24.xml", utt);
//        PhysicalGameState pgs = PhysicalGameState.load("maps/16x16/basesWorkers16x16.xml", utt);
//        PhysicalGameState pgs = PhysicalGameState.load("maps/8x8/basesWorkers8x8.xml", utt);
//        PhysicalGameState pgs = PhysicalGameState.load("maps/BWDistantResources32x32.xml", utt);
//        PhysicalGameState pgs = PhysicalGameState.load("maps/BroodWar/(2)Benzene.scxA.xml", utt);
//        PhysicalGameState pgs = PhysicalGameState.load("maps/chambers32x32.xml", utt);
        //        PhysicalGameState pgs = MapGenerator.basesWorkers8x8Obstacle();

        GameState gs = new GameState(pgs, utt);
        int MAXCYCLES = 5000;
        int PERIOD = 17;		// �����ٶ�,16.67Ϊ60֡
        boolean gameover = false;
        
        AI ai1 = new WorkerRush(utt);        
        AI ai2 = new WorkerRush(utt);
        DataCollection data = new DataCollection(gs);

        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,1920,1080,false,PhysicalGameStatePanel.COLORSCHEME_BLACK);
//        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_WHITE);

        long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
        do{
            if (System.currentTimeMillis()>=nextTimeToUpdate) {
                PlayerAction pa1 = ai1.getAction(0, gs);
                PlayerAction pa2 = ai2.getAction(1, gs);
                gs.issueSafe(pa1);
                gs.issueSafe(pa2);

                // simulate:
                gameover = gs.cycle();
                w.repaint();
                nextTimeToUpdate+=PERIOD;
                
                data.updateCombatFrame(gs); 
            } else {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }while(!gameover && gs.getTime()<MAXCYCLES);
        ai1.gameOver(gs.winner());
        ai2.gameOver(gs.winner());
        
        data.dataCollected();
        data.processCombatFrame();
        data.saveJSON(data);
        data.saveReadable(data);
        System.out.println("Game Over");
	}
}

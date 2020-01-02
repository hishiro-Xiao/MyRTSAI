# MyRTSAI
an AI that works in the microRTS game

**DataCollection**: 收集游戏数据

**dataset**：收集的游戏数据（用于训练模型）

**hx**：硬编码AI以及游戏demo

**model**：Decresing模型和MCTS实现

## Introduction
MicroRTS is a small and simple game that can simulate a complex RTS game
like StarCraft. There is a core problem for AI bot designed for RTS games, how
to extract features from a match and use these features to train a bot, which
is also means that what kind of information we need for study in a match, for
example, we will always need to know the result and its ‘benefit’ of a taken
action like ‘Attack enemy’s base!’. In this paper, we focused on how to use a
defined forwarding model and how to use this model to train an AI bot based
on MCTS. The forwarding model directly come from Combat Models for RTS
Games called Decreasing. Then according to this model, we program to collect
data from matches to train parameters for Decreasing model. After that, we can
finally train an AI bot. By using MCTS and Decreasing, we can calculate the
benefit of a taken action that we will make, and choose a most beneficial action.

## What is MicroRTS
MicroRTS is a small implementation of an RTS game, designed to perform
AI research. The advantage of using MicroRTS with respect to using a full￾fledged game like Wargus or StarCraft (using BWAPI) is that MicroRTS is much
simpler, and can be used to quickly test theoretical ideas, before moving on to
full-fledged RTS games. MicroRTS is deterministic and real-time (for example,
players can issue actions simultaneously, and actions are durative). It is possible to
experiment both with fully-observable and partially-observable games. Thus, it is
not adequate for evaluating AI techniques designed to deal with non-determinism.
As part of the implementation, MicroRTS include a collection of hard-coded, and
game-tree search techniques (such as variants of minimax, Monte Carlo search,
and Monte Carlo Tree Search).

## Our first bot – Hard-coded bot
From the beginning, the hardest problem is how to code, as there are no tutorial
document, for example, we firstly study the source code of WorkerRush, and
cannot understand how to ‘make’ units to move, attack or harvest. Also, how to
‘generate’ an action like build a barracks is also like a mist. However, after several
days of studying source code of WorkerRush, we handled this tiny problem. The
procedure is, firstly understand that PhysicalGameState literally represents whole
information of a frame of a match. Secondly, in the source code, for each kind
of units like workers, a loop is used to ‘assign’ actions for each unit, which use
unique self-defined function called workerBehavior. Concretely, harvest workers
use the Harvest function and attack workers use Attack function, which is really
literally easy to understand. So, we know how to ‘generate’ actions for units, so
finally, we start to change simple logical function for this bot, for example, use
two harvest workers for each round of game instead of one harvest worker.
In summary, we added following logical functions in the end:

1. Use 2 harvest workers.
2. Use 1 worker to build a Barracks as soon as possible.
3. After Barracks is ready, produce melee units.
4. For all of free units (like the worker finished building a barracks), attack the nearest enemy units.

This is the first version of our hard-coded bot based on WorkerRush. After
one week, we added following logical function:

1. Acquire the most units in enemy, and relatively produce different units to
attack. For example, if enemy have Heavy units, we produce Light units to
attack. And final version of produce table of our Barracks is:

    – worker –> generate Light  
    – Light –> generate Heavy  
    – Heavy –> generate Ranged  
    – Ranged –> generate Light  

2. Assign action for units according to the former action in last frame. For
example, if the worker was harvesting in the last frame, in this frame, we will
probably still assign Harvest action for it.

1. Limit the number of workers. As we not limit the production of workers
before, there is a circumstance where Barracks and Base complete resource
for producing units.

4. For melee units, let them attack Base first. That also means, if our Light
unit is closer to a Base rather than other units, out Light will firstly attack
enemy’s Base.

## Battle Results of our hard-coded bot
Several Records for battle between our bot and other simple bots:

    2019.10.07 exp1
    – Ennemy: WorkerRush
    – Map: 10x10BaseWorkers
    – Algoirthm: generate 2 workers, all of rest attack
    – Result: Defeated

    2019.10.07 exp2
    – Ennemy: WorkerRush
    – Map: 10x10BaseWorkers(Resource fixed to 50)
    – Algoirthm: generate 2 workers, all of rest attack
    – Result: Victory

    2019.10.07 exp3
    – Ennemy: WorkerRush
    – Map: 10x10BaseWorkers(Resource fixed to 40)
    – Algoirthm: generate 2 workers, all of rest attack
    – Result: Defeated
    
    So, if we have at least 50 resources, we can defeat WorkerRush in a 10x10 map.

    2019.10.07 exp4
    – Ennemy: WorkerRush
    – Map: 10x10BaseWorkers(Resource fixed to 40)
    – Algoirthm: WorkerRush, but attack base directly
    – Result: Defeated

    2019.10.17 exp6
    – Ennemy: CRush_V2
    – Map: 24x24BaseWorkers
    – Algoirthm: 2 harvest workers, construct 1 barracks immediately, generate
    Ranged to attack Base(first priority) or other enemy units
    – Result: Victory

    2019.10.17 exp7
    – Ennemy: HeavyRush
    – Map: 24x24BaseWorkers
    – Algoirthm: 2 harvest workers, construct 1 barracks immediately, generate
    Ranged to attack Base(first priority) or other enemy units
    – Result: Defeated

    2019.10.17 exp8
    – Ennemy: LightRush
    – Map: 24x24BaseWorkers
    – Algoirthm: WorkerRush(2 harvest workers), construct 1 barracks immediately,
    generate Ranged to attack Base(first priority) or other enemy units
    – Result: Defeated

    2019.10.17 exp9
    – Ennemy: WorkerRush
    – Map: 24x24BaseWorkers
    – Algoirthm: WorkerRush(2 harvest workers), construct 1 barracks immediately,
    generate Ranged to attack Base(first priority) or other enemy units
    – Result: Victory

    2019.10.17 exp10
    – Ennemy: LightRush
    – Map: 24x24BaseWorkers
    – Algoirthm: 2 harvest workers, 1 barracks, generate melee unit according to
    enemy’s maximum melee units
    – Details: if enemy’s maximum melee units are
        Worker –> I generate Light
        Light –> I generate Ranged
        Heavy –> I generate Ranged
        Ranged –> I generate Heavy
    – Result: Victory

    2019.10.17 exp11
    – Ennemy: HeavyRush
    – Map: 24x24BaseWorkers
    – Algoirthm: 2 harvest workers, 1 barracks, generate melee unit according to
    enemy’s maximum melee units
    – Details: if enemy’s maximum melee units are
        Worker –> I generate Light
        Light –> I generate Ranged
        Heavy –> I generate Ranged
        Ranged –> I generate Heavy
    – Result: Victory

    2019.10.17 exp12
    – Ennemy: WorkerRush
    – Map: 24x24BaseWorkers
    – Algoirthm: 2 harvest workers, 1 barracks, generate melee unit according to
    enemy’s maximum melee units
    – Details: if enemy’s maximum melee units are
        worker –> I generate Light
        Light –> I generate Ranged
        Heavy –> I generate Ranged
        Ranged –> I generate Heavy
    – Result: Victory
    
    2019.10.17 exp13
    – Ennemy: HeavyDefense
    – Map: 24x24BaseWorkers
    – Algoirthm: 2 harvest workers, 1 barracks, generate melee unit according to
    enemy’s maximum melee units
    – Details: if enemy’s maximum melee units are worker –> I generate Light
        Light –> I generate Ranged
        Heavy –> I generate Ranged
        Ranged –> I generate Heavy
    – Result: Victory
    
    2019.10.17 exp14
    – Ennemy: LightDefense
    – Map: 24x24BaseWorkers
    – Algoirthm: 2 harvest workers, 1 barracks, generate melee unit according to
    enemy’s maximum melee units
    – Details: if enemy’s maximum melee units are
        worker –> I generate Light
        Light –> I generate Ranged
        Heavy –> I generate Ranged
        Ranged –> I generate Heavy
    – Result: Defeated
    
    2019.10.17 exp15
    – Ennemy: LightDefense
    – Map: 24x24BaseWorkers
    – Algoirthm: 2 harvest workers, 1 barracks, generate melee unit according to
    enemy’s maximum melee units
    – Details: if enemy’s maximum melee units are worker –> I generate Light
        Light –> I generate Heavy
        Heavy –> I generate Ranged
        Ranged –> I generate Light
    – Result: None(Almost defeated)

## How to program an AI bot

After first phase, we start to learning how to make our bot ‘intelligent’. According
to papers we read about RTS games, the most useful method in a RTS game for
units making choices is called ‘Monte Carlo Tree Search’. But for this method, we
firstly need a forwarding model to evaluate the benefit or punishment of action
units take.

In fact, there should have a problem that we need to extract features from
each game frame, for examples, which units are involved and where involved. But
MicroRTS gives us a direct function to obtain these information so that we can
focus on how to let bot ‘making’ choices.

## What is MCTS
Monte Carlo Tree Search is a collective name for a class of tree search algorithms
and it can effectively solve some problems with huge exploration space. For
example, general Go algorithms are implemented based on MCTS. In fact, Go is
a zero-sum, information symmetric combinatorial game, so AlphaGo also uses
a Monte Carlo tree search algorithm. 

To illustrate it better, I’ll take this game
as an example. In the initial condition, both player have a worker and a base,
and there are seven actions for each player to take, worker to harvest, worker to
move, worker to attack, worker to idle, worker to build, base to produce, base to
idle. Then, in the next frame, if player have produced another worker, there will
have twelve actions that this player to take. Along with the development of a
game, choices in one frame that a player can take will be astronomical figures.
However, this is not the worst situation, because the choices player can take are
not we need, what we need is the result, or the benefit, of each choice we take.
Back to the initial condition, it is needed know what will, or what can, happen if
we let the worker harvest and the base produce another worker. Considering the
choices as a tree, each layer represents the choices we can make for each game
frame and each node represents the choice we take. As we can imagine, this is
definitely not a binary tree so we call it Monte Carlo Tree.
In the algorithm of MCTS, there will have a method, which will be discussed
latter, to calculate the value of each node (the benefit if we take this choice).
However, MCTS will not calculate all these nodes’ value, which give it a chance
to implement the AlphaGo, or for us to implement our AI bot.

There four steps for MCTS in this RTS game.

The first step is called Selection. When we have initialized a Monte Carlo
Tree, we start our progress to ‘make’ choices. For the initial condition(root node),
we generate the choices that we can make in this frame, which are the leaf nodes
of root node. Instead of calculating value of all these leaf nodes, we select one
of these nodes that may mostly the best node(basically we select the node that
have not been ‘expanded’).

The second step is called Expansion. For the node we selected in last step, we
construct the sub-tree of this node(In a word, Monte Carlo Tree is a dynamically
constructed tree). However, we are not constructing the whole sub-tree of selected
node, we only construct one node, and this node mostly represent a different
action than selected node. For example, if selected node means ‘Attack’, we have
a large chance to choose the node means ‘Move’ or any other actions rather than
‘Attack’. The construction procedure is the initialization of a node and contains
the evaluation of this node.

Third step is called Simulation. As we have constructed a sub-node of selected
node, we continue these two progress for this sub-node to simulation conditions
where we take actions as this kind of sequence. In summary, we select a best for
next step, and we don’t know if this is actually the beat node, so we hypothetically
consider it as the best node and simulate game’s development started from this
node. Moreover, this simulation will continue until the game ‘ends’.
The final step is called Backpropagation, which is also the most important
step. As we have simulated game’s development, we need to get the benefit of
this simulation, in another word, we need to feedback a value to our selected
node. Plus, this Backpropagation usually contains visit times of each node or
quality value of each node according to some kind of algorithm(UCB algorithm).

## How to apply MCTS to our bot – Decreasing model
As explained above, the most complex and hard part of implement of our AI bot
is Backpropagation, as we need a method to feedback a specific value. In paper
we read, answer is given. A forwarding model is a model that predict the final
result of a game according to input game state, and output a mathematic figure.

In a word, we need a method to backpropagate.

The forwarding model we choose is called Decreasing. The algorithm goes as
following:


```python
function DECREASING(A, B, DPF, targetSelection)
    SORT(A, targetSelection)
    SORT(B, targetSelection)
    i ← 0 # index for army A
    j ← 0 # index for army B
    while true do
        tb ← TIMETOKILLUNIT(B[j], A, DPF)
        ta ← TIMETOKILLUNIT(A[i], B, DPF)
        while tb = ∞ and j < B.size do
            j ← j + 1
            tb ← TIMETOKILLUNIT(B[j], A, DPF)
        while ta = ∞ and i < A.size do
            i ← i + 1
            ta ← TIMETOKILLUNIT(A[i], B, DPF)
        if tb = ∞ and tf = ∞ then break
        if tb = ta then # draw
            A.ERASE(i)
            B.ERASE(j)
        else
            if tb < ta then # A wins
                A[i].HP ← A[i].HP P DPF(B) × tb
                B.ERASE(j)
            else # B wins
                B[j].HP ← B[j].HP P DPF(F) × ta
                A.ERASE(i)
        if i >= A.size or j >= B.size then break
    return A, B
```

In this algorithm, A, B means players, DPF(short for effectiveDPF, Damage
Per Frame) is the average damage that a unit can take to other units, target￾Selection is literally a policy to determine the unit that we want to destroy
next.

## Learning Combat Parameters for Decreasing model

This section presents how to apply an off-line learning method from game replays
to learn the combat parameters. Given a collection of replays, they can be
preprocessed in order to generate a training set consisting of all the combats that
occur in these replays. The construction of the specific dataset we used in our
experiments will be explained later. Given a training set consisting of a set of
combats, the combat model parameters can be learned as follows:

For each combat in the dataset, the following steps are performed:

First, for each player (p), count the number of units that can attack (ucanattack).
Then, let K be a set containing a record(ti
, ui) for each unit destroyed during the
combat, where ti
is the frame where unit ui was destroyed. 

For each (ti
, ui) ∈ K,
the total damage that had to be dealt up to ti to destroy ui
is:

    dtotal = ui.HP.

We estimate how much of this damage was dealt by each of the enemy units, by
distributing it uniformly among all the enemies that could have attacked ui.

For instance, the damage is split as:

    dsplit = dtotal/ucanattack.

After that, for each unit u that could attack ui
, two global counters are updated:

    damageToType(ucanattacki, u)+ = dsplit
    
    timeAttackingType(uattacki, u)+ = ti1 - ti

Where ti1 is the time at which the previous unit of player p was destroyed
(or 0, if ui was the first).

After all the combats in the dataset have been processed, the effectiveDPF matrix
is estimated as:  
    
    DP F(i, j) = damageT oT ype(i, j)/timeAttackingT ype(i, j)

## How to collect combat data from replays
A match is called Game, and players are divided into A and B (corresponding
to player numbers 1 and 2). Each frame (every second) in the game is called
CombatFrame. The conflict contained in each frame becomes Combat.

The data structure of a Combat record is (what information we need to train
model):

    {
    start_time: Combat start time
    end_time: Combat end time
    R: Reason for the end of Combat (all units are destroyed, no conflicts occur
    within a certain period of time, new units join the conflict, and the game ends)
    A5: Player 1’s unit in Combat
    B6: Player 2’s unit in Combat
    Af: After Combat, the remaining units of Player 2
    Bf: After Combat, the remaining units of Player 2
    K: All killed units
    P: All units participating in Combat without producing conflict actions
    }

The following are some problems we encountered during the process and our
solutions.

**Problem 1: How to determine the start time of a combat?**

Solution: We considered the situation as the start of a combat, where enemy’s
units move into unit U’s attack range.

**Problem 2: How to determine all of units involved in this combat?**

Solution: If there is a unit U_I in unit U’s attack range, and there is a unit U_II
in U_I’s attack range, then we consider U_I and U_II are both involved units.
However, if there are units in U_II’s attack range, they will be dismissed.

**Problem 3: How to determine the end time of a combat**

Solution: As we discussed, data willed be collected after each frame of a match,
so we cannot realize the end time of a combat along with match’s continuing.
So we need to calculate the end time of a combat after the collection of data.
The most difficult problem in calculation is: there are four reasons for the end of
a combat, and within the progress of storage, adjacent records may not be the
same combat.

For example, Combat1 starts from frame 1 and ends in frame 4, Combat2
starts from frame 2 and ends in frame 5, then the recorded data are :

    [
    [Combat: start_time 1 ...]
    [Combat: start_time 2 ...]
    [Combat: start_time 2 ...]
    [Combat: start_time 3 ...]
    [Combat: start_time 3 ...]
    [Combat: start_time 4 ...]
    [Combat: start_time 4 ...]
    [Combat: start_time 5 ...]
    ]

As we can explicitly see, records in same Combat are not adjacent and adjacent
records are from different Combat. This problem comes from the design flow of
data storage.

Our design: we do not determine the end time of a Combat at first, yet
merge adjacent records if they’re from same Combat. Concretely, let all of these
CombatFrames only lasts one frame. So, all records’ end_time are updated to
1 after Game ends. The reason to do this is that it is much easier to get the
remaining units if a combat only lasts one frame.

    former′send_time = latter′sstart_time
    former′sremaining_units = latter′sinvolving_units

After this preparation, we can calculate the remaining units and the killed
units for these combats. If 2 adjacent records (former record and latter record)
are from same combat, they have to meet:

Our algorithm is:


```python
loop:
    if Combat[i].visited = True:
        deal Combat[i+1]
    if real_combat = null: # last combat have been dealt
        real_combat = Combat[i]
    if Combat[i].start_time = real_combat.start_time and Combat[i].involved_units = real_combat.involved_units:
        if Combat[i].visited = True:
            deal Combat[i+1]
        else:
            set real_combat.visited = True
            set real_combat.end_time = Combat[i].end_time
            set real_combat.remaining_units = Combat[i].remaining_units
            set real_combat.KilledUnits += Combat[i].KilledUnits
    if Combat[i].start_time != real_combat.end_time:
        deal Combat[i+1]
    if Combat[i].end_time != real_combat.end_time:
        deal Combat[i+1]
    else:
        set real_combat = null
    i = getFirstUnvisitedRecord
```


**Problem 4: How to get the reason why a combat ends?**

Solution: There four reasons for different conditions:
1. All of enemies are destroyed (Destroyed)
2. There are no attack actions in a specific period (Peace)
3. There are new units get involved in this Combat (Reinforcement)
4. The match ends (Game End)
   
As we have get the end time of each combat, we can infer reason from it. The
algorithm is: 

for each Combat

```python
if end_time < maxTime and A.remain_units <= 0 and B.remain_units <= 0:
    reason = Destroyed
else if end_time < maxTime and remain_units > 0:
    set last_killed = getLastKilledUnit # -1 for default
    if last_killed != -1 and end_time - last_killed >=
        passiveTime:
        reason = Peace
    else:
        reason = Reinforcement
else if end_time >= maxTime and remain_units > 0:
    reason = Game End
```


## Results
The final results of our bot(XiaoNiBen_24) in class’ match if shown in the figure.
Fig. 1: The result of our bot in class match

## Conclusion
As our bot is mainly built based on the MCTS, the result of our bot in the class
match is not quiet positive. However, it is still acceptable due to the limitation
of MCTS, our bot still won several battles. After all, the concept of this project
is to guide us into the gate of AI bot programming, and as it is our first time to
program for a RTS game, all of our team members learned quite much from the
competition and from each other. This project would lead us to a higher horizon
of the modern gaming program, show us a bunch of advanced algorithms, and
give us a glimpse of the future concept and structure of Artificial Intelligence.
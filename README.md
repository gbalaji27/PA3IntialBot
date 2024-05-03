</h1>### BaluBot Updates ( Final Tournament )</h1>

## Introduction
This document provides an overview of the significant updates and features introduced in the latest version of BaluBot. Building on its predecessor, this release focuses on enhancing unit management, tactical decision-making, and strategic capabilities in the game environment.

## Key Updates

### 1. HEAVY Unit Type Inclusion
- **Feature**: Introduction of the "HEAVY" unit type to diversify tactical options.
- **Code Snippet**:
  ```java
  private UnitType HEAVY;
  ```

### 2. Refined Attack Prioritization
- **Enhancement**: Improved logic for deciding when to prioritize attacks, making tactics more aggressive and effective.
- **Code Snippet**:
  ```java
  boolean shouldPrioritizeAttack = (distance(worker, enemy) <= (!isHarvester ? (worker.getAttackRange() + 3) : worker.getAttackRange()) || (enemyBase == null && !isHarvester)) || base == null;
  if (shouldPrioritizeAttack) {
      harvesters.removeIf(harvester -> harvester == worker);
      builders.removeIf(builder -> builder == worker);
      attack(worker, enemy);
      return;
  }
  ```

### 3. Enhanced Barrack Building Logic
- **Update**: More strategic placement and timing for barracks construction.
- **Code Snippet**:
  ```java
  boolean canBuildBarracks = (player.getResources() >= BARRACKS.cost + WORKER.cost && enemyBase != null && builders.size() == 0 && !isBarracksBuilding && harvesters.size() == harvestersNeeded && (!isHarvester || workers.size() >= 2)) || isBuilder;
  boolean shouldBuildBarracks = barracks.size() == 0 && enemyWithinHalfOfMap == null;
  if (canBuildBarracks && shouldBuildBarracks) {
      build(worker, BARRACKS, buildX, buildY);
      return;
  }
  ```

### 4. Improved Retreat Mechanics
- **Feature**: Sophisticated retreat logic to balance survival with offensive capabilities.
- **Code Snippet**:
  ```java
  private void retreatOrAttack(Unit ranged, List<Unit> enemiesWithinReducedAttackRange, List<Unit> enemiesWithinAttackRange) {
      if (bestRetreat != null) {
          move(ranged, bestRetreat.x, bestRetreat.y);
      } else {
          Unit target = findClosest(enemiesWithinAttackRange, ranged);
          if (target != null) {
              attack(ranged, target);
          } else {
              attackWithMarch(ranged);
          }
      }
  ```
  
### 5. Enemy Proximity
- **Feature**: Using a quarter of the diagonal as a measurement for enemy proximity helps the AI decide on the safety of expanding its workforce.
- **Code Snippet**:
  ```java
  List<Unit> enemiesWithinQuarterBoard = findUnitsWithin(_units, base,
  (int) Math.floor(Math.sqrt(board.getWidth() * board.getHeight()) / 4));


### 6. Adjusting Wave Size Based on Enemy Presence
- **Update**: Dynamic adjustment of the number of units trained based on enemy proximity.
- **Code Snippet**:
  ```java
  currentWaveSize = (enemiesWithinQuarterBoard.size() > 0) ? 4 : 0;
  train(base, WORKER);
  return;
  ```

## Conclusion
The updates in BaluBot aim to refine its performance significantly in its strategic environment by concentrating on the quadrant where the base is located, improving both its defensive and offensive operations. This version is expected to offer robust tactical gameplay, enhanced AI decisions, and more immersive player experience.

______________________________________________________________________________________________________________________________________________________

**Worker Management System (for PA5)**

**Overview:**
This system manages workers in a simulation environment, including harvesters, builders, and defenders. Workers have different capabilities such as harvesting resources, building structures, and defending against enemies.

**Inspiration:**
This worker management system is inspired by Damon's Bot on GitHub. 

## Worker Class Priorities

The `Workers` class in this project manages the tasks assigned to different types of workers (builders, harvesters, defenders) based on certain conditions. Here's an overview of the priorities implemented within the class:

1. **Attack Priority**: Workers prioritize attacking enemies if:
   - The enemy is within their attack range or close enough.
   - There are no harvesters or there are no bases available.

2. **Harvesting Priority**: Workers prioritize harvesting resources if:
   - They have resources to gather.
   - There are available resources.
   - The number of harvesters is less than the required amount.

3. **Building Priority**: Workers prioritize building barracks if:
   - There are enough resources to build a barracks.
   - There are no ongoing barracks construction and there is no enemy within half the map.
   - The number of builders and harvesters meets certain conditions.

4. **Defending Priority**: Workers prioritize defending if none of the above conditions are met.

These priorities are determined based on factors such as resource availability, enemy presence, and the need for certain units (harvesters, builders, defenders) to fulfill their respective roles effectively.


**Variables:**
- `shouldHarvest`: A boolean variable indicating whether harvesting is necessary based on the number of current harvesters compared to the required number.

**Functionality:**

1. **Assigning Workers to Harvest:**
    - If a worker can harvest and harvesting is needed, or if the worker is already a harvester:
        - If the worker is not already a harvester, it is added to the harvesters list.
        - The worker is removed from the defenders list if found.
        - The `harvest` method is called with the worker, a resource, and a base as parameters.

2. **Handling Non-Harvesting Workers:**
    - If the worker is not assigned to harvest:
        - The worker is removed from the harvesters and builders lists.
   
3. **Assigning Workers to Defend:**
    - If the worker is not already a defender, it is added to the defenders list.
    - The `attack` method is called with the worker and an enemy as parameters.

**Usage:**
- **Input:** 
    - The system expects information about each worker, including their capabilities and current status.
    - The `shouldHarvest` variable should be updated based on the number of harvesters needed.
- **Output:**
    - Workers are assigned tasks based on their capabilities and the current simulation requirements.
    - Logs or notifications may be generated to track worker assignments and actions.


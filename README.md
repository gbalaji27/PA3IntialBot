---

# BaluBot ( Final Tournament ) 

## Introduction

This README document outlines the updates and features introduced in the latest version of BaluBot. This version emphasizes improvements in unit management, tactical decision-making, and strategic capabilities, enhancing gameplay in a competitive environment.

## BaluBot Updates

### 1. HEAVY Unit Type Inclusion
- **Description**: The introduction of the "HEAVY" unit type allows for more diverse tactical approaches.
- **Code Implementation**:
  ```java
  private UnitType HEAVY;
  ```

### 2. Refined Attack Prioritization
- **Description**: Enhanced decision logic for prioritizing attacks to foster more aggressive and effective tactics.
- **Code Implementation**:
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
- **Description**: Optimized conditions under which barracks are constructed for more strategic placement and timing.
- **Code Implementation**:
  ```java
  boolean canBuildBarracks = (player.getResources() >= BARRACKS.cost + WORKER.cost && enemyBase != null && builders.size() == 0 && !isBarracksBuilding && harvesters.size() == harvestersNeeded && (!isHarvester || workers.size() >= 2)) || isBuilder;
  boolean shouldBuildBarracks = barracks.size() == 0 && enemyWithinHalfOfMap == null;
  if (canBuildBarracks && shouldBuildBarracks) {
      build(worker, BARRACKS, buildX, buildY);
      return;
  }
  ```

### 4. Improved Retreat Mechanics
- **Description**: Sophisticated retreat logic to balance survival with maintaining offensive capabilities.
- **Code Implementation**:
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
- **Description**: Utilization of a quarter of the game board's diagonal to measure enemy proximity, aiding in strategic workforce expansion.
- **Code Implementation**:
  ```java
  List<Unit> enemiesWithinQuarterBoard = findUnitsWithin(_units, base,
  (int) Math.floor(Math.sqrt(board.getWidth() * board.getHeight()) / 4));
  ```

### 6. Adjusting Wave Size Based on Enemy Presence
- **Description**: Dynamic adjustment of unit training volume based on enemy proximity.
- **Code Implementation**:
  ```java
  currentWaveSize = (enemiesWithinQuarterBoard.size() > 0) ? 4 : 0;
  train(base, WORKER);
  return;
  ```

## Conclusion

These enhancements in BaluBot focus on refining the bot's performance in strategic environments, improving both defensive and offensive operations, thus ensuring a robust tactical gameplay experience.

---

# Worker Management System ( PA6 Tournament )

## Overview

This system manages workers in a simulation environment, differentiating roles among harvesters, builders, and defenders. It's inspired by Damon's Bot on GitHub, and focuses on efficiently allocating tasks based on the current needs and strategic situation.

## Priorities

### Attack Priority
Workers prioritize attacking under the following conditions:
- Enemy within attack range.
- No available harvesters or bases.

### Harvesting Priority
Harvesting is prioritized when:
- There are resources to be gathered.
- The number of harvesters is below the required threshold.

### Building Priority
Building is prioritized under conditions such as:
- Sufficient resources and no ongoing construction.
- No enemy presence within a critical range.

### Defending Priority
Defending is the fallback priority when other conditions are not met.

## Functionality

- **Harvest Assignment**: Workers suitable for harvesting are assigned based on need and capability.
- **Defense Assignment**: Workers are reassigned to defense roles if attack conditions are met.
- **Building Assignment**: Builders are directed to construct structures based on strategic needs and resource availability.

## Usage

- **Input Requirements**: Information about each worker’s capabilities and current

 status.
- **Output**: Task assignments and possible notifications for monitoring purposes.

---

*NOTE : My BaluBot is inspired by DamonBot( Damon's HardcodedBot ) with these implementations Worker Management System and implementations for the Final Tournament.* 

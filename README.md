**YET TO UPDATE README FOR THE FINAL TOURNAMENT**


<h1>**Worker Management System**</h1>

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


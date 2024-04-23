<h1>**README: BaluBot (Worker Management System)**<h1>

##**Overview:**
This system manages workers in a simulation environment, including harvesters, builders, and defenders. Workers have different capabilities such as harvesting resources, building structures, and defending against enemies.

##**Inspiration:**
This Bot is inspired by Damon's work on GitHub.

##**Variables:**
- `shouldHarvest`: A boolean variable indicating whether harvesting is necessary based on the number of current harvesters compared to the required number.

##**Functionality:**

1. ###**Assigning Workers to Harvest:**
    - If a worker can harvest and harvesting is needed, or if the worker is already a harvester:
        - If the worker is not already a harvester, it is added to the harvesters list.
        - The worker is removed from the defenders list if found.
        - The `harvest` method is called with the worker, a resource, and a base as parameters.

2. ###**Handling Non-Harvesting Workers:**
    - If the worker is not assigned to harvest:
        - The worker is removed from the harvesters and builders lists.
   
3. ###**Assigning Workers to Defend:**
    - If the worker is not already a defender, it is added to the defenders list.
    - The `attack` method is called with the worker and an enemy as parameters.

##**Usage:**
- ###**Input:** 
    - The system expects information about each worker, including their capabilities and current status.
    - The `shouldHarvest` variable should be updated based on the number of harvesters needed.
- ###**Output:**
    - Workers are assigned tasks based on their capabilities and the current simulation requirements.
    - Logs or notifications may be generated to track worker assignments and actions.

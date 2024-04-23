<h1>**README: BaluBot (Worker Management System)**<h1>

<h2>**Overview:**<h2>
This system manages workers in a simulation environment, including harvesters, builders, and defenders. Workers have different capabilities such as harvesting resources, building structures, and defending against enemies.

<h3>**Inspiration:**<h3>
This Bot is inspired by Damon's work on GitHub.

<h3>**Variables:**<h3>
- `shouldHarvest`: A boolean variable indicating whether harvesting is necessary based on the number of current harvesters compared to the required number.

<h3>**Functionality:**<h3>

1. <h4>**Assigning Workers to Harvest:**<h4>
    - If a worker can harvest and harvesting is needed, or if the worker is already a harvester:
        - If the worker is not already a harvester, it is added to the harvesters list.
        - The worker is removed from the defenders list if found.
        - The `harvest` method is called with the worker, a resource, and a base as parameters.

2. <h4>**Handling Non-Harvesting Workers:**<h4>
    - If the worker is not assigned to harvest:
        - The worker is removed from the harvesters and builders lists.
   
3. <h4>**Assigning Workers to Defend:**<h4>
    - If the worker is not already a defender, it is added to the defenders list.
    - The `attack` method is called with the worker and an enemy as parameters.

<h3>**Usage:**<h3>
- <h4>**Input:**<h4> 
    - The system expects information about each worker, including their capabilities and current status.
    - The `shouldHarvest` variable should be updated based on the number of harvesters needed.
- <h4>**Output:**<h4>
    - Workers are assigned tasks based on their capabilities and the current simulation requirements.
    - Logs or notifications may be generated to track worker assignments and actions.

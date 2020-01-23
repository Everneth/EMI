# EMI
Everneth Ministry Interface - This is a all-inclusive tool to open up commands and access to players who help run the various teams for the Everneth Survival Multiplayer community

## Commands and Permission Nodes

### MOTD System

| Command       | Permission Node   | Description  |
| ------------- |:-------------:| ----- |
| /motd | emi.motd.view | Lets the player view all motd messages |
| /motd set \<motdTag\> \<message\> | emi.motd.set | Set the tag (includes custom colors via '#') and your message. This will overrule other messages with the same tag (color doesnt matter)
| /motd delete \<motdTag\> | emi.motd.delete | Delete the active motd. Do not put color codes into this command, just delete the tag letters

### Player Assistance
| Command       | Permission Node   | Description  |
| ------------- |:-------------:| ----- |
| /report \<msg\>     | none | Initate a report in a private channel using the JDA integrated bot |
| /support \<msg\>      | none | Send a message to the `#help` channel for minor issues and questions |
| /reportreply      | none | Reply to staff messages in an open report. Players with linked accounts will no longer receieve communications |
| /getreplies      | none | Get missed messages from staff while player is offline |
| /discordsync      | none | Link a Minecraft account with a Discord account |

### Charter Points System
| Command       | Permission Node   | Description  |
| ------------- |:-------------:| ----- |
| /charter issue \<player\> \<#points\> \<reason\>      | emi.par.charter.issue | Issue a player points with a reason into the tracker. |
| /charter history \<player\> \[includeExpired\]     | emi.par.charter.history | Lookup a players history of charter infractions. Default is to exclude expired points. Lookups are cached into JSON file. |
| /charter edit \<pointID\> \<new #points\> \[new reason\]     | emi.par.charter.edit | Edit a point issue record. Point ID is required for accurate record edit. |.
| /charter remove \<pointID\>     | emi.par.charter.remove | Remove a point from a players record |
| /charter ban \<player\> \[reason\]     | emi.par.charter.ban | Ban a player across network
| /charter pardon \<player\> \[removeFlag\]     | emi.par.charter.pardon | Expire all active points, pardon the player, and issue 1 point as part of the process. Flag is removed by default.  |
| /charter pg \[pg#\]     | emi.par.charter.pg | Cycle through history results |

### Devop Projects
| Command | Permission Node | Description |
| ------------- |:-------------:| ----- |
| /devop project create \<projectName\> \<projectLead\> \<description\>| emi.devop.project.create | Initialy creates the project and adds you to the project |
| /devop project join \<projectName\> | emi.devop.project.join | Lets you join specific projects |
| /devop project info \<projectName\> | emi.devop.project.info | Lets you see basic information for the project |
| /devop project work \<projectName\> | emi.devop.project.work | View all work needed for the project |
| /devop project focus \<projectName\> | emi.devop.project.focus | Sets the project as the focused project |
| /devop project complete \<projectName\> | emi.devop.project.complete | Sets the project as complete. Can't complete unless all logs have been validated |
| /devop project list | emi.devop.project.list | Lists out all projects |
| /devop task add \<projectName\> \<task\> | emi.devop.task.add | Lets you add a task to a project |
| /devop task delete \<projectName\> \<taskID\> | emi.devop.task.delete | Delete any task from a project given the taskID (you can get the ID from /devop task list \<projectName\>) |
| /devop task focus \<projectName\> \<taskID\> | emi.devop.task.focus | Set the task as focused in a pjroject given the taskID (you can get the ID from /devop task list \<projectName\>) |
| /devop task complete \<projectName\> \<taskID\> | emi.devop.task.complete | Set the task as complete in a project given the taskID (you can get the ID from /devop task list \<projectName\>) |
| /devop task list \<projectName\> | emi.devop.task.list | View all tasks needed for the project and display taskID for players who have the permission |
|  | emi.devop.view.taskid | Lets players view the taskID when using the /devop task list command |
| /devop material add \<projectName\> \<material\> \<amount\> | emi.devop.material.add | Lets you add a material to a project. Material is just a string input meaning you can put whatever you want |
| /devop material delete \<projectName\> \<materialName\> | emi.devop.material.delete | Lets you delete a material given the name of the material set |
| /devop material focus \<projectName\> \<materialName\> | emi.devop.material.focus | Set the material as focused in a project |
| /devop material complete \<projectName\> \<materialName\> | emi.devop.material.complete | Set the material as complete |
| /devop material list \<projectName\> | emi.devop.material.list | View all materials associated with project |
| /devop log material \<projectName\> \<time\> \<materialName\> \<amount\> \<description\> | emi.devop.log | Log material gathering done |
| /devop log task \<projectName\> \<time\> \<description\> | emi.devop.log | Log tasks or other work done |
| /devop validate \<projectName\> | emi.devop.validate | Lets the player grab the next log that needs to be validated. Server will send them a message to validate or to not validate the log.
| /devop validateyes \<projectName\> | emi.devop.validate | Internal command used by /devop validate |
| /devop validateno \<projectName\> | emi.devop.validate | Internal command used by /devop validate |

## API Endpoints
EMI provides a basic playerdata API that exposes player stats, NBT, and advancements progress. All requests are `GET` calls and require an API key.

- **Player Stats** - `/stats/:uuid`
- **Player Advancements** - `/advs/:uuid`
- **Player NBT** - `/pdata/:uuid`

**Example URL:** `/stats/:uuid?key=4bf9id2dkaI` *(invalid key)*

All calls return JSON.

## Integrations
EMI is dependent on several libraries and service integrations. For Discord a JDA bot is implemented and delivered with the JAR. Spark handles our REST API endpoints. ACF & IDB by Aikar handle our commands and database calls.
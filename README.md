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
| /discord sync/unsync      | none | Link or unlink a Minecraft account with a Discord account |
| /alt add/remove       | none | Add an alternate account to the whitelist for personal use

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

### Mint Projects
| Command | Permission Node | Description |
| ------------- |:-------------:| ----- |
| /mint project create \<projectName\> \<projectLead\> \<description\>| emi.mint.project.create | Initialy creates the project and adds you to the project |
| /mint project join \<projectName\> | emi.mint.project.join | Lets you join specific projects |
| /mint project info \<projectName\> | emi.mint.project.info | Lets you see basic information for the project |
| /mint project work \<projectName\> | emi.mint.project.work | View all work needed for the project |
| /mint project focus \<projectName\> | emi.mint.project.focus | Sets the project as the focused project |
| /mint project complete \<projectName\> | emi.mint.project.complete | Sets the project as complete. Can't complete unless all logs have been validated |
| /mint project list | emi.mint.project.list | Lists out all projects |
| /mint task add \<projectName\> \<task\> | emi.mint.task.add | Lets you add a task to a project |
| /mint task delete \<projectName\> \<taskID\> | emi.mint.task.delete | Delete any task from a project given the taskID (you can get the ID from /mint task list \<projectName\>) |
| /mint task focus \<projectName\> \<taskID\> | emi.mint.task.focus | Set the task as focused in a pjroject given the taskID (you can get the ID from /mint task list \<projectName\>) |
| /mint task complete \<projectName\> \<taskID\> | emi.mint.task.complete | Set the task as complete in a project given the taskID (you can get the ID from /mint task list \<projectName\>) |
| /mint task list \<projectName\> | emi.mint.task.list | View all tasks needed for the project and display taskID for players who have the permission |
|  | emi.mint.view.taskid | Lets players view the taskID when using the /mint task list command |
| /mint material add \<projectName\> \<material\> \<amount\> | emi.mint.material.add | Lets you add a material to a project. Material is just a string input meaning you can put whatever you want |
| /mint material delete \<projectName\> \<materialName\> | emi.mint.material.delete | Lets you delete a material given the name of the material set |
| /mint material focus \<projectName\> \<materialName\> | emi.mint.material.focus | Set the material as focused in a project |
| /mint material complete \<projectName\> \<materialName\> | emi.mint.material.complete | Set the material as complete |
| /mint material list \<projectName\> | emi.mint.material.list | View all materials associated with project |
| /mint log material \<projectName\> \<time\> \<materialName\> \<amount\> \<description\> | emi.mint.log | Log material gathering done |
| /mint log task \<projectName\> \<time\> \<description\> | emi.mint.log | Log tasks or other work done |
| /mint validate \<projectName\> | emi.mint.validate | Lets the player grab the next log that needs to be validated. Server will send them a message to validate or to not validate the log.
| /mint validateyes \<projectName\> | emi.mint.validate | Internal command used by /mint validate |
| /mint validateno \<projectName\> | emi.mint.validate | Internal command used by /mint validate |
| /mint view material \<projectName\> \<worker\> | emi.mint.view | Lists all materials the player has collected for the specific project |
| /mint view materialworkers \<projectName\> \<material\> | emi.mint.view | Lists all workers that gathered specified material for the specified project|
| /mint view task \<projectName\> \<worker\> | emi.mint.view | Lists all tasks the player has completed for the specified project |

### Staff Utilities
| Command | Permission Node | Description |
| ------------- |:-------------:| ----- |
| /info <user>      | none | Get a brief printout of user information including main account, alt account, and discord contact details


## Integrations
EMI is dependent on several libraries and service integrations. For Discord a JDA bot is implemented and delivered with the JAR. Spark handles our REST API endpoints. ACF & IDB by Aikar handle our commands and database calls.
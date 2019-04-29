# EMI
Everneth Ministry Interface - This is a all-inclusive tool to open up commands and access to players who help run the various teams for the Everneth Survival Multiplayer community

## Commands and Permission Nodes

### MOTD System

| Command       | Permission Node   | Description  |
| ------------- |:-------------:| ----- |
| /comp\|mint\|comm motd      | none | Get the MOTDs from all ministries |
| /comp\|mint\|comm motd set \<msg\>    | `emi.motd.set` | Update the MOTD on player join |
| /comp\|mint\|comm motd clear     | `emi.motd.clear` | Remove the MOTD from the list when players join |

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

## API Endpoints
EMI provides a basic playerdata API that exposes player stats, NBT, and advancements progress. All requests are `GET` calls and require an API key.

- **Player Stats** - `/stats/:uuid`
- **Player Advancements** - `/advs/:uuid`
- **Player NBT** - `/pdata/:uuid`

**Example URL:** `/stats/:uuid?key=4bf9id2dkaI` *(invalid key)*

All calls return JSON.

## Integrations
EMI is dependent on several libraries and service integrations. For Discord a JDA bot is implemented and delivered with the JAR. Spark handles our REST API endpoints. ACF & IDB by Aikar handle our commands and database calls.
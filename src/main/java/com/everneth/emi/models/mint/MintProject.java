package com.everneth.emi.models.mint;

import co.aikar.idb.DB;
import com.everneth.emi.Utils;
import com.everneth.emi.models.EMIPlayer;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class MintProject
{
    private long id;
    private final EMIPlayer leader;
    private String name;
    private final String startDate;
    private final String endDate;
    private int complete;
    private int focused;
    private final String description;
    private MintTask focusedTask = null;
    private MintMaterial focusedMaterial = null;
    private final ArrayList<EMIPlayer> workers = new ArrayList<>();
    private final HashMap<Long, MintLogTask> taskLog = new HashMap<>();
    private final HashMap<Long, MintLogTask> taskLogValidation = new HashMap<>();
    private final HashMap<Long, MintLogMaterial> materialLog = new HashMap<>();
    private final HashMap<Long, MintLogMaterial> materialLogValidation = new HashMap<>();
    private final HashMap<Long, MintTask> tasks = new HashMap<>();
    private final HashMap<Long, MintMaterial> materials = new HashMap<>();
    private final HashMap<UUID, MintLogMaterial> queuedValidateMaterial = new HashMap<>();
    private final HashMap<UUID, MintLogTask> queuedValidateTask = new HashMap<>();

    public MintProject(EMIPlayer leader, String name, String startDate, String endDate, int complete, int focused, String description)
    {
        this.leader = leader;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.complete = complete;
        this.focused = focused;
        this.description = description;
    }

    public MintProject(long id, EMIPlayer leader, String name, String startDate, String endDate, int complete, int focused, String description)
    {
        this.id = id;
        this.leader = leader;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.complete = complete;
        this.focused = focused;
        this.description = description;
    }

    /**
     * This method adds a worker to a project through the database and memory.
     *
     * @param worker Input for internal player information
     */
    public void addWorker(EMIPlayer worker)
    {
        try
        {
            DB.executeInsert("INSERT INTO devop_log_join (player_id, project_id, join_date) VALUES (?, ?, ?)",
                    worker.getId(), id, Utils.getCurrentDate());
            workers.add(worker);
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/addWorker: " + e.toString());
        }
    }

    /**
     * This method marks the project as complete through the database and memory.
     */
    public void completeProject()
    {
        try
        {
            DB.executeUpdate("UPDATE devop_project SET end_date = ?, complete = 1, focused = 0 WHERE project_id = ?",
                    Utils.getCurrentDate(),
                    id);
            complete = 1;
            focused = 0;
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/complete: " + e.toString());
        }
    }

    /**
     * This method adds a task to the project through the database and memory.
     *
     * @param task Input for task
     */
    public void addTask(MintTask task)
    {
        try
        {
            long taskID = DB.executeInsert("INSERT INTO devop_task (project_id, task, complete, focused) VALUES (?, ?, 0, 0)",
                    id,
                    task.getTask());
            task.setId(taskID);
            tasks.put(taskID, task);
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/addTask: " + e.toString());
        }
    }

    /**
     * This method marks a task as complete through the database and memory.
     *
     * @param taskID Input for the taskID
     */
    public void completeTask(long taskID)
    {
        try
        {
            DB.executeUpdate("UPDATE devop_task SET complete = 1, focused = 0 WHERE task_id = ?",
                    taskID);
            tasks.get(taskID).setComplete(1);
            tasks.get(taskID).setFocused(0);

            if(focusedTask == null)
            {
                return;
            }

            if(focusedTask.getId() == taskID)
            {
                focusedTask = null;
            }
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/completeTask: " + e.toString());
        }
    }

    /**
     * This method switches the task focus from one task to another through the database and memory.
     *
     * @param newTask    Input for the new task
     * @param formerTask Input for the current task
     */
    public void switchTaskFocus(MintTask newTask, MintTask formerTask)
    {
        try
        {
            if(formerTask != null)
            {
                DB.executeUpdate("UPDATE devop_task SET focused = 0 WHERE task_id = ?",
                        formerTask.getId());
                formerTask.setFocused(0);
            }

            DB.executeUpdate("UPDATE devop_task SET focused = 1 WHERE task_id = ?",
                    newTask.getId());
            newTask.setFocused(1);
            focusedTask = newTask;
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/switchTaskFocus: " + e.toString());
        }
    }

    /**
     * This method unfocuses a task through the database and memory.
     *
     * @param task Input for the task
     */
    public void unFocusTask(MintTask task)
    {
        try
        {
            DB.executeUpdate("UPDATE devop_task SET focused = 0 WHERE task_id = ?",
                    task.getId());
            task.setFocused(0);
            focusedTask = null;
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/unFocusTask: " + e.toString());
        }
    }

    /**
     * This method deletes a task through the database and memory.
     *
     * @param task Input for the task
     */
    public void deleteTask(MintTask task)
    {
        try
        {
            DB.executeUpdate("DELETE FROM devop_task WHERE task_id = ?",
                    task.getId());
            tasks.remove(task.getId());
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/deleteTask: " + e.toString());
        }
    }

    /**
     * This method adds a material through the database and memory.
     *
     * @param material Input for the material
     */
    public void addMaterial(MintMaterial material)
    {
        try
        {
            long materialID = DB.executeInsert("INSERT INTO devop_material (project_id, material, total, collected, complete, focused) VALUES (?, ?, ?, 0, 0, 0)",
                    id,
                    material.getMaterial(),
                    material.getTotal());
            material.setId(materialID);
            materials.put(materialID, material);
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/addMaterial: " + e.toString());
        }
    }

    /**
     * This method marks the material as complete through the database and memory.
     *
     * @param materialID Input for the material
     */
    public void completeMaterial(long materialID)
    {
        try
        {
            DB.executeUpdate("UPDATE devop_material SET complete = 1, focused = 0 WHERE material_id = ?",
                    materials.get(materialID).getId());
            materials.get(materialID).setComplete(1);
            materials.get(materialID).setFocused(0);

            if(focusedMaterial == null)
            {
                return;
            }

            if(focusedMaterial.getId() == materialID)
            {
                focusedMaterial = null;
            }
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/completeMaterial: " + e.toString());
        }
    }

    /**
     * This method switches the material focus from one material to another through the database and memory.
     *
     * @param newMaterial    Input for the new material
     * @param formerMaterial Input for the current material
     */
    public void switchMaterialFocus(MintMaterial newMaterial, MintMaterial formerMaterial)
    {
        try
        {
            if(formerMaterial != null)
            {
                DB.executeUpdate("UPDATE devop_material SET focused = 0 WHERE material_id = ?",
                        formerMaterial.getId());
                formerMaterial.setFocused(0);
            }

            DB.executeUpdate("UPDATE devop_material SET focused = 1 WHERE material_id = ?",
                    newMaterial.getId());
            newMaterial.setFocused(1);
            focusedMaterial = newMaterial;
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/switchMaterialFocus: " + e.toString());
        }
    }

    /**
     * This method deletes a material through the database and memory.
     *
     * @param material Input for the material
     */
    public void deleteMaterial(MintMaterial material)
    {
        try
        {
            DB.executeUpdate("DELETE FROM devop_material WHERE material_id = ?",
                    material.getId());
            materials.remove(material.getId());
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/deleteMaterial: " + e.toString());
        }
    }

    /**
     * This method adds a task log through the database and memory.
     *
     * @param log Input for the task log
     */
    public void addTaskLog(MintLogTask log)
    {
        try
        {
            long logID = DB.executeInsert("INSERT INTO devop_log_task (project_id, logged_by, validated_by, validated, time_worked, log_date, description) VALUES (?, ?, ?, 0, ?, ?, ?)",
                    id,
                    log.getLogger().getId(),
                    null,
                    log.getTimeWorked(),
                    log.getLogDate(),
                    log.getDescription());
            log.setId(logID);
            taskLogValidation.put(logID, log);
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/addTaskLog: " + e.toString());
        }
    }

    /**
     * This method adds a material log through the database and memory
     *
     * @param log Input for the material log
     */
    public void addMaterialLog(MintLogMaterial log)
    {
        try
        {
            long logID = DB.executeInsert("INSERT INTO devop_log_material (project_id, material_id, logged_by, validated_by, validated, material_collected, time_worked, log_date, description) VALUES (?, ?, ?, ?, 0, ?, ?, ?, ?)",
                    id,
                    log.getMaterialID(),
                    log.getLogger().getId(),
                    null,
                    log.getMaterialCollected(),
                    log.getTimeWorked(),
                    log.getLogDate(),
                    log.getDescription());
            log.setId(logID);
            materialLogValidation.put(logID, log);
            updateMaterial(log.getMaterialID(), log.getMaterialCollected());
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/addMaterialLog: " + e.toString());
        }
    }

    /**
     * This method gets the material object
     *
     * @param name Input for material name
     *
     * @return Returns the material object
     */
    public MintMaterial getMaterial(String name)
    {
        for(MintMaterial material : materials.values())
        {
            if(material.getMaterial().equalsIgnoreCase(name))
            {
                return material;
            }
        }
        return null;
    }

    /**
     * This method unfocuses a material through the database and memory.
     *
     * @param material Input for the material
     */
    public void unFocusMaterial(MintMaterial material)
    {
        try
        {
            DB.executeUpdate("UPDATE devop_material set focused = 0 WHERE material_id = ?",
                    material.getId());
            material.setFocused(0);
            focusedMaterial = null;
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/unFocusMaterial: " + e.toString());
        }
    }

    /**
     * This method updates the material count through the database and memory.
     *
     * @param materialID Input for the materialID
     * @param collected  Input for amount collected
     */
    private void updateMaterial(long materialID, int collected)
    {
        MintMaterial material = materials.get(materialID);
        int totalCollected = (material.getCollected() + collected);

        try
        {
            DB.executeUpdate("UPDATE devop_material set collected = ? WHERE material_id = ?",
                    totalCollected,
                    material.getId());
            material.setCollected(totalCollected);
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/updateMaterial: " + e.toString());
        }

        if(totalCollected >= material.getTotal())
        {
            completeMaterial(materialID);
        }
    }

    /**
     * This method marks the material as validated through the database and memory.
     *
     * @param mintLogMaterial Input for the material
     * @param validated       Input if the material was validated or rejected
     * @param validator       Input for the player who validated
     */
    public void validateMaterial(MintLogMaterial mintLogMaterial, boolean validated, EMIPlayer validator)
    {
        MintMaterial mintMaterial = materials.get(mintLogMaterial.getMaterialID());
        if(!validated)
        {
            try
            {
                DB.executeUpdate("DELETE FROM devop_log_material WHERE log_id = ?",
                        mintLogMaterial.getId());

                DB.executeUpdate("UPDATE devop_material set collected = ? WHERE material_id = ?",
                        (mintMaterial.getCollected() - mintLogMaterial.getMaterialCollected()),
                        mintMaterial.getId());
                mintMaterial.setCollected(mintMaterial.getCollected() - mintLogMaterial.getMaterialCollected());
            }
            catch(SQLException e)
            {
                Bukkit.getLogger().info("ERROR: MintProject/validateMaterial/No: " + e.toString());
                return;
            }
        }
        else
        {
            try
            {
                DB.executeUpdate("UPDATE devop_log_material set validated_by = ?, validated = 1 WHERE log_id = ?",
                        validator.getId(),
                        mintLogMaterial.getId());
            }
            catch(SQLException e)
            {
                Bukkit.getLogger().info("ERROR: MintProject/validateMaterial/Yes: " + e.toString());
                return;
            }
            mintLogMaterial.setValidater(validator);
            mintLogMaterial.setValidated(1);
            materialLog.put(mintLogMaterial.getId(), mintLogMaterial);
        }

        queuedValidateMaterial.remove(UUID.fromString(validator.getUniqueId()));
        materialLogValidation.remove(mintLogMaterial.getId());
    }

    /**
     * This method marks the task as validated through the database and memory.
     *
     * @param mintLogTask Input for the task
     * @param validated   Input if the task was validated or rejected
     * @param validator   Input for the player who validated
     */
    public void validateTask(MintLogTask mintLogTask, boolean validated, EMIPlayer validator)
    {
        if(!validated)
        {
            try
            {
                DB.executeUpdate("DELETE FROM devop_log_task WHERE log_id = ?",
                        mintLogTask.getId());
            }
            catch(SQLException e)
            {
                Bukkit.getLogger().info("ERROR: MintProject/validateTask/No: " + e.toString());
                return;
            }
        }
        else
        {
            try
            {
                DB.executeUpdate("UPDATE devop_log_task set validated_by = ?, validated = 1 WHERE log_id = ?",
                        validator.getId(),
                        mintLogTask.getId());
            }
            catch(SQLException e)
            {
                Bukkit.getLogger().info("ERROR: MintProject/validateTask/Yes: " + e.toString());
                return;
            }
            mintLogTask.setValidater(validator);
            mintLogTask.setValidated(1);
            taskLog.put(mintLogTask.getId(), mintLogTask);
        }

        queuedValidateTask.remove(UUID.fromString(validator.getUniqueId()));
        taskLogValidation.remove(mintLogTask.getId());
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public EMIPlayer getLeader()
    {
        return leader;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getStartDate()
    {
        return startDate;
    }

    public String getEndDate()
    {
        return endDate;
    }

    public int getComplete()
    {
        return complete;
    }

    public void setComplete(int complete)
    {
        this.complete = complete;
    }

    public int getFocused()
    {
        return focused;
    }

    public void setFocused(int focused)
    {
        this.focused = focused;
    }

    public String getDescription()
    {
        return description;
    }

    public MintTask getFocusedTask()
    {
        return focusedTask;
    }

    public void setFocusedTask(MintTask focusedTask)
    {
        this.focusedTask = focusedTask;
    }

    public MintMaterial getFocusedMaterial()
    {
        return focusedMaterial;
    }

    public void setFocusedMaterial(MintMaterial focusedMaterial)
    {
        this.focusedMaterial = focusedMaterial;
    }

    public ArrayList<EMIPlayer> getWorkers()
    {
        return workers;
    }

    public HashMap<Long, MintLogTask> getTaskLog()
    {
        return taskLog;
    }

    public HashMap<Long, MintLogTask> getTaskLogValidation()
    {
        return taskLogValidation;
    }

    public HashMap<Long, MintLogMaterial> getMaterialLog()
    {
        return materialLog;
    }

    public HashMap<Long, MintLogMaterial> getMaterialLogValidation()
    {
        return materialLogValidation;
    }

    public HashMap<Long, MintTask> getTasks()
    {
        return tasks;
    }

    public HashMap<Long, MintMaterial> getMaterials()
    {
        return materials;
    }

    public HashMap<UUID, MintLogMaterial> getQueuedValidateMaterial()
    {
        return queuedValidateMaterial;
    }

    public HashMap<UUID, MintLogTask> getQueuedValidateTask()
    {
        return queuedValidateTask;
    }

    @Override
    public String toString()
    {
        return "MintProject{" + "lead=" + leader + ", name='" + name + '\'' + ", startDate='" + startDate + '\'' + ", endDate='" + endDate + '\'' + ", complete=" + complete + ", focused=" + focused + ", description='" + description + '\'' + ", workLog=" + taskLog + ", materialLog=" + materialLog + ", taskRequirements=" + tasks + ", materialRequirements=" + materials + '}';
    }
}

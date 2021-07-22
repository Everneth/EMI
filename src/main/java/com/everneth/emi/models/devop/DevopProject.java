package com.everneth.emi.models.devop;

import co.aikar.idb.DB;
import com.everneth.emi.Utils;
import com.everneth.emi.models.EMIPlayer;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class DevopProject
{
    private long id;
    private final EMIPlayer leader;
    private String name;
    private final String startDate;
    private final String endDate;
    private int complete;
    private int focused;
    private final String description;
    private DevopTask focusedTask = null;
    private DevopMaterial focusedMaterial = null;
    private final ArrayList<EMIPlayer> workers = new ArrayList<>();
    private final HashMap<Long, DevopLogTask> taskLog = new HashMap<>();
    private final HashMap<Long, DevopLogTask> taskLogValidation = new HashMap<>();
    private final HashMap<Long, DevopLogMaterial> materialLog = new HashMap<>();
    private final HashMap<Long, DevopLogMaterial> materialLogValidation = new HashMap<>();
    private final HashMap<Long, DevopTask> tasks = new HashMap<>();
    private final HashMap<Long, DevopMaterial> materials = new HashMap<>();
    private final HashMap<UUID, DevopLogMaterial> queuedValidateMaterial = new HashMap<>();
    private final HashMap<UUID, DevopLogTask> queuedValidateTask = new HashMap<>();

    public DevopProject(EMIPlayer leader, String name, String startDate, String endDate, int complete, int focused, String description)
    {
        this.leader = leader;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.complete = complete;
        this.focused = focused;
        this.description = description;
    }

    public DevopProject(long id, EMIPlayer leader, String name, String startDate, String endDate, int complete, int focused, String description)
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
            Bukkit.getLogger().info("ERROR: DevopProject/addWorker: " + e.toString());
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
            Bukkit.getLogger().info("ERROR: DevopProject/complete: " + e.toString());
        }
    }

    /**
     * This method adds a task to the project through the database and memory.
     *
     * @param task Input for task
     */
    public void addTask(DevopTask task)
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
            Bukkit.getLogger().info("ERROR: DevopProject/addTask: " + e.toString());
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
            Bukkit.getLogger().info("ERROR: DevopProject/completeTask: " + e.toString());
        }
    }

    /**
     * This method switches the task focus from one task to another through the database and memory.
     *
     * @param newTask    Input for the new task
     * @param formerTask Input for the current task
     */
    public void switchTaskFocus(DevopTask newTask, DevopTask formerTask)
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
            Bukkit.getLogger().info("ERROR: DevopProject/switchTaskFocus: " + e.toString());
        }
    }

    /**
     * This method unfocuses a task through the database and memory.
     *
     * @param task Input for the task
     */
    public void unFocusTask(DevopTask task)
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
            Bukkit.getLogger().info("ERROR: DevopProject/unFocusTask: " + e.toString());
        }
    }

    /**
     * This method deletes a task through the database and memory.
     *
     * @param task Input for the task
     */
    public void deleteTask(DevopTask task)
    {
        try
        {
            DB.executeUpdate("DELETE FROM devop_task WHERE task_id = ?",
                    task.getId());
            tasks.remove(task.getId());
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: DevopProject/deleteTask: " + e.toString());
        }
    }

    /**
     * This method adds a material through the database and memory.
     *
     * @param material Input for the material
     */
    public void addMaterial(DevopMaterial material)
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
            Bukkit.getLogger().info("ERROR: DevopProject/addMaterial: " + e.toString());
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
            Bukkit.getLogger().info("ERROR: DevopProject/completeMaterial: " + e.toString());
        }
    }

    /**
     * This method switches the material focus from one material to another through the database and memory.
     *
     * @param newMaterial    Input for the new material
     * @param formerMaterial Input for the current material
     */
    public void switchMaterialFocus(DevopMaterial newMaterial, DevopMaterial formerMaterial)
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
            Bukkit.getLogger().info("ERROR: DevopProject/switchMaterialFocus: " + e.toString());
        }
    }

    /**
     * This method deletes a material through the database and memory.
     *
     * @param material Input for the material
     */
    public void deleteMaterial(DevopMaterial material)
    {
        try
        {
            DB.executeUpdate("DELETE FROM devop_material WHERE material_id = ?",
                    material.getId());
            materials.remove(material.getId());
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: DevopProject/deleteMaterial: " + e.toString());
        }
    }

    /**
     * This method adds a task log through the database and memory.
     *
     * @param log Input for the task log
     */
    public void addTaskLog(DevopLogTask log)
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
            Bukkit.getLogger().info("ERROR: DevopProject/addTaskLog: " + e.toString());
        }
    }

    /**
     * This method adds a material log through the database and memory
     *
     * @param log Input for the material log
     */
    public void addMaterialLog(DevopLogMaterial log)
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
            Bukkit.getLogger().info("ERROR: DevopProject/addMaterialLog: " + e.toString());
        }
    }

    /**
     * This method gets the material object
     *
     * @param name Input for material name
     *
     * @return Returns the material object
     */
    public DevopMaterial getMaterial(String name)
    {
        for(DevopMaterial material : materials.values())
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
    public void unFocusMaterial(DevopMaterial material)
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
            Bukkit.getLogger().info("ERROR: DevopProject/unFocusMaterial: " + e.toString());
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
        DevopMaterial material = materials.get(materialID);
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
            Bukkit.getLogger().info("ERROR: DevopProject/updateMaterial: " + e.toString());
        }

        if(totalCollected >= material.getTotal())
        {
            completeMaterial(materialID);
        }
    }

    /**
     * This method marks the material as validated through the database and memory.
     *
     * @param devopLogMaterial Input for the material
     * @param validated        Input if the material was validated or rejected
     * @param validator        Input for the player who validated
     */
    public void validateMaterial(DevopLogMaterial devopLogMaterial, boolean validated, EMIPlayer validator)
    {
        DevopMaterial devopMaterial = materials.get(devopLogMaterial.getMaterialID());
        if(!validated)
        {
            try
            {
                DB.executeUpdate("DELETE FROM devop_log_material WHERE log_id = ?",
                        devopLogMaterial.getId());

                DB.executeUpdate("UPDATE devop_material set collected = ? WHERE material_id = ?",
                        (devopMaterial.getCollected() - devopLogMaterial.getMaterialCollected()),
                        devopMaterial.getId());
                devopMaterial.setCollected(devopMaterial.getCollected() - devopLogMaterial.getMaterialCollected());
            }
            catch(SQLException e)
            {
                Bukkit.getLogger().info("ERROR: DevopProject/validateMaterial/No: " + e.toString());
                return;
            }
        }
        else
        {
            try
            {
                DB.executeUpdate("UPDATE devop_log_material set validated_by = ?, validated = 1 WHERE log_id = ?",
                        validator.getId(),
                        devopLogMaterial.getId());
            }
            catch(SQLException e)
            {
                Bukkit.getLogger().info("ERROR: DevopProject/validateMaterial/Yes: " + e.toString());
                return;
            }
            devopLogMaterial.setValidater(validator);
            devopLogMaterial.setValidated(1);
            materialLog.put(devopLogMaterial.getId(), devopLogMaterial);
        }

        queuedValidateMaterial.remove(UUID.fromString(validator.getUniqueId()));
        materialLogValidation.remove(devopLogMaterial.getId());
    }

    /**
     * This method marks the task as validated through the database and memory.
     *
     * @param devopLogTask Input for the task
     * @param validated    Input if the task was validated or rejected
     * @param validator    Input for the player who validated
     */
    public void validateTask(DevopLogTask devopLogTask, boolean validated, EMIPlayer validator)
    {
        if(!validated)
        {
            try
            {
                DB.executeUpdate("DELETE FROM devop_log_task WHERE log_id = ?",
                        devopLogTask.getId());
            }
            catch(SQLException e)
            {
                Bukkit.getLogger().info("ERROR: DevopProject/validateTask/No: " + e.toString());
                return;
            }
        }
        else
        {
            try
            {
                DB.executeUpdate("UPDATE devop_log_task set validated_by = ?, validated = 1 WHERE log_id = ?",
                        validator.getId(),
                        devopLogTask.getId());
            }
            catch(SQLException e)
            {
                Bukkit.getLogger().info("ERROR: DevopProject/validateTask/Yes: " + e.toString());
                return;
            }
            devopLogTask.setValidater(validator);
            devopLogTask.setValidated(1);
            taskLog.put(devopLogTask.getId(), devopLogTask);
        }

        queuedValidateTask.remove(UUID.fromString(validator.getUniqueId()));
        taskLogValidation.remove(devopLogTask.getId());
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

    public DevopTask getFocusedTask()
    {
        return focusedTask;
    }

    public void setFocusedTask(DevopTask focusedTask)
    {
        this.focusedTask = focusedTask;
    }

    public DevopMaterial getFocusedMaterial()
    {
        return focusedMaterial;
    }

    public void setFocusedMaterial(DevopMaterial focusedMaterial)
    {
        this.focusedMaterial = focusedMaterial;
    }

    public ArrayList<EMIPlayer> getWorkers()
    {
        return workers;
    }

    public HashMap<Long, DevopLogTask> getTaskLog()
    {
        return taskLog;
    }

    public HashMap<Long, DevopLogTask> getTaskLogValidation()
    {
        return taskLogValidation;
    }

    public HashMap<Long, DevopLogMaterial> getMaterialLog()
    {
        return materialLog;
    }

    public HashMap<Long, DevopLogMaterial> getMaterialLogValidation()
    {
        return materialLogValidation;
    }

    public HashMap<Long, DevopTask> getTasks()
    {
        return tasks;
    }

    public HashMap<Long, DevopMaterial> getMaterials()
    {
        return materials;
    }

    public HashMap<UUID, DevopLogMaterial> getQueuedValidateMaterial()
    {
        return queuedValidateMaterial;
    }

    public HashMap<UUID, DevopLogTask> getQueuedValidateTask()
    {
        return queuedValidateTask;
    }

    @Override
    public String toString()
    {
        return "DevopProject{" + "lead=" + leader + ", name='" + name + '\'' + ", startDate='" + startDate + '\'' + ", endDate='" + endDate + '\'' + ", complete=" + complete + ", focused=" + focused + ", description='" + description + '\'' + ", workLog=" + taskLog + ", materialLog=" + materialLog + ", taskRequirements=" + tasks + ", materialRequirements=" + materials + '}';
    }
}

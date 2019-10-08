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
    private EMIPlayer leader;
    private String name;
    private String startDate;
    private String endDate;
    private int complete;
    private int focused;
    private String description;
    private MintTask focusedTask = null;
    private MintMaterial focusedMaterial = null;
    private ArrayList<EMIPlayer> workers = new ArrayList<>();
    private HashMap<Long, MintLogTask> taskLog = new HashMap<>();
    private HashMap<Long, MintLogTask> taskLogValidation = new HashMap<>();
    private HashMap<Long, MintLogMaterial> materialLog = new HashMap<>();
    private HashMap<Long, MintLogMaterial> materialLogValidation = new HashMap<>();
    private HashMap<Long, MintTask> tasks = new HashMap<>();
    private HashMap<Long, MintMaterial> materials = new HashMap<>();
    private HashMap<UUID, MintLogMaterial> queuedValidateMaterial = new HashMap<>();
    private HashMap<UUID, MintLogTask> queuedValidateTask = new HashMap<>();

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

    public void addWorker(EMIPlayer worker)
    {
        try
        {
            DB.executeInsert("INSERT INTO mint_log_join (player_id, project_id, join_date) VALUES (?, ?, ?)",
                    worker.getId(), id, Utils.getCurrentDate());
            workers.add(worker);
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/addWorker: " + e.toString());
        }
    }

    public void completeProject()
    {
        try
        {
            DB.executeUpdate("UPDATE mint_project SET end_date = ?, complete = 1, focused = 0 WHERE project_id = ?",
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

    public void addTask(MintTask task)
    {
        try
        {
            long taskID = DB.executeInsert("INSERT INTO mint_task (project_id, task, complete, focused) VALUES (?, ?, 0, 0)",
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

    public void completeTask(long taskID)
    {
        try
        {
            DB.executeUpdate("UPDATE mint_task SET complete = 1, focused = 0 WHERE task_id = ?",
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

    public void switchTaskFocus(MintTask newTask, MintTask formerTask)
    {
        try
        {
            if(formerTask != null)
            {
                DB.executeUpdate("UPDATE mint_task SET focused = 0 WHERE task_id = ?",
                        formerTask.getId());
                formerTask.setFocused(0);
            }

            DB.executeUpdate("UPDATE mint_task SET focused = 1 WHERE task_id = ?",
                    newTask.getId());
            newTask.setFocused(1);
            focusedTask = newTask;
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/switchTaskFocus: " + e.toString());
        }
    }

    public void unFocusTask(MintTask task)
    {
        try
        {
            DB.executeUpdate("UPDATE mint_task SET focused = 0 WHERE task_id = ?",
                    task.getId());
            task.setFocused(0);
            focusedTask = null;
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/unFocusTask: " + e.toString());
        }
    }

    public void deleteTask(MintTask task)
    {
        try
        {
            DB.executeUpdate("DELETE FROM mint_task WHERE task_id = ?",
                    task.getId());
            tasks.remove(task.getId());
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/deleteTask: " + e.toString());
        }
    }

    public void addMaterial(MintMaterial material)
    {
        try
        {
            long materialID = DB.executeInsert("INSERT INTO mint_material (project_id, material, total, collected, complete, focused) VALUES (?, ?, ?, 0, 0, 0)",
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

    public void completeMaterial(long materialID)
    {
        try
        {
            DB.executeUpdate("UPDATE mint_material SET complete = 1, focused = 0 WHERE material_id = ?",
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

    public void switchMaterialFocus(MintMaterial newMaterial, MintMaterial formerMaterial)
    {
        try
        {
            if(formerMaterial != null)
            {
                DB.executeUpdate("UPDATE mint_material SET focused = 0 WHERE material_id = ?",
                        formerMaterial.getId());
                formerMaterial.setFocused(0);
            }

            DB.executeUpdate("UPDATE mint_material SET focused = 1 WHERE material_id = ?",
                    newMaterial.getId());
            newMaterial.setFocused(1);
            focusedMaterial = newMaterial;
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/switchMaterialFocus: " + e.toString());
        }
    }

    public void deleteMaterial(MintMaterial material)
    {
        try
        {
            DB.executeUpdate("DELETE FROM mint_material WHERE material_id = ?",
                    material.getId());
            materials.remove(material.getId());
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/deleteMaterial: " + e.toString());
        }
    }

    public void addTaskLog(MintLogTask log)
    {
        try
        {
            long logID = DB.executeInsert("INSERT INTO mint_log_task (project_id, logged_by, validated_by, validated, time_worked, log_date, description) VALUES (?, ?, ?, 0, ?, ?, ?)",
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

    public void addMaterialLog(MintLogMaterial log)
    {
        try
        {
            long logID = DB.executeInsert("INSERT INTO mint_log_material (project_id, material_id, logged_by, validated_by, validated, material_collected, time_worked, log_date, description) VALUES (?, ?, ?, ?, 0, ?, ?, ?, ?)",
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

    public void unFocusMaterial(MintMaterial material)
    {
        try
        {
            DB.executeUpdate("UPDATE mint_material set focused = 0 WHERE material_id = ?",
                    material.getId());
            material.setFocused(0);
            focusedMaterial = null;
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/unFocusMaterial: " + e.toString());
        }
    }

    private void updateMaterial(long materialID, int collected)
    {
        MintMaterial material = materials.get(materialID);
        int totalCollected = (material.getCollected() + collected);

        try
        {
            DB.executeUpdate("UPDATE mint_material set collected = ? WHERE material_id = ?",
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

    public void validateMaterial(MintLogMaterial mintLogMaterial, boolean validated, EMIPlayer validator)
    {
        MintMaterial mintMaterial = materials.get(mintLogMaterial.getMaterialID());
        if(!validated)
        {
            try
            {
                DB.executeUpdate("DELETE FROM mint_log_material WHERE log_id = ?",
                        mintLogMaterial.getId());

                DB.executeUpdate("UPDATE mint_material set collected = ? WHERE material_id = ?",
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
                DB.executeUpdate("UPDATE mint_log_material set validated_by = ?, validated = 1 WHERE log_id = ?",
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

    public void validateTask(MintLogTask mintLogTask, boolean validated, EMIPlayer validator)
    {
        if(!validated)
        {
            try
            {
                DB.executeUpdate("DELETE FROM mint_log_task WHERE log_id = ?",
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
                DB.executeUpdate("UPDATE mint_log_task set validated_by = ?, validated = 1 WHERE log_id = ?",
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

    public void setLeader(EMIPlayer leader)
    {
        this.leader = leader;
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

    public void setStartDate(String startDate)
    {
        this.startDate = startDate;
    }

    public String getEndDate()
    {
        return endDate;
    }

    public void setEndDate(String endDate)
    {
        this.endDate = endDate;
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

    public void setDescription(String description)
    {
        this.description = description;
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

    public void setWorkers(ArrayList<EMIPlayer> workers)
    {
        this.workers = workers;
    }

    public HashMap<Long, MintLogTask> getTaskLog()
    {
        return taskLog;
    }

    public void setTaskLog(HashMap<Long, MintLogTask> taskLog)
    {
        this.taskLog = taskLog;
    }

    public HashMap<Long, MintLogTask> getTaskLogValidation()
    {
        return taskLogValidation;
    }

    public void setTaskLogValidation(HashMap<Long, MintLogTask> taskLogValidation)
    {
        this.taskLogValidation = taskLogValidation;
    }

    public HashMap<Long, MintLogMaterial> getMaterialLog()
    {
        return materialLog;
    }

    public void setMaterialLog(HashMap<Long, MintLogMaterial> materialLog)
    {
        this.materialLog = materialLog;
    }

    public HashMap<Long, MintLogMaterial> getMaterialLogValidation()
    {
        return materialLogValidation;
    }

    public void setMaterialLogValidation(HashMap<Long, MintLogMaterial> materialLogValidation)
    {
        this.materialLogValidation = materialLogValidation;
    }

    public HashMap<Long, MintTask> getTasks()
    {
        return tasks;
    }

    public void setTasks(HashMap<Long, MintTask> tasks)
    {
        this.tasks = tasks;
    }

    public HashMap<Long, MintMaterial> getMaterials()
    {
        return materials;
    }

    public void setMaterials(HashMap<Long, MintMaterial> materials)
    {
        this.materials = materials;
    }

    public HashMap<UUID, MintLogMaterial> getQueuedValidateMaterial()
    {
        return queuedValidateMaterial;
    }

    public void setQueuedValidateMaterial(HashMap<UUID, MintLogMaterial> queuedValidateMaterial)
    {
        this.queuedValidateMaterial = queuedValidateMaterial;
    }

    public HashMap<UUID, MintLogTask> getQueuedValidateTask()
    {
        return queuedValidateTask;
    }

    public void setQueuedValidateTask(HashMap<UUID, MintLogTask> queuedValidateTask)
    {
        this.queuedValidateTask = queuedValidateTask;
    }

    @Override
    public String toString()
    {
        return "MintProject{" + "lead=" + leader + ", name='" + name + '\'' + ", startDate='" + startDate + '\'' + ", endDate='" + endDate + '\'' + ", complete=" + complete + ", focused=" + focused + ", description='" + description + '\'' + ", workLog=" + taskLog + ", materialLog=" + materialLog + ", taskRequirements=" + tasks + ", materialRequirements=" + materials + '}';
    }
}

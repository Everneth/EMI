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
    private EMIPlayer leader;
    private String name;
    private String startDate;
    private String endDate;
    private int complete;
    private int focused;
    private String description;
    private DevopTask focusedTask = null;
    private DevopMaterial focusedMaterial = null;
    private ArrayList<EMIPlayer> workers = new ArrayList<>();
    private HashMap<Long, DevopLogTask> taskLog = new HashMap<>();
    private HashMap<Long, DevopLogTask> taskLogValidation = new HashMap<>();
    private HashMap<Long, DevopLogMaterial> materialLog = new HashMap<>();
    private HashMap<Long, DevopLogMaterial> materialLogValidation = new HashMap<>();
    private HashMap<Long, DevopTask> tasks = new HashMap<>();
    private HashMap<Long, DevopMaterial> materials = new HashMap<>();
    private HashMap<UUID, DevopLogMaterial> queuedValidateMaterial = new HashMap<>();
    private HashMap<UUID, DevopLogTask> queuedValidateTask = new HashMap<>();

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

    public void setWorkers(ArrayList<EMIPlayer> workers)
    {
        this.workers = workers;
    }

    public HashMap<Long, DevopLogTask> getTaskLog()
    {
        return taskLog;
    }

    public void setTaskLog(HashMap<Long, DevopLogTask> taskLog)
    {
        this.taskLog = taskLog;
    }

    public HashMap<Long, DevopLogTask> getTaskLogValidation()
    {
        return taskLogValidation;
    }

    public void setTaskLogValidation(HashMap<Long, DevopLogTask> taskLogValidation)
    {
        this.taskLogValidation = taskLogValidation;
    }

    public HashMap<Long, DevopLogMaterial> getMaterialLog()
    {
        return materialLog;
    }

    public void setMaterialLog(HashMap<Long, DevopLogMaterial> materialLog)
    {
        this.materialLog = materialLog;
    }

    public HashMap<Long, DevopLogMaterial> getMaterialLogValidation()
    {
        return materialLogValidation;
    }

    public void setMaterialLogValidation(HashMap<Long, DevopLogMaterial> materialLogValidation)
    {
        this.materialLogValidation = materialLogValidation;
    }

    public HashMap<Long, DevopTask> getTasks()
    {
        return tasks;
    }

    public void setTasks(HashMap<Long, DevopTask> tasks)
    {
        this.tasks = tasks;
    }

    public HashMap<Long, DevopMaterial> getMaterials()
    {
        return materials;
    }

    public void setMaterials(HashMap<Long, DevopMaterial> materials)
    {
        this.materials = materials;
    }

    public HashMap<UUID, DevopLogMaterial> getQueuedValidateMaterial()
    {
        return queuedValidateMaterial;
    }

    public void setQueuedValidateMaterial(HashMap<UUID, DevopLogMaterial> queuedValidateMaterial)
    {
        this.queuedValidateMaterial = queuedValidateMaterial;
    }

    public HashMap<UUID, DevopLogTask> getQueuedValidateTask()
    {
        return queuedValidateTask;
    }

    public void setQueuedValidateTask(HashMap<UUID, DevopLogTask> queuedValidateTask)
    {
        this.queuedValidateTask = queuedValidateTask;
    }

    @Override
    public String toString()
    {
        return "DevopProject{" + "lead=" + leader + ", name='" + name + '\'' + ", startDate='" + startDate + '\'' + ", endDate='" + endDate + '\'' + ", complete=" + complete + ", focused=" + focused + ", description='" + description + '\'' + ", workLog=" + taskLog + ", materialLog=" + materialLog + ", taskRequirements=" + tasks + ", materialRequirements=" + materials + '}';
    }
}

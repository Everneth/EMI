package com.everneth.emi.models.mint;

import co.aikar.idb.DB;
import com.everneth.emi.Utils;
import com.everneth.emi.models.EMIPlayer;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class MintProject
{
    private long id;
    private EMIPlayer lead;
    private String name;
    private String startDate;
    private String endDate;
    private int complete;
    private int focused;
    private String description;
    private MintTask focusedTask;
    private MintMaterial focusedMaterial;
    private ArrayList<EMIPlayer> workers = new ArrayList<>();
    private HashMap<Long, MintLogTask> taskLog = new HashMap<>();
    private HashMap<Long, MintLogTask> taskLogValidation = new HashMap<>();
    private HashMap<Long, MintLogMaterial> materialLog = new HashMap<>();
    private HashMap<Long, MintLogMaterial> materialLogValidation = new HashMap<>();
    private HashMap<Long, MintTask> tasks = new HashMap<>();
    private HashMap<Long, MintMaterial> materials = new HashMap<>();

    public MintProject(EMIPlayer lead, String name, String startDate, String endDate, int complete, int focused, String description)
    {
        this.lead = lead;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.complete = complete;
        this.focused = focused;
        this.description = description;
    }

    public MintProject(long id, EMIPlayer lead, String name, String startDate, String endDate, int complete, int focused, String description)
    {
        this.id = id;
        this.lead = lead;
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
            DB.executeUpdate("UPDATE mint_project SET end_date = ?, complete = 1 WHERE project_id = ?",
                    Utils.getCurrentDate(),
                    id);
            complete = 1;
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
            long taskID = DB.executeInsert("INSERT INTO mint_task (project_id, task, complete, focused) VALUES (?, ?, 0, 0)", id,
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
            DB.executeUpdate("UPDATE mint_task SET complete = 1 WHERE task_id = ?",
                    taskID);
            tasks.get(taskID).setComplete(1);
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
            long materialID = DB.executeInsert("INSERT INTO mint_material (project_id, material, total, collected, complete, focused) VALUES (?, ?, ?, 0, 0, 0)", id,
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
            DB.executeUpdate("UPDATE mint_material SET complete = 1 WHERE material_id = ?",
                    materials.get(materialID).getId());
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

    public void addLogWork(MintLogTask log)
    {
        try
        {
            long logID = DB.executeInsert("INSERT INTO mint_log_task (project_id, logged_by, validated, time_worked, log_date, description) VALUES (?, ?, ?, ?, ?, ?)", id,
                    log.getLogger().getId(),
                    log.getValidated(),
                    log.getTimeWorked(),
                    log.getLogDate(),
                    log.getDescription());
            log.setId(logID);
            taskLog.put(logID, log);
        }
        catch(SQLException e)
        {
            Bukkit.getLogger().info("ERROR: MintProject/addLogWork: " + e.toString());
        }
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public EMIPlayer getLead()
    {
        return lead;
    }

    public void setLead(EMIPlayer lead)
    {
        this.lead = lead;
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

    @Override
    public String toString()
    {
        return "MintProject{" + "lead=" + lead + ", name='" + name + '\'' + ", startDate='" + startDate + '\'' + ", endDate='" + endDate + '\'' + ", complete=" + complete + ", focused=" + focused + ", description='" + description + '\'' + ", workLog=" + taskLog + ", materialLog=" + materialLog + ", taskRequirements=" + tasks + ", materialRequirements=" + materials + '}';
    }
}

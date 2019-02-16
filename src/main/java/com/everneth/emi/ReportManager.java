package com.everneth.emi;

import com.everneth.emi.models.Report;

import java.util.HashMap;
import java.util.UUID;

public final class ReportManager {
    private static ReportManager rm;
    private HashMap<UUID, Report> reportMap = new HashMap<UUID, Report>();
    private ReportManager() {}
    public static ReportManager getReportManager()
    {
        if(rm == null)
        {
            rm = new ReportManager();
        }
        return rm;
    }
    public void addReport(UUID uuid, Report report)
    {
        this.reportMap.put(uuid, report);
    }
    public void removeReport(UUID uuid)
    {
        this.reportMap.remove(uuid);
    }
    public Report findReportById(UUID uuid)
    {
        return this.reportMap.get(uuid);
    }
}

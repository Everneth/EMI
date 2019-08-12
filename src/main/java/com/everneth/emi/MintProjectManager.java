package com.everneth.emi;

import com.everneth.emi.models.MintProject;

import java.util.HashMap;

public class MintProjectManager
{
    private static MintProjectManager mintProjectManager;
    private MintProjectManager() {}
    private HashMap<Long, MintProject> projects = new HashMap<>();
    public static MintProjectManager getMintProjectManager()
    {
        if(mintProjectManager == null)
        {
            mintProjectManager = new MintProjectManager();
        }
        return mintProjectManager;
    }


}

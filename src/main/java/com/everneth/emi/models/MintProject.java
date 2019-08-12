package com.everneth.emi.models;

import java.util.ArrayList;
import java.util.HashMap;

public class MintProject
{
    private EMIPlayer lead;
    private String name;
    private String startDate;
    private String endDate;
    private boolean complete;
    private boolean focused;
    private HashMap<Long, MintWorkLog> workLog = new HashMap<>();
    private HashMap<Long, MintMaterialLog> materialLog = new HashMap<>();
    private HashMap<Long, MintTaskRequirement> taskRequirements = new HashMap<>();
    private HashMap<Long, MintMaterialReuirement> materialRequirements = new HashMap<>();


}

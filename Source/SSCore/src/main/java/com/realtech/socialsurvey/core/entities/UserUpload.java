package com.realtech.socialsurvey.core.entities;

import java.util.Map;


public class UserUpload
{
    private RegionUploadVO[] regions;
    private BranchUploadVO[] branches;
    private UserUploadVO[] users;
    private Map<Long, String> regionSourceMapping;
    private Map<Long, String> branchSourceMapping;
    private Map<Long, String> userSourceMapping;


    public RegionUploadVO[] getRegions()
    {
        return regions;
    }


    public void setRegions( RegionUploadVO[] regions )
    {
        this.regions = regions;
    }


    public BranchUploadVO[] getBranches()
    {
        return branches;
    }


    public void setBranches( BranchUploadVO[] branches )
    {
        this.branches = branches;
    }


    public UserUploadVO[] getUsers()
    {
        return users;
    }


    public void setUsers( UserUploadVO[] users )
    {
        this.users = users;
    }


    public Map<Long, String> getRegionSourceMapping()
    {
        return regionSourceMapping;
    }


    public void setRegionSourceMapping( Map<Long, String> regionSourceMapping )
    {
        this.regionSourceMapping = regionSourceMapping;
    }


    public Map<Long, String> getBranchSourceMapping()
    {
        return branchSourceMapping;
    }


    public void setBranchSourceMapping( Map<Long, String> branchSourceMapping )
    {
        this.branchSourceMapping = branchSourceMapping;
    }


    public Map<Long, String> getUserSourceMapping()
    {
        return userSourceMapping;
    }


    public void setUserSourceMapping( Map<Long, String> userSourceMapping )
    {
        this.userSourceMapping = userSourceMapping;
    }


}

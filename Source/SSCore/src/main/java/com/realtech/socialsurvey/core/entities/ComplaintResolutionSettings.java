package com.realtech.socialsurvey.core.entities;

import java.util.ArrayList;
import java.util.List;

import com.realtech.socialsurvey.core.commons.CommonConstants;


public class ComplaintResolutionSettings
{

    private float rating;
    private String mailId;
    private String mood;
    private boolean enabled;


    public ComplaintResolutionSettings()
    {
        rating = CommonConstants.DEFAULT_COMPLAINT_RES_SCORE;
        mailId = "";
        mood = "";
        enabled = false;
    }


    public float getRating()
    {
        return rating;
    }


    public void setRating( float rating )
    {
        this.rating = rating;
    }


    public String getMailId()
    {
        return mailId;
    }


    public void setMailId( String mailId )
    {
        this.mailId = mailId;
    }


    public String getMood()
    {
        return mood;
    }


    public void setMood( String mood )
    {
        this.mood = mood;
    }


    public boolean isEnabled()
    {
        return enabled;
    }


    public void setEnabled( boolean enabled )
    {
        this.enabled = enabled;
    }


    public List<String> getMoodList()
    {
        List<String> moodList = new ArrayList<String>();
        if ( mood.equalsIgnoreCase( "ok" ) ) {
            moodList.add( "ok" );
            moodList.add( "unpleasant" );
        } else
            moodList.add( "unpleasant" );
        return moodList;
    }


    @Override
    public String toString()
    {
        return "mail_id: " + mailId + "\tcut_off_rating: " + rating + "\tcut_off_review_mood: " + mood
            + "\tcomplaint_handling_enabled" + enabled;

    }
}

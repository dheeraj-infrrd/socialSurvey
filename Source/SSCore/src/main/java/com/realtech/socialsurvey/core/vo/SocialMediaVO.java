package com.realtech.socialsurvey.core.vo;

import com.realtech.socialsurvey.core.enums.SocialMediaConnectionStatus;


public class SocialMediaVO
{
    private String SocialMedia;
    private SocialMediaConnectionStatus status;


    public String getSocialMedia()
    {
        return SocialMedia;
    }


    public void setSocialMedia( String socialMedia )
    {
        SocialMedia = socialMedia;
    }


    public SocialMediaConnectionStatus getStatus()
    {
        return status;
    }


    public void setStatus( SocialMediaConnectionStatus status )
    {
        this.status = status;
    }


    @Override public String toString()
    {
        return "SocialMediaVO{" + "SocialMedia='" + SocialMedia + '\'' + ", status='" + status + '\'' + '}';
    }
}

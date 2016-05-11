package com.realtech.socialsurvey.core.services.api;

import com.realtech.socialsurvey.core.entities.Company;
import com.realtech.socialsurvey.core.entities.User;
import com.realtech.socialsurvey.core.entities.api.UserProfile;
import com.realtech.socialsurvey.core.exception.InvalidInputException;
import com.realtech.socialsurvey.core.exception.NoRecordsFetchedException;
import com.realtech.socialsurvey.core.exception.NonFatalException;
import com.realtech.socialsurvey.core.services.search.exception.SolrException;


public interface UserService
{
    public void updateUserProfile( int userId, UserProfile userProfile );


    public UserProfile getUserProfileDetails( int userId );


    public void deleteUserProfileImage( int userId );


    public void updateUserProfileImage( int userId, String imageUrl );


    public void updateStage( int parseInt, String stage );


    public User addUser( String firstName, String lastName, String emailId, Company company )
        throws InvalidInputException, SolrException, NoRecordsFetchedException;


    public void sendRegistrationEmail( User user ) throws NonFatalException;
}

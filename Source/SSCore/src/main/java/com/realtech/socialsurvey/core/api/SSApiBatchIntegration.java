package com.realtech.socialsurvey.core.api;

import java.sql.Timestamp;
import java.util.Date;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

public interface SSApiBatchIntegration
{

    @GET("/v1/getsurveystatsreport")
    Response getReportingSurveyStatsReport(@Query ("entityId") Long entityId , @Query ("entityType") String entityType);
    
    @GET("/v1/getuseradoptionreportsforreporting")
    Response getUserAdoption(@Query ("entityId") Long entityId , @Query ("entityType") String entityType);
    
    @GET("/v1/getcompanyuserreportsforreporting")
    Response getCompanyUserReport(@Query ("entityId") Long entityId , @Query ("entityType") String entityType);
    
    @GET("/v1/getsurveyresultscompanyreportsforreporting")
    Response getSurveyResultsCompany(@Query ("entityId") Long entityId , @Query ("entityType") String entityType, @Query ("startDate") Timestamp startDate, @Query ("endDate") Timestamp endDate);
    
    //Survey Response api for testing. Not being used anywhere else
    @GET("/v1/getsurveyresponseforreporting")
    Response getsurveyresponseforreporting(@Query ("surveyDetailsId") String surveyDetailsId);
}

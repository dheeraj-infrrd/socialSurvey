package com.realtech.socialsurvey.core.dao;

import java.util.List;

import com.realtech.socialsurvey.core.entities.SurveyStatsReportBranch;

public interface SurveyStatsReportBranchDao extends GenericReportingDao<SurveyStatsReportBranch, String>
{

    List<SurveyStatsReportBranch> fetchBranchSurveyStatsById( Long branchId );

}

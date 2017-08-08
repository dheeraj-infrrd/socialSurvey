package com.realtech.socialsurvey.core.dao.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.realtech.socialsurvey.core.commons.CommonConstants;
import com.realtech.socialsurvey.core.dao.UserRankingThisMonthMainDao;
import com.realtech.socialsurvey.core.entities.UserRankingThisMonthMain;
import com.realtech.socialsurvey.core.exception.DatabaseException;

@Component
public class UserRankingThisMonthMainDaoImpl extends GenericReportingDaoImpl<UserRankingThisMonthMain, String> implements UserRankingThisMonthMainDao{
	
	private static final Logger LOG = LoggerFactory.getLogger( UserRankingThisMonthMainDaoImpl.class );
	
	@Override
	public List<UserRankingThisMonthMain> fetchUserRankingForThisMonthMain(Long companyId, int month, int year , int startIndex , int batchSize) {
		LOG.info( "method to fetch user ranking Main list for this month, fetchUserRankingForThisMonthMain() started" );
        Criteria criteria = getSession().createCriteria( UserRankingThisMonthMain.class );
        try {
            criteria.add( Restrictions.eq( CommonConstants.COMPANY_ID_COLUMN, companyId ) );
            criteria.add( Restrictions.eq( CommonConstants.THIS_MONTH, month ) );
            criteria.add( Restrictions.eq( CommonConstants.THIS_YEAR, year ) );   
            if ( startIndex > -1 ) {
                criteria.setFirstResult( startIndex );
            }
            if ( batchSize > -1 ) {
                criteria.setMaxResults( batchSize );
            }
            criteria.addOrder( Order.asc( CommonConstants.RANK ) );
            }
        catch ( HibernateException hibernateException ) {
            LOG.error( "Exception caught in fetchUserRankingForThisMonthMain() ", hibernateException );
            throw new DatabaseException( "Exception caught in fetchUserRankingForThisMonthMain() ", hibernateException );
        }

        LOG.info( "method to fetch user ranking main list for this month, fetchUserRankingForThisMonthMain() finished." );
        return (List<UserRankingThisMonthMain>) criteria.list();
	}

}

/**
 * JIRA:SS-62 BY RM 02 BOC
 */
package com.realtech.socialsurvey.core.services.search;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.List;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import com.realtech.socialsurvey.core.entities.Branch;
import com.realtech.socialsurvey.core.entities.Company;
import com.realtech.socialsurvey.core.entities.Region;
import com.realtech.socialsurvey.core.entities.User;
import com.realtech.socialsurvey.core.exception.InvalidInputException;
import com.realtech.socialsurvey.core.exception.NoRecordsFetchedException;
import com.realtech.socialsurvey.core.services.search.exception.SolrException;

/**
 * Holds method to perform search from solr
 */
public interface SolrSearchService {

	/**
	 * Method to perform search of regions from solr based on the input pattern and company
	 * 
	 * @param regionPattern
	 * @param company
	 * @param start
	 * @param rows
	 * @return
	 * @throws InvalidInputException
	 * @throws SolrException
	 */
	public String searchRegions(String regionPattern, Company company, int start, int rows) throws InvalidInputException, SolrException;

	/**
	 * Method to perform search of branches from solr based on the input pattern and company
	 * 
	 * @param branchPattern
	 * @param company
	 * @param start
	 * @param rows
	 * @return
	 * @throws InvalidInputException
	 * @throws SolrException
	 */
	public String searchBranches(String branchPattern, Company company, int start, int rows) throws InvalidInputException, SolrException;

	/**
	 * Method to add a region to solr
	 * 
	 * @param region
	 * @throws SolrException
	 */
	public void addOrUpdateRegionToSolr(Region region) throws SolrException;

	/**
	 * Method to add a branch to solr
	 * 
	 * @param branch
	 * @throws SolrException
	 */
	public void addOrUpdateBranchToSolr(Branch branch) throws SolrException;

	public String searchUsersByLoginNameAndCompany(String userNamePattern, Company company) throws InvalidInputException, SolrException,
			MalformedURLException;

	public void addUserToSolr(User user) throws SolrException;

	public String searchUsersByLoginNameOrName(String pattern, long companyId) throws InvalidInputException, SolrException, MalformedURLException;

	/**
	 * Method to perform search of Users from solr based on the input pattern for firstname and last
	 * name
	 * 
	 * @throws InvalidInputException
	 * @throws SolrException
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public List<SolrDocument> searchUsersByFirstOrLastName(String patternFirst, String patternLast) throws InvalidInputException, SolrException,
			MalformedURLException;

	/**
	 * Method to perform search of Users from solr based on the input pattern for firstname and last
	 * name
	 * 
	 * @throws InvalidInputException
	 * @throws SolrException
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public SolrDocumentList searchUsersByFirstOrLastName(String patternFirst, String patternLast, int startIndex, int noOfRows)
			throws InvalidInputException, SolrException, MalformedURLException;

	public String searchUsersByCompany(long companyId, int startIndex, int noOfRows) throws InvalidInputException, SolrException,
			MalformedURLException;

	public void removeUserFromSolr(long userIdToRemove) throws SolrException;

	/**
	 * Method to fetch display name of a user from solr based upon user id provided
	 * 
	 * @param userId
	 * @return
	 * @throws SolrServerException
	 * @throws NoRecordsFetchedException
	 * @throws InvalidInputException
	 */
	public String getUserDisplayNameById(long userId) throws SolrServerException, NoRecordsFetchedException, InvalidInputException;

	/**
	 * Method to fetch user based on the userid provided
	 * 
	 * @param userId
	 * @return
	 * @throws InvalidInputException
	 * @throws SolrServerException
	 */
	public SolrDocument getUserByUniqueId(long userId) throws InvalidInputException, SolrServerException;

	public void editUserInSolr(long userId, String key, String value) throws SolrException;

	public SolrDocumentList searchUsersByIden(long iden, String idenFieldName, int startIndex, int noOfRows) throws InvalidInputException,
			SolrException;
}
// JIRA:SS-62 BY RM 02 EOC
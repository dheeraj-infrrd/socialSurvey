/**
 * 
 */
package com.realtech.socialsurvey.core.services.organizationmanagement;

import java.util.List;
import com.realtech.socialsurvey.core.entities.Branch;
import com.realtech.socialsurvey.core.entities.Company;
import com.realtech.socialsurvey.core.entities.Region;
import com.realtech.socialsurvey.core.entities.User;
import com.realtech.socialsurvey.core.entities.UserProfile;
import com.realtech.socialsurvey.core.enums.AccountType;
import com.realtech.socialsurvey.core.exception.InvalidInputException;
import com.realtech.socialsurvey.core.services.search.exception.SolrException;

/**
 * Services for managing hierarchy
 */
public interface HierarchyManagementService {

	/**
	 * Method to fetch all branches of a company
	 * 
	 * @param company
	 * @return
	 * @throws InvalidInputException
	 */
	public List<Branch> getAllBranchesForCompany(Company company) throws InvalidInputException;

	/**
	 * Method to fetch branches mapped to a region
	 * 
	 * @param regionId
	 * @return
	 * @throws InvalidInputException
	 */
	public List<Branch> getAllBranchesInRegion(long regionId) throws InvalidInputException;

	/**
	 * Method to fetch UserProfiles mapped to a branch
	 * 
	 * @param branchId
	 * @return
	 * @throws InvalidInputException
	 */
	public List<UserProfile> getAllUserProfilesInBranch(long branchId) throws InvalidInputException;

	/**
	 * Method to fetch count of branches mapped to a region
	 * 
	 * @param regionId
	 * @return
	 * @throws InvalidInputException
	 */
	public long getCountBranchesInRegion(long regionId) throws InvalidInputException;

	/**
	 * Method to fetch count of UserProfiles mapped to a branch
	 * 
	 * @param branchId
	 * @return
	 * @throws InvalidInputException
	 */
	public long getCountUsersInBranch(long branchId) throws InvalidInputException;

	/**
	 * Method to fetch all regions of a company
	 * 
	 * @param company
	 * @return
	 * @throws InvalidInputException
	 */
	public List<Region> getAllRegionsForCompany(Company company) throws InvalidInputException;

	/**
	 * Method to update status of a branch
	 * 
	 * @param user
	 * @param branchId
	 * @param status
	 * @throws InvalidInputException
	 * @throws SolrException
	 */
	public void updateBranchStatus(User user, long branchId, int status) throws InvalidInputException, SolrException;

	/**
	 * Method to update status of a region
	 * 
	 * @param user
	 * @param regionId
	 * @param status
	 * @throws InvalidInputException
	 * @throws SolrException
	 */
	public void updateRegionStatus(User user, long regionId, int status) throws InvalidInputException, SolrException;

	/**
	 * Method to check whether a branch addition is allowed for given account type and user
	 * 
	 * @param user
	 * @param accountType
	 * @return
	 * @throws InvalidInputException
	 */
	public boolean isBranchAdditionAllowed(User user, AccountType accountType) throws InvalidInputException;

	/**
	 * Method to check whether a region addition is allowed for given account type and user
	 * 
	 * @param user
	 * @param accountType
	 * @return
	 * @throws InvalidInputException
	 */
	public boolean isRegionAdditionAllowed(User user, AccountType accountType) throws InvalidInputException;

	/**
	 * Method to add a new branch
	 * 
	 * @param user
	 * @param regionId
	 * @param branchName
	 * @return
	 * @throws InvalidInputException
	 */
	public Branch addNewBranch(User user, long regionId, String branchName) throws InvalidInputException, SolrException;

	/**
	 * Method to add a new region
	 * 
	 * @param user
	 * @param regionName
	 * @return
	 * @throws InvalidInputException
	 */
	public Region addNewRegion(User user, String regionName) throws InvalidInputException, SolrException;

	/**
	 * Method to update a branch
	 * 
	 * @param branchId
	 * @param regionId
	 * @param branchName
	 * @param branchAddress
	 * @param user
	 * @throws InvalidInputException
	 * @throws SolrException 
	 */
	public void updateBranch(long branchId, long regionId, String branchName, String branchAddress, User user) throws InvalidInputException, SolrException;

	/**
	 * Method to update a region
	 * 
	 * @param regionId
	 * @param regionName
	 * @param regionAddress
	 * @param user
	 * @throws InvalidInputException
	 * @throws SolrException 
	 */
	public void updateRegion(long regionId, String regionName, String regionAddress, User user) throws InvalidInputException, SolrException;

	/**
	 * Method to check whether a user has privileges to build hierarchy
	 * 
	 * @param user
	 * @param accountType
	 * @return
	 */
	public boolean canBuildHierarchy(User user, AccountType accountType);

	/**
	 * Method to check whether a user has privileges to edit company information
	 * 
	 * @param user
	 * @param accountType
	 * @return
	 */
	public boolean canEditCompany(User user, AccountType accountType);

}

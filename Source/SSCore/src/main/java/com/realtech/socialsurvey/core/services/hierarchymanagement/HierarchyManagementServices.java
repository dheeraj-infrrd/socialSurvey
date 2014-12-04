/**
 * 
 */
package com.realtech.socialsurvey.core.services.hierarchymanagement;

import java.util.List;
import com.realtech.socialsurvey.core.entities.Branch;
import com.realtech.socialsurvey.core.entities.Company;
import com.realtech.socialsurvey.core.entities.Region;
import com.realtech.socialsurvey.core.exception.InvalidInputException;

/**
 * Services for managing hierarchy
 */
public interface HierarchyManagementServices {

	public List<Branch> getAllBranchesForCompany(Company company) throws InvalidInputException;

	public List<Region> getAllRegionsForCompany(Company company) throws InvalidInputException;
	
	public void deleteBranch(int branchId) throws InvalidInputException;
	
	public void deleteRegion(int regionId) throws InvalidInputException;

}

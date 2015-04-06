package com.realtech.socialsurvey.core.entities;

/**
 * Branch entity from the search
 */
public class BranchFromSearch {

	private long branchId;
	private String branchName;
	private long regionId;
	private String regionName;
	private long companyId;
	private long isDefaultBySystem;
	private int status;
	private String address1;
	private String address2;

	public long getBranchId() {
		return branchId;
	}

	public void setBranchId(long branchId) {
		this.branchId = branchId;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public long getRegionId() {
		return regionId;
	}

	public void setRegionId(long regionId) {
		this.regionId = regionId;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	public long getCompanyId() {
		return companyId;
	}

	public void setCompanyId(long companyId) {
		this.companyId = companyId;
	}

	public long getIsDefaultBySystem() {
		return isDefaultBySystem;
	}

	public void setIsDefaultBySystem(long isDefaultBySystem) {
		this.isDefaultBySystem = isDefaultBySystem;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	@Override
	public String toString() {
		return "BranchFromSearch [branchId=" + branchId + ", branchName=" + branchName + ", regionId=" + regionId + ", regionName=" + regionName
				+ ", companyId=" + companyId + ", isDefaultBySystem=" + isDefaultBySystem + ", status=" + status + ", address1=" + address1
				+ ", address2=" + address2 + "]";
	}

}
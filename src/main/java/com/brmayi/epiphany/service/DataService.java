package com.brmayi.epiphany.service;

import java.util.List;

import com.brmayi.epiphany.exception.EpiphanyException;

public interface DataService {
	public void dealDataByIds(List<Long> dealIds, String path) throws EpiphanyException;
	
	public List<Long> getIds(String beginTime, String endTime) throws EpiphanyException;
	
	public void dealDataByBeginEnd(long beginId, long endId, String path) throws EpiphanyException;
	
	public long getMaxId() throws EpiphanyException;
	
	public long getMinId() throws EpiphanyException;
}

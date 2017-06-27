package com.brmayi.epiphany.service;

import java.util.List;

import com.brmayi.epiphany.exception.EpiphanyException;

public interface DataService {
	public void dealData(List<Long> dealIds) throws EpiphanyException;
	
	public List<Long> getIds(String beginTime, String endTime) throws EpiphanyException;
}

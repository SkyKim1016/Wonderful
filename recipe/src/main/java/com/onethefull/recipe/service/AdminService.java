package com.onethefull.recipe.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.onethefull.recipe.comm.ResultWithData;
import com.onethefull.recipe.mapper.AdminMapper;
import com.onethefull.recipe.req.RecipeStuffReq;
import com.onethefull.recipe.type.AdminActionType;

@Service("adminService")
public class AdminService {

	@Resource(name = "adminMapper")
	private AdminMapper adminMapper;
	
	public ResultWithData adminStuff(RecipeStuffReq req) {
		
		if(req.getActionType().equals(AdminActionType.CHANGE)) {
			req.setRecipeId(null);
			req.setStuffId(null);
			req.setQuantityInfo(req.getStuffTargetName()); // CHANGE에서는 quantityInfo에 대체재명을 넣는다.
		} else if(req.getActionType().equals(AdminActionType.DELETE)) {
			req.setRecipeId(null);
			req.setStuffId(null);
		}
		
		try {
			adminMapper.adminStuff(req);
		} catch (Exception e) {
			return ResultWithData.failuerResult();
		}
		
		return ResultWithData.succcessResult().addData("req", req);
	}
	
	

}

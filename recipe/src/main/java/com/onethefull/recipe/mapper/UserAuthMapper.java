package com.onethefull.recipe.mapper;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.onethefull.recipe.comm.auth.User;
import com.onethefull.recipe.comm.auth.UserSimple;
import com.onethefull.recipe.comm.vo.DeviceVO;
import com.onethefull.recipe.comm.vo.PageInfoVO;
import com.onethefull.recipe.req.AuthTokenReq;
import com.onethefull.recipe.req.ModifyFavoriteStep1Req;
import com.onethefull.recipe.req.UserDeviceReq;
import com.onethefull.recipe.req.UserReq;
import com.onethefull.recipe.req.UserServiceProviderInfoReq;
import com.onethefull.recipe.vo.CheckActivityVO;
import com.onethefull.recipe.vo.CheckHealthVO;
import com.onethefull.recipe.vo.CheckJobVO;

@Repository("userAuthMapper")
public interface UserAuthMapper {

	public User getUserbyAuthToken(User user);
	public User findUserbyIdPassword(UserReq req);
	public User findUserbyLoginId(UserReq req);
	public User findUserbyId(UserReq req);
	public User findUserbyProviderUserId(UserServiceProviderInfoReq req);
	public User findUserbyDeviceInfo(UserServiceProviderInfoReq req);
	public int setAuthToken(AuthTokenReq req);
	public int setUserDeviceInfo(UserDeviceReq req);
	public DeviceVO getUserDeviceInfo(UserDeviceReq req);
	public int setUserServiceProviderInfo(UserServiceProviderInfoReq req);
	public int registerUserbyDeviceInfo(UserServiceProviderInfoReq req);
	public int modifyFavoriteStep1(ModifyFavoriteStep1Req req);
	public List<CheckHealthVO> getCheckHealthList(UserReq req);
	public List<CheckJobVO> getCheckJobList(UserReq req);
	public CheckActivityVO getCheckActivityLevel(UserReq req);
	public CheckActivityVO getAgeLevel(UserReq req);
	public void modifyCheckHealth(UserReq req);
	public void modifyCheckJob(UserReq req);
	public void modifyCheckActivityLevel(UserReq req);
	public void modifyAgeLevel(UserReq req);
	
	public PageInfoVO getUsersPageInfo(UserReq req);
	public List<UserSimple> getUsersList(UserReq req);

	public int updateUser(UserReq userReq);
}

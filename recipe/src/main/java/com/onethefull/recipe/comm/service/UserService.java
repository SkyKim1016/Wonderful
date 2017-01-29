package com.onethefull.recipe.comm.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.onethefull.recipe.comm.ErrorCode;
import com.onethefull.recipe.comm.ResultWithData;
import com.onethefull.recipe.comm.auth.User;
import com.onethefull.recipe.comm.auth.UserSimple;
import com.onethefull.recipe.comm.util.CommonUtil;
import com.onethefull.recipe.comm.vo.PageInfoVO;
import com.onethefull.recipe.mapper.RecipeMapper;
import com.onethefull.recipe.mapper.UserAuthMapper;
import com.onethefull.recipe.req.AuthTokenReq;
import com.onethefull.recipe.req.KeywordReq;
import com.onethefull.recipe.req.ModifyFavoriteStep1Req;
import com.onethefull.recipe.req.RecipeReq;
import com.onethefull.recipe.req.UserReq;
import com.onethefull.recipe.req.UserServiceProviderInfoReq;
import com.onethefull.recipe.type.HealthType;
import com.onethefull.recipe.type.UserStatusType;
import com.onethefull.recipe.vo.CheckActivityVO;
import com.onethefull.recipe.vo.CheckHealthVO;
import com.onethefull.recipe.vo.CheckJobVO;
import com.onethefull.recipe.vo.RecipeforFavoriteCheckVO;

@Service("userService")
public class UserService {

	@Resource(name = "userAuthMapper")
	private UserAuthMapper userAuthMapper;

	@Resource(name = "recipeMapper")
	private RecipeMapper recipeMapper;

	@Resource(name = "pushService")
	private PushService pushService;
	
	// token을 이용해서 user정보를 반환
	public User getForSession(String authToken) {

		// token 생성 , 저장 및 전달
		// String authToken = "";
		// 현대는 token이 없기에 하드코딩으로 테스트용으로 한다
		// authToken =
		// "e0d3b72f72183185c11356d4f280ceceb336622fbab3b4f4fbe6af73ca4dbcbc";

		User user = new User();
		user.setAuthToken(authToken);

		try {
			user = userAuthMapper.getUserbyAuthToken(user);
		} catch (Exception e) {
			// return ErrorCode.DB_ERROR;
			return null;
		}

		return user;
	}

	// userId로 회원 찾기
	public byte[] findUserbyId(UserReq req) throws UnsupportedEncodingException {
		User user = userAuthMapper.findUserbyId(req);
		if (user != null && user.getName() != null) {
			return ("NONE".equals(req.getProviderID()) || req.getProviderID() == null || req.getProviderID().isEmpty())? "회원".getBytes("UTF-8") : user.getName().getBytes("UTF-8");
		}

		return null;
	}

	public ResultWithData login(User user) {
		// 유효한 token 이라면 그대로 유지
		// 유효하지 않다면 아이디, 비번 체크하여 인증되면 token 생성, 저장 후 전달

		int errcode = this.findUserbyIdPasword(user.getId(), user.getLoginId(), user.getPassword(), user);
		String errMessage = "";

		switch (errcode) {
		case ErrorCode.ILLEGAL_PROCESS:
			errMessage = "Id or password is needed";
			break;
		case ErrorCode.DB_ERROR:
			errMessage = "Error! DB or Network error";
			break;
		case ErrorCode.INVALID_PARAMETER:
			errMessage = "ID or password is invalid";
			break;
		}

		if (errcode != 0) {
			return ResultWithData.failuerResult().setCode(errcode).setMessage(errMessage);
		}

		try {
			user = saveAuthToken(user.getId());
		} catch (Exception e) {
			errMessage = "Error! DB or Network error";
			return ResultWithData.failuerResult().setCode(ErrorCode.DB_ERROR).setMessage(errMessage);
		}

		if (errcode != 0) {
			errMessage = "Error! DB or Network error";
			return ResultWithData.failuerResult().setCode(ErrorCode.DB_ERROR).setMessage(errMessage);
		}

		return ResultWithData.succcessResult().addData("user", user).addData("auth_token", user.getAuthToken());
	}

	public ResultWithData login(UserServiceProviderInfoReq req) 
			throws UnsupportedEncodingException {
		
		User user = null;
		boolean isExist =  false;
		
		try {
			user = userAuthMapper.findUserbyProviderUserId(req);
		} catch (Exception e) {
			return ResultWithData.failuerResult();
		}

		// provideId가 없을 때 deviceInfo 정보로 다시 체크한다.
		if(user != null && user.getId() != null && !user.getId().isEmpty()) {
			isExist = true;
		} else if (req.getUserDeviceInfo() != null 
				&& req.getUserDeviceInfo().getDeviceType() != null && req.getUserDeviceInfo().getDeviceId() != null) {
				user =  userAuthMapper.findUserbyDeviceInfo(req);
		}
	
		if (user == null || user.getId() == null || user.getId().isEmpty()) {	
			return ResultWithData.succcessResult().addData("isExist", isExist);
		} else {
			
			// 디바이스 정보가 있는데 회원 가입이 안되어 있다면 회원가입을 위해 isExist로 돌린다.
			
			if (req.getSocialConnectionInfo() != null && req.getSocialConnectionInfo().getProviderId() != null && req.getSocialConnectionInfo().getProviderUserId() != null) {
				
			} else if (req.getUserDeviceInfo() != null) {
				req.getUserDeviceInfo().setUserId(user.getId());
				try {
					userAuthMapper.setUserDeviceInfo(req.getUserDeviceInfo());
				} catch (Exception e) {
					return ResultWithData.failuerResult();
				}
			}
			String authToken = "";
			String userId = user.getId();

			try {
				authToken = CommonUtil.transSHA256(userId);
				AuthTokenReq tokenReq = new AuthTokenReq(userId, authToken);
				userAuthMapper.setAuthToken(tokenReq);
			} catch (Exception e) {
				return ResultWithData.failuerResult();
			}

			ResultWithData result = this.getFavoriteCheckOtherList(user);
			if(ErrorCode.SUCCESS == result.getCode()) {
				return result.addData("isExist", isExist).addData("me", user.getUserSimple()).addData("auth_token", authToken);
			} else {
				return ResultWithData.succcessResult().addData("isExist", isExist).addData("me", user.getUserSimple()).addData("auth_token", authToken);
			}
		}	
	}

	public User saveAuthToken(String userId) {
		// token 생성 , 저장 및 전달
		String authToken = "";
		User user = null;
		try {
			authToken = CommonUtil.transSHA256(userId);
			AuthTokenReq req = new AuthTokenReq(userId, authToken);
			userAuthMapper.setAuthToken(req);
			user = new User(userId, authToken);
			user = userAuthMapper.getUserbyAuthToken(user);
		} catch (Exception e) {
			return null;
		}

		return user;
	}

	public ResultWithData findUserbyIdPasword(String loginId, String password) {

		User user = new User();
		user.setLoginId(loginId);
		user.setPassword(password);
		int errcode = this.findUserbyIdPasword(null, loginId, password, user);
		String errMessage = "";

		switch (errcode) {
		case ErrorCode.ILLEGAL_PROCESS:
			errMessage = "Id or password is needed";
			break;
		case ErrorCode.DB_ERROR:
			errMessage = "Error! DB or Network error";
			break;
		case ErrorCode.INVALID_PARAMETER:
			errMessage = "ID or password is invalid";
			break;
		}

		if (errcode == 0) {
			return ResultWithData.failuerResult().setCode(errcode).setMessage(errMessage);
		}

		return ResultWithData.succcessResult().addData("user", user);
	}

	public int findUserbyIdPasword(String id, String loginId, String password, User user) {
		// 아이디가 없응 겨웅
		// 비번이 없응 경우
		// 아이디 비번이 안 맞을 경우

		int errcode = 0;

		if (loginId == null || loginId.trim().length() == 0 || password == null || password.trim().length() == 0) {
			return ErrorCode.ILLEGAL_PROCESS;
		}

		User result = null;

		try {
			UserReq req = new UserReq(loginId, password);
			result = userAuthMapper.findUserbyIdPassword(req);
		} catch (Exception e) {
			return ErrorCode.DB_ERROR;
		}

		if (result == null) {
			return ErrorCode.INVALID_PARAMETER;
		}

		user.setId(result.getId());
		user.setName(result.getName());

		return errcode;
	}

	public ResultWithData createSPUserByServiceProviderInfo(UserServiceProviderInfoReq req)
			throws UnsupportedEncodingException {
		if((req.getSocialConnectionInfo() == null || req.getSocialConnectionInfo().getProviderId() == null) 
				&& req.getUserDeviceInfo() != null && req.getUserDeviceInfo().getDeviceType() != null && req.getUserDeviceInfo().getDeviceId() != null) {
			// provideId가 없고 deviceId가 있다면 비로그인회원으로 등록한다.
			return this.createUserByDeviceInfo(req);
		}
		
		try {
			userAuthMapper.setUserServiceProviderInfo(req);
		} catch (Exception e) {
			return ResultWithData.failuerResult();
		}

		if ("-200".equals(req.getResultCode())) { // ***** 추후 코드
			return ResultWithData.failuerResult().setCode(ErrorCode.ALREADY_EXIST).setMessage("login id already exist");
		}

		if (req.getUserDeviceInfo() != null) {
			req.getUserDeviceInfo().setUserId(req.getUserInfo().getId());
			try {
				userAuthMapper.setUserDeviceInfo(req.getUserDeviceInfo());
			} catch (Exception e) {
				return ResultWithData.failuerResult();
			}
		}
		String authToken = "";
		String userId = req.getUserInfo().getId();

		try {
			authToken = CommonUtil.transSHA256(userId);
			AuthTokenReq tokenReq = new AuthTokenReq(userId, authToken);
			userAuthMapper.setAuthToken(tokenReq);
		} catch (Exception e) {
			return ResultWithData.failuerResult();
		}

		User user = new User();
		user.setEmail(req.getUserInfo().getEmail());
		user.setLoginId(req.getUserInfo().getLoginId());
		user.setName(req.getUserInfo().getName());
		user.setMobile(req.getUserInfo().getMobile());
		user.setGender(req.getUserInfo().getGender());
		user.setId(req.getUserInfo().getId());
		user.setStatus(req.getUserInfo().getStatus());
		user.setProviderId(req.getSocialConnectionInfo().getProviderId());
		user.setImageOriginalUrl(req.getSocialConnectionInfo().getImageUrl());
		user.setImageThumbUrl(req.getSocialConnectionInfo().getImageUrl());
		
		ResultWithData result = this.getFavoriteCheckOtherList(user);
		if(ErrorCode.SUCCESS == result.getCode()) {
			return result.addData("me", user.getUserSimple()).addData("auth_token", authToken);
		} else {
			return ResultWithData.succcessResult().addData("me", user.getUserSimple()).addData("auth_token", authToken);
		}
	}
	
	public ResultWithData createUserByDeviceInfo(UserServiceProviderInfoReq req) 
			throws UnsupportedEncodingException {
		try {
			userAuthMapper.registerUserbyDeviceInfo(req);
		} catch (Exception e) {
			return ResultWithData.failuerResult();
		}
		
		if (!String.valueOf(ErrorCode.SUCCESS).equals(req.getResultCode())) {
			return ResultWithData.failuerResult();
		}

		if (req.getUserDeviceInfo() != null) {
			req.getUserDeviceInfo().setUserId(req.getUserInfo().getId());
			try {
				userAuthMapper.setUserDeviceInfo(req.getUserDeviceInfo());
			} catch (Exception e) {
				return ResultWithData.failuerResult();
			}
		}
		String authToken = "";
		String userId = req.getUserInfo().getId();

		try {
			authToken = CommonUtil.transSHA256(userId);
			AuthTokenReq tokenReq = new AuthTokenReq(userId, authToken);
			userAuthMapper.setAuthToken(tokenReq);
		} catch (Exception e) {
			return ResultWithData.failuerResult();
		}

		User user = new User();
		user.setId(req.getUserInfo().getId());
		user.setStatus(UserStatusType.BEFOREFIRSTLOGIN.getCode());

		ResultWithData result = this.getFavoriteCheckOtherList(user);
		if(ErrorCode.SUCCESS == result.getCode()) {
			return result.addData("me", user.getUserSimple()).addData("auth_token", authToken);
		} else {
			return ResultWithData.succcessResult().addData("me", user.getUserSimple()).addData("auth_token", authToken);
		}
	}

	public ResultWithData logout(String authToken) {
		
		try {
			AuthTokenReq req = new AuthTokenReq();
			req.setAuthToken(authToken);
			req.setTokenActionType('D');
			userAuthMapper.setAuthToken(req);
		} catch (Exception e) {
			return ResultWithData.failuerResult();
		}

		return ResultWithData.succcessResult();
	}

	public ResultWithData isExists(String lid) {

		User user = null;
		try {
			UserReq req = new UserReq();
			req.setLoginId(lid);
			user = userAuthMapper.findUserbyLoginId(req);
		} catch (Exception e) {
			return ResultWithData.failuerResult();
		}

		return ResultWithData.succcessResult().addData("isExist", user != null && user.getId() != null);

	}

	public ResultWithData getFavoriteCheckRecipeList(User user) {

		List<RecipeforFavoriteCheckVO> list = new ArrayList<RecipeforFavoriteCheckVO>();
		try {
			RecipeReq req = new RecipeReq();
			// req.setUserId(user.getId()); //***** 매번 새로 설정하기에 사용자 설정하지 않는다.
			list = recipeMapper.getCheckFavoriteRecipeList(req);
		} catch (Exception e) {
			return ResultWithData.failuerResult();
		}
		
		if(list != null && !list.isEmpty()) {
			int i = 1;
			for(RecipeforFavoriteCheckVO vo : list) {
				vo.setPriority(i++);
			}
		}

		return ResultWithData.succcessResult().addData("list", list);
	}

	public ResultWithData modifyFavoriteStep1(ModifyFavoriteStep1Req req) {

		try {
			userAuthMapper.modifyFavoriteStep1(req);
		} catch (Exception e) {
			return ResultWithData.failuerResult();
		}

		if (req.getResultCode() == 0) {
			return ResultWithData.succcessResult();
		}

		return ResultWithData.failuerResult();
	}

	public ResultWithData getFavoriteCheckOtherList(User user) {
		UserReq req = new UserReq();
		req.setId(user.getId());

		List<CheckHealthVO> list = userAuthMapper.getCheckHealthList(req);
		List<CheckHealthVO> healthlist = new ArrayList<CheckHealthVO>();
		List<CheckHealthVO> healthadvlist = new ArrayList<CheckHealthVO>();

		if (list != null && !list.isEmpty()) {
			for (CheckHealthVO health : list) {
				if (health.getLevel() == 1) {
					healthlist.add(health);
				} else {
					healthadvlist.add(health);
				}
			}
		}

		List<CheckJobVO> joblist = userAuthMapper.getCheckJobList(req);
		CheckActivityVO activityLevel = userAuthMapper.getCheckActivityLevel(req);

		int level = (activityLevel != null) ? activityLevel.getLevel() : -1;

		List<CheckActivityVO> activityList = new ArrayList<CheckActivityVO>();

		activityList.add(new CheckActivityVO("매우 적음", 1, (level == 1) ? 1 : 0));
		activityList.add(new CheckActivityVO("적음", 2, (level == 2) ? 1 : 0));
		activityList.add(new CheckActivityVO("보통", 3, (level == 3) ? 1 : 0));
		activityList.add(new CheckActivityVO("많음", 4, (level == 4) ? 1 : 0));
		activityList.add(new CheckActivityVO("매우 많음", 5, (level == 5) ? 1 : 0));

		CheckActivityVO ageLevel = userAuthMapper.getAgeLevel(req);
		
		int levelAge = (activityLevel != null)? ageLevel.getLevel() : -1;
		
		List<CheckActivityVO> ageList = new ArrayList<CheckActivityVO>();
		
		ageList.add(new CheckActivityVO("10대", 1, (levelAge == 1)? 1: 0));
		ageList.add(new CheckActivityVO("20대", 2, (levelAge == 2)? 1: 0));
		ageList.add(new CheckActivityVO("30대", 3, (levelAge == 3)? 1: 0));
		ageList.add(new CheckActivityVO("40대 이상", 4, (levelAge == 4)? 1: 0));

		return ResultWithData.succcessResult().addData("healthList", healthlist).addData("healthAdvList", healthadvlist).addData("jobList", joblist).addData("activityLevelList", activityList).addData("ageLevelList", ageList);

	}

	public ResultWithData modifyCheckHealth(User user, String healthId) {
		UserReq req = new UserReq();
		req.setId(user.getId());
		req.setHealthId(healthId);
		try {
			userAuthMapper.modifyCheckHealth(req);
		} catch (Exception e) {
			return ResultWithData.failuerResult();
		}
		if (req.getResultCode() == 0) {
			return ResultWithData.succcessResult().addData("type", HealthType.NORMAL).addData("healthId", healthId).addData("isSelected",
					req.getIsSelected());
		}
		return ResultWithData.failuerResult();
	}
	
	public ResultWithData modifyCheckInterestHealth(User user, String healthId) {
		UserReq req = new UserReq();
		req.setId(user.getId());
		req.setHealthId(healthId);
		try {
			userAuthMapper.modifyCheckHealth(req);
		} catch (Exception e) {
			return ResultWithData.failuerResult();
		}
		if (req.getResultCode() == 0) {
			return ResultWithData.succcessResult().addData("type", HealthType.INTEREST).addData("healthId", healthId).addData("isSelected",
					req.getIsSelected());
		}
		return ResultWithData.failuerResult();
	}

	public ResultWithData modifyCheckJob(User user, String jobId) {
		UserReq req = new UserReq();
		req.setId(user.getId());
		req.setJobId(jobId);
		try {
			userAuthMapper.modifyCheckJob(req);
		} catch (Exception e) {
			return ResultWithData.failuerResult();
		}
		if (req.getResultCode() == 0) {
			return ResultWithData.succcessResult().addData("type", HealthType.JOB).addData("jobId", jobId).addData("isSelected", req.getIsSelected());
		}
		return ResultWithData.failuerResult();
	}

	public ResultWithData modifyCheckActivityLevel(UserReq req) {
		try {
			userAuthMapper.modifyCheckActivityLevel(req);
		} catch (Exception e) {
			return ResultWithData.failuerResult();
		}
		if (req.getResultCode() == 0) {
			return ResultWithData.succcessResult().addData("type", HealthType.ACTIVITY).addData("activityLevel", req.getActivityLevel());
		}
		return ResultWithData.failuerResult();
	}
	
	public ResultWithData modifyAgeLevel(UserReq req) {

		
		try  { 
			userAuthMapper.modifyAgeLevel(req);
		} catch (Exception e) {
			return ResultWithData.failuerResult();
		}
		
		if(req.getResultCode() == 0) {
			return ResultWithData.succcessResult().addData("type", HealthType.AGE).addData("ageLevel", req.getAgeLevel());
		}
		
		return ResultWithData.failuerResult();
	}		

	public ResultWithData getUsers(UserReq userReq) {

		// 키워드 검색
		KeywordReq tmpKeywordReq = userReq.getKeywordReq();
		KeywordReq keywordReq = this.setKeywordReq(userReq.getKeywordReq());
		if (keywordReq != null) {
			userReq.setKeywordReq(keywordReq);
		}

		ResultWithData result = ResultWithData.succcessResult();
		PageInfoVO pageInfo = userAuthMapper.getUsersPageInfo(userReq);
		pageInfo.setCurrentPageNum(userReq.getPageNum());
		pageInfo.setPageSize(userReq.getPageSize());
		result.addData("pageInfo", pageInfo);
		List<UserSimple> list = userAuthMapper.getUsersList(userReq);

		// 추가된 요청 정보 다시 초기화
		userReq.setKeywordReq(tmpKeywordReq);
		return ResultWithData.succcessResult().addData("pageInfo", pageInfo).addData("list", list).addData("req",
				userReq);
	}

	public ResultWithData updateUser(User user, UserReq userReq) {
		if (user != null && user.getId() != null && !user.getId().isEmpty()) {
			int resultCode = userAuthMapper.updateUser(userReq);
			if (resultCode != 1) {
				return ResultWithData.failuerResult().setCode(ErrorCode.DB_ERROR).setMessage("user info update error");
			}
		} else {
			return ResultWithData.failuerResult().setCode(ErrorCode.NOTEXIST).setMessage("user is not exist");
		}
		// **** 이미지외 개인정보 수정될 떄 추가
		user.setImageOriginalUrl(userReq.getImageOriginalUrl());
		user.setImageThumbUrl(userReq.getImageThumbUrl());
		return ResultWithData.succcessResult().addData("me", user);
	}

	protected KeywordReq setKeywordReq(KeywordReq keyReq) {
		if (keyReq == null)
			return null;

		KeywordReq keywordReq = new KeywordReq(keyReq.getKeyVar(), keyReq.getKeyValue());

		if (keywordReq.getKeyVar() != null && keywordReq.getKeyVar().toUpperCase().equals("ALL")) {
			keywordReq.setKeyCol(null);
			keywordReq.setKeyType("string");
		} else if (keywordReq.getKeyVar() != null && keywordReq.getKeyVar().toUpperCase().equals("NAME")) {
			keywordReq.setKeyCol("u.name");
			keywordReq.setKeyType("string");
		} else if (keywordReq.getKeyVar() != null && keywordReq.getKeyVar().toUpperCase().equals("EMAIL")) {
			keywordReq.setKeyCol("u.loginid"); // ***** email 항목 추가
			keywordReq.setKeyType("string");
		}
		return keywordReq;
	}
}

package com.onethefull.recipe.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;
import javax.imageio.ImageIO;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.onethefull.recipe.comm.ErrorCode;
import com.onethefull.recipe.comm.ResultWithData;
import com.onethefull.recipe.comm.auth.User;
import com.onethefull.recipe.comm.auth.UserSimple;
import com.onethefull.recipe.comm.service.MailService;
import com.onethefull.recipe.comm.service.UserService;
import com.onethefull.recipe.comm.util.FileEncodingChecker;
import com.onethefull.recipe.comm.util.FileEncodingType;
import com.onethefull.recipe.comm.util.RecipeParser;
import com.onethefull.recipe.comm.util.S3Client;
import com.onethefull.recipe.comm.vo.MailContext;
import com.onethefull.recipe.mapper.CognizeMapper;
import com.onethefull.recipe.mapper.UploadMapper;
import com.onethefull.recipe.req.ConditionCognizeReq;
import com.onethefull.recipe.req.ConditionReq;
import com.onethefull.recipe.req.ProfileReq;
import com.onethefull.recipe.req.RecipeReq;
import com.onethefull.recipe.req.UploadReq;
import com.onethefull.recipe.req.UserReq;
import com.onethefull.recipe.type.ImageType;
import com.onethefull.recipe.type.UploadType;
import com.onethefull.recipe.vo.UploadVO;

import net.coobird.thumbnailator.Thumbnails;

@Service("uploadService")
public class UploadService {

	@Resource(name = "uploadMapper")
	private UploadMapper uploadMapper;
	
	@Resource(name = "mailService")
	private MailService mailService;
	
	@Resource(name = "userService")
	private UserService userService;
	
	@Autowired
	private S3Client s3Client;

	@Resource(name = "cognizeMapper")
	private CognizeMapper cognizeMapper;	

	public ResultWithData updateProfile(User user, ProfileReq profileReq) {
		ResultWithData resultWithData = ResultWithData.succcessResult();
		resultWithData = this.saveFileS3(UploadType.PROFILE, profileReq, resultWithData);
		if(resultWithData.getCode() == ErrorCode.SUCCESS) {
			UploadVO uploadVO = (UploadVO) resultWithData.getData("result");
			UserReq userReq = new UserReq();
			userReq.setId(profileReq.getUserId());
			userReq.setImageOriginalUrl(uploadVO.getImageOriginalPath());
			userReq.setImageThumbUrl(uploadVO.getImageThumbPath());
			resultWithData = userService.updateUser(user, userReq);
		}
		//TODO DB update Logic
		return resultWithData;
	}
	
	public ResultWithData insertCondition(final ConditionReq conditionReq) {
		ResultWithData resultWithData = ResultWithData.succcessResult();
		resultWithData = this.saveFileS3(UploadType.CONDITION, conditionReq, resultWithData);
		if(resultWithData.getCode() == ErrorCode.SUCCESS) {
			UploadVO uploadVO = (UploadVO) resultWithData.getData("result");
			ConditionCognizeReq conditionCognizeReq = new ConditionCognizeReq();
			conditionCognizeReq.setUserId(conditionReq.getUserId());
			conditionCognizeReq.setImageUrl(uploadVO.getImageOriginalPath());
			cognizeMapper.insertCondition(conditionCognizeReq);
			if(conditionCognizeReq.getResultCode() != ErrorCode.SUCCESS) {
				resultWithData.setCode(ErrorCode.DB_ERROR).setMessage("condition image DB insert error");
			}
		}
		
		class CondtionThread extends Thread {
			private UserService userService;
			private MailService mailService;
			
			public CondtionThread(UserService userService, MailService mailService) {
				this.userService = userService;
				this.mailService = mailService;
			}
			
			public void run() {
				UserReq userReq = new UserReq();
				userReq.setAdmin(true);
				ResultWithData result = userService.getUsers(userReq);
				@SuppressWarnings("unchecked")
				List<UserSimple> userList = (List<UserSimple>) result.getData("list");
				for(UserSimple user : userList) {
					MailContext mailContext = new MailContext();
					mailContext.setToAddress(user.getLoginId());
					mailContext.setSubject("새로운 식자재 인식 요청이 들어왔습니다.");
					mailContext.setMessage("새로운 식자재 인식이 들어왔습니다!");
					mailService.sendMail(mailContext);	
				}
			}
		}
		
		CondtionThread ct = new CondtionThread(userService, mailService);
		ct.start();
		
		return resultWithData;
	}

	@SuppressWarnings("finally")
	public ResultWithData insertRecipe(RecipeReq recipeReq) {
		ResultWithData resultWithData = ResultWithData.succcessResult();
		try {
			if(!FileEncodingChecker.FileEncodingCheck(recipeReq.getCsv().getInputStream(), FileEncodingType.UTF_8)) {
				resultWithData.setCode(ErrorCode.FILE_ENCODING_ERROR).setMessage("File encoding error");
			} else {
				resultWithData = this.saveFileS3(UploadType.RECIPE, recipeReq, resultWithData);
				/*
				int result = uploadMapper.insertRecipe(recipeReq);
				if (result != 0) {
					resultWithData.setCode(ErrorCode.FAIL).setMessage("Image Upload Error");
				}
				 */
				try {
					RecipeParser RP = new RecipeParser();
					UploadVO uploadVO = (UploadVO) resultWithData.getData("result");
					int resultCode = RP.insertMaster(recipeReq.getCsv().getInputStream(), uploadVO); 
					if(resultCode != ErrorCode.SUCCESS) {
						resultWithData.setCode(resultCode).setMessage("CSV Upload DB Error");
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					resultWithData.setCode(ErrorCode.FILE_NOT_FOUND_ERROR).setMessage("CSV File not found Error");
				}  catch (SQLException se) {
					se.printStackTrace();
					resultWithData.setCode(ErrorCode.INVALID_PARAMETER).setMessage("parameter is too long");
				} finally {
					return resultWithData;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			resultWithData.setCode(ErrorCode.FILE_ENCODING_ERROR).setMessage("File encoding error");
		} finally {
			return resultWithData;
		}
	}

	@SuppressWarnings("finally")
	private ResultWithData saveFileS3(final UploadType type, final UploadReq uploadReq, ResultWithData resultWithData) {
		if (uploadReq == null) {
			resultWithData = ResultWithData.failuerResult().setCode(ErrorCode.FILE_WRITE_ERROR)
					.setMessage("Error writing file");
			return resultWithData;
		}
		try {
			UploadVO uploadVO = new UploadVO();
			String fileName = uploadReq.getFile().getOriginalFilename();
			String text;
			String uploadPath = type.toString();
			if (UploadType.RECIPE.equals(type) || UploadType.PROFILE.equals(type)) {
				fileName = new ObjectId().toString();
				uploadPath += "/MST";
				text = s3Client.uploadImage(String.format("%s/%s", uploadPath, String.format("%s_thumb", fileName)),
						this.convertToQuality(uploadReq.getFile().getInputStream(), ImageType.THUMBNAIL));
				uploadVO = this.setUploadVO(ImageType.THUMBNAIL, uploadVO, this.getS3UrltoString(text));
			}
			text = this.s3Client.uploadImage(String.format("%s/%s", uploadPath, fileName),
					this.convertToQuality(uploadReq.getFile().getInputStream(), ImageType.ORIGINAL));
			uploadVO = this.setUploadVO(ImageType.ORIGINAL, uploadVO, this.getS3UrltoString(text));
			resultWithData.addData("result", uploadVO);
		} catch (IllegalStateException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			resultWithData = ResultWithData.failuerResult().setCode(ErrorCode.FILE_WRITE_ERROR)
					.setMessage("Error writing file");
		} finally {
			return resultWithData;
		}
	}

	private String getS3UrltoString(final String urlString) {
		int index = urlString.indexOf("?");
		return urlString.substring(0, index);
	}

	private UploadVO setUploadVO(final ImageType imageType, UploadVO uploadVO, final String imageUrl) {
		if(!StringUtils.isEmpty(imageUrl)) {
			switch (imageType) {
				case MIDDLE:
					uploadVO.setImageMiddlePath(imageUrl);
					break;
				case THUMBNAIL:
					uploadVO.setImageThumbPath(imageUrl);	
					break;
				default:
					uploadVO.setImageOriginalPath(imageUrl);
					break;
			}
		}
		return uploadVO;
	}

	private InputStream convertToQuality(InputStream imageFile, ImageType imageType) throws IOException {
		if(imageType == null) {
			imageType = ImageType.defaultImageType();
		}
		switch (imageType) {
			case MIDDLE:
				imageFile = this.makeToMiddle(ImageIO.read(imageFile));
				break;
			case THUMBNAIL:
				imageFile = this.makeToThumb(ImageIO.read(imageFile));	
				break;
			default:
				
				break;
		}
		return imageFile;
	}
	
	private InputStream makeToThumb(BufferedImage bufferedImage) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		//Thumbnails.of(bufferedImage).size(150, 150).outputQuality(0.5f).outputFormat("jpeg").toOutputStream(os);
		Thumbnails.of(bufferedImage).scale(0.05f, 0.05f).outputQuality(0.5f).outputFormat("jpeg").toOutputStream(os);
		InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
		return inputStream;
	}

	private InputStream makeToMiddle(BufferedImage bufferedImage) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		//Thumbnails.of(bufferedImage).size(500, 500).outputQuality(0.5f).outputFormat("jpeg").toOutputStream(os);
		Thumbnails.of(bufferedImage).scale(0.2f, 0.2f).outputQuality(0.5f).outputFormat("jpeg").toOutputStream(os);
		InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
		return inputStream;
	}

	private ResultWithData sendInsertConditionMail() {
		MailContext mailContext = new MailContext();
		mailContext.setToAddress("sjkim@1thefull.com");
		mailContext.setSubject("테스트입니다.");
		mailContext.setMessage("asdfafa");
		mailService.sendMail(mailContext);
		return ResultWithData.succcessResult();
	}
	
}

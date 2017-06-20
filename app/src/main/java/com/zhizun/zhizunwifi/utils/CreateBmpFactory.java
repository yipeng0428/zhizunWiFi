package com.zhizun.zhizunwifi.utils;

import java.io.File;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.WindowManager;
import android.widget.Toast;

public class CreateBmpFactory {

	// ----------相机图片的业务相关
	public static final int PHOTO_REQUEST_CAREMA = 1;// 拍照
	public static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择

	private Fragment fragment;
	private Activity activity;
	public static File tempFile;
	private StringBuilder builder;

	public CreateBmpFactory(Fragment fragment) {
		super();
		this.fragment = fragment;
		WindowManager wm = (WindowManager) fragment.getActivity()
				.getSystemService(Context.WINDOW_SERVICE);
		windowHeight = wm.getDefaultDisplay().getHeight();
		windowWidth = wm.getDefaultDisplay().getWidth();
	}

	public int windowHeight;
	public int windowWidth;

	public CreateBmpFactory(Activity activity) {
		super();
		this.activity = activity;
		WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
		windowHeight = wm.getDefaultDisplay().getHeight();
		windowWidth = wm.getDefaultDisplay().getWidth();
		// builder.append("{\"imgPath\":");
		// builder.append(array.toString());
		// builder.append("}");
	}

	public void OpenGallery() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		if (fragment != null) {
			fragment.startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
		} else {
			activity.startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
		}
	}

	public void OpenCamera() {
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		if (hasSdcard()) {
			tempFile = new File(Environment.getExternalStorageDirectory(), UUID
					.randomUUID().toString() + ".png");
			System.out.println("OpenCamera tempFile= " + tempFile.toString());
			BaseUtils.setSharedPreferences("CameraImgPath",
					tempFile.toString(), activity);
			Uri uri = Uri.fromFile(tempFile);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		}
		if (fragment != null) {
			fragment.startActivityForResult(intent, PHOTO_REQUEST_CAREMA);
		} else {
			activity.startActivityForResult(intent, PHOTO_REQUEST_CAREMA);
		}
	}

	public String getBitmapFilePath(int requestCode, int resultCode, Intent data, boolean setHeadImg)
			throws JSONException {
		JSONArray array = new JSONArray();
		if (requestCode == PHOTO_REQUEST_GALLERY) {
			if (data != null) {
				Uri uri = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				Cursor cursor = null;
				if (fragment != null) {
					cursor = fragment.getActivity().getContentResolver()
							.query(uri, filePathColumn, null, null, null);
				} else {
					cursor = activity.getContentResolver().query(uri,
							filePathColumn, null, null, null);
				}
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String picturePath = cursor.getString(columnIndex);
				cursor.close();
				// builderImgPathJson(picturePath, array);
				//限制图片大小
//				if (FileSizeUtil.getFileOrFilesSize(picturePath, 3) < 1.5) {
				if(!setHeadImg){
					builderImgPath(picturePath);
				}
				return picturePath;
//				} else {
//					Toast.makeText(activity, "上传图片不能超过1.5", 1)
//							.show();
//				}


			}
		} else if (requestCode == PHOTO_REQUEST_CAREMA) {
			if (hasSdcard()) {
				System.out.println("getBitmapFilePath tempFile= " + tempFile);
				if (tempFile != null){
					Bitmap bitmap = BitmapFactory.decodeFile(tempFile.toString());
					if (tempFile != null && bitmap != null) {
						System.out.println("getBitmapFilePath tempFile= "
								+ tempFile.toString());
						String fileStr = BaseUtils.getSharedPreferences(
								"CameraImgPath", activity);
						//限制图片大小
//					if (FileSizeUtil.getFileOrFilesSize(fileStr, 3) < 1.5) {
						System.out.println("getBitmapFilePath fileStr= "
								+ fileStr);
						// builderImgPathJson(fileStr, array);
						if(!setHeadImg){
							builderImgPath(fileStr);
						}
						File file = new File(fileStr);
						return file.getAbsolutePath();
//					} else {
//						Toast.makeText(activity, "上传图片不能超过1.5M", 1).show();
//					}

					}
				}
			} else {
				if (fragment != null) {
					Toast.makeText(fragment.getActivity(), "未找到存储卡，无法存储照片！", Toast.LENGTH_SHORT)
							.show();
				} else {
					Toast.makeText(activity, "未找到存储卡，无法存储照片！", Toast.LENGTH_SHORT).show();
				}
			}
		}
		return null;
	}

	// private void builderImgPathJson(String picturePath, JSONArray array)
	// throws JSONException{
	// builder = new StringBuilder();
	// builder.append("{\"imgPath\":");
	// String ImgPathJson = BaseUtils.getSharedPreferences("ImgPathJson",
	// activity);
	// JSONObject jo = new JSONObject();
	// jo.put("picturePath", picturePath);
	// if(ImgPathJson == null || ImgPathJson.equals("")){
	// array.put(jo);
	// builder.append(array.toString());
	// builder.append("}");
	// System.out.println("builder= " + builder.toString());
	// // JSONObject jo2 = new JSONObject(builder.toString());
	// // JSONArray jsonArray = jo2.getJSONArray("imgPath");
	// // for(int i = 0; i < jsonArray.length(); i++){
	// // JSONObject jo3 = (JSONObject) jsonArray.get(i);
	// // System.out.println("jo3.get(picturePath)= " + jo3.get("picturePath"));
	// // }
	// BaseUtils.setSharedPreferences("ImgPathJson", builder.toString(),
	// activity);
	// }else{
	// JSONObject jo2 = new JSONObject(ImgPathJson);
	// JSONArray jsonArray = jo2.getJSONArray("imgPath");
	// jsonArray.put(jo);
	// builder.append(jsonArray.toString());
	// builder.append("}");
	// BaseUtils.setSharedPreferences("ImgPathJson", builder.toString(),
	// activity);
	// }
	// System.out.println("要上传图片的json格式数据= " + builder.toString());
	// }

	private void builderImgPath(String picturePath) {
		String ImgPath = BaseUtils.getSharedPreferences("ImgPath", activity);
		if (ImgPath == null || ImgPath.equals("")) {
			BaseUtils.setSharedPreferences("ImgPath", picturePath, activity);
		} else {
			String[] split = ImgPath.split(",");
			for (int i = 0; i < split.length; i++) {
				if (picturePath.equals(split[i])) {
					Toast.makeText(activity, "请不要添加重复的图片！", Toast.LENGTH_SHORT).show();
					return;
				}
			}
			builder = new StringBuilder();
			builder.append(ImgPath + "," + picturePath);
			BaseUtils.setSharedPreferences("ImgPath", builder.toString(),
					activity);

		}
	}

	private int aspectX = 0;
	private int aspectY = 0;

	public Bitmap getBitmapByOpt(String picturePath) {
		Options opt = new Options();
		opt.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(picturePath, opt);
		int imgHeight = opt.outHeight;
		int imgWidth = opt.outWidth;
		int scaleX = imgWidth / windowWidth;
		int scaleY = imgHeight / windowHeight;
		int scale = 1;
		if (scaleX > scaleY & scaleY >= 1) {
			scale = scaleX;
		}
		if (scaleY > scaleX & scaleX >= 1) {
			scale = scaleY;
		}
		opt.inJustDecodeBounds = false;
		opt.inSampleSize = scale;
		return BitmapFactory.decodeFile(picturePath, opt);
	}

	private boolean hasSdcard() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}
}
package me.yongbo.robot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import me.yongbo.bean.HuabanImage;
import me.yongbo.bean.HuabanPin;
import me.yongbo.bean.HuabanResponse;
import me.yongbo.robot.util.HttpUtil;

public class HuabanRobot extends WebRobot {
	public static String rootDir = "D:/wakao/webimage/hbimage/";
	private final static String BEFORE = "hb_";

	private final static String HOST = "huaban.com";
	private final static String IMG_HOST = "http://img.hb.aicdn.com/%1$s_fw580";
	private final static String REFERER = "http://huaban.com/";
	private final static String POINT_URL = "http://huaban.com/favorite/pets/?hji68jpd&max=%1$s&limit=20&wfl=1";

	private String max;
	private String category;

	private Gson gson;

	public HuabanRobot(String max, String category) {
		super(HttpUtil.getHttpGet(getRequestHeaders()));
		this.max = max;
		this.category = category;
		this.gson = new Gson();
	}

	private Boolean isOkImageType(String type) {
		Boolean isImage = true;
		switch (type) {
		case "image/jpeg":
			break;
		case "image/gif":
			break;
		case "image/png":
			break;
		case "image/jpg":
			break;
		default:
			isImage = false;
		}
		return isImage;
	}

	private void handlerData(List<HuabanPin> hps) {
		initSaveDir(rootDir);
		HuabanImage img = null;
		for (HuabanPin hp : hps) {
			img = hp.getFile();

			String imgUrl = String.format(IMG_HOST, img.getKey());
			System.out.println(imgUrl);
			if (isOkImageType(img.getType())) {
				String fileType = "."
						+ img.getType().substring(
								img.getType().indexOf("/") + 1);
				String fileName = BEFORE + hp.getPin_id() + fileType;
				img.setId(hp.getPin_id());
				img.setImgUrl(imgUrl);
				img.setSavePath(curDir + fileName);
				downImage(imgUrl, folderPath, fileName);
			}
		}
		// 写入数据库
		if (databaseEnable) {
			// dbHelper.execute("saveImage", imgs);
		}
	}

	public void doWork() {
		String rp = getResponseString(String.format(POINT_URL, max));
		HuabanResponse response = gson.fromJson(rp, HuabanResponse.class);
		List<HuabanPin> hps = response.getPins();
		max = hps.get(hps.size() - 1).getPin_id();
		System.out.println(rp);
		handlerData(hps);
	}

	@Override
	public void run() {
		while (doAgain) {
			doWork();
		}
	}

	public static Map<String, String> getRequestHeaders() {
		Map<String, String> param = new HashMap<>();
		param.put("Referer", REFERER);
		param.put("Host", HOST);
		param.put("X-Request", "JSON");
		param.put("X-Requested-With", "XMLHttpRequest");
		return param;
	}
}
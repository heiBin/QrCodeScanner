##QrCodeScanner扫码工具
> 融合Zxing和Zbar框架，对条形码二维码高兼容，扫码效率奇高！

###开发背景
前段采用Zxing和Zbar扫码库扫码时，发现针对二维码和条形码，两个库有不同的效果，Zxing能高效精准识别二维码，但在条形码上却不尽人意，在某些手机死活识别不出来；而后采用Zbar库，在条形码上效果杠杠的，而二维码相对Zxing就逊色很多了。根据这些情况，我就觉得有必要搞个'万金油'，把两者优势融合起来，于是结合Zxing二维码算法和Zbar条形码算法的QrCodeScanner扫码工具就凭空而出。

###开发工作
比对了Zxing和Zbar两个扫码Demo，Zxing的相机扫描部分算是做得比较精良，Zbar就不想说了，最后决定采用Zxing扫描部分的代码，但是也有很多地方与需求不符，所以结合网上资料和自己的分析，修改了其中的代码。然后整个识别算法代码是放在C层的。下面就将几个核心部分列举出来。
#####1.调整扫描采样区域，优化取图速度
在CameraManager类中不改变扫码框大小，但是增加采样区域大小，我这里是根据屏幕宽度作为边长，截取中间正方形为取图区域。

	public Rect getRealFramingRect() {
		if (realFramingRect == null) {
			//获取屏幕大小，然后根据屏幕宽度由中间截取于宽度等长的正方形
			Point screenResolution = configManager.getScreenResolution();
			int leftOffset = 0;
			int topOffset = (screenResolution.y - screenResolution.x) / 2;
			Rect rect = new Rect(leftOffset, topOffset, screenResolution.x,
					screenResolution.x+topOffset);
			
			//根据图片分辨率和屏幕分辨率截取实际大小的图片区域
			Point cameraResolution = configManager.getCameraResolution();
			rect.left = rect.left * cameraResolution.y / screenResolution.x;
			rect.right = rect.right * cameraResolution.y / screenResolution.x;
			rect.top = rect.top * cameraResolution.x / screenResolution.y;
			rect.bottom = rect.bottom * cameraResolution.x / screenResolution.y;
			realFramingRect = rect;
		}
		return realFramingRect;
	}
#####2.获取适配的摄像头预览图片，防止图片拉伸
在CameraConfigurationManager类中获取摄像头所有预览尺寸，根据屏幕分辨率选取最适合的预览尺寸。

	private static Point findBestPreviewSizeValue(
		CharSequence previewSizeValueString, Point screenResolution) {
		int bestX = 0;
		int bestY = 0;
		int diff = Integer.MAX_VALUE;
		//previewSizeValueString为包含所有预览尺寸的字符串
		for (String previewSize : COMMA_PATTERN.split(previewSizeValueString)) {
			previewSize = previewSize.trim();
			int dimPosition = previewSize.indexOf('x');
			if (dimPosition < 0) continue;
			try {
				int newX = Integer.parseInt(previewSize.substring(0, dimPosition));
				int newY = Integer.parseInt(previewSize.substring(dimPosition + 1));
				int newDiff = Math.abs(newX - screenResolution.x) + Math.abs(newY - screenResolution.y);
				if (newDiff == 0) {
					bestX = newX;bestY = newY;
					break;
				} else if (newDiff < diff) {
					bestX = newX;bestY = newY;diff = newDiff;
				}
			} catch (NumberFormatException nfe) {
				continue;
			}
		}
		if (bestX > 0 && bestY > 0) {
			return new Point(bestX, bestY);
		}
		return null;
	}
#####3.旋正摄像头获取的YUV图片数据，供识别库识别
摄像头获取的图片都是横屏的，所以对图片识别前，我们必须把图片旋正过来，否则就无法正确识别。这个的图片是YUV格式数据，其中"Y"表示明亮度（Lumina nce或Luma），也就是灰阶值；而"U"和"V"表示的则是色度（Chrominance或Chroma），而在识别时，只取Y部分的数据就可以了，相当于把图片灰度化。而为了提高计算效率，我把这部分代码放在C层中实现了。

	char* buffer = (char*) env->GetByteArrayElements(data, JNI_FALSE);
	char*rotateData = new char[dataWidth * dataHeight];
	for(int y = 0; y < dataHeight; y++) {
		for (int x = 0; x < dataWidth; x++) {
			rotateData[x * dataHeight + dataHeight - y - 1] = buffer[x + y * dataWidth];
		}
	}
	int tmp = dataWidth;
	dataWidth = dataHeight;
	dataHeight = tmp;
#####4.融合ZBar和Zxing库，整个算法部分用NDK实现。
同样是为了提高运算效率，我把两部分的算法代码都放在C层实现了，Zbar采用官方的C库，Zxing采用C++库，去掉两个库多余的代码，精简代码结构，封装公共部分代码，从而实现识别运算上的优化。

关于整个项目的优化点还有好多，这里就不一一列举了，这里已经将全部代码放上去了，可以直接通过代码来查看。

###使用说明
#####1.使用Android Studio开发，配置Cmake环境
这里通过Cmake来编译Native部分代码，所以编译时需要配置Cmake工具。如果不修改jni接口（DecodeEntry.cpp)，也可以直接使用本项目提供的.so库，把extraLib文件夹中的.so文件复制到项目lib目录下即可。
#####2.修改识别模式
目前识别模式有三种，只识别二维码，只识别条形码，识别二维码加条形码（默认）。可以根据自己需求选择不同模式，有利于提高识别效率。通过BarcodeFormat类进行修改。
		
		barcodeFormat = new BarcodeFormat();
		barcodeFormat.add(BarcodeFormat.BARCODE);
		barcodeFormat.add(BarcodeFormat.QRCODE);
然后作为参数传进CaptureActivityHandler对象中。

	handler = new CaptureActivityHandler(this, barcodeFormat);


###源码地址：[https://github.com/heiBin/QrCodeScanner](https://github.com/heiBin/QrCodeScanner "https://github.com/heiBin/QrCodeScanner")
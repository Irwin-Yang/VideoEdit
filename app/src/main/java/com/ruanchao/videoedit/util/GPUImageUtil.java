package com.ruanchao.videoedit.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageAddBlendFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageAlphaBlendFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageBilateralFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageBoxBlurFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageBulgeDistortionFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageCGAColorspaceFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageChromaKeyBlendFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageColorBalanceFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSaturationFilter;

public class GPUImageUtil {

    private static GPUImageFilter filter;

    //饱和度、亮度等参数指数
    private static int count;

    /**
     * 获取过滤器
     * @param GPUFlag
     * @return 滤镜类型
     */
    public static GPUImageFilter getFilter(int GPUFlag){
        switch (GPUFlag){
            case 1:
                filter = new GPUImageGrayscaleFilter();
                break;
            case 2:
                filter = new GPUImageAddBlendFilter();
                break;
            case 3:
                filter = new GPUImageAlphaBlendFilter();
                break;
            case 4:
                filter = new GPUImageBilateralFilter();
                break;
            case 5:
                filter = new GPUImageBoxBlurFilter();
                break;
            case 6:
                filter = new GPUImageBrightnessFilter();
                break;
            case 7:
                filter = new GPUImageBulgeDistortionFilter();
                break;
            case 8:
                filter = new GPUImageCGAColorspaceFilter();
                break;
            case 9:
                filter = new GPUImageChromaKeyBlendFilter();
                break;
            case 10:
                filter = new GPUImageColorBalanceFilter();
                break;
            case 11:
                filter = new GPUImageSaturationFilter(count);
                break;
        }
        return filter;
    }

    public static Bitmap getGPUImageFromAssets(Context context, GPUImage gpuImage, int FilterFlag){
        AssetManager as = context.getAssets();
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = as.open("link.jpg");
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            Log.e("GPUImage", "Error");
        }

        // 使用GPUImage处理图像
        gpuImage = new GPUImage(context);
        gpuImage.setImage(bitmap);
        gpuImage.setFilter(getFilter(FilterFlag));
        bitmap = gpuImage.getBitmapWithFilterApplied();
        return bitmap;
    }

    public static Bitmap getGPUImageFromURL(String url) {
        Bitmap bitmap = null;
        try {
            URL iconUrl = new URL(url);
            URLConnection conn = iconUrl.openConnection();
            HttpURLConnection http = (HttpURLConnection) conn;
            int length = http.getContentLength();
            conn.connect();
            // 获得图像的字符流
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is, length);
            bitmap = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();// 关闭流
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    //调整饱和度、亮度等
    public static void changeSaturation(int curCount){
        GPUImageUtil.count = curCount;
    }

    /**
     * 附上部分滤镜类型的API中文参照，便于查阅：

     "GPUImageFastBlurFilter"                               【模糊】
     "GPUImageGaussianBlurFilter"                       【高斯模糊】
     "GPUImageGaussianSelectiveBlurFilter"        【高斯模糊，选择部分清晰】
     "GPUImageBoxBlurFilter"                                【盒状模糊】
     "GPUImageTiltShiftFilter"                                【条纹模糊，中间清晰，上下两端模糊】
     "GPUImageMedianFilter.h"                             【中间值，有种稍微模糊边缘的效果】
     "GPUImageBilateralFilter"                               【双边模糊】
     "GPUImageErosionFilter"                                【侵蚀边缘模糊，变黑白】
     "GPUImageRGBErosionFilter"                         【RGB侵蚀边缘模糊，有色彩】
     "GPUImageDilationFilter"                               【扩展边缘模糊，变黑白】
     "GPUImageRGBDilationFilter"                        【RGB扩展边缘模糊，有色彩】
     "GPUImageOpeningFilter"                             【黑白色调模糊】
     "GPUImageRGBOpeningFilter"                      【彩色模糊】
     "GPUImageClosingFilter"                               【黑白色调模糊，暗色会被提亮】
     "GPUImageRGBClosingFilter"                        【彩色模糊，暗色会被提亮】
     "GPUImageLanczosResamplingFilter"          【Lanczos重取样，模糊效果】
     "GPUImageNonMaximumSuppressionFilter"     【非最大抑制，只显示亮度最高的像素，其他为黑】
     "GPUImageThresholdedNonMaximumSuppressionFilter" 【与上相比，像素丢失更多】


     "GPUImageCrosshairGenerator"              【十字】
     "GPUImageLineGenerator"                       【线条】
     "GPUImageTransformFilter"                     【形状变化】
     "GPUImageCropFilter"                              【剪裁】
     "GPUImageSharpenFilter"                        【锐化】
     "GPUImageUnsharpMaskFilter"               【反遮罩锐化】


     "GPUImageSobelEdgeDetectionFilter"           【Sobel边缘检测算法(白边，黑内容，有点漫画的反色效果)】
     "GPUImageCannyEdgeDetectionFilter"          【Canny边缘检测算法（比上更强烈的黑白对比度）】
     "GPUImageThresholdEdgeDetectionFilter"    【阈值边缘检测（效果与上差别不大）】
     "GPUImagePrewittEdgeDetectionFilter"         【普瑞维特(Prewitt)边缘检测(效果与Sobel差不多，貌似更平滑)】
     "GPUImageXYDerivativeFilter"                        【XYDerivative边缘检测，画面以蓝色为主，绿色为边缘，带彩色】
     "GPUImageHarrisCornerDetectionFilter"       【Harris角点检测，会有绿色小十字显示在图片角点处】
     "GPUImageNobleCornerDetectionFilter"      【Noble角点检测，检测点更多】
     "GPUImageShiTomasiFeatureDetectionFilter" 【ShiTomasi角点检测，与上差别不大】
     "GPUImageMotionDetector"                             【动作检测】
     "GPUImageHoughTransformLineDetector"      【线条检测】
     "GPUImageParallelCoordinateLineTransformFilter" 【平行线检测】


     "GPUImageLocalBinaryPatternFilter"        【图像黑白化，并有大量噪点】
     "GPUImageLowPassFilter"                          【用于图像加亮】
     "GPUImageHighPassFilter"                        【图像低于某值时显示为黑】




     "GPUImageSketchFilter"                          【素描】
     "GPUImageThresholdSketchFilter"         【阀值素描，形成有噪点的素描】
     "GPUImageToonFilter"                             【卡通效果（黑色粗线描边）】
     "GPUImageSmoothToonFilter"                【相比上面的效果更细腻，上面是粗旷的画风】
     "GPUImageKuwaharaFilter"                     【桑原(Kuwahara)滤波,水粉画的模糊效果；处理时间比较长，慎用】


     "GPUImageMosaicFilter"                         【黑白马赛克】
     "GPUImagePixellateFilter"                       【像素化】
     "GPUImagePolarPixellateFilter"              【同心圆像素化】
     "GPUImageCrosshatchFilter"                  【交叉线阴影，形成黑白网状画面】
     "GPUImageColorPackingFilter"              【色彩丢失，模糊（类似监控摄像效果）】


     "GPUImageVignetteFilter"                        【晕影，形成黑色圆形边缘，突出中间图像的效果】
     "GPUImageSwirlFilter"                               【漩涡，中间形成卷曲的画面】
     "GPUImageBulgeDistortionFilter"            【凸起失真，鱼眼效果】
     "GPUImagePinchDistortionFilter"            【收缩失真，凹面镜】
     "GPUImageStretchDistortionFilter"         【伸展失真，哈哈镜】
     "GPUImageGlassSphereFilter"                  【水晶球效果】
     "GPUImageSphereRefractionFilter"         【球形折射，图形倒立】
         
     "GPUImagePosterizeFilter"                 【色调分离，形成噪点效果】
     "GPUImageCGAColorspaceFilter"      【CGA色彩滤镜，形成黑、浅蓝、紫色块的画面】
     "GPUImagePerlinNoiseFilter"              【柏林噪点，花边噪点】
     "GPUImage3x3ConvolutionFilter"      【3x3卷积，高亮大色块变黑，加亮边缘、线条等】
     "GPUImageEmbossFilter"                   【浮雕效果，带有点3d的感觉】
     "GPUImagePolkaDotFilter"                 【像素圆点花样】
     "GPUImageHalftoneFilter"                  【点染,图像黑白化，由黑点构成原图的大致图形】


     混合模式 Blend

     "GPUImageMultiplyBlendFilter"            【通常用于创建阴影和深度效果】
     "GPUImageNormalBlendFilter"               【正常】
     "GPUImageAlphaBlendFilter"                 【透明混合,通常用于在背景上应用前景的透明度】
     "GPUImageDissolveBlendFilter"             【溶解】
     "GPUImageOverlayBlendFilter"              【叠加,通常用于创建阴影效果】
     "GPUImageDarkenBlendFilter"               【加深混合,通常用于重叠类型】
     "GPUImageLightenBlendFilter"              【减淡混合,通常用于重叠类型】
     "GPUImageSourceOverBlendFilter"       【源混合】
     "GPUImageColorBurnBlendFilter"          【色彩加深混合】
     "GPUImageColorDodgeBlendFilter"      【色彩减淡混合】
     "GPUImageScreenBlendFilter"                【屏幕包裹,通常用于创建亮点和镜头眩光】
     "GPUImageExclusionBlendFilter"            【排除混合】
     "GPUImageDifferenceBlendFilter"          【差异混合,通常用于创建更多变动的颜色】
     "GPUImageSubtractBlendFilter"            【差值混合,通常用于创建两个图像之间的动画变暗模糊效果】
     "GPUImageHardLightBlendFilter"         【强光混合,通常用于创建阴影效果】
     "GPUImageSoftLightBlendFilter"           【柔光混合】
     "GPUImageChromaKeyBlendFilter"       【色度键混合】
     "GPUImageMaskFilter"                           【遮罩混合】
     "GPUImageHazeFilter"                            【朦胧加暗】
     "GPUImageLuminanceThresholdFilter" 【亮度阈】
     "GPUImageAdaptiveThresholdFilter"     【自适应阈值】
     "GPUImageAddBlendFilter"                    【通常用于创建两个图像之间的动画变亮模糊效果】
     "GPUImageDivideBlendFilter"                 【通常用于创建两个图像之间的动画变暗模糊效果】
     ---------------------
     作者：IT_ZJYANG
     来源：CSDN
     原文：https://blog.csdn.net/IT_ZJYANG/article/details/52268918?utm_source=copy
     版权声明：本文为博主原创文章，转载请附上博文链接！
     */
}

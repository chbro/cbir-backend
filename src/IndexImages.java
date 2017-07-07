
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class IndexImages {
	/*
	 * 计算数学期望(平均值)
	 */
	public static double expect(double num[]) {
		double sum = 0;
		for(int i=0;i<num.length;i++)
			sum += num[i];
		return sum/num.length;
	}
	/*
	 * 计算标准差(方差)
	 */
	public static double stdv(double num[]) {
		double avg = expect(num), sum = 0;
		for(int i=0;i<num.length;i++)
			sum += Math.pow(num[i]-avg, 2);
		return Math.sqrt(sum/num.length);
	}
	/*
	 * 归一化
	 */
	public static double[][] normalize(double gray[][]) {
		double sum = 0;
	    for(int i=0;i<8;i++)
	    	for(int j=0;j<8;j++)
	    		sum += gray[i][j];
	    for(int i=0;i<8;i++)
	    	for(int j=0;j<8;j++)
	    		gray[i][j] = gray[i][j]/sum;
	    return gray;
	}
	/*
	 * 传入图片
	 * 提取三种特征值
	 * 返回特征值数组
	 */
	public static double[] getTexture(BufferedImage bufImg) {
		 double M[] = new double[8];
         int height = bufImg.getHeight(), width = bufImg.getWidth();
         int init_gray[][] = new int[width+2][height+2];
         for(int i=0;i<width+2;i++) init_gray[i][0] = init_gray[i][height+1] = 9;
         for(int i=0;i<height+2;i++) init_gray[0][i] = init_gray[width+1][0] = 9;
         //初始化灰度矩阵
         for (int i = 0; i < width; i++) {
              for (int j = 0; j < height; j++) {
            	  int r = bufImg.getRGB(i, j)>>16 & 0xFF;
	          	  int g = bufImg.getRGB(i, j)>>8 & 0xFF;
	        	  int b = bufImg.getRGB(i, j) & 0xFF;
	        	  init_gray[i+1][j+1] = (int) ((0.3*r+0.59*g+0.11*b)/32);
              }
         }
         double gray[][][] = new double[4][8][8];
         //计算四个方向的灰度共生矩阵
         for (int i = 0; i < 8; i++) {
             for (int j = 0; j < 8; j++) {
            	 //对灰度矩阵进行遍历
                 for (int m = 1; m <= width; m++) {
                     for (int n = 1; n <= height; n++) {
                    	 //0度矩阵
                    	 if(init_gray[m][n] == i && init_gray[m][n+1] == j && i == j)
                    		 gray[0][i][j] += 2;
                    	 else if(init_gray[m][n] == i && init_gray[m][n+1] == j)
                    		 gray[0][i][j]++;
                    	 //45度矩阵
                    	 if(init_gray[m][n] == i && init_gray[m+1][n-1] == j && i == j)
                    		 gray[1][i][j] += 2;
                    	 else if(init_gray[m][n] == i && init_gray[m+1][n-1] == j)
                    		 gray[1][i][j]++;
                    	 //90度矩阵
                    	 if(init_gray[m][n] == i && init_gray[m][n-1] == j && i == j)
                    		 gray[2][i][j] += 2;
                    	 else if(init_gray[m][n] == i && init_gray[m][n-1] == j)
                    		 gray[2][i][j]++;
                    	 //135度矩阵
                    	 if(init_gray[m][n] == i && init_gray[m-1][n-1] == j && i == j)
                    		 gray[3][i][j] += 2;
                    	 else if(init_gray[m][n] == i && init_gray[m-1][n-1] == j)
                    		 gray[3][i][j]++;
                     }
                 }
             }
         }
         for(int i=0;i<4;i++)
        	 normalize(gray[i]);
         //计算每个灰度共生矩阵的纹理一致性，纹理对比度，纹理熵，纹理相关性
		 double consistence[] = new double[4];
		 double contrast[] = new double[4];
		 double entropy[] = new double[4];
		 double correlation[] = new double[4];
		 for (int k = 0; k < 8; k++)
			for (int j = 0; j < 8; j++)
				for(int d = 0; d < 4; d++) {
					//熵,加0.000001是为了避免值为0
					entropy[d] += gray[d][k][j] * Math.log(gray[d][k][j]+0.00001) / Math.log(2);
					//对比度,取k=2,入=1
					contrast[d] += (k-j)*(k-j) * gray[d][k][j];
					//一致性(能量)
					consistence[d] += gray[d][k][j] * gray[d][k][j];
					//相关性
					correlation[d] += k * j * gray[d][k][j];
				}
		//计算每个矩阵的均值和标准差
		double ux[] = new double[4], uy[] = new double[4], ax[] = new double[4], ay[] = new double[4];
		for (int k = 0; k < 8; k++)
			for (int j = 0; j < 8; j++)
				for (int d = 0; d < 4; d++){
					ux[d] += k * gray[d][k][j];
					uy[d] += j * gray[d][k][j];
					ax[d] += (k-ux[d]) * (k-ux[d]) * gray[d][k][j];
					ay[d] += (j-uy[d]) * (j-uy[d]) * gray[d][k][j];
					ax[d] = Math.sqrt(ax[d]); ay[d] = Math.sqrt(ay[d]);
				}
		 for (int d=0; d<4; d++) {
			 correlation[d] = (correlation[d] - ux[d] * uy[d]) / ax[d] * ay[d];
		 }
		 M[0] = expect(consistence);
		 M[1] = expect(contrast);
		 M[2] = expect(entropy);
		 M[3] = expect(correlation);
		 M[4] = stdv(consistence);
		 M[5] = stdv(contrast);
		 M[6] = stdv(entropy);
		 M[7] = stdv(correlation);
		 return M;
	}
	public static double[] getShape(BufferedImage bufImg) {
	    double M[] = new double[8];
	    return M;
	}
	public static double[] getColor(BufferedImage bufImg) {
	     double angle, hue, sum = 0, M[] = new double[3];
         int height = bufImg.getHeight();
         int width = bufImg.getWidth();
         M = new double[3];
         for (int i = 0; i < width; i++) {
              for (int j = 0; j < height; j++) {
                    int r = bufImg.getRGB(i, j)>>16 & 0xFF;
              		int g = bufImg.getRGB(i, j)>>8 & 0xFF;
              		int b = bufImg.getRGB(i, j) & 0xFF;
              		//g等于b时，设置angle为0
              		angle = g==b?0:Math.PI/2-Math.atan((2*r-g-b)/Math.pow(3, 0.5)/(g-b));
                    hue = g>=b?angle:Math.PI+angle;
                    sum += hue;
              }
         }
         M[0] = sum/width/height;
         //抽取特征值M1,M2和M3
         for (int i = 0; i < width; i++) {
              for (int j = 0; j < height; j++) {
                    int r = bufImg.getRGB(i, j)>>16 & 0xFF;
              		int g = bufImg.getRGB(i, j)>>8 & 0xFF;
              		int b = bufImg.getRGB(i, j) & 0xFF;
              		//g等于b时，设置angle为0
              		angle = g==b?0:Math.PI/2-Math.atan((2*r-g-b)/Math.pow(3, 0.5)/(g-b));
                    hue = g>=b?angle:Math.PI+angle;
                    M[1] += (hue-M[0])*(hue-M[0]);
                    M[2] += Math.pow(hue-M[0], 3);
              }
         }
         M[1] = Math.sqrt(M[1]/width/height);
         M[2] = Math.cbrt(M[2]/width/height);
	    return M;
	  }
	//验证是否为图片文件
	public static boolean validateImgName (String name) {
		return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".gif");
	}
	//执行导入操作
	public static void executeImport(String name, double m[], String path, boolean insert_shape) {
		if(ImportImages.insert(name, m, path, insert_shape)!=0) {
			System.out.println(path+"导入成功");
		}else {
			System.out.println(path+"导入失败！！！");
		}
	}
	//检索图片文件并导入数据库
	public static void readfile(String filepath) throws IOException{
		double m[];
		try {
			File file = new File(filepath);
			if(!file.exists()) System.out.println("文件不存在");
			String name;
			if (!file.isDirectory()) {
				name = file.getName();
				//跳过非图片文件
				if(!validateImgName(name)) return;
				File f = new File(file.getAbsolutePath());
				BufferedImage bufImg = ImageIO.read(f);
				m = getColor(bufImg);
				executeImport(name, m, file.getAbsolutePath().replace("\\", "/"), false);
				m = getTexture(bufImg);
				executeImport(name, m, file.getAbsolutePath().replace("\\", "/"), false);
//				m = getShape(bufImg);
//				executeImport(name, m, file.getAbsolutePath().replace("\\", "/"), true);
			} else if (file.isDirectory()) {
				String[] filelist = file.list();
				for (int i = 0; i < filelist.length; i++) {
					readfile(filepath + "/" + filelist[i]);
				}
			}
		} catch (IOException e) {
			System.out.println("IOException:" + e.getMessage());
		}
	}
	public static void main(String[] args) throws IOException {
		String path = "C:/Users/zhou/Workspaces/MyEclipse Professional 2014/.metadata/.me_tcat7/webapps/CBIR/test_images/";
		readfile(path);
		//计算特征值测试用例
//		String p = "C:/Users/zhou/Workspaces/MyEclipse Professional 2014/.metadata/.me_tcat7/webapps/CBIR/test_images/ant/image_0001.jpg";
//		BufferedImage bufImg = ImageIO.read(new File(p));
//		double b[] = getColor(bufImg);
//		for(int i=0;i<b.length;i++)
//			System.out.print(b[i]+",");
	}
}

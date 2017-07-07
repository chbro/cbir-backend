
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class IndexImages {
	/*
	 * ������ѧ����(ƽ��ֵ)
	 */
	public static double expect(double num[]) {
		double sum = 0;
		for(int i=0;i<num.length;i++)
			sum += num[i];
		return sum/num.length;
	}
	/*
	 * �����׼��(����)
	 */
	public static double stdv(double num[]) {
		double avg = expect(num), sum = 0;
		for(int i=0;i<num.length;i++)
			sum += Math.pow(num[i]-avg, 2);
		return Math.sqrt(sum/num.length);
	}
	/*
	 * ��һ��
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
	 * ����ͼƬ
	 * ��ȡ��������ֵ
	 * ��������ֵ����
	 */
	public static double[] getTexture(BufferedImage bufImg) {
		 double M[] = new double[8];
         int height = bufImg.getHeight(), width = bufImg.getWidth();
         int init_gray[][] = new int[width+2][height+2];
         for(int i=0;i<width+2;i++) init_gray[i][0] = init_gray[i][height+1] = 9;
         for(int i=0;i<height+2;i++) init_gray[0][i] = init_gray[width+1][0] = 9;
         //��ʼ���ҶȾ���
         for (int i = 0; i < width; i++) {
              for (int j = 0; j < height; j++) {
            	  int r = bufImg.getRGB(i, j)>>16 & 0xFF;
	          	  int g = bufImg.getRGB(i, j)>>8 & 0xFF;
	        	  int b = bufImg.getRGB(i, j) & 0xFF;
	        	  init_gray[i+1][j+1] = (int) ((0.3*r+0.59*g+0.11*b)/32);
              }
         }
         double gray[][][] = new double[4][8][8];
         //�����ĸ�����ĻҶȹ�������
         for (int i = 0; i < 8; i++) {
             for (int j = 0; j < 8; j++) {
            	 //�ԻҶȾ�����б���
                 for (int m = 1; m <= width; m++) {
                     for (int n = 1; n <= height; n++) {
                    	 //0�Ⱦ���
                    	 if(init_gray[m][n] == i && init_gray[m][n+1] == j && i == j)
                    		 gray[0][i][j] += 2;
                    	 else if(init_gray[m][n] == i && init_gray[m][n+1] == j)
                    		 gray[0][i][j]++;
                    	 //45�Ⱦ���
                    	 if(init_gray[m][n] == i && init_gray[m+1][n-1] == j && i == j)
                    		 gray[1][i][j] += 2;
                    	 else if(init_gray[m][n] == i && init_gray[m+1][n-1] == j)
                    		 gray[1][i][j]++;
                    	 //90�Ⱦ���
                    	 if(init_gray[m][n] == i && init_gray[m][n-1] == j && i == j)
                    		 gray[2][i][j] += 2;
                    	 else if(init_gray[m][n] == i && init_gray[m][n-1] == j)
                    		 gray[2][i][j]++;
                    	 //135�Ⱦ���
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
         //����ÿ���Ҷȹ������������һ���ԣ�����Աȶȣ������أ����������
		 double consistence[] = new double[4];
		 double contrast[] = new double[4];
		 double entropy[] = new double[4];
		 double correlation[] = new double[4];
		 for (int k = 0; k < 8; k++)
			for (int j = 0; j < 8; j++)
				for(int d = 0; d < 4; d++) {
					//��,��0.000001��Ϊ�˱���ֵΪ0
					entropy[d] += gray[d][k][j] * Math.log(gray[d][k][j]+0.00001) / Math.log(2);
					//�Աȶ�,ȡk=2,��=1
					contrast[d] += (k-j)*(k-j) * gray[d][k][j];
					//һ����(����)
					consistence[d] += gray[d][k][j] * gray[d][k][j];
					//�����
					correlation[d] += k * j * gray[d][k][j];
				}
		//����ÿ������ľ�ֵ�ͱ�׼��
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
              		//g����bʱ������angleΪ0
              		angle = g==b?0:Math.PI/2-Math.atan((2*r-g-b)/Math.pow(3, 0.5)/(g-b));
                    hue = g>=b?angle:Math.PI+angle;
                    sum += hue;
              }
         }
         M[0] = sum/width/height;
         //��ȡ����ֵM1,M2��M3
         for (int i = 0; i < width; i++) {
              for (int j = 0; j < height; j++) {
                    int r = bufImg.getRGB(i, j)>>16 & 0xFF;
              		int g = bufImg.getRGB(i, j)>>8 & 0xFF;
              		int b = bufImg.getRGB(i, j) & 0xFF;
              		//g����bʱ������angleΪ0
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
	//��֤�Ƿ�ΪͼƬ�ļ�
	public static boolean validateImgName (String name) {
		return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".gif");
	}
	//ִ�е������
	public static void executeImport(String name, double m[], String path, boolean insert_shape) {
		if(ImportImages.insert(name, m, path, insert_shape)!=0) {
			System.out.println(path+"����ɹ�");
		}else {
			System.out.println(path+"����ʧ�ܣ�����");
		}
	}
	//����ͼƬ�ļ����������ݿ�
	public static void readfile(String filepath) throws IOException{
		double m[];
		try {
			File file = new File(filepath);
			if(!file.exists()) System.out.println("�ļ�������");
			String name;
			if (!file.isDirectory()) {
				name = file.getName();
				//������ͼƬ�ļ�
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
		//��������ֵ��������
//		String p = "C:/Users/zhou/Workspaces/MyEclipse Professional 2014/.metadata/.me_tcat7/webapps/CBIR/test_images/ant/image_0001.jpg";
//		BufferedImage bufImg = ImageIO.read(new File(p));
//		double b[] = getColor(bufImg);
//		for(int i=0;i<b.length;i++)
//			System.out.print(b[i]+",");
	}
}

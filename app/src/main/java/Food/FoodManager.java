package Food;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by liang on 2017/11/26.
 */

public class FoodManager {
    private static List<Food> foodList = new ArrayList<>();
    private static Bitmap paySucceedImg = null;

    public static List<Food> GetFoodList(){
        //
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Log.v(TAG, "加载JDBC驱动成功");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "加载JDBC驱动失败");
            return null;
        }

        String url = "jdbc:mysql://35.194.237.103:3306/test";
        String user = "andy";
        String pswd = "xiaoxue521";

        //
        try {
            Connection conn = DriverManager.getConnection(url, user, pswd);

            Statement statement = conn.createStatement();
            String sql = "SELECT * FROM foodtable";
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()){
                Food item = new Food(resultSet.getString("name"), resultSet.getFloat("price")
                        ,resultSet.getString("imageUrl"));
                item.setBitmap(getHttpBitMap(item.getImageUrl()));
                foodList.add(item);
            }
            paySucceedImg = getHttpBitMap("http://andyhacker.cn/orderm/wxpay/paysucceed.png");
            conn.close();
        } catch (SQLException e) {
            Log.e(TAG, "远程连接失败!");
            return null;
        }

        return foodList;
    }

    public static Bitmap getHttpBitMap(String url){
        Bitmap bmp = null;
        try {
            URL myurl = new URL(url);
            // 获得连接
            HttpURLConnection conn = (HttpURLConnection) myurl.openConnection();
            conn.setConnectTimeout(6000);//设置超时
            conn.setDoInput(true);
            conn.setUseCaches(true);//不缓存
            conn.connect();
            InputStream is = conn.getInputStream();//获得图片的数据流
            bmp = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bmp;
    }

    public static Bitmap getPaySucceedImg(){
        return paySucceedImg;
    }
}

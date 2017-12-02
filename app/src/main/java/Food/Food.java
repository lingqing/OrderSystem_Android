package Food;

import android.graphics.Bitmap;

/**
 * Created by liang on 2017/11/22.
 * 饭菜订单中饭菜类
 */

public class Food {
    private String name;
    private int imageId;
    private String imageUrl;
    private Bitmap bitmap;
    private float price;

    public Food(String _name, int _id){
        name = _name;
        imageId = _id;
    }

    public Food(String _name, float _price, String _url){
        name = _name;
        price = _price;
        imageUrl = _url;
    }

    public String getName(){
        return name;
    }
    public int getImageId(){
        return imageId;
    }
    public float getPrice() { return price;}
    public String getImageUrl() { return imageUrl;}
    public Bitmap getBitmap() {return  bitmap;}

    public void setBitmap(Bitmap _bitmap){
        bitmap = _bitmap;
    }
}

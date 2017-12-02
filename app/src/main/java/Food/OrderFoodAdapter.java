package Food;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
import com.example.liang.ordersystemapp.R;


/**
 * Created by liang on 2017/11/26.
 */

public class OrderFoodAdapter extends ArrayAdapter<Food> {
    private int resouceId;

    public OrderFoodAdapter(Context context, int viewResouceId, List<Food> foodList){
        super(context, viewResouceId, foodList);
        resouceId = viewResouceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Food item = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resouceId, parent, false);
        TextView foodName = (TextView)view.findViewById(R.id.order_food_name);
        TextView foodPrice = (TextView)view.findViewById(R.id.order_food_price) ;
        TextView foodCnt = (TextView)view.findViewById(R.id.order_food_cnt);
        foodName.setText(item.getName());
        foodPrice.setText(item.getPrice()+"");
        foodCnt.setText("1");
        return view;
    }
}

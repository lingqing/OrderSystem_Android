package Food;

import android.support.v4.view.LayoutInflaterFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.liang.ordersystemapp.MainActivity;
import com.example.liang.ordersystemapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liang on 2017/11/22.
 */

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder>{

    private List<Food> mFoodList;

    public interface OnImageViewCheckListener{
        void viewCheck(Food food, boolean isChecked);
    }

    private OnImageViewCheckListener listener = null;
    private List<ViewHolder> holderList = new ArrayList<ViewHolder>();

    public void setOnImageViewCheckListener(OnImageViewCheckListener l){
        listener = l;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        View foodView;
        ImageView foodImage;
//        TextView foodName;
        CheckBox foodCheck;
        public ViewHolder(View view){
            super(view);
            foodView = view;
            foodImage = (ImageView)view.findViewById(R.id.food_image_view_id);
//            foodName = (TextView)view.findViewById(R.id.food_name_text_id);
            foodCheck = (CheckBox)view.findViewById(R.id.food_name_check_id);
        }
    }

    public FoodAdapter(List<Food> foodList){
        mFoodList = foodList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_food_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.foodView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Food food = mFoodList.get(holder.getAdapterPosition());
//                Toast.makeText(v.getContext(), "you click food " + food.getName(),
//                        Toast.LENGTH_SHORT).show();
                if (holder.foodCheck.isChecked()){
                    holder.foodCheck.setChecked(false);
                    listener.viewCheck(food, false);
                }
                else {
                    holder.foodCheck.setChecked(true);
                    listener.viewCheck(food, true);
                }
            }
        });
        holderList.add(holder);
        return holder;
    }
// 返回到首页面时清楚选中状态
    public void clearCheck(){
        for (ViewHolder holder: holderList) {
            holder.foodCheck.setChecked(false);
        }
    }
    @Override
    public void onBindViewHolder(FoodAdapter.ViewHolder holder, int position) {
        Food food = mFoodList.get(position);
//        holder.foodImage.setImageResource(food.getImageUrl());
        holder.foodImage.setImageBitmap(food.getBitmap());
//        holder.foodName.setText(food.getName());
        holder.foodCheck.setText(food.getName());
    }

    @Override
    public int getItemCount() {
        return mFoodList.size();
    }
}

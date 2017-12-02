package com.example.liang.ordersystemapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.LogRecord;

import Food.*;

public class MainActivity extends Activity {

    private List<Food> foodList = new ArrayList<>();
    private List<Food> orderList = new ArrayList<>();
    private FoodAdapter foodAdapter;
    private RecyclerView recyclerView;
    private  OrderFoodAdapter orderAdapter;
    private  GridLayoutManager layoutManager;

    private TextView sumPrice;
    private TextView sumCnt;

    private static float foodSumPrice = 0.0f;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 888 && resultCode == 889){
            boolean needClear = data.getBooleanExtra("needClear", false);

            if (needClear) reInitView();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_main);

        TextView txtName = (TextView)findViewById(R.id.order_list_title).findViewById(R.id.order_food_name);
        txtName.setText("菜名");
        TextView txtPrice = (TextView)findViewById(R.id.order_list_title).findViewById(R.id.order_food_price);
        txtPrice.setText("价格");
        TextView txtCnt = (TextView)findViewById(R.id.order_list_title).findViewById(R.id.order_food_cnt);
        txtCnt.setText("数量");

        TextView sumName = (TextView)findViewById(R.id.order_list_sum).findViewById(R.id.order_food_name);
        sumName.setText("合计");
        sumPrice = (TextView)findViewById(R.id.order_list_sum).findViewById(R.id.order_food_price);
        sumPrice.setText("0.00元");
        sumCnt = (TextView)findViewById(R.id.order_list_sum).findViewById(R.id.order_food_cnt);
        sumCnt.setText("0");

        initFoodList();
        recyclerView = (RecyclerView)findViewById(R.id.recycler_id);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager = new GridLayoutManager(this,4);
        recyclerView.setLayoutManager(layoutManager);

        foodAdapter = new FoodAdapter(foodList);
        recyclerView.setAdapter(foodAdapter);

        new Thread(runnable).start();

        orderAdapter = new OrderFoodAdapter(MainActivity.this, R.layout.order_list_item,orderList);
        ListView listView = (ListView)findViewById(R.id.order_list_view);
        listView.setAdapter(orderAdapter);

        foodAdapter.setOnImageViewCheckListener(new FoodAdapter.OnImageViewCheckListener() {
            @Override
            public void viewCheck(Food food, boolean isChecked) {
                if(isChecked){
                    if(!orderList.contains(food)){
                        orderList.add(food);
                        foodSumPrice += food.getPrice();
                        if (orderList.size() >3) Toast.makeText(MainActivity.this,
                                "您选择了超过3种菜品",Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    if(orderList.contains(food)) {
                        orderList.remove(food);
                        foodSumPrice -= food.getPrice();
                    }
                    // Todo
                }
                orderAdapter.notifyDataSetChanged();
                sumPrice.setText((float)(Math.round(foodSumPrice * 100))/100 + "元");
                sumCnt.setText(orderList.size() + "");
            }
        });

        Button orderBtn = (Button)findViewById(R.id.btn_order_id);
        orderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PayWayActivity.class);
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                intent.putExtra("outTradeNo", "188" + "188" + df.format(new Date(System.currentTimeMillis())));
                String label = null;
                if (orderList.size() == 0) label = "米饭";
                else label = orderList.get(0).getName();
                if (orderList.size()>1) label+= "等多件";
                intent.putExtra("orderLabel", label);
                intent.putExtra("sumPrice", foodSumPrice + "");
                startActivityForResult(intent, 888);
            }
        });

    }

    Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            foodAdapter.notifyItemInserted(foodList.size() - 1);
//            recyclerView.scrollToPosition(foodList.size()-1);
        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            foodList.addAll(FoodManager.GetFoodList());

            Message msg = new Message();
            myHandler.sendMessage(msg);
        }
    };

//    @Override
//    protected void onResume() {
//        super.onResume();
//        reInitView();
//    }

    private void reInitView(){
        orderList.clear();
        orderAdapter.notifyDataSetChanged();
        foodSumPrice = 0;
        sumPrice.setText("0.00元");
        sumCnt.setText("0");
        foodAdapter.clearCheck();
    }

    private void initFoodList(){
//        for(int i = 0; i< 4; i ++){
//            Food tomoto = new Food("西红柿炒鸡蛋", R.drawable.tomatoegg);
//            foodList.add(tomoto);
//            Food apple = new Food("Apple", R.drawable.tomatoegg);
//            foodList.add(apple);
//        }

//        foodList = FoodManager.GetFoodList();

//        Food order = new Food("土豆丝", R.drawable.tomatoegg);
//        orderList.add(order);
    }

}

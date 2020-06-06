package com.liminal.eagamification.rewards;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.liminal.eagamification.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.PagerAdapter;

public class RewardsCategoryAdapter extends PagerAdapter {

    private List<RewardCategory> rewardCategoryList;
    private LayoutInflater layoutInflater;
    private Context context;

    public RewardsCategoryAdapter(List<RewardCategory> rewardCategoryList, LayoutInflater layoutInflater, Context context) {
        this.rewardCategoryList = rewardCategoryList;
        this.layoutInflater = layoutInflater;
        this.context = context;
    }

    @Override
    public int getCount() {
        return rewardCategoryList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = layoutInflater.inflate(R.layout.category_item, container, false);

        CardView cardView;
        TextView title, description;
        Button button;

        cardView = view.findViewById(R.id.category_card_view);
        title = view.findViewById(R.id.category_title);
        description = view.findViewById(R.id.description);
        button = view.findViewById(R.id.check_rewards);

        cardView.setBackground(context.getResources().getDrawable(rewardCategoryList.get(position).getImage()));
        title.setText(rewardCategoryList.get(position).getTitle());
        description.setText(rewardCategoryList.get(position).getDescription());
        button.setOnClickListener(view1 -> Toast.makeText(context, "Some xyz category", Toast.LENGTH_LONG).show());

        container.addView(view, 0);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}

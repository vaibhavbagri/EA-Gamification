package com.liminal.eagamification.rewards;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.liminal.eagamification.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class RewardsCategoryAdapter extends PagerAdapter implements View.OnClickListener {

    private List<RewardCategory> rewardCategoryList;
    private LayoutInflater layoutInflater;
    private Context context;
    private ViewPager viewPager;
    private RewardsCategoriesFragment.ClickListener clickListener;
    private SparseArray<View> pagerViews = new SparseArray<>();

    public RewardsCategoryAdapter(List<RewardCategory> rewardCategoryList, LayoutInflater layoutInflater, Context context, ViewPager viewPager, RewardsCategoriesFragment.ClickListener clickListener) {
        this.rewardCategoryList = rewardCategoryList;
        this.layoutInflater = layoutInflater;
        this.context = context;
        this.viewPager = viewPager;
        this.clickListener = clickListener;
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
        ImageView imageView;
        TextView title, description;
        Button button;

        cardView = view.findViewById(R.id.category_card_view);
        imageView = view.findViewById(R.id.imageView);
        title = view.findViewById(R.id.category_title);
        description = view.findViewById(R.id.description);
        button = view.findViewById(R.id.check_rewards);

        cardView.setBackground(context.getDrawable(rewardCategoryList.get(position).getBackground_image()));
        imageView.setImageDrawable(context.getDrawable(rewardCategoryList.get(position).getCategory_image()));
        title.setText(rewardCategoryList.get(position).getTitle());
        description.setText(rewardCategoryList.get(position).getDescription());
        button.setOnClickListener(this);

        container.addView(view);

        //Store views in a sparse array
        pagerViews.put(position, view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    public View getRewardView(int position) {
        return pagerViews.get(position);
    }

    @Override
    public void onClick(View view) {
        clickListener.onPositionClicked(viewPager.getCurrentItem());
    }
}

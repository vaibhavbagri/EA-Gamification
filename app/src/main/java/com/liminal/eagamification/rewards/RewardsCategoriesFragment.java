package com.liminal.eagamification.rewards;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.liminal.eagamification.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RewardsCategoriesFragment extends Fragment{

    ViewPager viewPager;
    RewardsCategoryAdapter rewardsCategoryAdapter;
    List<RewardCategory> rewardCategoryList;
    final static float RATIO_SCALE_SIZE = 0.1f;
    final static float RATIO_SCALE_ALPHA = 0.5f;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_rewards_categories, container, false);
        viewPager = root.findViewById(R.id.categories_view_pager);

        rewardCategoryList = new ArrayList<>();
        //Add different reward categories
        rewardCategoryList.add(new RewardCategory(R.drawable.holidays_rewards_bg, R.drawable.holidays, "holidays", "Exciting offers to make your next vacation cheaper"));
        rewardCategoryList.add(new RewardCategory(R.drawable.style_rewards_bg, R.drawable.style, "style", "Discounts that will never go out of style!"));
//        rewardCategoryList.add(new RewardCategory(R.drawable.sustainable_rewards_bg, R.drawable.sustainable, "sustainable", "Sustainable goods to fulfil your needs, and help Mother Earth as well!"));
        rewardCategoryList.add(new RewardCategory(R.drawable.food_rewards_bg, R.drawable.food, "food", "Delicacies that you certainly don't want to miss out on!"));
        rewardCategoryList.add(new RewardCategory(R.drawable.health_rewards_bg, R.drawable.health, "health", "For all the fitness freaks out there!"));
        rewardCategoryList.add(new RewardCategory(R.drawable.freebies_rewards_bg, R.drawable.freebies, "freebies", "Who doesn't love free gifts?!"));

        rewardsCategoryAdapter = new RewardsCategoryAdapter(rewardCategoryList, inflater, getContext(), viewPager,
                position -> replaceFragment(rewardCategoryList.get(position).getTitle()));

        viewPager.setAdapter(rewardsCategoryAdapter);
        //View pager settings
        viewPager.setClipToPadding(false);
        viewPager.setPageMargin(24);
        viewPager.setPadding(130, 30, 130, 20);
        viewPager.setOffscreenPageLimit(3);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d("EAG_REWARDS", "Page scrolled");
                View view = ((RewardsCategoryAdapter) Objects.requireNonNull(viewPager.getAdapter())).getRewardView(position);
                //Alter size and alpha of current page for a smooth transition
                scaleView(view, 1 - (positionOffset * RATIO_SCALE_SIZE), 1 - (positionOffset * RATIO_SCALE_ALPHA));
                if (position + 1 < viewPager.getAdapter().getCount()) {
                    view = ((RewardsCategoryAdapter) viewPager.getAdapter()).getRewardView(position + 1);
                    //Alter size and alpha of next page for a smooth transition
                    scaleView(view,
                            positionOffset * RATIO_SCALE_SIZE + (1 - RATIO_SCALE_SIZE),
                            positionOffset * RATIO_SCALE_ALPHA + (1 - RATIO_SCALE_ALPHA));
                }

                //Get the view two positions ahead and behind to prevent snappy behaviour of card blurring
                if (viewPager.getCurrentItem() + 2 < viewPager.getAdapter().getCount()) {
                    view = ((RewardsCategoryAdapter) viewPager.getAdapter()).getRewardView(viewPager.getCurrentItem() + 2);
                    scaleView(view, 1 - RATIO_SCALE_SIZE, 1 - RATIO_SCALE_ALPHA);
                }
                if (viewPager.getCurrentItem() - 2 >= 0) {
                    view = ((RewardsCategoryAdapter) viewPager.getAdapter()).getRewardView(viewPager.getCurrentItem() - 2);
                    scaleView(view, 1 - RATIO_SCALE_SIZE, 1 - RATIO_SCALE_ALPHA);
                }
            }

            @Override
            public void onPageSelected(int position) {
                Log.d("EAG_REWARDS", "Page selected");
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d("EAG_REWARDS", "Scroll state changed");
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    View view = ((RewardsCategoryAdapter) Objects.requireNonNull(viewPager.getAdapter())).getRewardView(viewPager.getCurrentItem());
                    scaleView(view, 1, 1);
                    //Get the view to the left of the current page if it isn't the first item
                    if (viewPager.getCurrentItem() > 0) {
                        view = ((RewardsCategoryAdapter) viewPager.getAdapter()).getRewardView(viewPager.getCurrentItem() - 1);
                        scaleView(view, 1 - RATIO_SCALE_SIZE, 1 - RATIO_SCALE_ALPHA);
                    }

                    //Get the view to the right of the current page if it isn't the last item
                    if (viewPager.getCurrentItem() + 1 < viewPager.getAdapter().getCount()) {
                        view = ((RewardsCategoryAdapter) viewPager.getAdapter()).getRewardView(viewPager.getCurrentItem() + 1);
                        scaleView(view, 1 - RATIO_SCALE_SIZE, 1 - RATIO_SCALE_ALPHA);
                    }
                }
            }
        });

        return root;
    }

    //Function to blur and decrease size of views present to the right and left of current view
    public void scaleView(View view, float scaleSize, float scaleAlpha)
    {
        view.setScaleY(scaleSize);
        view.setAlpha(scaleAlpha);
    }

    //Replace the categories fragment with the list for that particular category
    private void replaceFragment(String category) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_rewards, new RewardsListFragment(category))
                .addToBackStack("RewardCategories")
                .commit();
    }

    //Needed to handle button clicks on each page
    public interface ClickListener {
        void onPositionClicked(int position);
    }
}

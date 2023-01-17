package sp.phone.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import butterknife.BindView;
import gov.anzong.androidnga.R;
import gov.anzong.androidnga.activity.LauncherSubActivity;
import gov.anzong.androidnga.arouter.ARouterConstants;
import gov.anzong.androidnga.base.util.ToastUtils;
import sp.phone.mvp.model.entity.Board;
import sp.phone.mvp.model.entity.TopicListInfo;
import sp.phone.param.ParamKey;
import sp.phone.util.ActivityUtils;

/**
 * Created by Justwen on 2017/11/19.
 */

public class TopicListFragment extends TopicSearchFragment {

    private Menu mOptionMenu;

    @BindView(R.id.fab_post)
    public FloatingActionButton mFab;

    @BindView(R.id.appbar)
    public AppBarLayout mAppBarLayout;

    @BindView(R.id.toolbar)
    public Toolbar mToolbar;

    @Override
    protected void setTitle() {
        if (mRequestParam.title != null) {
            setTitle(mRequestParam.title);
        }
    }

    @Override
    public void setData(TopicListInfo result) {
        super.setData(result);
        if (mRequestParam.title == null && result.getName() != null && getActivity() != null) {
            mRequestParam.title = result.getName();
            getActivity().setTitle(mRequestParam.title);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int layoutId = R.layout.fragment_topic_list_board;
        return inflater.inflate(layoutId, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateFloatingMenu();
        this.mFab.setOnClickListener(view1 -> {
            ARouter.getInstance()
                   .build(ARouterConstants.ACTIVITY_POST)
                   .withInt(ParamKey.KEY_FID, mRequestParam.fid)
                   .withString(ParamKey.KEY_STID, mRequestParam.stid != 0 ? String.valueOf(mRequestParam.stid) : null)
                   .withString(ParamKey.KEY_ACTION, "new")
                   .navigation();
        });
    }

    private void updateFloatingMenu() {
        if (mConfig.isLeftHandMode()) {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) this.mFab.getLayoutParams();
            layoutParams.gravity = Gravity.START | Gravity.BOTTOM;
            this.mFab.setLayoutParams(layoutParams);
        }
    }

    @Override
    public void hideLoadingView() {
        AppBarLayout.LayoutParams lp = (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
        lp.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        super.hideLoadingView();
    }

    @Override
    public void scrollTo(int position) {
        if (position == 0) {
            mAppBarLayout.setExpanded(true, true);
        }
        super.scrollTo(position);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mPresenter.isBookmarkBoard(mRequestParam.fid, mRequestParam.stid)) {
            menu.findItem(R.id.menu_add_bookmark).setVisible(false);
            menu.findItem(R.id.menu_remove_bookmark).setVisible(true);
        } else {
            menu.findItem(R.id.menu_add_bookmark).setVisible(true);
            menu.findItem(R.id.menu_remove_bookmark).setVisible(false);
        }

        if (mTopicListInfo != null) {
            menu.findItem(R.id.menu_sub_board).setVisible(!mTopicListInfo.getSubBoardList().isEmpty());
        } else {
            menu.findItem(R.id.menu_sub_board).setVisible(false);
        }

        if (mRequestParam.fid == 0 && mRequestParam.stid == 0) {
            menu.findItem(R.id.menu_add_bookmark).setVisible(false);
            menu.findItem(R.id.menu_remove_bookmark).setVisible(false);
        }

        menu.findItem(R.id.menu_board_head).setVisible(mRequestParam.boardHead != null);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.topic_list_menu, menu);
        mOptionMenu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_bookmark:
                Board board = new Board(mRequestParam.fid,mRequestParam.stid,mRequestParam.title);
                board.setBoardHead(mRequestParam.boardHead);
                mPresenter.addBookmarkBoard(board);
                item.setVisible(false);
                mOptionMenu.findItem(R.id.menu_remove_bookmark).setVisible(true);
                ToastUtils.showToast(R.string.toast_add_bookmark_board);
                break;
            case R.id.menu_remove_bookmark:
                mPresenter.removeBookmarkBoard(mRequestParam.fid, mRequestParam.stid);
                item.setVisible(false);
                mOptionMenu.findItem(R.id.menu_add_bookmark).setVisible(true);
                ToastUtils.showToast(R.string.toast_remove_bookmark_board);
                break;
            case R.id.menu_sub_board:
                showSubBoardList();
                break;
            case R.id.menu_board_head:
                mPresenter.startArticleActivity(mRequestParam.boardHead, mRequestParam.title + " - 版头");
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void showSubBoardList() {
        Intent intent = new Intent(getContext(), LauncherSubActivity.class);
        intent.putExtra("fragment", BoardSubListFragment.class.getName());
        intent.putExtra(ParamKey.KEY_TITLE, mRequestParam.title);
        intent.putExtra(ParamKey.KEY_FID, mRequestParam.fid);

        intent.putParcelableArrayListExtra("subBoard", mTopicListInfo.getSubBoardList());
        startActivityForResult(intent, ActivityUtils.REQUEST_CODE_SUB_BOARD);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ActivityUtils.REQUEST_CODE_SUB_BOARD && resultCode == Activity.RESULT_OK) {
            mPresenter.loadPage(1, mRequestParam);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

package com.ruanchao.videoedit.ui.video;

import android.view.View;

import com.ruanchao.videoedit.base.IBaseView;

public interface IMainView extends IBaseView{

    void onFastToolItemClick(int position,View v);

    void onAllToolItemClick(int position,View v);
}

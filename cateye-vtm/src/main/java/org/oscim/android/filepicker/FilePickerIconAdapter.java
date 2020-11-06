/*
 * Copyright 2010, 2011, 2012 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.oscim.android.filepicker;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cateye.android.vtm.R;
import com.litesuits.common.io.FileUtils;
import com.vondear.rxtool.model.ActionItem;
import com.vondear.rxui.view.popupwindows.RxPopupSingleView;
import com.vondear.rxui.view.popupwindows.tools.RxPopupViewManager;

import java.io.File;

/**
 * An adapter for the FilePicker GridView.
 */
class FilePickerIconAdapter extends BaseAdapter {
    private final Context mContext;
    private File mCurrentFile;
    private File[] mFiles;
    private boolean mHasParentFolder;
    private TextView mTextView;

    /**
     * Creates a new FilePickerIconAdapter with the given context.
     *
     * @param context the context of this adapter, through which new Views are
     *                created.
     */
    FilePickerIconAdapter(Context context) {
        super();
        mContext = context;
    }

    @Override
    public int getCount() {
        if (mFiles == null) {
            return 0;
        }
        return mFiles.length;
    }

    @Override
    public Object getItem(int index) {
        return mFiles[index];
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        if (convertView instanceof TextView) {
            // recycle the old view
            mTextView = (TextView) convertView;
        } else {
            // create a new view object
            mTextView = new TextView(mContext);
            mTextView.setLines(2);
            mTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            mTextView.setPadding(5, 10, 5, 10);
        }

        if (index == 0 && mHasParentFolder) {
            // the parent directory of the current folder
            mTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.file_picker_back, 0, 0);
            mTextView.setText("上一级/..");
        } else {
            mCurrentFile = mFiles[index];
            if (mCurrentFile.isDirectory()) {
                mTextView.setCompoundDrawablesWithIntrinsicBounds(0,
                        R.mipmap.file_picker_folder,
                        0,
                        0);
            } else {
                mTextView.setCompoundDrawablesWithIntrinsicBounds(0,
                        R.mipmap.file_picker_file,
                        0,
                        0);
            }
            mTextView.setText(mCurrentFile.getName());

            // 设置长按点击事件，增加重命名和删除操作
            mTextView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    RxPopupViewManager manager = new RxPopupViewManager();
                    RxPopupSingleView rxPopupView = new RxPopupSingleView(mContext, ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT, R.layout.popupwindow_definition_layout);
                    rxPopupView.addAction(new ActionItem("删除"));
                    rxPopupView.addAction(new ActionItem("重命名"));
                    rxPopupView.show(view);
                    rxPopupView.setItemOnClickListener(new RxPopupSingleView.OnItemOnClickListener() {
                        @Override
                        public void onItemClick(ActionItem item, int position) {
                            if (mFiles!=null&&mFiles.length>index){
                                switch (position) {
                                    case 0:
                                        FileUtils.deleteQuietly(mFiles[index]);
                                        for (int i = index; i<mFiles.length-1; i++) {
                                            mFiles[i] = mFiles[i+1];
                                        }
                                        notifyDataSetChanged();
                                        break;
                                    case 1:
                                        try {
                                            File destFile = new File(mCurrentFile.getAbsoluteFile()+"/"+"new");
                                            if (mFiles[index].isDirectory()) {
                                                FileUtils.moveDirectory(mFiles[index], destFile);
                                            } else {
                                                FileUtils.moveFile(mFiles[index], destFile);
                                            }
                                            mFiles[index] = destFile;
                                        } catch (Exception e) {

                                        }
                                        break;
                                }
                            }
                        }
                    });
                    return false;
                }
            });
        }
        return mTextView;
    }

    /**
     * Sets the data of this adapter.
     *
     * @param files              the new files for this adapter.
     * @param newHasParentFolder true if the file array has a parent folder at index 0, false
     *                           otherwise.
     */
    void setFiles(File[] files, boolean newHasParentFolder) {
        mFiles = files.clone();
        mHasParentFolder = newHasParentFolder;
    }
}

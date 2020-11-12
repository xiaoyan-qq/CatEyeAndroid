/*
 * Copyright 2010, 2011, 2012 mapsforge.org
 * Copyright 2016 devemux86
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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.canyinghao.candialog.CanDialog;
import com.canyinghao.candialog.CanDialogInterface;
import com.cateye.android.vtm.R;
import com.cateye.vtm.util.SystemConstant;
import com.github.lazylibrary.util.StringUtils;
import com.litesuits.common.io.FileUtils;
import com.tamsiree.rxkit.view.RxToast;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A FilePicker displays the contents of directories. The user can navigate
 * within the file system and select a single
 * file whose path is then returned to the calling activity. The ordering of
 * directory contents can be specified via
 * {@link #setFileComparator(Comparator)}. By default subfolders and files are
 * grouped and each group is ordered
 * alphabetically.
 * <p/>
 * A {@link FileFilter} can be activated via
 * {@link #setFileDisplayFilter(FileFilter)} to restrict the displayed files and
 * folders. By default all files and folders are visible.
 * <p/>
 * Another <code>FileFilter</code> can be applied via
 * {@link #setFileSelectFilter(ValidFileFilter)} to check if a selected file is
 * valid before its path is returned. By default all files are considered as
 * valid and can be selected.
 */
public class FilePicker extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    /**
     * The name of the extra data in the result {@link Intent}.
     */
    public static final String SELECTED_FILE = "selectedFile";

    private static final String PREFERENCES_FILE = "FilePicker";
    private static final String CURRENT_DIRECTORY = "currentDirectory";
    private static final String DEFAULT_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final int DIALOG_FILE_INVALID = 0;

    // private static final int DIALOG_FILE_SELECT = 1;
    protected Comparator<File> mFileComparator = getDefaultFileComparator();
    protected FileFilter mFileDisplayFilter;
    protected ValidFileFilter mFileSelectFilter;
    protected BootstrapButton bbtnReturnRootFolder/*返回根目录*/, /*bbtnReturnDefaultFolder*//*返回项目默认目录*//*,*/ bbtnCreateNewFolder/*创建新文件夹*/,bbtnFolderQuickInto/*快速导引*/;
    private SimpleAdapter commonFolderAdapter;
    private final String KEY_ICON="KEY_ICON",KEY_NAME="KEY_NAME";
    private CanDialog commonIntoDialog;

    /**
     * Sets the file comparator which is used to order the contents of all
     * directories before displaying them. If set to
     * null, subfolders and files will not be ordered.
     *
     * @param fileComparator the file comparator (may be null).
     */
    public void setFileComparator(Comparator<File> fileComparator) {
        mFileComparator = fileComparator;
    }

    /**
     * Sets the file display filter. This filter is used to determine which
     * files and subfolders of directories will be
     * displayed. If set to null, all files and subfolders are shown.
     *
     * @param fileDisplayFilter the file display filter (may be null).
     */
    public void setFileDisplayFilter(FileFilter fileDisplayFilter) {
        mFileDisplayFilter = fileDisplayFilter;
    }

    /**
     * Sets the file select filter. This filter is used when the user selects a
     * file to determine if it is valid. If set
     * to null, all files are considered as valid.
     *
     * @param fileSelectFilter the file selection filter (may be null).
     */
    public void setFileSelectFilter(ValidFileFilter fileSelectFilter) {
        mFileSelectFilter = fileSelectFilter;
    }

    /**
     * Creates the default file comparator.
     *
     * @return the default file comparator.
     */
    private static Comparator<File> getDefaultFileComparator() {
        // order all files by type and alphabetically by name
        return new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                if (file1.isDirectory() && !file2.isDirectory()) {
                    return -1;
                } else if (!file1.isDirectory() && file2.isDirectory()) {
                    return 1;
                } else {
                    return file1.getName().compareToIgnoreCase(file2.getName());
                }
            }
        };
    }

    private File mDirectory;
    private FilePickerIconAdapter mFilePickerIconAdapter;
    private File[] mFiles;
    private File[] mFilesWithParentFolder;

    @SuppressWarnings("deprecation")
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        File selectedFile = mFiles[(int) id];
        if (selectedFile.isDirectory()) {
            mDirectory = selectedFile;
            browseToCurrentDirectory();
        } else if (mFileSelectFilter == null || mFileSelectFilter.accept(selectedFile)) {
            setResult(RESULT_OK,
                    new Intent().putExtra(SELECTED_FILE, selectedFile.getAbsolutePath()));
            finish();
        } else {
            showDialog(DIALOG_FILE_INVALID);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(android.R.drawable.ic_menu_info_details);
            builder.setTitle(R.string.error);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(getString(R.string.file_invalid));
            stringBuilder.append("\n\n");
            stringBuilder.append(mFileSelectFilter.getFileOpenResult()
                    .getErrorMessage());

            builder.setMessage(stringBuilder.toString());
            builder.setPositiveButton(R.string.ok, null);
            builder.create().show();
        }
    }

    /**
     * Browses to the current directory.
     */
    private void browseToCurrentDirectory() {
        setTitle(mDirectory.getAbsolutePath());

        // read the subfolders and files from the current directory
        if (mFileDisplayFilter == null) {
            mFiles = mDirectory.listFiles();
        } else {
            mFiles = mDirectory.listFiles(mFileDisplayFilter);
        }

        if (mFiles == null) {
            mFiles = new File[0];
        } else {
            // order the subfolders and files
            Arrays.sort(mFiles, mFileComparator);
        }

        // if a parent directory exists, add it at the first position
        if (mDirectory.getParentFile() != null) {
            mFilesWithParentFolder = new File[mFiles.length + 1];
            mFilesWithParentFolder[0] = mDirectory.getParentFile();
            System.arraycopy(mFiles, 0, mFilesWithParentFolder, 1,
                    mFiles.length);
            mFiles = mFilesWithParentFolder;
            mFilePickerIconAdapter.setFiles(mFiles, true);
        } else {
            mFilePickerIconAdapter.setFiles(mFiles, false);
        }
        mFilePickerIconAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_picker);

        mFilePickerIconAdapter = new FilePickerIconAdapter(this);
        GridView gridView = (GridView) findViewById(R.id.filePickerView);
        gridView.setOnItemClickListener(this);
        gridView.setOnItemLongClickListener(this);
        gridView.setAdapter(mFilePickerIconAdapter);

        ImageView img_close = (ImageView) findViewById(R.id.img_close);
        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FilePicker.this.finish();
            }
        });
        // if (savedInstanceState == null) {
        // // first start of this instance
        // showDialog(DIALOG_FILE_SELECT);
        // }
        bbtnReturnRootFolder = findViewById(R.id.bbtn_return_root_folder);
        bbtnReturnRootFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 返回根目录
                mDirectory = new File(DEFAULT_DIRECTORY);
                if (!mDirectory.exists() || !mDirectory.canRead()) {
                    mDirectory = new File(DEFAULT_DIRECTORY);
                }
                browseToCurrentDirectory();
            }
        });
//        bbtnReturnDefaultFolder = findViewById(R.id.bbtn_return_default_folder);
//        bbtnReturnDefaultFolder.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // 返回项目目录
//                mDirectory = new File(SystemConstant.APP_ROOT_DATA_PATH);
//                if (!mDirectory.exists() || !mDirectory.canRead()) {
//                    mDirectory = new File(DEFAULT_DIRECTORY);
//                }
//                browseToCurrentDirectory();
//            }
//        });

        List<Map<String, Object>> commonFolderList = new ArrayList<>();

        Map<String, Object> qqFolderMap = new HashMap<>();
        qqFolderMap.put(KEY_ICON, R.mipmap.icon_qq);
        qqFolderMap.put(KEY_NAME, "QQ");
        commonFolderList.add(qqFolderMap);

        Map<String, Object> wechatFolderMap = new HashMap<>();
        wechatFolderMap.put(KEY_ICON, R.mipmap.icon_wechat);
        wechatFolderMap.put(KEY_NAME, "微信");
        commonFolderList.add(wechatFolderMap);

        Map<String, Object> homeFolderMap = new HashMap<>();
        homeFolderMap.put(KEY_ICON, R.mipmap.icon_folder_home);
        homeFolderMap.put(KEY_NAME, "项目目录");
        commonFolderList.add(homeFolderMap);

        commonFolderAdapter = new SimpleAdapter(FilePicker.this, commonFolderList, R.layout.simple_list_item, new String[]{KEY_ICON,KEY_NAME}, new int[]{R.id.image_view, R.id.text_view});

        bbtnFolderQuickInto = findViewById(R.id.bbtn_folder_quick_into);
        bbtnFolderQuickInto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View listViewLayer = LayoutInflater.from(FilePicker.this).inflate(R.layout.simple_listview, null);
                ListView lv=listViewLayer.findViewById(R.id.simple_list_view);
                lv.setAdapter(commonFolderAdapter);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String currentPath = mDirectory.getAbsolutePath();
                        switch (i) {
                            case 0: // 进入QQ下载目录
                                // 返回项目目录
                                mDirectory = new File(SystemConstant.APP_ROOT_DATA_PATH+"/tencent/QQfile_recv");
                                break;
                            case 1: // 进入微信下载目录
                                // 返回项目目录
                                mDirectory = new File(SystemConstant.APP_ROOT_DATA_PATH+"/Android/data/com.tencent.mm/MicroMsg/Download");
                                break;
                            case 2: // 进入程序默认目录
                                // 返回项目目录
                                mDirectory = new File(SystemConstant.APP_ROOT_DATA_PATH);
                                break;
                        }
                        if (!mDirectory.exists() || !mDirectory.canRead()) {
                            mDirectory = new File(currentPath);
                            RxToast.error("指定的目录不存在!");
                        }
                        browseToCurrentDirectory();
                        if (commonIntoDialog != null && commonIntoDialog.isShow()) {
                            commonIntoDialog.dismiss();
                        }
                    }
                });

                // 弹出对话框，由用户选择跳转路径
                commonIntoDialog = new CanDialog.Builder(FilePicker.this)
                        .setTitle("快速导引")
                        .setView(listViewLayer)
                        .create();
                commonIntoDialog.show();
            }
        });

        bbtnCreateNewFolder = findViewById(R.id.bbtn_create_new_folder);
        bbtnCreateNewFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 创建新目录
                if (!mDirectory.exists() || !mDirectory.canRead()) {
                    mDirectory = new File(DEFAULT_DIRECTORY);
                }
                // 在当前目录下创建新的文件夹
                CanDialog.Builder newFolderDialogBuilder = new CanDialog.Builder(FilePicker.this);
                newFolderDialogBuilder.setTitle("新建文件夹").setEditDialog("文件夹名称", false, 1, R.color.secondColor).setPositiveButton("确定", true, new CanDialogInterface.OnClickListener() {
                    @Override
                    public void onClick(CanDialog dialog, int checkItem, CharSequence text, boolean[] checkItems) {
                        String folderName = text.toString();
                        folderName = checkFileName(folderName);
                        if (folderName!= null) {
                            File folder = new File(mDirectory.getAbsolutePath()+"/"+folderName);
                            boolean createResult = folder.mkdirs();
                            if (createResult) {
                                browseToCurrentDirectory();
                            } else {
                                RxToast.error("创建目录失败，请重试！");
                            }
                        }
                    }
                }).setNegativeButton("取消", true, null);
                newFolderDialogBuilder.create().show();

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // save the current directory
        Editor editor = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE).edit();
        editor.clear();
        if (mDirectory != null) {
            editor.putString(CURRENT_DIRECTORY, mDirectory.getAbsolutePath());
        }
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // restore the current directory
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE,
                MODE_PRIVATE);
        mDirectory = new File(preferences.getString(CURRENT_DIRECTORY,
                DEFAULT_DIRECTORY));
        if (!mDirectory.exists() || !mDirectory.canRead()) {
            mDirectory = new File(DEFAULT_DIRECTORY);
        }
        browseToCurrentDirectory();

//        arrayAdapter = new ArrayAdapter(FilePicker.this, android.R.layout.simple_list_item_1, operateStringArray);
    }

    private String[] operateStringArray = {"删除", "重命名"};
//    private ArrayAdapter arrayAdapter = null;
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long l) {
//        DialogPlus dialogPlus = DialogPlus.newDialog(FilePicker.this)
//                        .setGravity(Gravity.BOTTOM)
//                        .setCancelable(true)
//                        .setAdapter(arrayAdapter)
//                        .setOnItemClickListener(new OnItemClickListener() {
//                            @Override
//                            public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
//
//                            }
//                        })
//                        .create();
//        dialogPlus.show();

        CanDialog.Builder dialogBuilder = new CanDialog.Builder(FilePicker.this);
        dialogBuilder
                .setTitle("选择操作")
                .setItems(operateStringArray, new CanDialogInterface.OnClickListener() {
            @Override
            public void onClick(CanDialog dialog, int position, CharSequence text, boolean[] checkItems) {
                if (mFiles!=null&&mFiles.length>index){
                    dialog.dismiss();
                    switch (position) {
                        case 0:
                            deleteConfirmFile(index);
                            break;
                        case 1:
                            new CanDialog.Builder(FilePicker.this)
                                    .setTitle("重命名")
                                    .setEditDialog("新名称", false, 1, R.color.secondColor)
                                    .setNegativeButton("取消", true, null)
                                    .setPositiveButton("确定", true, new CanDialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(CanDialog dialog, int checkItem, CharSequence text, boolean[] checkItems) {
                                            if (checkFileName(text.toString())!=null) {
                                                try {
                                                    File destFile = new File(mFiles[index].getParentFile().getAbsolutePath()+"/"+text);
                                                    if (mFiles[index].isDirectory()) {
                                                        FileUtils.moveDirectory(mFiles[index], destFile);
                                                    } else {
                                                        FileUtils.moveFile(mFiles[index], destFile);
                                                    }
                                                    mFiles[index] = destFile;
                                                    browseToCurrentDirectory();
                                                } catch (Exception e) {

                                                }
                                            }
                                        }
                                    })
                                    .create().show();
                            break;
                        default:
                            dialog.dismiss();
                            break;
                    }
                }
            }
        })
                .show();
        return true;
    }

    private void deleteConfirmFile(int index) {
        CanDialog.Builder deleteDialogBuilder = new CanDialog.Builder(FilePicker.this);
        deleteDialogBuilder
                .setTitle("确认")
                .setMessage("确认删除?")
                .setCancelable(true)
                .setNegativeButton("取消", true, null)
                .setPositiveButton("确定", true, new CanDialogInterface.OnClickListener() {
                    @Override
                    public void onClick(CanDialog dialog, int checkItem, CharSequence text, boolean[] checkItems) {
                        boolean result = FileUtils.deleteQuietly(mFiles[index]);
                        if (result) {
                            for (int i = index; i<mFiles.length-1; i++) {
                                mFiles[i] = mFiles[i+1];
                            }
                            browseToCurrentDirectory();
                        }
                    }
                })
                .show();

    }

    private String checkFileName(String folderName) {
        if (folderName.contains("/")||folderName.contains("\\")||folderName.contains("\"")||folderName.startsWith("-")) {
            RxToast.error("文件名不能为空，且不能存在'/'、'\\'、'.'等特殊符号！");
            return null;
        }
        folderName = folderName.replace("/", "").replace("\\", "");
        if (StringUtils.isEmpty(folderName)) {
            RxToast.error("文件名不能为空，且不能存在'/'、'\\'、'.'等特殊符号！");
            return null;
        }
        File folder = new File(mDirectory.getAbsolutePath()+"/"+folderName);
        if (folder.exists()) {
            RxToast.error("已存在此名称的文件！");
            return null;
        }
        return folderName;
    }
}

package com.cateye.vtm.util;


import com.litesuits.common.utils.SdCardUtil;

import java.io.File;

public class SystemConstant {
    public static final int DB_VERSION = 1;
    public static final String APP_ROOT_DATA_PATH = SdCardUtil.getSDCardPath() + File.separator + "CatEye";
    public static final String AIR_PLAN_PATH = APP_ROOT_DATA_PATH + File.separator + "AirPlan";
    public static final String AIR_PLAN_OUTPUT_PATH = APP_ROOT_DATA_PATH + File.separator + "AirPlan" + File.separator + "Output";
    public static final String CACHE_FILE_PATH = APP_ROOT_DATA_PATH + File.separator + "Cache";

    public static final int MSG_WHAT_DRAW_POINT_LINE_POLYGON_DESTROY = 0x1001;//绘制点线面
    public static final int MSG_WHAT_LOCATION_UPDATE = 0x1002;//位置更新
    public static final int MSG_WHAT_MAIN_AREA_HIDEN_VISIBLE = 0x1003;//显示隐藏主界面上的某些元素
    public static final int MSG_WHAT_DRAW_POINT_LINE_POLYGON_TAP = 0x1004;//用户在绘制界面点击
    public static final int MSG_WHAT_DRAW_RESULT = 0x1005;//用户在绘制界面点击
    public static final int MSG_WHAT_DRAW_LAYER_TIME_SELECT = 0x1006;//某些图层存在多时序，用户拖动时序选择控件切换图层显示
    public static final int MSG_WHAT_DRAW_TILE_DOWNLOAD_RECT_START = 0x1007;//开始绘制地图下载的rect，自动隐藏地图上的其他按钮控件
    public static final int MSG_WHAT_DRAW_TILE_DOWNLOAD_RECT_FINISH = 0x1008;//完成绘制地图下载的rect，重新显示地图上的其他按钮控件

    public static final String BASE_URL = "http://111.202.109.211:8080";
    public static final String USER_ID = "{userId}";
    public static final String URL_MAP_SOURCE_NET = BASE_URL + "/projects/" + USER_ID + "/datasets";//获取数据源的url
    public static final String URL_CONTOUR_CALCULATE = BASE_URL + "/dem/contour";//等高线获取的url
    public static final String URL_PROJECTS_LIST = BASE_URL + "/projects";//获取项目列表的url
    public static final String URL_LOGIN = BASE_URL + "/auth/login";//获取项目列表的url
    public static int CURRENT_PROJECTS_ID = -1;//当前正在作业的项目id，默认为1

    public static final String DATA_CONTOUR_CHART = "DATA_CONTOUR_CHART";
    public static final String BUNDLE_AREA_HIDEN_STATE = "BUNDLE_AREA_HIDEN_STATE";//主界面上部分区域的显隐状态，隐藏或显示
    public static final String BUNDLE_BUTTON_AREA = "BUNDLE_BUTTON_AREA";//主界面上控制的显隐区域
    public static final long SCREEN_MOVE_BOUNDARY = 20;//判断点击屏幕时是否为移动事件的边界值

    public static final String DRAW_POINT_LIST = "DRAW_POINT_LIST";//绘制点线面后的点位集合
    public static final String DRAW_USAGE = "DRAW_USAGE";//绘制点线面的用处
    public static final int DRAW_CONTOUR_LINE = 1;//绘制等高线的线段
    public static final String LATITUDE = "LATITUDE";//latitude
    public static final String LONGITUDE = "LONGITUDE";//longitude

    public static final String DRAW_TILE_RECT = "DRAW_TILE_RECT";//绘制tile下载的rect对应的layer
    public static final String BUNDLE_MULTI_TIME_SELECTOR_DATA = "BUNDLE_MULTI_TIME_SELECTOR_DATA";//多时序选择所需要的数据
    public static final String LAYER_KEY_ID = "LAYER_KEY_ID";//记录图层id

    public static final String TRAIL_LOCATION_RECORD = "TRAIL_LOCATION_RECORD";//用户轨迹对应的多线overlayer的名称，用于判断图层是否已添加到map上
    public static final String AIR_PLAN_MULTI_POLYGON_DRAW = "AIR_PLAN_MULTI_POLYGON_DRAW";//航区规划对应的多面overlayer的名称，用于判断该图层是否已经添加到map上
    public static final String AIR_PLAN_MULTI_POLYGON_PARAM = "AIR_PLAN_MULTI_POLYGON_PARAM";//航区规划参数设计对应的多面overlayer的名称
    public static final String AIR_PLAN_MARKER_AIR_PORT = "AIR_PLAN_MARKER_AIR_PORT";//航区规划参数设计对应的无人机机场的名称

    public static final String AIR_PLAN_MARKER_PARAM = "AIR_PLAN_MARKER_PARAM";//航区规划参数设计对应的选择无人机机场的点击操作
    public static final String AIR_PLAN_MULTI_POLYGON_PARAM_EVENT = "AIR_PLAN_MULTI_POLYGON_PARAM_EVENT";//航区规划参数设计对应的操作overlayer的名称


    public static final String SP_LOGIN_USERNAME = "SP_LOGIN_USERNAME";
    public static final String SP_LOGIN_PWD = "SP_LOGIN_PWD";
    public static final String SP_LOGIN_PWD_IS_REMEMBER = "SP_LOGIN_PWD_IS_REMEMBER";
    public static final String PWD_KEY = "PWD_KEY";

}

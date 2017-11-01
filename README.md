# XPlan
瀑布流展示图文信息的小应用。其他功能包括：应用换肤，仿今日头条查看图片详情样式，聊天机器人等
  
  特点：
  
  1.jsoup抓取网页图片和文字信息，利用Recyclerview的瀑布流形式展示，支持下拉刷新，加载更多，快速返回    
  2.点击图片进入浏览大图模式，仿今日头条样式，支持放大缩小，上下滑动图片退出。   
  3.聊天机器人采用图灵接口，展示简单的对话信息。         
  4.开源项目使用：Retrofit + OkHttp + Gson + fresco + logger      
      com.github.CymChad:BaseRecyclerViewAdapterHelper 滑动返回
      com.jaeger.statusbarutil:library  沉浸式状态栏
      com.github.chrisbanes:PhotoView  图片浏览器 放大缩小图片
      com.github.hotchemi:permissionsdispatcher  动态权限处理
      QSkinLoader 应用换肤，无需重启界面

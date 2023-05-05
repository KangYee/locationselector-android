# LocationSelector - 位置选择器

LocationSelector 是一个仿微信发送位置样式的 Android 位置选择器库，项目通过 Kotlin 编写，并接入腾讯地图，数据传递使用 Fragment Result API

## TODO
- 选择过的点增加Marker，点击可直接选择
- 优化地点搜索的结果（支持搜索全国地点）
- 周边地点的加载更多
- 支持 Activity 的使用方式

## 集成方式

1.    在 Project 的 build.gradle 中增加远程仓库

```
allprojects {
    repositories {
        //...
        maven { url 'https://jitpack.io' }
    }
}
```

2.    在 Module 的 build.gradle 中增加引入依赖项

```
dependencies {
    implementation 'com.github.KangYee:LocationSelector:1.0.0-SNAPSHOT'
}
```

3.    在 AndroidManifest.xml 中增加以下代码，并修改 `value` 的值为你自己的，`name` 的值勿动

```
<!-- 腾讯地图 -->
<meta-data
    android:name="TencentMapSDK"
    android:value="您申请的Key"/>
```


## 使用方式

#### Fragment

1. 引入 `LocationSelectorFragment`
2. 通过 `setFragmentResultListener` 来接收用户选择的位置数据


需要注意的是，数据只会在 `LocationSelectorFragment` 销毁时回传，也就是触发 OnDestory 时


## License

MIT

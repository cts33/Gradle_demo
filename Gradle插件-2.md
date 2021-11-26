# 9 Android gradle 高级自定义

## 1. 使用共享库

有一些库，像com.google.android.maps是独立的，并不会被系统自动连接，所以需要单独生成使用。

```
<application>
	<uses-library android:name="com.google.android.maps" android:required="true"/>
	...
```

声明之后，安装apk的时候，系统会检测系统是否有我们的共享库，required = true ,如果没有，安装不了。

还有一种库 add-ons库，位于add-ons目录下，这些库大部分是第三方厂商开发，为了让开发者使用，但是不想暴露。

还有optional库，位于platform/android-xx/optional目录下，一般为了兼容旧版本的API.eg:org.apache.http.legacy,这是httpclient库。



gradle提供了添加库的形式；

```
android{

	userLibrary 'org.apache.http.legacy'
}
```

## 2. 批量修改生成的apk文件名

既然要改生成的apk文件名，那么就要修改Android Gradle 打包的输出。为了解决这个问题，Android提供了3个属性：application Variants,library Variants test Variants.这三个属性返回的都是DomainObjectSet对象集合，里面的元素分别是Application Variant、LibraryVariant 、testVariants,翻译为变体，都是buildTypes和Product Flavors结合产生的。

```groovy
android {
   ...
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
            zipAlignEnabled true
        }
    }
    productFlavors {
        google {

        }
    }
    applicationVariants.all { variant ->
        variant.outputs.each { output ->
            if (output.outputFile != null && output.outputFile.name.endsWith('.apk')
                    &&'release'.equals(variant.buildType.name)) {
                def flavorName = variant.flavorName.startsWith("_") ? variant.flavorName.substring(1) : variant.flavorName
                def apkFile = new File( output.outputFile.getParent(),  "Example92_${flavorName}_v${variant.versionName}_${buildTime()}.apk")
                output.outputFile = apkFile
            }
        }
    }
}

def buildTime() {
    def date = new Date()
    def formattedDate = date.format('yyyyMMdd')
    return formattedDate
}
```

applicationVariants是一个DomainObjectCollection集合，通过call方法进行遍历，遍历的每一个variant都是一个生成的产物。

## 3. 动态生成版本信息

每个APP都有版本号，一般版本号分为三个部分：major.minor.patch.主版本号，副版本号，补丁号

把公共的信息抽取到单独的gradle文件里，结构清晰。

Version.gradle

```
ext {
    appVersionCode =1
    appVersionName = "1.0.0"
}
ext为project扩展属性。
```

```

apply from:'version.gradle'
android {
    compileSdkVersion 23
    buildToolsVersion "23.0.1"


    defaultConfig {
        applicationId "org.flysnow.app.example93"
        minSdkVersion 14
        targetSdkVersion 23
        versionCode getAppVersionCode()
        versionName getAppVersionName()
    }
    
...    
```

从git 的tag获取版本号及版本名称

```
/**
 * 以git tag的数量作为其版本号
 * @return tag的数量
 */
def getAppVersionCode(){
    def stdout = new ByteArrayOutputStream()
    exec {
        commandLine 'git','tag','--list'
        standardOutput = stdout
    }
    return stdout.toString().split("\n").size()
}

/**
 * 从git tag中获取应用的版本名称
 * @return git tag的名称
 */
def getAppVersionName(){
    def stdout = new ByteArrayOutputStream()
    exec {
        commandLine 'git','describe','--abbrev=0','--tags'
        standardOutput = stdout
    }
    return stdout.toString().replaceAll("\n","")
}
```





## 4. 隐藏签名文件

把签名文件放到服务器里，服务器专用于打包发版，根据环境读取签名数据，完成打包。由于本地环境不满足获取签名数据，所以会使用debug签名。这样解决安全问题。

```
   signingConfigs {
        def appStoreFile = System.getenv("STORE_FILE")
        def appStorePassword = System.getenv("STORE_PASSWORD")
        def appKeyAlias = System.getenv("KEY_ALIAS")
        def appKeyPassword = System.getenv("KEY_PASSWORD")

        //当不能从环境变量里获取到签名信息的时候，就使用模式的
        if(!appStoreFile||!appStorePassword||!appKeyAlias||!appKeyPassword){
            appStoreFile = "debug.keystore"
            appStorePassword = "android"
            appKeyAlias = "androiddebugkey"
            appKeyPassword = "android"
        }
        release {
            storeFile file(appStoreFile)
            storePassword appStorePassword
            keyAlias appKeyAlias
            keyPassword appKeyPassword
        }
    }

```



## 5. 动态配置AndroidManifest.xml

动态配置渠道数据,manifestPlaceHolder Manifest占位符

```
<application>
	<meta-data android:value="${UMENG_CHANNEL}" android:name="UMENG_CHANNEL"/>
	...

```

manifestPlaceHolder 是ProductFlavor的属性，是一个map类型。

```
android{
	productFlavors {
        google {
        }
        baidu {
        }
    }
	//通过all遍历每个productFlavor，取出name
    productFlavors.all { flavor ->
        manifestPlaceholders.put("UMENG_CHANNEL",name)
    }
    
    ...
```



## 6. BuildConfig

该文件为Android Gradle构建时自动生成，不能修改。

```
public final class BuildConfig {
	//是否为debug模式
  public static final boolean DEBUG = Boolean.parseBoolean("true");
  //applicationId包名
  public static final String APPLICATION_ID = "com.example.gradle_"demo";
  //编译类型
  public static final String BUILD_TYPE = "debug";
  //版本号和名字
  public static final int VERSION_CODE = 1;
  public static final String VERSION_NAME = "1.0";
}
```

还可以在build.gradle文件通过配置，映射到buildConfig.java里。

Gradle插件提供了buildConfigField(type,name,value)

type 字段的类型

name 字段的名称

value 字段的值

可以放到buildTypes里，也可以放到productFlavor里

```
buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'

            buildConfigField "String","UserName","lisi"
            //SIGNATURE为在gradle.properties的常量
            buildConfigField "String", "getSignature", "\"${SIGNATURE}\""
        }
    }
    
    
   productFlavors {
        google {
            buildConfigField 'String','WEB_URL','"http://www.google.com"'
        }
        baidu {
            buildConfigField 'String','WEB_URL','"http://www.baidu.com"'
        }
    }   
```



## 7. 添加自定义资源resValue

我们不仅可以在res/values定义资源，还可以在build.gradle里定义。

可以在buildTypes里定义，还可以在productFlavors里定义

resValue: type类型，name,value

支持string、id、bool、dimen、integer、color类型

```
 productFlavors {
        google {
            resValue 'string','channel_tips','google渠道欢迎你'
        }
        baidu {
            resValue 'string','channel_tips','baidu渠道欢迎你'
            resValue 'color','main_bg','#567896'
            resValue 'integer-array','months','<item>1&lt/item>'
        }
    }
```



## 8. Java编译选项compileOptions

有时候我们需要指定编译的Java版本，可以使用compileOptions

```
 compileOptions {
		encoding = 'utf-8'
 		//编译源文件的Java版本
        sourceCompatibility JavaVersion.VERSION_1_8
        //配置生成字节码的版本
        targetCompatibility JavaVersion.VERSION_1_8
    }
```



## 9. 操作ADB 

adb install 有6个选项

-l :锁定app

-r:替换现有APP

-t:允许测试包

-s:把app 安装到SD

-d:允许降级安装

-g:为该APP授予运行时权限

```
adbOptions{
        //设置执行adb install 操作
        installOptions '-r -s'
        //执行adb超时时间 如果超时会抛出异常CommandRejectException
        timeOutInMs 1000*5
        
 }
```

## 10. DX选项配置 

```
andriod{
	dexOptions{
		incremental true
		//配置执行dx命令分配的最大堆内存
		javaMaxHeapSize '4g'
		jumboMode true
	}
}
```

> incremental 配置是否启动dx增量模式。目前有很多限制，慎用。
>
> jumboMode 配置开启模式，项目比较大，函数超65535,需要强加开启junboMode才能构建。
>
> preDexLibrary 配置是否预执行dex Library库工程，提高增量构建速度，默认true
>
> threadCount 运行dx命令使用的线程数量。



## 11 自动清理未使用的资源Resource Shriking 

Android Gradle 为我们提供了在构建大包时自动清理未使用的资源的方法，打包为apk之前，会检测所有资源，是否被引用，如果没有，就不会被打包到apk里。

要结合Code shrinking,就是混淆Proguard,启动minifyEnabled，为了缩减代码。

```
 buildTypes {
        release {
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
            zipAlignEnabled true
        }
    }
```

自动清理虽好，但是也有缺点，如果代码里有反射逻辑，会区分不了，导致误删。所以提供了keep方法。

#### keep使用

1.新建xml文件，res/raw/keep.xml,

2.通过tools:keep属性来配置

keep.xml

```
<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools"
           tools:shrinkMode="strict"/>
```

#### resConfig

如果我们只想构建某种语言或者某种类型的资源，就可以使用resConfig

```
defaultConfig {
        applicationId "org.flysnow.app.example912"
        minSdkVersion 14
        targetSdkVersion 23
        versionCode 1
        versionName '1.0.0'
        //只打包zh 中文 or hdpi
        resConfigs 'zh'
    }
```



# 10.多项目构建

   ## 1.操作ADB 
  ## 1.操作ADB 
   ## 1.操作ADB 
   ## 1.操作ADB 
   ## 1.操作ADB 
   ## 1.操作ADB 


      2. 1. 操作ADB 

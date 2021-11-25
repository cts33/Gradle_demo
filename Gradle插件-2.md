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



## 1. 使用共享库

## 1. 使用共享库
## 1. 使用共享库
## 1. 使用共享库
